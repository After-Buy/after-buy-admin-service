package com.After_Buy.AdminService.Entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 관리자 계정 엔티티
 * admin_db.admins 테이블과 매핑됩니다.
 * 사전 지급된 관리자 계정 정보를 관리하며, 로그인 실패 횟수와 계정 잠금 상태를 포함합니다.
 *
 * @author 최준혁
 * @since 2026.03.26
 * @version 0.0.1
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "admins", uniqueConstraints = @UniqueConstraint(name = "uq_admin_account", columnNames = "admin_account"), indexes = @Index(name = "idx_admin_account", columnList = "admin_account"))
public class Admin {

    /** 관리자 고유 ID (PK, AUTO_INCREMENT) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    /** 사전 지급 로그인 아이디 (UNIQUE) */
    @Column(name = "admin_account", nullable = false, length = 50)
    private String adminAccount;

    /** 암호화된 비밀번호 (BCrypt) */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    /** 연속 로그인 실패 횟수 (기본값 0) */
    @Column(name = "login_fail_count", nullable = false)
    private int loginFailCount = 0;

    /** 계정 잠금 여부 (0=정상, 1=잠금) */
    @Column(name = "is_locked", nullable = false)
    private boolean isLocked = false;

    /** 계정 생성 일시 */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Admin(String adminAccount, String passwordHash) {
        this.adminAccount = adminAccount;
        this.passwordHash = passwordHash;
        this.loginFailCount = 0;
        this.isLocked = false;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    /**
     * 로그인 실패 횟수를 1 증가시킵니다.
     */
    public void incrementLoginFailCount() {
        this.loginFailCount++;
    }

    /**
     * 로그인 실패 횟수를 초기화합니다. (로그인 성공 시 호출)
     */
    public void resetLoginFailCount() {
        this.loginFailCount = 0;
    }

    /**
     * 계정을 잠금 상태로 전환합니다.
     */
    public void lock() {
        this.isLocked = true;
    }
}
