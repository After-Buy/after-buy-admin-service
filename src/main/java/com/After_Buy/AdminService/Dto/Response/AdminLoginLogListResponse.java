package com.After_Buy.AdminService.Dto.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 관리자 로그인 내역 목록 조회 응답 DTO
 * GET /api/admin/login-logs 응답에 사용됩니다.
 *
 * @author 최준혁
 * @since 2026.03.30
 * @version 0.0.1
 */
@Getter
@Builder
public class AdminLoginLogListResponse {

	/** 로그 목록 */
	@JsonProperty("login_logs")
	private List<LoginLogSummary> loginLogs;

	/** 페이징 정보 */
	private Pagination pagination;

	/**
	 * 목록의 개별 로그 항목
	 */
	@Getter
	@Builder
	public static class LoginLogSummary {

		/** 로그 고유 ID */
		@JsonProperty("log_id")
		private Long logId;

		/** 관리자 고유 ID */
		@JsonProperty("admin_id")
		private Long adminId;

		/** 로그인 시점 계정명 스냅샷 */
		@JsonProperty("admin_account")
		private String adminAccount;

		/** 인증 성공 여부 (1=성공, 0=실패) */
		@JsonProperty("is_success")
		private int isSuccess;

		/** 로그인 실패 사유 (성공 시 null) */
		@JsonProperty("failure_reason")
		private String failureReason;

		/**
		 * 접속 상태
		 * session_id와 현재 활성 세션을 비교하여 "접속 중" 또는 "로그아웃" 결정
		 */
		@JsonProperty("login_status")
		private String loginStatus;

		/** 로그인 일시 */
		@JsonProperty("login_at")
		private LocalDateTime loginAt;
	}

	/**
	 * 페이징 메타데이터
	 */
	@Getter
	@Builder
	public static class Pagination {

		/** 현재 페이지 번호 (1-based) */
		@JsonProperty("current_page")
		private int currentPage;

		/** 전체 페이지 수 */
		@JsonProperty("total_pages")
		private int totalPages;

		/** 전체 항목 수 */
		@JsonProperty("total_count")
		private long totalCount;

		/** 페이지당 항목 수 */
		private int size;
	}
}
