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
 */
@Slf4j
@RequiredArgsConstructor
public class AdminSessionAuthFilter extends OncePerRequestFilter {

	private final ObjectMapper objectMapper;

	/** 인증 없이 접근 가능한 경로 (로그인 엔드포인트) */
	private static final String LOGIN_PATH = "/api/admin/auth/login";

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {

		String requestUri = request.getRequestURI();

		// 로그인 엔드포인트는 인증 없이 통과
		if (requestUri.equals(LOGIN_PATH) && request.getMethod().equals("POST")) {
			filterChain.doFilter(request, response);
			return;
		}

		// 기존 세션 조회 (false = 없어도 새로 생성하지 않음)
		HttpSession session = request.getSession(false);

		// 세션 없음 또는 adminId 속성 없음 → 401 반환
		if (session == null || session.getAttribute("adminId") == null) {
			log.warn("[SessionAuthFilter] 미인증 접근 차단 - path={}", requestUri);
			sendUnauthorizedResponse(response, request);
			return;
		}

		filterChain.doFilter(request, response);
	}

	/**
	 * 401 Unauthorized 에러 응답을 JSON으로 직접 작성합니다.
	 */
	private void sendUnauthorizedResponse(
			HttpServletResponse response,
			HttpServletRequest request
	) throws IOException {
		ErrorCode errorCode = ErrorCode.ADMIN_UNAUTHORIZED;

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
