package com.After_Buy.AdminService.Dto.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 에러 로그 응답 DTO
 * 대시보드 미해결 에러 목록에 사용됩니다.
 *
 * @since : 2026.04.15
 * @version : 0.0.1
 * @author : 최준혁
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ErrorLogResponse {

	@JsonProperty("log_id")
	private Long logId;

	@JsonProperty("error_type")
	private String errorType;

	@JsonProperty("error_message")
	private String errorMessage;

	@JsonProperty("created_at")
	private LocalDateTime createdAt;

	@Builder
	public ErrorLogResponse(Long logId, String errorType, String errorMessage, LocalDateTime createdAt) {
		this.logId = logId;
		this.errorType = errorType;
		this.errorMessage = errorMessage;
		this.createdAt = createdAt;
	}
}
