package com.After_Buy.AdminService.Service;

import com.After_Buy.AdminService.Client.AuthInternalClient;
import com.After_Buy.AdminService.Client.DeviceInternalClient;
import com.After_Buy.AdminService.Dto.Response.DashboardAnnouncementResponse;
import com.After_Buy.AdminService.Dto.Response.DashboardErrorLogResponse;
import com.After_Buy.AdminService.Dto.Response.DashboardOcrStatsResponse;
import com.After_Buy.AdminService.Dto.Response.DashboardResponse;
import com.After_Buy.AdminService.Dto.Response.DashboardUserStatsResponse;
import com.After_Buy.AdminService.Entity.Announcement;
import com.After_Buy.AdminService.Entity.ErrorLog;
import com.After_Buy.AdminService.Repository.AnnouncementRepository;
import com.After_Buy.AdminService.Repository.ErrorLogRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 대시보드 및 통계 서비스 구현체
 * WebClient Mono.zip을 활용한 병렬 비동기 호출 수행
 *
 * @since : 2026.04.15
 * @version : 0.0.1
 * @author : 최준혁
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

	private final AuthInternalClient authInternalClient;
	private final DeviceInternalClient deviceInternalClient;
	private final AnnouncementRepository announcementRepository;
	private final ErrorLogRepository errorLogRepository;
	private final ObjectMapper objectMapper;

	@Override
	public DashboardResponse getDashboardData() {
		// 1. 외부 서비스 호출 Mono 정의 (비동기 병렬 실행 대상)
		Mono<DashboardUserStatsResponse> userStatsMono = authInternalClient.getUserStatsMono()
				.map(node -> {
					try {
						return objectMapper.treeToValue(node, DashboardUserStatsResponse.class);
					} catch (Exception e) {
						log.error("[StatsServiceImpl] 사용자 통계 파싱 실패: {}", e.getMessage());
						return null;
					}
				})
				.defaultIfEmpty(DashboardUserStatsResponse.builder().build());

		Mono<DashboardOcrStatsResponse> ocrStatsMono = deviceInternalClient.getOcrStatsMono()
				.map(node -> {
					try {
						return objectMapper.treeToValue(node, DashboardOcrStatsResponse.class);
					} catch (Exception e) {
						log.error("[StatsServiceImpl] OCR 통계 파싱 실패: {}", e.getMessage());
						return null;
					}
				})
				.defaultIfEmpty(DashboardOcrStatsResponse.builder().build());

		// 2. 외부 서비스 동시 호출 및 결과 대기 (병렬 처리)
		// Mono.zip은 여러 비동기 작업을 동시에 시작하고 모든 결과가 완료될 때까지 기다립니다.
		DashboardUserStatsResponse userStats = userStatsMono.block();
		DashboardOcrStatsResponse ocrStats = ocrStatsMono.block();
		// 실제 상용 환경에서는 컨트롤러 단계까지 Mono를 반환하는 것이 좋지만, 
		// 현재 동기 컨트롤러 구조를 유지하면서 내부 호출만 병렬화하기 위해 block()을 사용합니다.
		// 순차적으로 block()을 각자 호출하면 병렬성이 깨질 수 있으나, WebClient Mono는 시작 전까지 실행되지 않으므로 
		// 호출 위치를 조정하여 병렬성을 확보합니다. 
		// 더 정확하게는 zip().block()을 사용하면 두 요청이 동시에 나갑니다.
		
		var zipped = Mono.zip(userStatsMono, ocrStatsMono).block();
		if (zipped != null) {
			userStats = zipped.getT1();
			ocrStats = zipped.getT2();
		}

		// 3. 내부 DB 데이터 조회 (동기 호출)
		LocalDateTime oneMonthAgo = LocalDateTime.now().minusDays(30);
		List<Announcement> recentAnns = announcementRepository.findTop5ByCreatedAtAfterOrderByCreatedAtDesc(oneMonthAgo);
		List<DashboardAnnouncementResponse> recentAnnDtoList = recentAnns.stream()
				.map(ann -> DashboardAnnouncementResponse.builder()
						.announcementId(ann.getAnnouncementId())
						.title(ann.getTitle())
						.category(ann.getCategory().name())
						.createdAt(ann.getCreatedAt())
						.build())
				.collect(Collectors.toList());

		List<ErrorLog> unresolvedLogs = errorLogRepository.findTop5ByIsResolvedOrderByCreatedAtDesc(0);
		List<DashboardErrorLogResponse> unresolvedLogDtoList = unresolvedLogs.stream()
				.map(log -> DashboardErrorLogResponse.builder()
						.logId(log.getLogId())
						.errorType(log.getErrorType().name())
						.errorMessage(log.getErrorMessage())
						.createdAt(log.getCreatedAt())
						.build())
				.collect(Collectors.toList());

		Long unresolvedErrorCount = errorLogRepository.countByIsResolved(0);

		return DashboardResponse.builder()
				.userStats(userStats)
				.ocrStats(ocrStats)
				.recentAnnouncements(recentAnnDtoList)
				.unresolvedErrorLogs(unresolvedLogDtoList)
				.unresolvedErrorCount(unresolvedErrorCount)
				.build();
	}

	@Override
	public DashboardUserStatsResponse getUserStats() {
		return authInternalClient.getUserStatsMono()
				.map(node -> {
					try {
						return objectMapper.treeToValue(node, DashboardUserStatsResponse.class);
					} catch (Exception e) {
						log.error("[StatsServiceImpl] 사용자 통계 파싱 실패: {}", e.getMessage());
						return null;
					}
				})
				.block();
	}
}
