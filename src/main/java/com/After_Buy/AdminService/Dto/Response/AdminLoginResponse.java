package com.After_Buy.AdminService.Dto.Response;

import lombok.Builder;
import lombok.Getter;

/**
 * 관리자 로그인 성공 응답 DTO (Service → Controller 내부 전달용)
 * 클라이언트에게 직접 반환되는 JSON이 아니라,
 * Controller에서 Set-Cookie 처리 후 응답을 직접 구성합니다.
 *
 * @author 최준혁
 * @since 2026.03.26
 * @version 0.0.1
 */
@Getter
@Builder
public class AdminLoginResponse {

	/** 관리자 고유 ID */
	private Long adminId;

	/** 관리자 아이디 */
	private String adminAccount;

	/** 발급된 세션 ID (Set-Cookie 헤더에 사용) */
	private String sessionId;
}
