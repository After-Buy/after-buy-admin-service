package com.After_Buy.AdminService.Client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Auth Service 내부 API 호출 클라이언트
 *
 * @since : 2026.04.15
 * @version : 0.0.1
 * @author : 최준혁
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInternalClient {

	private final WebClient webClient;
	private final ObjectMapper objectMapper;

	@Value("${INTERNAL_SECRET_KEY}")
	private String internalSecret;

	@Value("${AUTH_SERVICE_URL}")
	private String authServiceUrl;

	/**
	 * 사용자 통계 정보 조회 (비동기 Mono 반환)
	 */
	public Mono<JsonNode> getUserStatsMono() {
		return webClient.get()
				.uri(authServiceUrl + "/internal/users/stats")
				.header("X-Internal-Secret", internalSecret)
				.retrieve()
				.bodyToMono(String.class)
				.flatMap(responseString -> {
					try {
						JsonNode rootNode = objectMapper.readTree(responseString);
						return Mono.just(rootNode);
					} catch (Exception e) {
						log.error("[AuthInternalClient] JSON 파싱 실패: {}", e.getMessage());
					}
					return Mono.empty();
				})
				.onErrorResume(e -> {
					log.error("[AuthInternalClient] 통계 조회 실패: {}", e.getMessage());
					return Mono.empty();
				});
	}
}
