package com.After_Buy.AdminService.Dto.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 관리자 로그인 내역 상세 조회 응답 DTO
 * GET /api/admin/login-logs/{log_id} 응답에 사용됩니다.
 *
 * @author 최준혁
 * @since 2026.03.30
 * @version 0.0.1
 */
@Getter
@Builder
public class AdminLoginLogDetailResponse {

	/** 로그 고유 ID */
	@JsonProperty("log_id")
	private Long logId;

	/** 관리자 고유 ID */
	@JsonProperty("admin_id")
	private Long adminId;

	/** 로그인 시점 계정명 스냅샷 */
	@JsonProperty("admin_account")
	private String adminAccount;

	/** 접속 IP */
	@JsonProperty("ip_address")
	private String ipAddress;

	/** 기기 환경 (User-Agent) */
	@JsonProperty("user_agent")
	private String userAgent;

	/** 인증 성공 여부 (1=성공, 0=실패) */
	@JsonProperty("is_success")
	private int isSuccess;

	/** 로그인 실패 사유 (성공 시 null) */
	@JsonProperty("failure_reason")
	private String failureReason;

	/** 세션 쿠키 ID (실패 시 null) */
	@JsonProperty("session_id")
	private String sessionId;

	/** 로그인 일시 */
	@JsonProperty("login_at")
	private LocalDateTime loginAt;

	/**
	 * 로그아웃 일시
	 * null일 경우 프론트에서 "기록 없음" 또는 "접속 중" 표시
	 */
	@JsonProperty("logout_at")
	private LocalDateTime logoutAt;

	/**
	 * 접속 상태
	 * session_id와 현재 활성 세션을 비교하여 "접속 중" 또는 "로그아웃" 결정
	 */
	@JsonProperty("login_status")
	private String loginStatus;
}
