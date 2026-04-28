package com.After_Buy.AdminService.Dto.Response;

import com.After_Buy.AdminService.Entity.Faq;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 이용 안내(FAQ) 목록 조회 응답 DTO
 * GET /api/admin/faqs 응답에 사용됩니다.
 *
 * @since : 2026.04.26
 * @version : 0.0.1
 * @author : 신태훈
 */
@Getter
@Builder
public class FaqListResponse {

	private List<FaqItem> faqs;
	private Pagination pagination;

	/**
	 * FAQ 목록 개별 항목 DTO
	 */
	@Getter
	@Builder
	public static class FaqItem {

		@JsonProperty("faq_id")
		private Long faqId;

		private String title;

		@JsonProperty("created_by")
		private Long createdBy;

		@JsonProperty("created_at")
		private LocalDateTime createdAt;

		@JsonProperty("updated_at")
		private LocalDateTime updatedAt;

		/**
		 * Faq 엔티티로부터 FaqItem DTO를 생성합니다.
		 *
		 * @param faq : FAQ 엔티티
		 * @return FaqItem DTO
		 */
		public static FaqItem from(Faq faq) {
			return FaqItem.builder()
					.faqId(faq.getFaqId())
					.title(faq.getTitle())
					.createdBy(faq.getCreatedBy())
					.createdAt(faq.getCreatedAt())
					.updatedAt(faq.getUpdatedAt())
					.build();
		}
	}

	/**
	 * 페이지네이션 정보 DTO
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
