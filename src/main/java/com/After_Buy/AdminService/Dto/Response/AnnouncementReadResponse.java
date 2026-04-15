package com.After_Buy.AdminService.Dto.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 공지사항 읽음 처리 응답 DTO
 * POST /api/admin/announcements/{announcement_id}/read 응답에 사용됩니다.
 *
 * @since : 2026.04.15
 * @version : 0.0.1
 * @author : 최준혁
 */
@Getter
@Builder
public class AnnouncementReadResponse {
    
    @JsonProperty("read_at")
    private LocalDateTime readAt;
}
