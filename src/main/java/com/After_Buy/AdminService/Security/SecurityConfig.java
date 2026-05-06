package com.After_Buy.AdminService.Security;

import com.After_Buy.AdminService.Config.CorsConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 설정
 * Admin Service는 세션 쿠키(ADMIN_SESSION_ID) 기반 인증을 사용합니다.
 *
 * 인증 흐름:
 *  /internal/**            → InternalSecretAuthFilter가 전담 (Filter 레벨)
 *  POST /api/admin/auth/login → 인증 없이 허용
 *  그 외 /api/admin/**     → AdminSessionAuthFilter가 세션 검증
 *
 * @author 최준혁
 * @since 2026.03.26
 * @version 0.0.3
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final ObjectMapper objectMapper;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final CorsConfig corsConfig;

	/**
	 * Security 필터 체인 설정
	 *
	 * @param http HttpSecurity
	 * @return SecurityFilterChain
	 */
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			// CORS 설정 적용 (CorsConfig에서 정의한 CorsConfigurationSource 사용)
			.cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))

			// REST API이므로 CSRF 비활성화
			.csrf(AbstractHttpConfigurer::disable)

			// 세션 정책: Spring Security가 자동으로 세션을 생성하지 않음
			// (세션은 AdminAuthServiceImpl.login()에서 직접 생성)
			.sessionManagement(session -> session
				.sessionCreationPolicy(SessionCreationPolicy.NEVER)
			)

			// 엔드포인트 접근 권한 설정 (실제 검증은 커스텀 필터에서 전담)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/internal/**").permitAll()
				.anyRequest().permitAll()
			)

			// Spring Security 기본 폼 로그인 / HTTP Basic 비활성화
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)

			// 1) 사용자 JWT 인증 필터 (공지사항 등 일부 경로의 일반 유저 접근용)
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			// 2) 관리자 세션 인증 필터 (/api/admin/** 보호)
			// (주의: Spring Security 필터 등록 시 커스텀 필터를 기준점으로 삼을 수 없으므로 UsernamePasswordAuthenticationFilter 뒤에 등록)
			.addFilterAfter(adminSessionAuthFilter(), UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	/**
	 * 관리자 세션 인증 필터 Bean 등록
	 */
	@Bean
	public AdminSessionAuthFilter adminSessionAuthFilter() {
		return new AdminSessionAuthFilter(objectMapper);
	}

	/**
	 * BCrypt 비밀번호 인코더 Bean 등록
	 * AdminAuthServiceImpl에서 로그인 시 비밀번호 검증에 사용합니다.
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
