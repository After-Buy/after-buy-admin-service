package com.After_Buy.AdminService.Security;

import com.After_Buy.AdminService.Dto.Response.ErrorResponse;
import com.After_Buy.AdminService.Exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 관리자 세션 인증 필터
 * 보호된 엔드포인트 요청 시 HttpSession에 adminId가 존재하는지 검증합니다.
 * 세션이 없거나 만료된 경우 401 ADMIN-004를 반환합니다.
 *
 * @author 최준혁
 * @since 2026.03.26
 * @version 0.0.1
 *
 * [수정 이력]
 * @modified 신태훈
 * @since 2026.05.07
 * @version 0.0.2 - GET /api/admin/faqs JWT 허용 경로 추가 (일반 사용자 접근 허용)
 */
@Slf4j
@RequiredArgsConstructor
public class AdminSessionAuthFilter extends OncePerRequestFilter {

	private final ObjectMapper objectMapper;

	/** 인증 없이 접근 가능한 경로 목록 (로그인 엔드포인트 및 Swagger)
	 * /internal/** 경로는 InternalSecretAuthFilter가 전담하여 검증합니다. */
	private static final List<String> PUBLIC_PATHS = List.of(
			"/api/admin/auth/login",
			"/swagger-ui",           // Swagger UI 기본 서빙 경로
			"/v3/api-docs",          // OpenAPI 스펙 JSON 기본 경로
			"/api/admin/swagger-ui", // 배포 환경 Swagger 경로
			"/api/admin/v3/api-docs", // 배포 환경 OpenAPI 경로
			"/internal"); // MSA 내부 통신 API 경로 (InternalSecretAuthFilter에서 별도 검증)


	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {

		// CORS preflight 요청(OPTIONS 메서드)은 인증 없이 통과
		// 브라우저가 실제 요청 전 OPTIONS를 먼저 전송하며, 이를 차단하면 CORS 동작 불가
		if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
			filterChain.doFilter(request, response);
			return;
		}

		String requestUri = request.getRequestURI();

		// 퍼블릭 경로(Swagger, 로그인 등)는 인증 없이 통과 (접두사 매칭 허용)
		boolean isPublicPath = PUBLIC_PATHS.stream().anyMatch(requestUri::startsWith);
		if (isPublicPath) {
			filterChain.doFilter(request, response);
			return;
		}

		// 기존 세션 조회 (false = 없어도 새로 생성하지 않음)
		HttpSession session = request.getSession(false);
		boolean hasValidSession = (session != null && session.getAttribute("adminId") != null);

		if (hasValidSession) {
			filterChain.doFilter(request, response);
			return;
		}

		// GET /api/admin/announcements (목록 및 상세) 경로는 JWT 인증 사용자도 접근 가능
		// GET /api/admin/faqs (목록 및 상세) 경로는 JWT 인증 사용자도 접근 가능
		// POST /api/admin/announcements/{id}/read 경로도 JWT 인증 사용자 전용 접근 가능
		boolean isJwtAllowedPath = ("GET".equalsIgnoreCase(request.getMethod()) && requestUri.startsWith("/api/admin/announcements")) ||
				("GET".equalsIgnoreCase(request.getMethod()) && requestUri.startsWith("/api/admin/faqs")) ||
				("POST".equalsIgnoreCase(request.getMethod()) && requestUri.matches("^/api/admin/announcements/\\d+/read$"));

		if (isJwtAllowedPath) {
			org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
			if (auth != null && auth.isAuthenticated()) {
				filterChain.doFilter(request, response);
				return;
			}
		}

		// 세션도 없고 (혹은 JWT 허용 경로인데 JWT도 없는 경우) 401 반환
		log.warn("[SessionAuthFilter] 미인증 접근 차단 - path={}", requestUri);
		sendUnauthorizedResponse(response, request);
		return;
	}

	/**
	 * 401 Unauthorized 에러 응답을 JSON으로 직접 작성합니다.
	 */
	private void sendUnauthorizedResponse(
			HttpServletResponse response,
			HttpServletRequest request) throws IOException {
		ErrorCode errorCode = ErrorCode.UNAUTHORIZED_ADMIN_SESSION;

		ErrorResponse errorResponse = ErrorResponse.builder()
				.timestamp(LocalDateTime.now())
				.status(errorCode.getHttpStatus().value())
				.code(errorCode.getCode())
				.message(errorCode.getMessage())
				.errors(List.of())
				.path(request.getRequestURI())
				.build();

		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
	}
}
