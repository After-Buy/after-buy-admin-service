package com.After_Buy.AdminService.Service;

import com.After_Buy.AdminService.Dto.Request.AdminLoginRequest;
import com.After_Buy.AdminService.Dto.Response.AdminLoginResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 관리자 인증 서비스 인터페이스
 *
 * @author 최준혁
 * @since 2026.03.26
 * @version 0.0.1
 */
public interface AdminAuthService {

	/**
	 * 관리자 로그인 처리
	 *
	 * @param request     로그인 요청 DTO (admin_account, password)
	 * @param httpRequest HTTP 요청 객체 (IP, User-Agent 추출용)
	 * @return 로그인 성공 응답 DTO (admin_id, admin_account, sessionId)
	 */
	AdminLoginResponse login(AdminLoginRequest request, HttpServletRequest httpRequest);

	/**
	 * 관리자 로그아웃 처리
	 *
	 * @param httpRequest HTTP 요청 객체 (세션 쿠키 추출용)
	 */
	void logout(HttpServletRequest httpRequest);
}
