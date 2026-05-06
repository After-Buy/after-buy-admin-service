package com.After_Buy.AdminService.Config;

import org.springframework.context.annotation.Configuration;

/**
 * WebClient 설정
 * 타 마이크로서비스 내부 호출 지원 부분
 *
 * @since : 2026.04.14
 * @version : 0.0.2
 * @author : 최준혁
 */
@Configuration
public class WebClientConfig {
	/*
	 * WebClient.Builder는 Spring Boot AutoConfiguration이 자동으로 Bean을 제공합니다.
	 * 각 Client 클래스가 생성자에서 WebClient.Builder를 주입받아 개별 WebClient를 빌드하므로
	 * 공유 WebClient Bean 정의는 불필요합니다.
	 */
}
