package com.After_Buy.AdminService.Dto.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 에러 로그 해결 상태 토글 응답 DTO
 * PATCH /api/admin/error-logs/{log_id}/resolve 응답에 사용됩니다.
 *
 * @since : 2026.04.26
 * @version : 0.0.1
 * @author : 신태훈
 */
@Getter
@Builder
public class ErrorLogResolveResponse {

	@JsonProperty("log_id")
	private Long logId;

	@JsonProperty("is_resolved")
	private Integer isResolved;

	@JsonProperty("resolved_at")
	private LocalDateTime resolvedAt;
}
