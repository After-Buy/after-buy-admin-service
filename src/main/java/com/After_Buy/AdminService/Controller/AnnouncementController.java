package com.After_Buy.AdminService.Controller;

import com.After_Buy.AdminService.Dto.Request.AnnouncementCreateRequest;
import com.After_Buy.AdminService.Dto.Response.AnnouncementCreateResponse;
import com.After_Buy.AdminService.Dto.Response.AnnouncementListResponse;
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

    @Operation(summary = "공지사항 목록 조회 (관리자/사용자 공용)", description = "JWT 토큰을 제공하면 사용자별 읽음(is_read) 정보가 반영됩니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<AnnouncementListResponse>> getAnnouncementList(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long userId = null;
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof com.After_Buy.AdminService.Security.UserPrincipal) {
            userId = ((com.After_Buy.AdminService.Security.UserPrincipal) auth.getPrincipal()).getUserId();
        }

        AnnouncementListResponse response = announcementService.getAnnouncementList(category, keyword, page, size, userId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
