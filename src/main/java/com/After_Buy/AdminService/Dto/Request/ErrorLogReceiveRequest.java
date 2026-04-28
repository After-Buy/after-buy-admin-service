package com.After_Buy.AdminService.Dto.Request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 타 마이크로서비스에서 Admin Service로 전송하는 에러 로그 수신 DTO
 * POST /internal/error-logs 요청 바디에 사용됩니다.
 *
 * 호출 방향: Auth / Device / Notification → Admin Service
 * 처리 방식: 비동기 Fire & Forget
 *
 * @since : 2026.04.26
 * @version : 0.0.1
 * @author : 신태훈
 */
@Getter
@NoArgsConstructor
public class ErrorLogReceiveRequest {

	/** 에러 발생 서비스명 (예: "AUTH", "DEVICE", "NOTIFICATION") */
	@JsonProperty("service_name")
	private String serviceName;

	/** 에러 발생 엔드포인트 경로 (예: "/api/auth/users/me") */
	@JsonProperty("endpoint_path")
	private String endpointPath;

	/** 예외 클래스 타입 (예: "NullPointerException") */
	@JsonProperty("error_type")
	private String errorType;

	/** 에러 메시지 본문 */
	@JsonProperty("error_message")
	private String errorMessage;

	/** 에러 발생 시각 */
	@JsonProperty("occurred_at")
	private LocalDateTime occurredAt;
}
