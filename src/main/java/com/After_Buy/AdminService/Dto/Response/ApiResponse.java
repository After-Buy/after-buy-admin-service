package com.After_Buy.AdminService.Dto.Response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 공통 성공 응답 DTO
 * 모든 API 성공 응답을 감싸는 제네릭 래퍼 클래스입니다.
 * DeviceService의 ApiResponse.java와 동일한 구조를 사용합니다.
 *
 * 성공 응답 형식:
 * { "success": true, "data": { ... } }
 *
 * @since : 2026.03.26
 * @version : 1.0.0
 * @author : 최준혁
 */
@Getter
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

	/** 요청 성공 여부 (항상 true) */
	private final boolean success;

	/** 실제 응답 데이터 */
	private final T data;

	/** 응답 부가 메시지 (필요 시) */
	private final String message;

	/**
	 * 데이터가 있는 성공 응답 생성
	 *
	 * @param data 응답 데이터
	 * @param <T>  응답 데이터 타입
	 * @return success=true, data=data 응답
	 */
	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(true, data, null);
	}

	/**
	 * 데이터와 메시지가 있는 성공 응답 생성
	 *
	 * @param data    응답 데이터
	 * @param message 응답 메시지
	 * @param <T>     응답 데이터 타입
	 * @return success=true, data=data, message=message 응답
	 */
	public static <T> ApiResponse<T> success(T data, String message) {
		return new ApiResponse<>(true, data, message);
	}

	/**
	 * 데이터 없이 메시지만 있는 성공 응답 생성 (로그아웃 등)
	 *
	 * @param message 응답 메시지
	 * @return success=true, message=message 응답
	 */
	public static <T> ApiResponse<T> successWithMessage(String message) {
		return new ApiResponse<>(true, null, message);
	}
}
