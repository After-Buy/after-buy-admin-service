package com.After_Buy.AdminService.Dto.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 대시보드 미해결 에러 로그 요약 응답 DTO
 * GET /api/admin/dashboard 응답의 unresolved_error_logs 항목에 사용됩니다.
 * 목록 5건 요약 정보만 제공하며, 상세/전체 필드는 에러 로그 관리 페이지 API에서 별도 제공됩니다.
 *
 * @since : 2026.04.15
 * @version : 0.0.1
 * @author : 최준혁
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DashboardErrorLogResponse {

	@JsonProperty("log_id")
	private Long logId;

	@JsonProperty("error_type")
	private String errorType;

	@JsonProperty("error_message")
	private String errorMessage;

	@JsonProperty("created_at")
	private LocalDateTime createdAt;

	@Builder
	public DashboardErrorLogResponse(Long logId, String errorType, String errorMessage, LocalDateTime createdAt) {
		this.logId = logId;
		this.errorType = errorType;
		this.errorMessage = errorMessage;
		this.createdAt = createdAt;
	}
}
