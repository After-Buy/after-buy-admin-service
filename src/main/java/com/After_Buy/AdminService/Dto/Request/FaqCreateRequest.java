package com.After_Buy.AdminService.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 이용 안내(FAQ) 등록/수정 요청 DTO
 * POST /api/admin/faqs 및 PUT /api/admin/faqs/{id} 에서 공용으로 사용됩니다.
 *
 * @since : 2026.04.26
 * @version : 0.0.1
 * @author : 신태훈
 */
@Getter
@NoArgsConstructor
public class FaqCreateRequest {

	@NotBlank(message = "제목은 필수 입력값입니다.")
	private String title;

	@NotBlank(message = "본문은 필수 입력값입니다.")
	private String content;
}
