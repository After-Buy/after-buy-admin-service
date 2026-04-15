package com.After_Buy.AdminService.Dto.Response;

import com.After_Buy.AdminService.Entity.Announcement;
import com.After_Buy.AdminService.Entity.AnnouncementCategory;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 공지사항 상세 조회 응답 DTO
 * GET /api/admin/announcements/{announcement_id} 응답에 사용됩니다.
 * 관리자/사용자 공용으로 사용됩니다.
 *
 * @author 최준혁
 * @since 2026.04.15
 * @version 0.0.1
 */
@Getter
@Builder
public class AnnouncementDetailResponse {

	@JsonProperty("announcement_id")
	private Long announcementId;

	private String title;

	private AnnouncementCategory category;

	private String content;

	@JsonProperty("is_pinned")
	private Integer isPinned;

	/**
	 * is_new: DATE(created_at) = CURDATE() 조건으로 서버에서 계산
	 * DB 필드 없이 서버 로직으로만 처리
	 */
	@JsonProperty("is_new")
	private Boolean isNew;

	@JsonProperty("created_by")
	private Long createdBy;

	@JsonProperty("created_at")
	private LocalDateTime createdAt;

	@JsonProperty("updated_at")
	private LocalDateTime updatedAt;

	/**
	 * Announcement 엔티티와 오늘 날짜를 받아 상세 응답 DTO를 생성합니다.
	 *
	 * @param announcement 공지사항 엔티티
	 * @param today        서버 현재 날짜 (is_new 계산용)
	 * @return 상세 조회 응답 DTO
	 */
	public static AnnouncementDetailResponse from(Announcement announcement, LocalDate today) {
		// is_new: 공지사항 등록일이 오늘과 동일한 경우 true
		boolean isNew = announcement.getCreatedAt().toLocalDate().isEqual(today);

		return AnnouncementDetailResponse.builder()
				.announcementId(announcement.getAnnouncementId())
				.title(announcement.getTitle())
				.category(announcement.getCategory())
				.content(announcement.getContent())
				.isPinned(announcement.getIsPinned())
				.isNew(isNew)
				.createdBy(announcement.getCreatedBy())
				.createdAt(announcement.getCreatedAt())
				.updatedAt(announcement.getUpdatedAt())
				.build();
	}
}
