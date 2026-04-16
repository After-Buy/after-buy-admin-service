package com.After_Buy.AdminService.Dto.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * OCR 실패 통계 응답 DTO
 *
 * @since : 2026.04.15
 * @version : 0.0.1
 * @author : 최준혁
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OcrStatsResponse {

	@JsonProperty("total_attempts")
	private Long totalAttempts;

	@JsonProperty("success_count")
	private Long successCount;

	@JsonProperty("modified_count")
	private Long modifiedCount;

	@JsonProperty("failure_count")
	private Long failureCount;

	@JsonProperty("success_rate")
	private Double successRate;

	@JsonProperty("failure_rate")
	private Double failureRate;

	@JsonProperty("modified_rate")
	private Double modifiedRate;

	@Builder
	public OcrStatsResponse(Long totalAttempts, Long successCount, Long modifiedCount, Long failureCount, Double successRate, Double failureRate, Double modifiedRate) {
		this.totalAttempts = totalAttempts;
		this.successCount = successCount;
		this.modifiedCount = modifiedCount;
		this.failureCount = failureCount;
		this.successRate = successRate;
		this.failureRate = failureRate;
		this.modifiedRate = modifiedRate;
	}
}
