package com.After_Buy.AdminService.Service;

import com.After_Buy.AdminService.Dto.Response.AdminLoginLogDetailResponse;
import com.After_Buy.AdminService.Dto.Response.AdminLoginLogListResponse;
import com.After_Buy.AdminService.Entity.AdminLoginLog;
import com.After_Buy.AdminService.Exception.CustomException;
import com.After_Buy.AdminService.Exception.ErrorCode;
import com.After_Buy.AdminService.Repository.AdminLoginLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 관리자 로그인 내역 조회 서비스 구현체
 * login_status는 로그의 session_id가 현재 요청자의 session_id와 일치하는지로 판별합니다.
 *
 * @author 최준혁
 * @since 2026.03.30
 * @version 0.0.1
 */
@Service
@RequiredArgsConstructor
public class AdminLoginLogServiceImpl implements AdminLoginLogService {

	private final AdminLoginLogRepository adminLoginLogRepository;

	/**
	 * 로그인 내역 목록 페이징 조회 (최신순)
	 * login_status: 해당 로그의 session_id가 현재 요청자의 session_id와 동일하면 "접속 중", 아니면 "로그아웃"
	 */
	@Override
	@Transactional(readOnly = true)
	public AdminLoginLogListResponse getLoginLogs(int page, int size, String currentSessionId) {
		// page는 1-based이므로 0-based로 변환
		Page<AdminLoginLog> logPage = adminLoginLogRepository
				.findAllByOrderByLoginAtDesc(PageRequest.of(page - 1, size));

		List<AdminLoginLogListResponse.LoginLogSummary> summaries = logPage.getContent().stream()
				.map(log -> AdminLoginLogListResponse.LoginLogSummary.builder()
						.logId(log.getLogId())
						.adminId(log.getAdminId())
						.adminAccount(log.getAdminAccount())
						.isSuccess(log.isSuccess() ? 1 : 0)
						.failureReason(log.getFailureReason())
						.loginStatus(resolveLoginStatus(log, currentSessionId))
						.loginAt(log.getLoginAt())
						.build())
				.toList();

		AdminLoginLogListResponse.Pagination pagination = AdminLoginLogListResponse.Pagination.builder()
				.currentPage(page)
				.totalPages(logPage.getTotalPages())
				.totalCount(logPage.getTotalElements())
				.size(size)
				.build();

		return AdminLoginLogListResponse.builder()
				.loginLogs(summaries)
				.pagination(pagination)
				.build();
	}

	/**
	 * 로그인 내역 상세 조회
	 *
	 * @throws CustomException ADMIN-006 (로그 미존재)
	 */
	@Override
	@Transactional(readOnly = true)
	public AdminLoginLogDetailResponse getLoginLogDetail(Long logId, String currentSessionId) {
		AdminLoginLog log = adminLoginLogRepository.findById(logId)
				.orElseThrow(() -> new CustomException(ErrorCode.ADMIN_LOGIN_LOG_NOT_FOUND));

		return AdminLoginLogDetailResponse.builder()
				.logId(log.getLogId())
				.adminId(log.getAdminId())
				.adminAccount(log.getAdminAccount())
				.ipAddress(log.getIpAddress())
				.userAgent(log.getUserAgent())
				.isSuccess(log.isSuccess() ? 1 : 0)
				.failureReason(log.getFailureReason())
				.sessionId(log.getSessionId())
				.loginAt(log.getLoginAt())
				.logoutAt(log.getLogoutAt())
				.loginStatus(resolveLoginStatus(log, currentSessionId))
				.build();
	}

	/**
	 * 접속 상태 문자열을 결정합니다.
	 * 성공 로그이고 session_id가 현재 요청자의 세션과 동일하면 "접속 중", 그 외에는 "로그아웃"
	 *
	 * @param log              조회된 로그인 로그
	 * @param currentSessionId 현재 요청자의 HTTP 세션 ID
	 * @return "접속 중" 또는 "로그아웃"
	 */
	private String resolveLoginStatus(AdminLoginLog log, String currentSessionId) {
		if (log.getSessionId() != null
				&& currentSessionId != null
				&& log.getSessionId().equals(currentSessionId)) {
			return "접속 중";
		}
		return "로그아웃";
	}
}
