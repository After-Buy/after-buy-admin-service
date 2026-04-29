package com.After_Buy.AdminService.Dto.Response;

import com.After_Buy.AdminService.Entity.Faq;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 이용 안내(FAQ) 수정 응답 DTO
 * PUT /api/admin/faqs/{faq_id} 응답에 사용됩니다.
 *
 * @since : 2026.04.26
 * @version : 0.0.1
 * @author : 신태훈
 */
@Getter
@Builder
public class FaqUpdateResponse {

	@JsonProperty("faq_id")
	private Long faqId;

	private String title;

	private String content;

	@JsonProperty("created_by")
	private Long createdBy;

	@JsonProperty("created_at")
	private LocalDateTime createdAt;

	@JsonProperty("updated_at")
	private LocalDateTime updatedAt;

	/**
	 * Faq 엔티티로부터 수정 응답 DTO를 생성합니다.
	 *
	 * @param faq : 수정된 FAQ 엔티티
	 * @return FaqUpdateResponse DTO
	 */
	public static FaqUpdateResponse from(Faq faq) {
		return FaqUpdateResponse.builder()
				.faqId(faq.getFaqId())
				.title(faq.getTitle())
				.content(faq.getContent())
				.createdBy(faq.getCreatedBy())
				.createdAt(faq.getCreatedAt())
				.updatedAt(faq.getUpdatedAt())
				.build();
	}
}
