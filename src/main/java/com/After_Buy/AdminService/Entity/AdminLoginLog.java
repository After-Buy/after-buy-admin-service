package com.After_Buy.AdminService.Entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 관리자 로그인 감사 로그 엔티티
 * admin_db.admin_login_logs 테이블과 매핑됩니다.
 * 로그인 시도(성공/실패) 시 반드시 INSERT되며, 계정 삭제 후에도 로그를 보존하기 위해
 * admin_account를 로그인 시점에 스냅샷으로 저장합니다.
 *
 * @author 최준혁
 * @since 2026.03.30
 * @version 0.0.1
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
		name = "admin_login_logs",
		indexes = {
				@Index(name = "idx_all_admin",    columnList = "admin_id"),
				@Index(name = "idx_all_login_at", columnList = "login_at"),
				@Index(name = "idx_all_session",  columnList = "session_id")
		}
)
public class AdminLoginLog {

	/** 로그 고유 ID (PK, AUTO_INCREMENT) */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "log_id", nullable = false)
	private Long logId;

	/** 로그인 시도한 관리자 ID (논리 참조 — 삭제되어도 로그는 보존) */
	@Column(name = "admin_id", nullable = false)
	private Long adminId;

	/** 로그인 시점 계정명 스냅샷 (계정 삭제 후에도 계정명 보존) */
	@Column(name = "admin_account", nullable = false, length = 50)
	private String adminAccount;

	/** 접속 IP (IPv4/IPv6 지원, 최대 45자) */
	@Column(name = "ip_address", nullable = false, length = 45)
	private String ipAddress;

	/** 기기 환경 — OS, 브라우저 정보 (User-Agent) */
	@Column(name = "user_agent", length = 500)
	private String userAgent;

	/** 인증 성공 여부 (true=성공, false=실패) */
	@Column(name = "is_success", nullable = false)
	private boolean isSuccess;

	/** 로그인 실패 사유 (성공 시 null) */
	@Column(name = "failure_reason", length = 100)
	private String failureReason;

	/** 세션 쿠키 ID — 접속 중/로그아웃 상태 매핑용 (실패 시 null) */
	@Column(name = "session_id", length = 128)
	private String sessionId;

	/** 로그인 일시 (INSERT 시 자동 설정) */
	@Column(name = "login_at", nullable = false, updatable = false)
	private LocalDateTime loginAt;

	/** 로그아웃 일시 (null = 접속 중 또는 미기록) */
	@Column(name = "logout_at")
	private LocalDateTime logoutAt;

	@Builder
	public AdminLoginLog(Long adminId, String adminAccount, String ipAddress,
			String userAgent, boolean isSuccess, String failureReason, String sessionId) {
		this.adminId       = adminId;
		this.adminAccount  = adminAccount;
		this.ipAddress     = ipAddress;
		this.userAgent     = userAgent;
		this.isSuccess     = isSuccess;
		this.failureReason = failureReason;
		this.sessionId     = sessionId;
	}

	@PrePersist
	protected void onCreate() {
		if (this.loginAt == null) {
			this.loginAt = LocalDateTime.now();
		}
	}

	/**
	 * 로그아웃 시각을 현재 시각으로 기록합니다.
	 */
	public void recordLogout() {
		this.logoutAt = LocalDateTime.now();
	}
}
