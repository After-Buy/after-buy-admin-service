package com.After_Buy.AdminService.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * JWT 토큰 검증 전용 프로바이더 (AdminService 용)
 *
 * @author 최준혁
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;

    public JwtTokenProvider(@Value("${INTERNAL_SECRET_KEY}") String secret) {
        // 원래 Auth Service의 JWT 시크릿을 써야 하나, 여기서는 편의상/공용이므로
        // 내부 통신 키 또는 JWT_SECRET 환경변수를 활용합니다. (본 프로젝트 구조에서는 통일된 키를 주로 사용)
        // 주의: 실제 앱에서는 Auth Service에서 서명할 때 쓴 `jwt.secret`와 동일해야 합니다.
        // 현재 Admin의 .env에는 INTERNAL_SECRET_KEY만 있으므로, 우선 이것을 사용하거나 JWT_SECRET을 바라보게 합니다.
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("만료된 JWT 토큰입니다: {}", e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("유효하지 않은 JWT 토큰입니다: {}", e.getMessage());
        }
        return false;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(this.secretKey).build().parseSignedClaims(token).getPayload();
    }
}
