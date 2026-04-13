package com.After_Buy.AdminService.Dto.Response;

import com.After_Buy.AdminService.Entity.AnnouncementCategory;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 공지사항 목록 응답 DTO
 *
 * @author 최준혁
 */
@Getter
@Builder
public class AnnouncementListResponse {

    @JsonProperty("pinned_announcements")
    private List<AnnouncementItem> pinnedAnnouncements;

    @JsonProperty("announcements")
    private List<AnnouncementItem> announcements;

    private Pagination pagination;

    @Getter
    @Builder
    public static class AnnouncementItem {
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

        @JsonProperty("is_new")
        private Boolean isNew;

        @JsonProperty("is_read")
        private Boolean isRead;
    }

    @Getter
    @Builder
    public static class Pagination {
        @JsonProperty("current_page")
        private int currentPage;

        @JsonProperty("total_pages")
        private int totalPages;

        @JsonProperty("total_count")
        private long totalCount;

        private int size;
    }
}
