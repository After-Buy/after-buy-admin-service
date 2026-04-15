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
 * Device Service 내부 API 호출 클라이언트
 *
 * @since : 2026.04.15
 * @version : 0.0.1
 * @author : 최준혁
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceInternalClient {

	private final WebClient webClient;
	private final ObjectMapper objectMapper;

	@Value("${INTERNAL_SECRET_KEY}")
	private String internalSecret;

	@Value("${DEVICE_SERVICE_URL}")
	private String deviceServiceUrl;

	/**
	 * OCR 실패 통계 조회 (비동기 Mono 반환)
	 * 실패 시 empty Mono 반환
	 */
	public Mono<JsonNode> getOcrStatsMono() {
		return webClient.get()
				.uri(deviceServiceUrl + "/internal/ocr-stats")
				.header("X-Internal-Secret", internalSecret)
				.retrieve()
				.bodyToMono(String.class)
				.flatMap(responseString -> {
					try {
						JsonNode rootNode = objectMapper.readTree(responseString);
						if (rootNode.has("success") && rootNode.get("success").asBoolean() && rootNode.has("data")) {
							JsonNode dataNode = rootNode.get("data");
							if (dataNode.has("summary")) {
								return Mono.just(dataNode.get("summary"));
							}
							return Mono.just(dataNode);
						}
					} catch (Exception e) {
						log.error("[DeviceInternalClient] JSON 파싱 실패: {}", e.getMessage());
					}
					return Mono.empty();
				})
				.onErrorResume(e -> {
					log.error("[DeviceInternalClient] OCR 통계 조회 실패: {}", e.getMessage());
					return Mono.empty();
				});
	}
}
