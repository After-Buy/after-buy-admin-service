package com.After_Buy.AdminService.Controller;

import com.After_Buy.AdminService.Dto.Request.AnnouncementCreateRequest;
import com.After_Buy.AdminService.Dto.Response.AnnouncementCreateResponse;
import com.After_Buy.AdminService.Dto.Response.AnnouncementDetailResponse;
import com.After_Buy.AdminService.Dto.Response.AnnouncementListResponse;
import com.After_Buy.AdminService.Dto.Response.AnnouncementReadResponse;
import com.After_Buy.AdminService.Dto.Response.AnnouncementUpdateResponse;
import com.After_Buy.AdminService.Dto.Response.ApiResponse;
import com.After_Buy.AdminService.Exception.CustomException;
import com.After_Buy.AdminService.Exception.ErrorCode;
import com.After_Buy.AdminService.Security.UserPrincipal;
import com.After_Buy.AdminService.Service.AnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 관리자 공지사항 관리 컨트롤러
 * GET    /api/admin/announcements              — 공지사항 목록 조회 (관리자/사용자 공용)
 * POST   /api/admin/announcements              — 공지사항 등록 (관리자 전용)
 * GET    /api/admin/announcements/{id}         — 공지사항 상세 조회 (관리자/사용자 공용)
 * POST   /api/admin/announcements/{id}/read    — 공지사항 읽음 처리 (사용자 전용, JWT 인증)
 * PUT    /api/admin/announcements/{id}         — 공지사항 수정 (관리자 전용)
 * DELETE /api/admin/announcements/{id}         — 공지사항 삭제 (관리자 전용)
 *
 * @since : 2026.04.15
 * @version : 0.0.4
 * @author : 최준혁
 */
@Tag(name = "Announcements Admin", description = "공지사항 관리 (관리자)")
@RestController
@RequestMapping("/api/admin/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

	private final AnnouncementService announcementService;

	/**
	 * 공지사항 등록 (관리자 전용)
	 * AdminSessionAuthFilter에서 세션 검증 완료 후 진입하므로 session NPE 방어 불필요.
	 *
	 * @param request            : 공지사항 등록 DTO
	 * @param httpServletRequest : HttpServletRequest (세션 추출용)
	 * @return : 등록 완료된 공지사항 정보 (201 Created)
	 * @since : 2026.04.15
	 * @version : 0.0.2
	 * @author : 최준혁
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

	/**
	 * 공지사항 목록 조회 (관리자/사용자 공용)
	 * JWT 토큰이 있으면 SecurityContext에서 userId를 추출하여 읽음(is_read) 여부를 반영합니다.
	 * JWT 토큰이 없는 관리자 세션 접근의 경우 userId는 null로 처리됩니다.
	 *
	 * @param category : 카테고리 필터 (NOTICE / MAINTENANCE / UPDATE / ALL)
	 * @param keyword  : 제목/본문 검색어
	 * @param page     : 페이지 번호 (1-based, 기본값 1)
	 * @param size     : 페이지당 항목 수 (기본값 10)
	 * @return : 공지사항 목록 (상단고정 + 일반 + 페이지네이션)
	 * @since : 2026.04.15
	 * @version : 0.0.2
	 * @author : 최준혁
	 */
	@Operation(summary = "공지사항 목록 조회 (관리자/사용자 공용)", description = "JWT 토큰을 제공하면 사용자별 읽음(is_read) 정보가 반영됩니다.")
	@GetMapping
	public ResponseEntity<ApiResponse<AnnouncementListResponse>> getAnnouncementList(
			@RequestParam(required = false) String category,
			@RequestParam(required = false) String keyword,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "10") int size) {

		// JWT 인증 사용자인 경우 SecurityContext에서 userId 추출
		Long userId = null;
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserPrincipal) {
			userId = ((UserPrincipal) auth.getPrincipal()).getUserId();
		}

		AnnouncementListResponse response = announcementService.getAnnouncementList(category, keyword, page, size, userId);

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * 공지사항 상세 조회 (관리자/사용자 공용)
	 * 딥링크(afterbuy://announcements/{announcement_id}) 진입점으로도 활용됩니다.
	 * 존재하지 않는 공지사항 ID 요청 시 404(ADMIN-003)을 반환합니다.
	 *
	 * @param announcementId : 조회할 공지사항 ID (Path Variable)
	 * @return : 공지사항 상세 정보
	 * @throws CustomException : 공지사항 미존재 시 ANNOUNCEMENT_NOT_FOUND (ADMIN-003)
	 * @since : 2026.04.15
	 * @version : 0.0.1
	 * @author : 최준혁
	 */
	@Operation(summary = "공지사항 상세 조회 (관리자/사용자 공용)", description = "공지사항 ID로 상세 내용을 조회합니다. 딥링크(afterbuy://announcements/{id})로도 접근 가능합니다.")
	@GetMapping("/{announcementId}")
	public ResponseEntity<ApiResponse<AnnouncementDetailResponse>> getAnnouncementDetail(
			@PathVariable Long announcementId) {

		AnnouncementDetailResponse response = announcementService.getAnnouncementDetail(announcementId);

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * 공지사항 읽음 처리 (사용자 전용 — JWT 인증 필수)
	 * 공지사항 상세 채화면 진입 시 호출되며, announcement_reads 테이블에 기록됩니다.
	 * 이미 읽은 공지사항일 경우 멱등성(idempotent) 처리되어 기존 read_at을 반환합니다.
	 *
	 * @param announcementId : 공지사항 ID (Path Variable)
	 * @return : 읽음 처리 시간 (read_at)
	 * @throws CustomException : JWT 사용자 인증 없음 시 UNAUTHORIZED_ADMIN_SESSION
	 * @throws CustomException : 공지사항 미존재 시 ANNOUNCEMENT_NOT_FOUND
	 * @since : 2026.04.15
	 * @version : 0.0.1
	 * @author : 최준혁
	 */
	@Operation(summary = "공지사항 읽음 처리 (사용자 전용)", description = "사용자가 공지사항 상세를 조회할 때 호출합니다. 이미 읽은 업목시 기존 read_at을 반환(idempotent)합니다.")
	@PostMapping("/{announcementId}/read")
	public ResponseEntity<ApiResponse<AnnouncementReadResponse>> readAnnouncement(
			@PathVariable Long announcementId) {

		/* JWT 인증 사용자인 경우에만 SecurityContext에서 userId 추출 */
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserPrincipal)) {
			/* 사용자 전용 엔드포인트 — JWT 토큰 없으면 원칙적으로 필터에서 유입 차단되어야 하나,
			 * 관리자 세션으로 접근하는 경우 등의 방어 코드로 CustomException을 throw */
			throw new CustomException(ErrorCode.UNAUTHORIZED_ADMIN_SESSION);
		}

		Long userId = ((UserPrincipal) auth.getPrincipal()).getUserId();
		AnnouncementReadResponse response = announcementService.readAnnouncement(announcementId, userId);

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * 공지사항 수정 (관리자 전용)
	 * AdminSessionAuthFilter에서 세션 검증 완료 후 진입하므로 session NPE 방어 불필요.
	 * 요청 구조는 POST 등록과 동일 (title, category, content, is_pinned).
	 *
	 * @param announcementId    : 수정할 공지사항 ID (Path Variable)
	 * @param request           : 수정 요청 DTO
	 * @param httpServletRequest : HttpServletRequest (세션 추출용)
	 * @return : 수정된 공지사항 전체 데이터
	 * @throws CustomException : 공지사항 미존재 시 ANNOUNCEMENT_NOT_FOUND
	 * @since : 2026.04.15
	 * @version : 0.0.1
	 * @author : 최준혁
	 */
	@Operation(summary = "공지사항 수정 (관리자 전용)", description = "제목, 카테고리, 본문, 상단 고정 여부를 수정합니다. 실패 시 400(필수항목 누락) 404(미존재) 반환.")
	@PutMapping("/{announcementId}")
	public ResponseEntity<ApiResponse<AnnouncementUpdateResponse>> updateAnnouncement(
			@PathVariable Long announcementId,
			@Valid @RequestBody AnnouncementCreateRequest request) {

		AnnouncementUpdateResponse response = announcementService.updateAnnouncement(announcementId, request);

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * 공지사항 삭제 (관리자 전용)
	 * AdminSessionAuthFilter에서 세션 검증 완료 후 진입하므로 session NPE 방어 불필요.
	 * 연관된 읽음 이력(announcement_reads)은 서비스 로직에서 명시적으로 삭제됩니다.
	 *
	 * @param announcementId : 삭제할 공지사항 ID (Path Variable)
	 * @return : 삭제 성공 메시지
	 * @throws CustomException : 공지사항 미존재 시 ANNOUNCEMENT_NOT_FOUND (ADMIN-003)
	 * @since : 2026.04.15
	 * @version : 0.0.1
	 * @author : 최준혁
	 */
	@Operation(summary = "공지사항 삭제 (관리자 전용)", description = "해당 공지사항을 삭제합니다. 연관 읽음 이력도 CASCADE 삭제됩니다. 실패 시 404(미존재) 반환.")
	@DeleteMapping("/{announcementId}")
	public ResponseEntity<ApiResponse<Void>> deleteAnnouncement(
			@PathVariable Long announcementId) {

		announcementService.deleteAnnouncement(announcementId);

		return ResponseEntity.ok(ApiResponse.successWithMessage("공지사항이 삭제되었습니다."));
	}
}
