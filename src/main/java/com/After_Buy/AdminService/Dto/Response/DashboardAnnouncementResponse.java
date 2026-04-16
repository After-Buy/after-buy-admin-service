package com.After_Buy.AdminService.Dto.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 대시보드용 공지사항 요약 응답 DTO
 *
 * @since : 2026.04.15
 * @version : 0.0.1
 * @author : 최준혁
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DashboardAnnouncementResponse {

	@JsonProperty("announcement_id")
	private Long announcementId;

	private String title;

	private String category;

	@JsonProperty("created_at")
	private LocalDateTime createdAt;

	@Builder
	public DashboardAnnouncementResponse(Long announcementId, String title, String category, LocalDateTime createdAt) {
		this.announcementId = announcementId;
		this.title = title;
		this.category = category;
		this.createdAt = createdAt;
	}
}
