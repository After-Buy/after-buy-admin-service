package com.After_Buy.AdminService.Entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 공지사항 사용자별 읽음 처리 엔티티
 * announcement_reads 테이블과 매핑됩니다.
 * (user_id, announcement_id) UNIQUE 제약조건으로 중복 읽음 기록을 방지합니다.
 *
 * @since : 2026.04.10
 * @version : 0.0.1
 * @author : 최준혁
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "announcement_reads", uniqueConstraints = {
		@UniqueConstraint(columnNames = {"user_id", "announcement_id"})
})
public class AnnouncementRead {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "read_id")
	private Long readId;

	/** 읽음 처리를 수행한 사용자 ID (JWT 인증 사용자) */
	@Column(name = "user_id", nullable = false)
	private Long userId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "announcement_id", nullable = false)
	private Announcement announcement;

	/** read_at: 레코드 최초 삽입 시 자동 생성, 이후 수정 불가 */
	@CreationTimestamp
	@Column(name = "read_at", nullable = false, updatable = false)
	private LocalDateTime readAt;

	/**
	 * 읽음 기록 생성자
	 *
	 * @param userId       : 읽음 처리를 요청한 사용자 ID
	 * @param announcement : 읽음 처리할 공지사항 엔티티
	 */
	@Builder
	public AnnouncementRead(Long userId, Announcement announcement) {
		this.userId = userId;
		this.announcement = announcement;
	}
}
