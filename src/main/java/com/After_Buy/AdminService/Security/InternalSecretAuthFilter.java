package com.After_Buy.AdminService.Security;

import com.After_Buy.AdminService.Dto.Response.ErrorResponse;
import com.After_Buy.AdminService.Exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * MSA 내부 통신 시크릿 헤더 인증 필터
 * {@code /internal/**} 경로에 대해 X-Internal-Secret 헤더를 검증합니다.
 *
 * @since : 2026.05.06
 * @version : 0.0.1
 * @author : 최준혁
 */
@Slf4j
@Component
@Order(1)
public class InternalSecretAuthFilter extends OncePerRequestFilter {

	/** MSA 내부 통신 인증에 사용되는 시크릿 키 */
	private final String internalSecretKey;

	private final ObjectMapper objectMapper;

	public InternalSecretAuthFilter(
			@Value("${internal.secret-key}") String internalSecretKey,
			ObjectMapper objectMapper) {
		this.internalSecretKey = internalSecretKey;
		this.objectMapper = objectMapper;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {

		String requestUri = request.getRequestURI();

		/* /internal/** 경로가 아닌 요청은 이 필터에서 처리하지 않음 */
		if (!requestUri.startsWith("/internal")) {
			filterChain.doFilter(request, response);
			return;
		}

		/* X-Internal-Secret 헤더 존재 여부 확인 */
		String secret = request.getHeader("X-Internal-Secret");
		if (secret == null || secret.isBlank()) {
			log.warn("[InternalSecretAuthFilter] X-Internal-Secret 헤더 누락 - path={}", requestUri);
			sendUnauthorizedResponse(response, request);
			return;
		}

		/* 시크릿 키 일치 여부 검증 */
		if (!internalSecretKey.equals(secret)) {
			log.warn("[InternalSecretAuthFilter] X-Internal-Secret 불일치 - path={}", requestUri);
			sendUnauthorizedResponse(response, request);
			return;
		}

		/* 검증 성공 → 다음 필터로 통과 */
		filterChain.doFilter(request, response);
	}

	/**
	 * 401 Unauthorized 에러 응답을 JSON으로 반환합니다.
	 */
	private void sendUnauthorizedResponse(
			HttpServletResponse response,
			HttpServletRequest request) throws IOException {
		ErrorCode errorCode = ErrorCode.UNAUTHORIZED_INTERNAL_SECRET;

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
