package com.After_Buy.AdminService.Client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

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
	 * OCR 실패 통계 조회
	 * 실패 시 null 반환 (부분 응답 지원)
	 */
	public JsonNode getOcrStats() {
		try {
			String responseString = webClient.get()
					.uri(deviceServiceUrl + "/internal/ocr-stats")
					.header("X-Internal-Secret", internalSecret)
					.retrieve()
					.bodyToMono(String.class)
					.block();

			if (responseString != null) {
				JsonNode rootNode = objectMapper.readTree(responseString);
				if (rootNode.has("success") && rootNode.get("success").asBoolean() && rootNode.has("data")) {
					JsonNode dataNode = rootNode.get("data");
					// Device 서비스 API는 data 내부의 summary 안에 통계를 반환하는 구조일 가능성이 높음. 명세서에 따라 data 안에 있는지 직접 확인 필요. 
					// Admin 명세에서는 ocr_stats로 그대로 사용되므로, 데이터 노드 자체를 넘겨 파싱.
					if (dataNode.has("summary")) {
						return dataNode.get("summary");
					}
					return dataNode;
				}
			}
		} catch (Exception e) {
			log.error("[DeviceInternalClient] OCR 통계 조회 실패: {}", e.getMessage());
		}
		return null;
	}
}
