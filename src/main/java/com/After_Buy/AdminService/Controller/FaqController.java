package com.After_Buy.AdminService.Controller;

import com.After_Buy.AdminService.Dto.Request.FaqCreateRequest;
import com.After_Buy.AdminService.Dto.Response.ApiResponse;
import com.After_Buy.AdminService.Dto.Response.FaqCreateResponse;
import com.After_Buy.AdminService.Dto.Response.FaqDetailResponse;
import com.After_Buy.AdminService.Dto.Response.FaqListResponse;
import com.After_Buy.AdminService.Dto.Response.FaqUpdateResponse;
import com.After_Buy.AdminService.Service.FaqService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 이용 안내(FAQ) 관리 컨트롤러
 * GET    /api/admin/faqs          — FAQ 목록 조회 (관리자/사용자 공용)
 * POST   /api/admin/faqs          — FAQ 등록 (관리자 전용)
 * GET    /api/admin/faqs/{id}     — FAQ 상세 조회 (관리자/사용자 공용)
 * PUT    /api/admin/faqs/{id}     — FAQ 수정 (관리자 전용)
 * DELETE /api/admin/faqs/{id}     — FAQ 삭제 (관리자 전용)
 *
 * @since : 2026.04.26
 * @version : 0.0.1
 * @author : 신태훈
 */
@Tag(name = "FAQs", description = "이용 안내(FAQ) 관리 (관리자/사용자)")
@RestController
@RequestMapping("/api/admin/faqs")
@RequiredArgsConstructor
public class FaqController {

	private final FaqService faqService;

	/**
	 * FAQ 등록 (관리자 전용)
	 * AdminSessionAuthFilter에서 세션 검증 완료 후 진입하므로 session NPE 방어 불필요.
	 *
	 * @param request            : FAQ 등록 DTO
	 * @param httpServletRequest : HttpServletRequest (세션 추출용)
	 * @return 등록 완료된 FAQ 정보 (201 Created)
	 * @since : 2026.04.26
	 * @version : 0.0.1
	 * @author : 신태훈
	 */
	@Operation(summary = "FAQ 등록 (관리자 전용)", description = "제목과 본문을 입력하여 이용 안내 FAQ를 등록합니다.")
	@PostMapping
	public ResponseEntity<ApiResponse<FaqCreateResponse>> createFaq(
			@Valid @RequestBody FaqCreateRequest request,
			HttpServletRequest httpServletRequest) {

		HttpSession session = httpServletRequest.getSession(false);
		Long adminId = ((Number) session.getAttribute("adminId")).longValue();

		FaqCreateResponse response = faqService.createFaq(request, adminId);

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success(response));
	}

	/**
	 * FAQ 목록 조회 (관리자/사용자 공용)
	 * 키워드가 없으면 전체를 최신순으로 반환합니다.
	 *
	 * @param keyword : 제목/본문 검색어 (선택)
	 * @param page    : 페이지 번호 (1-based, 기본값 1)
	 * @param size    : 페이지당 항목 수 (기본값 10)
	 * @return FAQ 목록 및 페이지네이션 정보
	 * @since : 2026.04.26
	 * @version : 0.0.1
	 * @author : 신태훈
	 */
	@Operation(summary = "FAQ 목록 조회 (관리자/사용자 공용)", description = "키워드로 제목/본문 검색이 가능합니다. 키워드 없으면 전체 최신순 반환.")
	@GetMapping
	public ResponseEntity<ApiResponse<FaqListResponse>> getFaqList(
			@RequestParam(required = false) String keyword,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "10") int size) {

		FaqListResponse response = faqService.getFaqList(keyword, page, size);

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * FAQ 상세 조회 (관리자/사용자 공용)
	 * 존재하지 않는 FAQ ID 요청 시 404(ADMIN-004)를 반환합니다.
	 *
	 * @param faqId : 조회할 FAQ ID (Path Variable)
	 * @return FAQ 상세 정보
	 * @since : 2026.04.26
	 * @version : 0.0.1
	 * @author : 신태훈
	 */
	@Operation(summary = "FAQ 상세 조회 (관리자/사용자 공용)", description = "FAQ ID로 상세 내용을 조회합니다.")
	@GetMapping("/{faqId}")
	public ResponseEntity<ApiResponse<FaqDetailResponse>> getFaqDetail(
			@PathVariable Long faqId) {

		FaqDetailResponse response = faqService.getFaqDetail(faqId);

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * FAQ 수정 (관리자 전용)
	 * AdminSessionAuthFilter에서 세션 검증 완료 후 진입하므로 session NPE 방어 불필요.
	 * 요청 구조는 POST 등록과 동일 (title, content).
	 *
	 * @param faqId   : 수정할 FAQ ID (Path Variable)
	 * @param request : 수정 요청 DTO
	 * @return 수정된 FAQ 전체 데이터
	 * @since : 2026.04.26
	 * @version : 0.0.1
	 * @author : 신태훈
	 */
	@Operation(summary = "FAQ 수정 (관리자 전용)", description = "제목과 본문을 수정합니다. 실패 시 400(필수항목 누락) 404(미존재) 반환.")
	@PutMapping("/{faqId}")
	public ResponseEntity<ApiResponse<FaqUpdateResponse>> updateFaq(
			@PathVariable Long faqId,
			@Valid @RequestBody FaqCreateRequest request) {

		FaqUpdateResponse response = faqService.updateFaq(faqId, request);

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * FAQ 삭제 (관리자 전용)
	 * AdminSessionAuthFilter에서 세션 검증 완료 후 진입하므로 session NPE 방어 불필요.
	 *
	 * @param faqId : 삭제할 FAQ ID (Path Variable)
	 * @return 삭제 성공 메시지
	 * @since : 2026.04.26
	 * @version : 0.0.1
	 * @author : 신태훈
	 */
	@Operation(summary = "FAQ 삭제 (관리자 전용)", description = "해당 FAQ를 삭제합니다. 실패 시 404(미존재) 반환.")
	@DeleteMapping("/{faqId}")
	public ResponseEntity<ApiResponse<Void>> deleteFaq(
			@PathVariable Long faqId) {

		faqService.deleteFaq(faqId);

		return ResponseEntity.ok(ApiResponse.successWithMessage("이용 안내가 삭제되었습니다."));
	}
}
