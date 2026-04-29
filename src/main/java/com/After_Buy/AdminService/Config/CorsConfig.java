package com.After_Buy.AdminService.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CORS(Cross-Origin Resource Sharing) 전역 설정
 * Admin_web(React)에서 Admin_Service API 호출 시 Cross-Origin 요청을 허용합니다.
 *
 * @author 최준혁
 * @since 2026.04.29
 * @version 0.0.1
 */
@Configuration
public class CorsConfig {

	/** CORS 허용 Origin (환경변수 CORS_ALLOWED_ORIGINS에서 주입) */
	@Value("${cors.allowed-origins}")
	private String allowedOriginsRaw;

	/**
	 * CorsConfigurationSource Bean 등록
	 * SecurityConfig에서 주입받아 .cors() 설정에 사용합니다.
	 *
	 * @return CorsConfigurationSource
	 */
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();

		config.setAllowedOrigins(List.of(allowedOriginsRaw.trim()));
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));

		// withCredentials: true 사용으로 세션 쿠키 전송 허용 필수
		config.setAllowCredentials(true);
		config.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);

		return source;
	}
}
