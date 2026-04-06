package com.After_Buy.AdminService.Controller;

import com.After_Buy.AdminService.Dto.Request.AdminLoginRequest;
import com.After_Buy.AdminService.Dto.Response.AdminLoginResponse;
import com.After_Buy.AdminService.Dto.Response.ApiResponse;
import com.After_Buy.AdminService.Dto.Response.ErrorResponse;
import com.After_Buy.AdminService.Service.AdminAuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Admin Auth", description = "관리자 인증 API (세션 쿠키 발급 및 로그아웃)")
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
	 * @param request     로그인 요청 DTO (admin_account, password)
	 * @param httpRequest 내부 요청 처리용
	 * @param httpResponse 쿠키 발급용
	 * @return 로그인 성공 응답 (admin_id, admin_account)
	 */
	@Operation(summary = "관리자 로그인", description = "사전 지급된 아이디/비밀번호로 인증하고 세션 쿠키를 발급합니다.")
	@ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공 - Set-Cookie로 ADMIN_SESSION_ID 발급"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "필수 입력값 누락", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "아이디/비밀번호 불일치 (ADMIN-001)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "423", description = "계정 잠금 (ADMIN-002)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@PostMapping("/login")
	public ResponseEntity<ApiResponse<Map<String, Object>>> login(
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

		Map<String, Object> data = Map.of(
				"admin_id", loginResponse.getAdminId(),
				"admin_account", loginResponse.getAdminAccount()
		);
		return ResponseEntity.ok(ApiResponse.success(data, "로그인에 성공했습니다."));
	}

	/**
	 * 관리자 로그아웃
	 * POST /api/admin/auth/logout
	 * 세션 쿠키를 무효화합니다.
	 *
	 * @param httpRequest  내부 요청 처리용
	 * @param httpResponse 쿠키 만료용
	 * @return 로그아웃 성공 메시지
	 */
	@Operation(summary = "관리자 로그아웃", description = "발급된 세션 쿠키를 무효화합니다.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그아웃 성공")
	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<Void>> logout(
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

		return ResponseEntity.ok(ApiResponse.successWithMessage("로그아웃되었습니다."));
	}

	/**
	 * 관리자 세션 검증 (인증 체크)
	 * GET /api/admin/auth/check
	 * 현재 발급된 세션 쿠키가 유효한지 검사합니다.
	 *
	 * @param request HTTP 요청 객체 (세션 추출용)
	 * @return 기존 관리자 정보 또는 401 Unauthorized
	 */
	@Operation(summary = "관리자 세션 검증", description = "현재 브라우저가 가지고 있는 세션 쿠키가 유효한지 검사합니다.")
	@ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인증 성공 (세션 유효)"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (세션 만료 또는 없음)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@GetMapping("/check")
	public ResponseEntity<ApiResponse<Map<String, Object>>> checkSession(HttpServletRequest request) {
		jakarta.servlet.http.HttpSession session = request.getSession(false);

		if (session == null || session.getAttribute("adminId") == null) {
			// 세션이 없거나 세션 내에 adminId가 존재하지 않으면 401 반환
			throw new com.After_Buy.AdminService.Exception.CustomException(
					com.After_Buy.AdminService.Exception.ErrorCode.UNAUTHORIZED_ADMIN_SESSION);
		}

		Long adminId = (Long) session.getAttribute("adminId");
		String adminAccount = (String) session.getAttribute("adminAccount");

		Map<String, Object> data = Map.of(
				"admin_id", adminId,
				"admin_account", adminAccount
		);
		return ResponseEntity.ok(ApiResponse.success(data, "유효한 세션입니다."));
	}
}
