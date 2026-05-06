package com.After_Buy.AdminService.Client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * Notification Service 내부 API 호출 클라이언트
 *
 * @since : 2026.04.14
 * @version : 0.0.2
 * @author : 최준혁
 */
@Slf4j
@Component
public class NotificationInternalClient {

	private final WebClient webClient;

	public NotificationInternalClient(
			WebClient.Builder webClientBuilder,
			@Value("${services.notification-url}") String notificationUrl,
			@Value("${internal.secret-key}") String internalSecret) {
		this.webClient = webClientBuilder
				.baseUrl(notificationUrl)
				.defaultHeader("X-Internal-Secret", internalSecret)
				.build();
	}

	/**
	 * 알림 서비스에 푸시 브로드캐스트 비동기 요청 (Fire & Forget 권장)
	 */
	public void broadcastPushAsync(String title, String body, String deepLink, Long announcementId) {
		Map<String, Object> requestBody = Map.of(
				"title", title,
				"body", body,
				"deep_link", deepLink,
				"announcement_id", announcementId);

		webClient.post()
				.uri("/internal/push/broadcast")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(requestBody)
				.retrieve()
				.bodyToMono(Void.class)
				.doOnSuccess(response -> log.info("[NotificationInternalClient] 푸시 발송 요청 성공: announcementId={}",
						announcementId))
				.doOnError(error -> log.error("[NotificationInternalClient] 푸시 발송 요청 실패: {}", error.getMessage()))
				.subscribe(); // 비동기 실행 (Fire & Forget)
	}
}
