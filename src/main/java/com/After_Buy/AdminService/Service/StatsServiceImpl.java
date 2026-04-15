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
						return DashboardUserStatsResponse.builder().build();
					}
				})
				.defaultIfEmpty(DashboardUserStatsResponse.builder().build());

		Mono<DashboardOcrStatsResponse> ocrStatsMono = deviceInternalClient.getOcrStatsMono()
				.map(node -> {
					try {
						// 1. 기본 필드 매핑 (total_attempts, failure_count, modified_count)
						DashboardOcrStatsResponse rawResponse = objectMapper.treeToValue(node, DashboardOcrStatsResponse.class);
						
						// 2. 누락된 필드 계산 (success_count, success_rate)
						long total = rawResponse.getTotalAttempts() != null ? rawResponse.getTotalAttempts() : 0L;
						long failure = rawResponse.getFailureCount() != null ? rawResponse.getFailureCount() : 0L;
						long success = Math.max(0, total - failure);
						double rate = (total > 0) ? (double) success / total * 100.0 : 0.0;

						return DashboardOcrStatsResponse.builder()
								.totalAttempts(total)
								.failureCount(failure)
								.modifiedCount(rawResponse.getModifiedCount())
								.successCount(success)
								.successRate(rate)
								.build();
					} catch (Exception e) {
						log.error("[StatsServiceImpl] OCR 통계 파싱 및 계산 실패: {}", e.getMessage());
						return DashboardOcrStatsResponse.builder().build();
					}
				})
				.defaultIfEmpty(DashboardOcrStatsResponse.builder().build());

		// 2. 외부 서비스 동시 호출 및 결과 대기 (병렬 병합 처리)
		// 이전 코드의 개별 .block() 호출을 제거하여 실제 병렬 기동을 보장함.
		var zippedResult = Mono.zip(userStatsMono, ocrStatsMono).block();

		DashboardUserStatsResponse userStats = (zippedResult != null) ? zippedResult.getT1() : DashboardUserStatsResponse.builder().build();
		DashboardOcrStatsResponse ocrStats = (zippedResult != null) ? zippedResult.getT2() : DashboardOcrStatsResponse.builder().build();

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
