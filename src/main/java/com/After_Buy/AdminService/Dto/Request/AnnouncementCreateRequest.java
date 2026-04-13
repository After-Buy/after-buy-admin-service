package com.After_Buy.AdminService.Dto.Request;

import com.After_Buy.AdminService.Entity.AnnouncementCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 공지사항 등록 요청 DTO
 *
 * @author 최준혁
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
