package com.After_Buy.AdminService.Entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 공지사항 엔티티
 * announcements 테이블과 매핑됩니다.
 *
 * @since : 2026.04.10
 * @version : 0.0.1
 * @author : 최준혁
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "announcements")
public class Announcement {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "announcement_id")
	private Long announcementId;

	@Column(name = "title", nullable = false, length = 200)
	private String title;

	@Enumerated(EnumType.STRING)
	@Column(name = "category", nullable = false)
	private AnnouncementCategory category;

	@Column(name = "content", nullable = false, columnDefinition = "TEXT")
	private String content;

	/** is_pinned: 0=일반, 1=상단 고정 */
	@Column(name = "is_pinned", nullable = false)
	private Integer isPinned;

	/** created_by: 공지사항을 작성한 관리자 ID */
	@Column(name = "created_by", nullable = false)
	private Long createdBy;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	/**
	 * 공지사항 생성자
	 *
	 * @param title      : 제목
	 * @param category   : 카테고리
	 * @param content    : 본문
	 * @param isPinned   : 상단 고정 여부 (null이면 0으로 처리)
	 * @param createdBy  : 작성 관리자 ID
	 */
	@Builder
	public Announcement(String title, AnnouncementCategory category, String content, Integer isPinned, Long createdBy) {
		this.title = title;
		this.category = category;
		this.content = content;
		this.isPinned = isPinned != null ? isPinned : 0;
		this.createdBy = createdBy;
	}

	/**
	 * 공지사항 내용을 수정합니다.
	 * @UpdateTimestamp에 의해 updated_at이 자동 갱신됩니다.
	 *
	 * @param title    : 수정할 제목
	 * @param category : 수정할 카테고리
	 * @param content  : 수정할 본문
	 * @param isPinned : 수정할 상단 고정 여부 (null이면 0으로 처리)
	 * @since : 2026.04.15
	 * @version : 0.0.1
	 * @author : 최준혁
	 */
	public void update(String title, AnnouncementCategory category, String content, Integer isPinned) {
		this.title = title;
		this.category = category;
		this.content = content;
		this.isPinned = isPinned != null ? isPinned : 0;
	}
}
