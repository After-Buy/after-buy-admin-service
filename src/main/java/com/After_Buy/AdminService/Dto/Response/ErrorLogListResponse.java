package com.After_Buy.AdminService.Dto.Response;

import com.After_Buy.AdminService.Entity.ErrorLog;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 에러 로그 목록 조회 응답 DTO
 * GET /api/admin/error-logs 응답에 사용됩니다.
 *
 * @since : 2026.04.26
 * @version : 0.0.1
 * @author : 신태훈
 */
@Getter
@Builder
public class ErrorLogListResponse {

	@JsonProperty("error_logs")
	private List<ErrorLogItem> errorLogs;

	@JsonProperty("unresolved_count")
	private Long unresolvedCount;

	private Pagination pagination;

	/**
	 * 에러 로그 목록 개별 항목 DTO
	 */
	@Getter
	@Builder
	public static class ErrorLogItem {

		@JsonProperty("log_id")
		private Long logId;

		@JsonProperty("service_name")
		private String serviceName;

		@JsonProperty("endpoint_path")
		private String endpointPath;

		@JsonProperty("error_type")
		private String errorType;

		@JsonProperty("error_message")
		private String errorMessage;

		@JsonProperty("full_message")
		private String fullMessage;

		@JsonProperty("is_resolved")
		private Integer isResolved;

		@JsonProperty("resolved_at")
		private LocalDateTime resolvedAt;

		@JsonProperty("created_at")
		private LocalDateTime createdAt;

		/**
		 * ErrorLog 엔티티로부터 ErrorLogItem DTO를 생성합니다.
		 *
		 * @param errorLog : 에러 로그 엔티티
		 * @return ErrorLogItem DTO
		 */
		public static ErrorLogItem from(ErrorLog errorLog) {
			return ErrorLogItem.builder()
					.logId(errorLog.getLogId())
					.serviceName(errorLog.getServiceName())
					.endpointPath(errorLog.getEndpointPath())
					.errorType(errorLog.getErrorType().name())
					.errorMessage(errorLog.getErrorMessage())
					.fullMessage(errorLog.getFullMessage())
					.isResolved(errorLog.getIsResolved())
					.resolvedAt(errorLog.getResolvedAt())
					.createdAt(errorLog.getCreatedAt())
					.build();
		}
	}

	/**
	 * 페이지네이션 정보 DTO
	 */
	@Getter
	@Builder
	public static class Pagination {

		@JsonProperty("current_page")
		private int currentPage;

		@JsonProperty("total_pages")
		private int totalPages;

		@JsonProperty("total_count")
		private long totalCount;

		private int size;
	}
}
