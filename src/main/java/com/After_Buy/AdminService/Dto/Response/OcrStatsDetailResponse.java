package com.After_Buy.AdminService.Dto.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

/**
 * OCR 오인식 관리 통계 상세 응답 DTO
 * API 명세서 구조에 맞추어 내부 정적 클래스로 하위 객체들을 정의합니다.
 *
 * @since : 2026.04.17
 * @version : 1.0.0
 * @author : 최준혁
 */
@Getter
@Builder
public class OcrStatsDetailResponse {

	private String period;
	private Summary summary;

	@JsonProperty("field_modified_stats")
	private List<FieldModifiedStat> fieldModifiedStats;

	@JsonProperty("daily_failure_trend")
	private List<DailyFailureTrend> dailyFailureTrend;

	@Getter
	@Builder
	public static class Summary {
		@JsonProperty("total_attempts")
		private long totalAttempts;

		@JsonProperty("failure_count")
		private long failureCount;

		@JsonProperty("modified_count")
		private long modifiedCount;

		@JsonProperty("failure_rate")
		private double failureRate;

		@JsonProperty("modified_rate")
		private double modifiedRate;
	}

	@Getter
	@Builder
	public static class FieldModifiedStat {
		@JsonProperty("field_name")
		private String fieldName;

		@JsonProperty("modified_count")
		private long modifiedCount;

		private double rate;
	}

	@Getter
	@Builder
	public static class DailyFailureTrend {
		private String date;

		@JsonProperty("failure_count")
		private long failureCount;
	}
}
