package com.After_Buy.AdminService.Dto.Request;

import com.After_Buy.AdminService.Entity.AnnouncementCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 공지사항 등록/수정 요청 DTO
 * POST /api/admin/announcements 및 PUT /api/admin/announcements/{id} 에서 공용으로 사용됩니다.
 *
 * @since : 2026.04.10
 * @version : 0.0.1
 * @author : 최준혁
 */
@Getter
@NoArgsConstructor
public class AnnouncementCreateRequest {

    @NotBlank(message = "제목은 필수 입력값입니다.")
    private String title;

    @NotNull(message = "카테고리는 필수 입력값입니다.")
    private AnnouncementCategory category;

    @NotBlank(message = "본문은 필수 입력값입니다.")
    private String content;

    private Integer isPinned; // 기본값 0 처리는 서비스나 엔티티에서
}
