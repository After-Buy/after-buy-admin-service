package com.After_Buy.AdminService.Controller;

import com.After_Buy.AdminService.Dto.Request.AdminLoginRequest;
import com.After_Buy.AdminService.Dto.Response.AdminLoginResponse;
import com.After_Buy.AdminService.Service.AdminAuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 관리자 인증 컨트롤러
 * Base URL: /api/admin/auth
 * 관리자 로그인 / 로그아웃 엔드포인트를 처리합니다.
 *
 * @author 최준혁
 * @since 2026.03.26
 * @version 0.0.1
 */
@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

	private final AdminAuthService adminAuthService;

	/**
	 * 관리자 로그인
	 * POST /api/admin/auth/login
	 * 사전 지급된 아이디/비밀번호로 인증하고 세션 쿠키(ADMIN_SESSION_ID)를 발급합니다.
	 *
	 * @param request  로그인 요청 DTO (admin_account, password)
	 * @param response 응답 객체 (Set-Cookie 헤더 설정용)
	 * @return 로그인 성공 응답 (admin_id, admin_account)
	 * @since 2026.03.26
	 * @version 0.0.1
	 * @throws com.After_Buy.AdminService.Exception.CustomException ADMIN-001 (아이디/비밀번호 불일치), ADMIN-002 (계정 잠금)
	 */
	@PostMapping("/login")
	public ResponseEntity<Map<String, Object>> login(
			@Valid @RequestBody AdminLoginRequest request,
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse
	) {
		AdminLoginResponse loginResponse = adminAuthService.login(request, httpRequest);

		// Set-Cookie: ADMIN_SESSION_ID=...; HttpOnly; Secure; SameSite=Strict; Path=/api/admin
		Cookie sessionCookie = new Cookie("ADMIN_SESSION_ID", loginResponse.getSessionId());
		sessionCookie.setHttpOnly(true);
		sessionCookie.setSecure(true);
		sessionCookie.setPath("/api/admin");
		sessionCookie.setMaxAge(60 * 60 * 8); // 8시간
		httpResponse.addCookie(sessionCookie);

		return ResponseEntity.ok(Map.of(
				"success", true,
				"data", Map.of(
						"admin_id", loginResponse.getAdminId(),
						"admin_account", loginResponse.getAdminAccount()
				),
				"message", "로그인에 성공했습니다."
		));
	}

	/**
	 * 관리자 로그아웃
	 * POST /api/admin/auth/logout
	 * 세션 쿠키를 무효화합니다.
	 *
	 * @param httpRequest  요청 객체 (세션 쿠키 추출용)
	 * @param httpResponse 응답 객체 (쿠키 만료 처리용)
	 * @return 로그아웃 성공 메시지
	 * @since 2026.03.26
	 * @version 0.0.1
	 */
	@PostMapping("/logout")
	public ResponseEntity<Map<String, Object>> logout(
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse
	) {
		adminAuthService.logout(httpRequest);

		// 쿠키 만료 처리 (maxAge=0으로 즉시 삭제)
		Cookie expiredCookie = new Cookie("ADMIN_SESSION_ID", null);
		expiredCookie.setHttpOnly(true);
		expiredCookie.setSecure(true);
		expiredCookie.setPath("/api/admin");
		expiredCookie.setMaxAge(0);
		httpResponse.addCookie(expiredCookie);

		return ResponseEntity.ok(Map.of(
				"success", true,
				"message", "로그아웃되었습니다."
		));
	}
}
