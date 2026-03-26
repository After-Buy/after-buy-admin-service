package com.After_Buy.AdminService.Security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
 * JWT를 사용하지 않으며, 커스텀 세션 필터(AdminSessionAuthFilter)로 인증을 처리합니다.
 *
 * 인증 흐름:
 *  POST /api/admin/auth/login → 인증 없이 허용
 *  그 외 /api/admin/** → AdminSessionAuthFilter가 세션 검증
 *
 * @author 최준혁
 * @since 2026.03.26
 * @version 0.0.1
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final ObjectMapper objectMapper;

	/**
	 * Security 필터 체인 설정
	 *
	 * @param http HttpSecurity
	 * @return SecurityFilterChain
	 */
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			// REST API이므로 CSRF 비활성화
			.csrf(AbstractHttpConfigurer::disable)

			// 세션 정책: Spring Security가 자동으로 세션을 생성하지 않음
			// (세션은 AdminAuthServiceImpl.login()에서 직접 생성)
			.sessionManagement(session -> session
				.sessionCreationPolicy(SessionCreationPolicy.NEVER)
			)

			// 엔드포인트 접근 권한 설정
			.authorizeHttpRequests(auth -> auth
				// 로그인은 인증 없이 허용
				.requestMatchers(HttpMethod.POST, "/api/admin/auth/login").permitAll()
				// 나머지 모든 요청은 세션 필터에서 검증
				.anyRequest().permitAll()
			)

			// Spring Security 기본 폼 로그인 / HTTP Basic 비활성화
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)

			// 커스텀 세션 인증 필터 등록
			.addFilterBefore(adminSessionAuthFilter(), UsernamePasswordAuthenticationFilter.class);

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
