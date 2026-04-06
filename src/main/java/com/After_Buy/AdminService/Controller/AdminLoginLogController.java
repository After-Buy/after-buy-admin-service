package com.After_Buy.AdminService.Controller;

import com.After_Buy.AdminService.Dto.Response.AdminLoginLogDetailResponse;
import com.After_Buy.AdminService.Dto.Response.AdminLoginLogListResponse;
import com.After_Buy.AdminService.Dto.Response.ApiResponse;
import com.After_Buy.AdminService.Dto.Response.ErrorResponse;
import com.After_Buy.AdminService.Service.AdminLoginLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 관리자 로그인 내역 조회 컨트롤러
 * Base URL: /api/admin/login-logs
 * 관리자 접속 내역(감사 로그)의 목록 및 상세 조회 엔드포인트를 처리합니다.
 *
 * @author 최준혁
 * @since 2026.03.30
 * @version 0.0.1
 */
@Tag(name = "Login Logs", description = "관리자 로그인 내역 조회 API (목록 / 상세)")
@RestController
@RequestMapping("/api/admin/login-logs")
@RequiredArgsConstructor
public class AdminLoginLogController {

	private final AdminLoginLogService adminLoginLogService;

	/**
	 * 관리자 로그인 내역 목록 조회
	 * GET /api/admin/login-logs?page=1&size=10
	 *
	 * @param page        페이지 번호 (1-based, 기본값 1)
	 * @param size        페이지당 항목 수 (기본값 10)
	 * @param httpRequest 현재 세션 ID 추출용 (login_status 판별)
	 * @return 로그인 내역 목록 + 페이징 정보
	 */
	@Operation(
			summary = "관리자 로그인 내역 목록 조회",
			description = "관리자 접속 내역을 최신순으로 페이징 조회합니다. login_status는 현재 세션과 비교하여 '접속 중' 또는 '로그아웃'으로 반환됩니다."
	)
	@ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인 필요 (ADMIN-004)",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@GetMapping
	public ResponseEntity<ApiResponse<AdminLoginLogListResponse>> getLoginLogs(
			@Parameter(description = "페이지 번호 (1-based)", example = "1")
			@RequestParam(defaultValue = "1") int page,

			@Parameter(description = "페이지당 항목 수", example = "10")
			@RequestParam(defaultValue = "10") int size,

			HttpServletRequest httpRequest
	) {
		String currentSessionId = resolveSessionId(httpRequest);
		AdminLoginLogListResponse result = adminLoginLogService.getLoginLogs(page, size, currentSessionId);
		return ResponseEntity.ok(ApiResponse.success(result));
	}

	/**
	 * 관리자 로그인 내역 상세 조회
	 * GET /api/admin/login-logs/{log_id}
	 *
	 * @param logId       조회할 로그 ID
	 * @param httpRequest 현재 세션 ID 추출용 (login_status 판별)
	 * @return 로그인 내역 상세 정보
	 */
	@Operation(
			summary = "관리자 로그인 내역 상세 조회",
			description = "특정 로그 ID의 상세 정보를 조회합니다. IP, User-Agent, 실패 사유, 로그아웃 일시 등 전체 컬럼을 반환합니다."
	)
	@ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인 필요 (ADMIN-004)",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "로그 미존재 또는 보존기간 경과 (ADMIN-006)",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@GetMapping("/{log_id}")
	public ResponseEntity<ApiResponse<AdminLoginLogDetailResponse>> getLoginLogDetail(
			@Parameter(description = "조회할 로그 ID", example = "1")
			@PathVariable("log_id") Long logId,

			HttpServletRequest httpRequest
	) {
		String currentSessionId = resolveSessionId(httpRequest);
		AdminLoginLogDetailResponse result = adminLoginLogService.getLoginLogDetail(logId, currentSessionId);
		return ResponseEntity.ok(ApiResponse.success(result));
	}

	/**
	 * 현재 HTTP 요청에서 세션 ID를 추출합니다.
	 * 세션이 없으면 null을 반환합니다 (필터에서 이미 인증 확인 완료 후 호출됨).
	 *
	 * @param httpRequest HTTP 요청 객체
	 * @return 세션 ID 또는 null
	 */
	private String resolveSessionId(HttpServletRequest httpRequest) {
		HttpSession session = httpRequest.getSession(false);
		return session != null ? session.getId() : null;
	}
}
