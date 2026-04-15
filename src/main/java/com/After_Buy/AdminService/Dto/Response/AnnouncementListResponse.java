package com.After_Buy.AdminService.Dto.Response;

import com.After_Buy.AdminService.Entity.AnnouncementCategory;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 공지사항 목록 조회 응답 DTO
 * GET /api/admin/announcements 응답에 사용됩니다.
 * 상단 고정 공지(pinned_announcements)와 일반 공지(announcements)를 분리하여 반환합니다.
 *
 * @since : 2026.04.10
 * @version : 0.0.1
 * @author : 최준혁
 */
@Getter
@Builder
public class AnnouncementListResponse {

	@JsonProperty("pinned_announcements")
	private List<AnnouncementItem> pinnedAnnouncements;

	@JsonProperty("announcements")
	private List<AnnouncementItem> announcements;

	private Pagination pagination;

	/**
	 * 공지사항 목록 개별 항목 DTO
	 * 명세서 기준 필드: announcement_id, title, category, is_pinned, is_new, is_read, created_at
	 * 목록에서는 content, created_by를 반환하지 않습니다. (상세 조회에서만 제공)
	 *
	 * @since : 2026.04.10
	 * @version : 0.0.1
	 * @author : 최준혁
	 */
	@Getter
	@Builder
	public static class AnnouncementItem {

		@JsonProperty("announcement_id")
		private Long announcementId;

		private String title;

		private AnnouncementCategory category;

		@JsonProperty("is_pinned")
		private Integer isPinned;

		@JsonProperty("created_at")
		private LocalDateTime createdAt;

		/**
		 * is_new: DATE(created_at) = CURDATE() 조건으로 서버에서 계산
		 * DB 필드 없이 서버 로직으로만 처리됩니다.
		 */
		@JsonProperty("is_new")
		private Boolean isNew;

		/**
		 * is_read: announcement_reads 테이블에서 userId-announcementId 조합 존재 여부로 판단
		 * JWT 사용자 인증 시에만 의미 있는 값. 관리자 세션 접근 시 false로 반환됩니다.
		 */
		@JsonProperty("is_read")
		private Boolean isRead;
	}

	/**
	 * 페이지네이션 정보 DTO
	 *
	 * @since : 2026.04.10
	 * @version : 0.0.1
	 * @author : 최준혁
	 */
	@Getter
	@Builder
	public static class Pagination {

		@JsonProperty("current_page")
		private int currentPage;

		@JsonProperty("total_pages")
		private int totalPages;

		@JsonProperty("total_count")
		private long totalCount;

		private int size;
	}
}
