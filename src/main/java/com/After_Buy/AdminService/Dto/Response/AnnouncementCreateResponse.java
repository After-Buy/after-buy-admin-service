package com.After_Buy.AdminService.Dto.Response;

import com.After_Buy.AdminService.Entity.Announcement;
import com.After_Buy.AdminService.Entity.AnnouncementCategory;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 공지사항 등록 응답 DTO
 *
 * @author 최준혁
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
    
    @JsonProperty("push_sent")
    private Boolean pushSent;

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
