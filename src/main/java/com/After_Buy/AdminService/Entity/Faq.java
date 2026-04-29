package com.After_Buy.AdminService.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 이용 안내(FAQ) 엔티티
 * faqs 테이블과 매핑됩니다.
 *
 * @since : 2026.04.26
 * @version : 0.0.1
 * @author : 신태훈
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "faqs")
public class Faq {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "faq_id")
	private Long faqId;

	@Column(name = "title", length = 200, nullable = false)
	private String title;

	@Column(name = "content", columnDefinition = "TEXT", nullable = false)
	private String content;

	/** created_by: FAQ를 작성한 관리자 ID */
	@Column(name = "created_by", nullable = false)
	private Long createdBy;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	/**
	 * FAQ 생성자
	 *
	 * @param title     : 질문 제목
	 * @param content   : 답변 본문
	 * @param createdBy : 작성 관리자 ID
	 */
	@Builder
	public Faq(String title, String content, Long createdBy) {
		this.title = title;
		this.content = content;
		this.createdBy = createdBy;
	}

	/**
	 * FAQ 내용을 수정합니다.
	 * @UpdateTimestamp에 의해 updated_at이 자동 갱신됩니다.
	 *
	 * @param title   : 수정할 제목
	 * @param content : 수정할 본문
	 * @since : 2026.04.26
	 * @version : 0.0.1
	 * @author : 신태훈
	 */
	public void update(String title, String content) {
		this.title = title;
		this.content = content;
	}
}
