package com.After_Buy.AdminService.Service;

import com.After_Buy.AdminService.Dto.Request.AdminLoginRequest;
import com.After_Buy.AdminService.Dto.Response.AdminLoginResponse;
import com.After_Buy.AdminService.Entity.Admin;
import com.After_Buy.AdminService.Entity.AdminLoginLog;
import com.After_Buy.AdminService.Exception.CustomException;
import com.After_Buy.AdminService.Exception.ErrorCode;
import com.After_Buy.AdminService.Repository.AdminAuthRepository;
import com.After_Buy.AdminService.Repository.AdminLoginLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 관리자 인증 서비스 구현체
 * BCrypt 비밀번호 검증, 세션 쿠키 발급, 로그인 실패 횟수 관리, 계정 잠금 처리를 담당합니다.
 * 모든 로그인 시도(성공/실패)와 로그아웃은 admin_login_logs 테이블에 감사 로그로 기록됩니다.
 *
 * @author 최준혁
 * @since 2026.03.26
 * @version 0.0.2
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService {

	/** 최대 로그인 연속 실패 허용 횟수 */
	private static final int MAX_LOGIN_FAIL_COUNT = 5;

	private final AdminAuthRepository adminAuthRepository;
	private final AdminLoginLogRepository adminLoginLogRepository;
	private final PasswordEncoder passwordEncoder;

	/**
	 * 관리자 로그인 처리
	 * 1. adminAccount로 계정 조회 → 없으면 ADMIN-001
	 * 2. 계정 잠금 여부 확인 → 잠금 시 ADMIN-002
	 * 3. BCrypt 비밀번호 검증 → 불일치 시 실패 횟수 증가 후 ADMIN-001
	 * 4. 성공 시 실패 횟수 초기화, HttpSession 생성
	 * 5. 모든 시도(성공/실패)를 admin_login_logs에 INSERT
	 *
	 * @param request     로그인 요청 DTO
	 * @param httpRequest HTTP 요청 객체 (IP, User-Agent, 세션 생성용)
	 * @return 로그인 응답 DTO (admin_id, admin_account, sessionId)
	 */
	@Override
	@Transactional
	public AdminLoginResponse login(AdminLoginRequest request, HttpServletRequest httpRequest) {
		String ipAddress  = resolveClientIp(httpRequest);
		String userAgent  = httpRequest.getHeader("User-Agent");

		// 1. 계정 조회 — 존재하지 않으면 자격증명 오류로 처리 (계정 존재 여부 노출 방지)
		Admin admin = adminAuthRepository.findByAdminAccount(request.getAdminAccount())
				.orElseGet(() -> {
					// 존재하지 않는 계정 시도도 실패 로그로 기록
					saveFailLog(null, request.getAdminAccount(), ipAddress, userAgent,
							"존재하지 않는 계정");
					throw new CustomException(ErrorCode.ADMIN_INVALID_CREDENTIALS);
				});

		// 2. 계정 잠금 확인
		if (admin.isLocked()) {
			saveFailLog(admin.getAdminId(), admin.getAdminAccount(), ipAddress, userAgent,
					"계정 잠금 상태");
			throw new CustomException(ErrorCode.ADMIN_ACCOUNT_LOCKED);
		}

		// 3. BCrypt 비밀번호 검증
		if (!passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())) {
			admin.incrementLoginFailCount();
			String failureReason = "비밀번호 불일치";

			// 최대 실패 횟수 초과 시 계정 잠금
			if (admin.getLoginFailCount() >= MAX_LOGIN_FAIL_COUNT) {
				admin.lock();
				failureReason = "비밀번호 불일치 (5회 초과 잠금)";
			}
			adminAuthRepository.save(admin);
			saveFailLog(admin.getAdminId(), admin.getAdminAccount(), ipAddress, userAgent,
					failureReason);
			throw new CustomException(ErrorCode.ADMIN_INVALID_CREDENTIALS);
		}

		// 4. 로그인 성공 — 실패 횟수 초기화
		admin.resetLoginFailCount();
		adminAuthRepository.save(admin);

		// 5. HttpSession 생성 및 속성 저장
		HttpSession session = httpRequest.getSession(true);
		session.setAttribute("adminId", admin.getAdminId());
		session.setAttribute("adminAccount", admin.getAdminAccount());

		// 6. 성공 로그 INSERT
		AdminLoginLog loginLog = AdminLoginLog.builder()
				.adminId(admin.getAdminId())
				.adminAccount(admin.getAdminAccount())
				.ipAddress(ipAddress)
				.userAgent(userAgent)
				.isSuccess(true)
				.failureReason(null)
				.sessionId(session.getId())
				.build();
		adminLoginLogRepository.save(loginLog);
		log.info("[AdminAuth] 로그인 성공 - account={}, ip={}", admin.getAdminAccount(), ipAddress);

		return AdminLoginResponse.builder()
				.adminId(admin.getAdminId())
				.adminAccount(admin.getAdminAccount())
				.sessionId(session.getId())
				.build();
	}

	/**
	 * 관리자 로그아웃 처리
	 * 세션을 무효화하고, 해당 세션의 admin_login_logs.logout_at을 현재 시각으로 업데이트합니다.
	 *
	 * @param httpRequest HTTP 요청 객체 (기존 세션 조회용)
	 */
	@Override
	@Transactional
	public void logout(HttpServletRequest httpRequest) {
		HttpSession session = httpRequest.getSession(false);
		if (session != null) {
			String sessionId = session.getId();

			// 해당 세션의 로그인 로그에 로그아웃 시각 기록
			adminLoginLogRepository.findBySessionId(sessionId)
					.ifPresent(AdminLoginLog::recordLogout);

			session.invalidate();
			log.info("[AdminAuth] 로그아웃 - sessionId={}", sessionId);
		}
	}

	/**
	 * 로그인 실패 로그를 저장하는 내부 헬퍼 메서드
	 *
	 * @param adminId       관리자 ID (계정 미존재 시 null)
	 * @param adminAccount  계정명 (요청 파라미터 또는 DB 조회값)
	 * @param ipAddress     접속 IP
	 * @param userAgent     User-Agent
	 * @param failureReason 실패 사유
	 */
	private void saveFailLog(Long adminId, String adminAccount, String ipAddress,
			String userAgent, String failureReason) {
		AdminLoginLog failLog = AdminLoginLog.builder()
				.adminId(adminId != null ? adminId : 0L)
				.adminAccount(adminAccount)
				.ipAddress(ipAddress)
				.userAgent(userAgent)
				.isSuccess(false)
				.failureReason(failureReason)
				.sessionId(null)
				.build();
		adminLoginLogRepository.save(failLog);
		log.warn("[AdminAuth] 로그인 실패 - account={}, ip={}, reason={}",
				adminAccount, ipAddress, failureReason);
	}

	/**
	 * 요청 객체에서 실제 클라이언트 IP를 추출합니다.
	 * 프록시 경유 시 X-Forwarded-For 헤더를 우선합니다.
	 *
	 * @param request HTTP 요청 객체
	 * @return 클라이언트 IP 문자열
	 */
	private String resolveClientIp(HttpServletRequest request) {
		String xForwardedFor = request.getHeader("X-Forwarded-For");
		if (xForwardedFor != null && !xForwardedFor.isBlank()) {
			return xForwardedFor.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}
}
