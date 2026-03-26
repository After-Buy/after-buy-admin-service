package com.After_Buy.AdminService.Dto.Response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 공통 에러 응답 DTO
 * 예외 처리 방법.md 에 정의된 표준 에러 응답 JSON 규격입니다.
 *
 * @author 최준혁
 * @since 2026.03.26
 * @version 0.0.1
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

	/** 에러 발생 서버 시각 */
	private final boolean success = false;

	/** 에러 발생 서버 시각 */
	private final LocalDateTime timestamp;

	/** HTTP 상태 코드 (400, 401, 500 등) */
	private final int status;

	/** 프론트엔드 분기용 커스텀 에러 코드 (예: ADMIN-001) */
	private final String code;

	/** 클라이언트에게 전달할 에러 안내 메시지 */
	private final String message;

	/**
	 * @Valid 유효성 검사 실패 시 필드별 오류 목록
	 * 일반 비즈니스 에러의 경우 빈 배열로 반환
	 */
	private final List<FieldError> errors;

	/** 에러가 발생한 API 엔드포인트 경로 */
	private final String path;

	/**
	 * @Valid 유효성 검사 실패 필드 정보
	 */
	@Getter
	@Builder
	public static class FieldError {
		private final String field;
		private final String value;
		private final String reason;
	}
}
