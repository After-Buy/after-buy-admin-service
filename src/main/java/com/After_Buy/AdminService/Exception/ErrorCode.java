package com.After_Buy.AdminService.Exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 서비스 전역 에러 코드 Enum
 * 에러 발생 시 프론트에게 내려줄 HTTP 상태 코드, 커스텀 코드, 메시지를 관리합니다.
 *
 * @author 최준혁
 * @since 2026.03.26
 * @version 0.0.1
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	/* ===== Admin 인증 관련 (ADMIN-0XX) ===== */
	ADMIN_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "ADMIN-001", "아이디 또는 비밀번호가 일치하지 않습니다."),
	ADMIN_ACCOUNT_LOCKED(HttpStatus.valueOf(423), "ADMIN-002", "실패 횟수 초과로 계정이 잠겼습니다. 관리자에게 문의해주세요."),
	ADMIN_NOT_FOUND(HttpStatus.NOT_FOUND, "ADMIN-003", "존재하지 않는 관리자 계정입니다."),
	ADMIN_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "ADMIN-004", "로그인이 필요합니다."),

	/* ===== 공통 (COMMON-0XX) ===== */
	INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON-001", "입력값이 올바르지 않습니다."),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-500", "서버 내부 오류가 발생했습니다.");

	/** HTTP 상태 코드 */
	private final HttpStatus httpStatus;

	/** 프론트엔드 분기용 커스텀 에러 코드 */
	private final String code;

	/** 클라이언트에게 전달할 에러 메시지 */
	private final String message;
}
