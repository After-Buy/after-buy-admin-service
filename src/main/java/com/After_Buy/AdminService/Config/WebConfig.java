package com.After_Buy.AdminService.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 전역 CORS(Cross-Origin Resource Sharing) 설정
 * .env 파일의 ADMIN_FRONTEND_URL 값과 연동하여 프론트엔드 통신 허용
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${ADMIN_FRONTEND_URL:http://localhost:5173}")
    private String adminFrontendUrl;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 경로에 대해
                .allowedOrigins(adminFrontendUrl) // 허용할 프론트엔드 출처
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS") // 허용할 HTTP 메서드
                .allowedHeaders("*") // 모든 헤더 허용
                .allowCredentials(true) // 세션 쿠키 등 자격 증명 허용 (중요)
                .maxAge(3600); // 1시간 동안 프리플라이트 응답 캐싱
    }
}
