package com.After_Buy.AdminService.Controller;

import com.After_Buy.AdminService.Dto.Request.AnnouncementCreateRequest;
import com.After_Buy.AdminService.Dto.Response.AnnouncementCreateResponse;
import com.After_Buy.AdminService.Dto.Response.ApiResponse;
import com.After_Buy.AdminService.Service.AnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 관리자 공지사항 관리 컨트롤러
 *
 * @author 최준혁
 */
@Tag(name = "Announcements Admin", description = "공지사항 관리 (관리자)")
@RestController
@RequestMapping("/api/admin/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    /**
     * 공지사항 등록
     *
     * @param request 공지사항 등록 DTO
     * @param httpServletRequest HttpServletRequest
     * @return 등록 완료된 공지사항 정보
     */
    @Operation(summary = "공지사항 등록 (관리자 전용)", description = "등록 완료 후 push_enabled=1인 사용자에게 FCM 푸시를 발송합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<AnnouncementCreateResponse>> createAnnouncement(
            @Valid @RequestBody AnnouncementCreateRequest request,
            HttpServletRequest httpServletRequest) {

        HttpSession session = httpServletRequest.getSession(false);
        Long adminId = ((Number) session.getAttribute("adminId")).longValue();

        AnnouncementCreateResponse response = announcementService.createAnnouncement(request, adminId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }
}
