package com.After_Buy.AdminService.Controller;

import com.After_Buy.AdminService.Dto.Response.ApiResponse;
import com.After_Buy.AdminService.Dto.Response.ErrorLogListResponse;
import com.After_Buy.AdminService.Dto.Response.ErrorLogResolveResponse;
import com.After_Buy.AdminService.Service.ErrorLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 에러 로그 관리 컨트롤러
 * GET   /api/admin/error-logs                  — 에러 로그 목록 조회 (관리자 전용)
 * PATCH /api/admin/error-logs/{id}/resolve     — 에러 해결 상태 토글 (관리자 전용)
 *
 * @since : 2026.04.26
 * @version : 0.0.1
 * @author : 신태훈
 */
@Tag(name = "Error Logs", description = "에러 로그 관리 (관리자 전용)")
@RestController
@RequestMapping("/api/admin/error-logs")
@RequiredArgsConstructor
public class ErrorLogController {

	private final ErrorLogService errorLogService;

	/**
	 * 에러 로그 목록 조회 (관리자 전용)
	 * error_type 파라미터로 WARNING/ERROR/ALL 필터링이 가능합니다.
	 * 응답에 미해결(is_resolved=0) 총 카운트를 함께 반환합니다.
	 *
	 * @param errorType : 필터 타입 ("WARNING", "ERROR", "ALL"), 기본값 "ALL"
	 * @param page      : 페이지 번호 (1-based, 기본값 1)
	 * @param size      : 페이지당 항목 수 (기본값 10)
	 * @return 에러 로그 목록, 미해결 카운트, 페이지네이션
	 * @since : 2026.04.26
	 * @version : 0.0.1
	 * @author : 신태훈
	 */
	@Operation(summary = "에러 로그 목록 조회 (관리자 전용)",
			description = "error_type(WARNING/ERROR/ALL) 필터링 가능. 미해결 에러 총 카운트(unresolved_count)를 함께 반환합니다.")
	@GetMapping
	public ResponseEntity<ApiResponse<ErrorLogListResponse>> getErrorLogs(
			@RequestParam(defaultValue = "ALL") String errorType,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "10") int size) {

		ErrorLogListResponse response = errorLogService.getErrorLogs(errorType, page, size);

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * 에러 로그 해결 상태 토글 (관리자 전용)
	 * is_resolved를 현재 값의 반대로 전환합니다.
	 * 해결(0→1) 시 resolved_at=NOW() 설정, 미해결(1→0) 시 resolved_at=null 초기화.
	 *
	 * @param logId : 토글할 에러 로그 ID (Path Variable)
	 * @return log_id, is_resolved, resolved_at
	 * @since : 2026.04.26
	 * @version : 0.0.1
	 * @author : 신태훈
	 */
	@Operation(summary = "에러 해결 상태 토글 (관리자 전용)",
			description = "is_resolved를 현재 값에서 반전시킵니다. 해결 시 resolved_at 설정, 미해결로 되돌릴 시 null 초기화.")
	@PatchMapping("/{logId}/resolve")
	public ResponseEntity<ApiResponse<ErrorLogResolveResponse>> toggleResolve(
			@PathVariable Long logId) {

		ErrorLogResolveResponse response = errorLogService.toggleResolve(logId);

		return ResponseEntity.ok(ApiResponse.success(response));
	}
}
