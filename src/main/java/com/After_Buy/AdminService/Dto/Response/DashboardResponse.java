package com.After_Buy.AdminService.Dto.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 대시보드 API 응답 DTO
 * 
 * @since : 2026.04.15
 * @version : 0.0.1
 * @author : 최준혁
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DashboardResponse {

	@JsonProperty("user_stats")
	private DashboardUserStatsResponse userStats;

	@JsonProperty("ocr_stats")
	private DashboardOcrStatsResponse ocrStats;

	@JsonProperty("recent_announcements")
	private List<AnnouncementListResponse.AnnouncementItem> recentAnnouncements;

	@JsonProperty("unresolved_error_logs")
	private List<DashboardErrorLogResponse> unresolvedErrorLogs;

	@JsonProperty("unresolved_error_count")
	private Long unresolvedErrorCount;

	@Builder
	public DashboardResponse(DashboardUserStatsResponse userStats, DashboardOcrStatsResponse ocrStats, 
							 List<AnnouncementListResponse.AnnouncementItem> recentAnnouncements, 
							 List<DashboardErrorLogResponse> unresolvedErrorLogs, Long unresolvedErrorCount) {
		this.userStats = userStats;
		this.ocrStats = ocrStats;
		this.recentAnnouncements = recentAnnouncements;
		this.unresolvedErrorLogs = unresolvedErrorLogs;
		this.unresolvedErrorCount = unresolvedErrorCount;
	}
}
