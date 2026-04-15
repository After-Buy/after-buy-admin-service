package com.After_Buy.AdminService.Dto.Response;

import com.After_Buy.AdminService.Entity.Announcement;
import com.After_Buy.AdminService.Entity.AnnouncementCategory;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 공지사항 등록 응답 DTO
 * POST /api/admin/announcements 응답에 사용됩니다.
 * 등록된 공지사항 전체 정보와 FCM 푸시 발송 여부(push_sent)를 반환합니다.
 *
 * @since : 2026.04.10
 * @version : 0.0.1
 * @author : 최준혁
 */
@Getter
@Builder
public class AnnouncementCreateResponse {

	@JsonProperty("announcement_id")
	private Long announcementId;

	private String title;

	private AnnouncementCategory category;

	private String content;

	@JsonProperty("is_pinned")
	private Integer isPinned;

	@JsonProperty("created_by")
	private Long createdBy;

	@JsonProperty("created_at")
	private LocalDateTime createdAt;

	@JsonProperty("updated_at")
	private LocalDateTime updatedAt;

	/**
	 * push_sent: 비동기 FCM 푸시 발송 요청 여부. 실제 수신 여부가 아닌 발송 요청 성공 여부를 반환합니다.
	 */
	@JsonProperty("push_sent")
	private Boolean pushSent;

	/**
	 * Announcement 엔티티로부터 등록 응답 DTO를 생성합니다.
	 *
	 * @param announcement : 저장된 공지사항 엔티티
	 * @param pushSent     : FCM 푸시 발송 요청 성공 여부
	 * @return : 공지사항 등록 응답 DTO
	 */
	public static AnnouncementCreateResponse from(Announcement announcement, boolean pushSent) {
		return AnnouncementCreateResponse.builder()
				.announcementId(announcement.getAnnouncementId())
				.title(announcement.getTitle())
				.category(announcement.getCategory())
				.content(announcement.getContent())
				.isPinned(announcement.getIsPinned())
				.createdBy(announcement.getCreatedBy())
				.createdAt(announcement.getCreatedAt())
				.updatedAt(announcement.getUpdatedAt())
				.pushSent(pushSent)
				.build();
	}
}
