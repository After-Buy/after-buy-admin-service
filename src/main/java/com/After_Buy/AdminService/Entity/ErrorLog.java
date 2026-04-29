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
 * Admin Service 자체 또는 타 마이크로서비스(Auth, Device, Notification)에서
 * 발생하는 500급 서버 오류를 중앙 수집하여 기록합니다.
 *
 * @since : 2026.04.15
 * @version : 0.0.2
 * @author : 최준혁
 * @author : 신태훈 (2026.04.26 — service_name, endpoint_path 컬럼 추가, unresolve() 추가)
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

	/** 에러 발생 서비스명 (예: "AUTH", "DEVICE", "NOTIFICATION", "ADMIN") */
	@Column(name = "service_name", length = 50)
	private String serviceName;

	/** 에러 발생 엔드포인트 경로 (예: "/api/auth/users/me") */
	@Column(name = "endpoint_path", length = 500)
	private String endpointPath;

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

	/**
	 * 에러 로그 생성자
	 * Internal API 수신 시 serviceName, endpointPath 포함하여 저장합니다.
	 *
	 * @param serviceName  : 에러 발생 서비스명
	 * @param endpointPath : 에러 발생 엔드포인트
	 * @param errorType    : 에러 타입 (WARNING/ERROR)
	 * @param errorMessage : 에러 메시지 요약
	 * @param fullMessage  : 전체 스택트레이스 등 상세 메시지
	 * @since : 2026.04.26
	 * @author : 신태훈
	 */
	@Builder
	public ErrorLog(String serviceName, String endpointPath, ErrorType errorType,
			String errorMessage, String fullMessage) {
		this.serviceName = serviceName;
		this.endpointPath = endpointPath;
		this.errorType = errorType;
		this.errorMessage = errorMessage;
		this.fullMessage = fullMessage;
		this.isResolved = 0;
	}

	/**
	 * 에러 해결 처리 — is_resolved=1, resolved_at=NOW() 설정
	 */
	public void resolve() {
		this.isResolved = 1;
		this.resolvedAt = LocalDateTime.now();
	}

	/**
	 * 에러 해결 취소 — is_resolved=0, resolved_at=null 초기화 (토글)
	 * @since : 2026.04.26
	 * @author : 신태훈
	 */
	public void unresolve() {
		this.isResolved = 0;
		this.resolvedAt = null;
	}
}
