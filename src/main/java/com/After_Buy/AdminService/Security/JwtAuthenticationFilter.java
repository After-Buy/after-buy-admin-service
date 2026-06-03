package com.After_Buy.AdminService.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 토큰 검증 필터 (AdminService 용)
 * <p>
 * 사용자 앱에서 넘어오는 JWT를 검사하여 SecurityContext에 등록합니다.
 *
 * @author 최준혁
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private static final String TOKEN_EXPIRED_RESPONSE = """
            {
              "success": false,
              "error": {
                "code": "TOKEN_EXPIRED",
                "message": "Access Token이 만료되었거나 유효하지 않습니다."
              }
            }
            """;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);
        if (StringUtils.hasText(token)) {
            if (!jwtTokenProvider.validateToken(token)) {
                if (isPublicPath(request)) {
                    filterChain.doFilter(request, response);
                    return;
                }
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(TOKEN_EXPIRED_RESPONSE);
                return;
            }
            try {
                Long userId = jwtTokenProvider.getUserIdFromToken(token);
                UserPrincipal userPrincipal = new UserPrincipal(userId);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userPrincipal, null, List.of());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                log.debug("JWT 인증 처리 중 오류: {}", e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/internal/")
                || path.equals("/api/admin/auth/login")
                || path.equals("/swagger-ui.html")
                || path.startsWith("/swagger-ui/")
                || path.startsWith("/v3/api-docs/")
                || path.startsWith("/api/admin/swagger-ui/")
                || path.startsWith("/api/admin/v3/api-docs/");
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
