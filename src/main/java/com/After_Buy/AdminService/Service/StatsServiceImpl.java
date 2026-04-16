package com.After_Buy.AdminService.Service;

import com.After_Buy.AdminService.Client.AuthInternalClient;
import com.After_Buy.AdminService.Client.DeviceInternalClient;
import com.After_Buy.AdminService.Dto.Response.DashboardAnnouncementResponse;
import com.After_Buy.AdminService.Dto.Response.DashboardErrorLogResponse;
import com.After_Buy.AdminService.Dto.Response.DashboardOcrStatsResponse;
import com.After_Buy.AdminService.Dto.Response.DashboardResponse;
import com.After_Buy.AdminService.Dto.Response.DashboardUserStatsResponse;
import com.After_Buy.AdminService.Dto.Response.OcrStatsDetailResponse;
import com.After_Buy.AdminService.Entity.Announcement;
import com.After_Buy.AdminService.Entity.ErrorLog;
import com.After_Buy.AdminService.Exception.CustomException;
import com.After_Buy.AdminService.Exception.ErrorCode;
import com.After_Buy.AdminService.Repository.AnnouncementRepository;
import com.After_Buy.AdminService.Repository.ErrorLogRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
						// 1. 기본 필드 매핑
						long total = node.path("total_users").asLong(0L);
						long totalPrev7d = node.path("total_users_prev_7d").asLong(0L);
						long current7d = node.path("new_users_7d").asLong(0L);
						long prev7d = node.path("new_users_prev_7d").asLong(0L);

						// 2. 전체 사용자 증감률 및 방향 계산 (누적 vs 7일 전 누적)
						double totalRate = 0.0;
						String totalDirection = "STAY";
						if (totalPrev7d == 0) {
							if (total > 0) {
								totalRate = 100.0;
								totalDirection = "UP";
							}
						} else {
							totalRate = Math.round(((double) (total - totalPrev7d) / totalPrev7d) * 1000.0) / 10.0;
							if (total > totalPrev7d) totalDirection = "UP";
							else if (total < totalPrev7d) totalDirection = "DOWN";
						}

						// 3. 신규 사용자 증감률 및 방향 계산 (최근 7일 합계 vs 직전 7일 합계)
						double newRate = 0.0;
						String newDirection = "STAY";
						if (prev7d == 0) {
							if (current7d > 0) {
								newRate = 100.0;
								newDirection = "UP";
							}
						} else {
							newRate = Math.round(((double) (current7d - prev7d) / prev7d) * 1000.0) / 10.0;
							if (current7d > prev7d) newDirection = "UP";
							else if (current7d < prev7d) newDirection = "DOWN";
						}

						return DashboardUserStatsResponse.builder()
								.totalUsers(total)
								.totalUsersChangeRate(totalRate)
								.totalUsersChangeDirection(totalDirection)
								.newUsers7d(current7d)
								.newUsers7dChangeRate(newRate)
								.changeDirection(newDirection)
								.build();
					} catch (Exception e) {
						log.error("[StatsServiceImpl] 사용자 통계 파싱 및 계산 실패: {}", e.getMessage());
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

	@Override
	public OcrStatsDetailResponse getOcrStatsDetail(String period) {
		LocalDate endDate = LocalDate.now();
		LocalDate startDate;

		switch (period != null ? period.toUpperCase() : "MONTH") {
			case "WEEK":
				startDate = endDate.minusDays(7);
				break;
			case "ALL":
				startDate = LocalDate.of(2000, 1, 1);
				break;
			case "MONTH":
			default:
				startDate = endDate.minusDays(30);
				period = "MONTH"; // 잘못된 값일 경우 강제 리셋
				break;
		}

		final String safePeriod = period.toUpperCase();

		return deviceInternalClient.getOcrStatsMono(startDate, endDate)
				.map(node -> {
					try {
						long total = node.path("total_attempts").asLong(0L);
						long failure = node.path("failure_count").asLong(0L);
						long modified = node.path("modified_count").asLong(0L);

						double failRate = total > 0 ? (double) failure / total * 100.0 : 0.0;
						double modRate = total > 0 ? (double) modified / total * 100.0 : 0.0;

						// 소수점 1자리 통일
						failRate = Math.round(failRate * 10.0) / 10.0;
						modRate = Math.round(modRate * 10.0) / 10.0;

						OcrStatsDetailResponse.Summary summary = OcrStatsDetailResponse.Summary.builder()
								.totalAttempts(total)
								.failureCount(failure)
								.modifiedCount(modified)
								.failureRate(failRate)
								.modifiedRate(modRate)
								.build();

						List<OcrStatsDetailResponse.FieldModifiedStat> fieldStats = new ArrayList<>();
						if (node.hasNonNull("field_modified_stats") && node.get("field_modified_stats").isArray()) {
							for (JsonNode fNode : node.get("field_modified_stats")) {
								String fName = fNode.path("field_name").asText("");
								long fCount = fNode.path("modified_count").asLong(0L);
								double fRate = modified > 0 ? (double) fCount / modified * 100.0 : 0.0;
								fRate = Math.round(fRate * 10.0) / 10.0;

								fieldStats.add(OcrStatsDetailResponse.FieldModifiedStat.builder()
										.fieldName(fName)
										.modifiedCount(fCount)
										.rate(fRate)
										.build());
							}
						}

						List<OcrStatsDetailResponse.DailyFailureTrend> dailyTrend = new ArrayList<>();
						if (node.hasNonNull("daily_failure_trend") && node.get("daily_failure_trend").isArray()) {
							for (JsonNode tNode : node.get("daily_failure_trend")) {
								dailyTrend.add(OcrStatsDetailResponse.DailyFailureTrend.builder()
										.date(tNode.path("date").asText(""))
										.failureCount(tNode.path("failure_count").asLong(0L))
										.build());
							}
						}

						return OcrStatsDetailResponse.builder()
								.period(safePeriod)
								.summary(summary)
								.fieldModifiedStats(fieldStats)
								.dailyFailureTrend(dailyTrend)
								.build();

					} catch (Exception e) {
						log.error("[StatsServiceImpl] OCR 통계 상세 파싱 실패: {}", e.getMessage());
						throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
					}
				})
				.switchIfEmpty(Mono.error(new CustomException(ErrorCode.INTERNAL_SERVER_ERROR)))
				.block();
	}
}
