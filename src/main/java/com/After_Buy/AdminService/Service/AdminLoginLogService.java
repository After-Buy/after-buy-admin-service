package com.After_Buy.AdminService.Service;

import com.After_Buy.AdminService.Dto.Response.AdminLoginLogDetailResponse;
import com.After_Buy.AdminService.Dto.Response.AdminLoginLogListResponse;

/**
 * 관리자 로그인 내역 조회 서비스 인터페이스
 *
 * @author 최준혁
 * @since 2026.03.30
 * @version 0.0.1
 */
public interface AdminLoginLogService {

	/**
	 * 관리자 로그인 내역 목록을 최신순으로 페이징 조회합니다.
	 *
	 * @param page             페이지 번호 (1-based)
	 * @param size             페이지당 항목 수
	 * @param currentSessionId 현재 세션 ID (접속 중 여부 판별용)
	 * @return 목록 응답 DTO (login_logs, pagination)
	 */
	AdminLoginLogListResponse getLoginLogs(int page, int size, String currentSessionId);

	/**
	 * 특정 로그의 상세 정보를 조회합니다.
	 *
	 * @param logId            조회할 로그 ID
	 * @param currentSessionId 현재 세션 ID (접속 중 여부 판별용)
	 * @return 상세 응답 DTO
	 * @throws CustomException ADMIN-006 (로그 미존재)
	 */
	AdminLoginLogDetailResponse getLoginLogDetail(Long logId, String currentSessionId);
}
