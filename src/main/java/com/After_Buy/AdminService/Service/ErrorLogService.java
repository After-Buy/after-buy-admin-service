package com.After_Buy.AdminService.Service;

import com.After_Buy.AdminService.Dto.Request.ErrorLogReceiveRequest;
import com.After_Buy.AdminService.Dto.Response.ErrorLogListResponse;
import com.After_Buy.AdminService.Dto.Response.ErrorLogResolveResponse;

/**
 * 에러 로그 서비스 인터페이스
 *
 * @since : 2026.04.26
 * @version : 0.0.1
 * @author : 신태훈
 */
public interface ErrorLogService {

	/**
	 * 에러 로그 목록을 최신순으로 페이징 조회합니다.
	 *
	 * @param errorType : 필터 타입 ("WARNING", "ERROR", "ALL")
	 * @param page      : 페이지 번호 (1-based)
	 * @param size      : 페이지당 항목 수
	 * @return 에러 로그 목록, 미해결 카운트, 페이지네이션
	 */
	ErrorLogListResponse getErrorLogs(String errorType, int page, int size);

	/**
	 * 에러 로그 해결 상태를 토글합니다.
	 * is_resolved=0 → 1 (resolve) / is_resolved=1 → 0 (unresolve)
	 *
	 * @param logId : 토글할 에러 로그 ID
	 * @return 변경 후 log_id, is_resolved, resolved_at
	 * @throws com.After_Buy.AdminService.Exception.CustomException : 로그 미존재 시 ERROR_LOG_NOT_FOUND (ADMIN-005)
	 */
	ErrorLogResolveResponse toggleResolve(Long logId);

	/**
	 * 타 마이크로서비스에서 전송된 에러 로그를 DB에 저장합니다.
	 * POST /internal/error-logs 엔드포인트에서 호출됩니다.
	 *
	 * @param request : 에러 로그 수신 DTO
	 */
	void saveFromInternal(ErrorLogReceiveRequest request);
}
