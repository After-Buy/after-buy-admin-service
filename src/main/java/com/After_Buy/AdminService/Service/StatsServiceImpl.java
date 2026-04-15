package com.After_Buy.AdminService.Service;

import com.After_Buy.AdminService.Client.AuthInternalClient;
import com.After_Buy.AdminService.Client.DeviceInternalClient;
import com.After_Buy.AdminService.Dto.Response.AnnouncementListResponse;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 대시보드 및 통계 서비스 구현체
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
		DashboardUserStatsResponse userStats = getUserStats();
		DashboardOcrStatsResponse ocrStats = getOcrStats();

		LocalDateTime oneMonthAgo = LocalDateTime.now().minusDays(30);
		List<Announcement> recentAnns = announcementRepository.findTop5ByCreatedAtAfterOrderByCreatedAtDesc(oneMonthAgo);
		List<AnnouncementListResponse.AnnouncementItem> recentAnnDtoList = recentAnns.stream()
				.map(ann -> AnnouncementListResponse.AnnouncementItem.builder()
						.announcementId(ann.getAnnouncementId())
						.title(ann.getTitle())
						.category(ann.getCategory())
						.isPinned(ann.getIsPinned())
						.createdAt(ann.getCreatedAt())
						.build())
				.limit(5)
				.collect(Collectors.toList());

		List<ErrorLog> unresolvedLogs = errorLogRepository.findTop5ByIsResolvedOrderByCreatedAtDesc(0); // 0=미해결
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
		JsonNode statsNode = authInternalClient.getUserStats();
		if (statsNode != null) {
			try {
				return objectMapper.treeToValue(statsNode, DashboardUserStatsResponse.class);
			} catch (Exception e) {
				log.error("[StatsServiceImpl] 사용자 통계 파싱 실패: {}", e.getMessage());
			}
		}
		return null;
	}

	private DashboardOcrStatsResponse getOcrStats() {
		JsonNode statsNode = deviceInternalClient.getOcrStats();
		if (statsNode != null) {
			try {
				return objectMapper.treeToValue(statsNode, DashboardOcrStatsResponse.class);
			} catch (Exception e) {
				log.error("[StatsServiceImpl] OCR 통계 파싱 실패: {}", e.getMessage());
			}
		}
		return null;
	}
}
