package com.After_Buy.AdminService.Exception;

import lombok.Getter;

/**
 * 커스텀 전역 예외 클래스
 * 비즈니스 로직에서 의도적으로 발생시키는 예외입니다.
 * ErrorCode를 담아 GlobalExceptionHandler에서 표준 에러 응답으로 변환합니다.
 *
 * 사용 예시: throw new CustomException(ErrorCode.ADMIN_INVALID_CREDENTIALS);
 *
 * @author 최준혁
 * @since 2026.03.26
 * @version 0.0.1
 */
@Getter
public class CustomException extends RuntimeException {

	/** 에러 상태 코드, 커스텀 코드, 메시지를 담은 Enum */
	private final ErrorCode errorCode;

	public CustomException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}
}
