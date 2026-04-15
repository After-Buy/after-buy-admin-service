package com.After_Buy.AdminService.Client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

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
	 * 사용자 통계 정보 조회
	 * 실패 시 null 반환 (부분 응답 지원)
	 */
	public JsonNode getUserStats() {
		try {
			String responseString = webClient.get()
					.uri(authServiceUrl + "/internal/users/stats")
					.header("X-Internal-Secret", internalSecret)
					.retrieve()
					.bodyToMono(String.class)
					.block();

			if (responseString != null) {
				JsonNode rootNode = objectMapper.readTree(responseString);
				if (rootNode.has("success") && rootNode.get("success").asBoolean() && rootNode.has("data")) {
					return rootNode.get("data");
				}
			}
		} catch (Exception e) {
			log.error("[AuthInternalClient] 통계 조회 실패: {}", e.getMessage());
		}
		return null;
	}
}
