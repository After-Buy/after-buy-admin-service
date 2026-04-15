package com.After_Buy.AdminService.Dto.Response;

import com.After_Buy.AdminService.Entity.Announcement;
import com.After_Buy.AdminService.Entity.AnnouncementCategory;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 공지사항 수정 응답 DTO
 * PUT /api/admin/announcements/{announcement_id} 응답에 사용됩니다.
 * 수정된 공지사항의 전체 필드를 반환합니다.
 *
 * @since : 2026.04.15
 * @version : 0.0.1
 * @author : 최준혁
 */
@Getter
@Builder
public class AnnouncementUpdateResponse {

	@JsonProperty("announcement_id")
	private Long announcementId;

	private String title;

	private AnnouncementCategory category;

	private String content;

	@JsonProperty("is_pinned")
	private Integer isPinned;

	@JsonProperty("created_by")
	private Long createdBy;

	@JsonProperty("created_at")
	private LocalDateTime createdAt;

	@JsonProperty("updated_at")
	private LocalDateTime updatedAt;

	/**
	 * Announcement 엔티티로부터 수정 응답 DTO를 생성합니다.
	 *
	 * @param announcement : 수정 완료된 공지사항 엔티티
	 * @return : 공지사항 수정 응답 DTO
	 */
	public static AnnouncementUpdateResponse from(Announcement announcement) {
		return AnnouncementUpdateResponse.builder()
				.announcementId(announcement.getAnnouncementId())
				.title(announcement.getTitle())
				.category(announcement.getCategory())
				.content(announcement.getContent())
				.isPinned(announcement.getIsPinned())
				.createdBy(announcement.getCreatedBy())
				.createdAt(announcement.getCreatedAt())
				.updatedAt(announcement.getUpdatedAt())
				.build();
	}
}
