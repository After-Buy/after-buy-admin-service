package com.After_Buy.AdminService.Entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * 시스템 에러 로그 엔티티
 * 시스템에서 발생하는 에러(Warning/Error)를 기록합니다.
 *
 * @since : 2026.04.15
 * @version : 0.0.1
 * @author : 최준혁
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "error_logs")
@EntityListeners(AuditingEntityListener.class)
public class ErrorLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "log_id")
	private Long logId;

	@Enumerated(EnumType.STRING)
	@Column(name = "error_type", nullable = false)
	private ErrorType errorType;

	@Column(name = "error_message", length = 500, nullable = false)
	private String errorMessage;

	@Column(name = "full_message", columnDefinition = "TEXT")
	private String fullMessage;

	@Column(name = "is_resolved", nullable = false)
	private Integer isResolved = 0;

	@Column(name = "resolved_at")
	private LocalDateTime resolvedAt;

	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	/**
	 * 에러 타입 Enum 정의
	 */
	public enum ErrorType {
		WARNING,
		ERROR
	}

	@Builder
	public ErrorLog(ErrorType errorType, String errorMessage, String fullMessage) {
		this.errorType = errorType;
		this.errorMessage = errorMessage;
		this.fullMessage = fullMessage;
		this.isResolved = 0;
	}

	public void resolve() {
		this.isResolved = 1;
		this.resolvedAt = LocalDateTime.now();
	}
}
