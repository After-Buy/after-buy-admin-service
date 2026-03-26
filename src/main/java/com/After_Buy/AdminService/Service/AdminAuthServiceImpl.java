package com.After_Buy.AdminService.Service;

import com.After_Buy.AdminService.Dto.Request.AdminLoginRequest;
import com.After_Buy.AdminService.Dto.Response.AdminLoginResponse;
import com.After_Buy.AdminService.Entity.Admin;
import com.After_Buy.AdminService.Exception.CustomException;
import com.After_Buy.AdminService.Exception.ErrorCode;
import com.After_Buy.AdminService.Repository.AdminAuthRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 관리자 인증 서비스 구현체
 * BCrypt 비밀번호 검증, 세션 쿠키 발급, 로그인 실패 횟수 관리, 계정 잠금 처리를 담당합니다.
 *
 * @author 최준혁
 * @since 2026.03.26
 * @version 0.0.1
 */
@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService {

	/* 최대 로그인 연속 실패 허용 횟수 */
	private static final int MAX_LOGIN_FAIL_COUNT = 5;

	private final AdminAuthRepository adminAuthRepository;
	private final PasswordEncoder passwordEncoder;

	/**
	 * 관리자 로그인 처리
	 * 1. adminAccount로 계정 조회 → 없으면 ADMIN-001
	 * 2. 계정 잠금 여부 확인 → 잠금 시 ADMIN-002
	 * 3. BCrypt 비밀번호 검증 → 불일치 시 실패 횟수 증가 후 ADMIN-001
	 * 4. 성공 시 실패 횟수 초기화, HttpSession 생성 및 반환
	 *
	 * @param request     로그인 요청 DTO
	 * @param httpRequest HTTP 요청 객체 (세션 생성용)
	 * @return 로그인 응답 DTO (admin_id, admin_account, sessionId)
	 */
	@Override
	@Transactional
	public AdminLoginResponse login(AdminLoginRequest request, HttpServletRequest httpRequest) {
		// 1. 계정 조회 — 존재하지 않으면 자격증명 오류로 처리 (계정 존재 여부 노출 방지)
		Admin admin = adminAuthRepository.findByAdminAccount(request.getAdminAccount())
				.orElseThrow(() -> new CustomException(ErrorCode.ADMIN_INVALID_CREDENTIALS));

		// 2. 계정 잠금 확인
		if (admin.isLocked()) {
			throw new CustomException(ErrorCode.ADMIN_ACCOUNT_LOCKED);
		}

		// 3. BCrypt 비밀번호 검증
		if (!passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())) {
			admin.incrementLoginFailCount();

			// 최대 실패 횟수 초과 시 계정 잠금
			if (admin.getLoginFailCount() >= MAX_LOGIN_FAIL_COUNT) {
				admin.lock();
			}

			adminAuthRepository.save(admin);
			throw new CustomException(ErrorCode.ADMIN_INVALID_CREDENTIALS);
		}

		// 4. 로그인 성공 — 실패 횟수 초기화
		admin.resetLoginFailCount();
		adminAuthRepository.save(admin);

		// 5. HttpSession 생성 (Spring Security가 ADMIN_SESSION_ID 쿠키를 관리)
		HttpSession session = httpRequest.getSession(true);
		session.setAttribute("adminId", admin.getAdminId());
		session.setAttribute("adminAccount", admin.getAdminAccount());

		return AdminLoginResponse.builder()
				.adminId(admin.getAdminId())
				.adminAccount(admin.getAdminAccount())
				.sessionId(session.getId())
				.build();
	}

	/**
	 * 관리자 로그아웃 처리
	 * 현재 세션을 무효화합니다.
	 *
	 * @param httpRequest HTTP 요청 객체 (기존 세션 조회용)
	 */
	@Override
	public void logout(HttpServletRequest httpRequest) {
		// 기존 세션 조회 (false = 없으면 새로 생성하지 않음)
		HttpSession session = httpRequest.getSession(false);
		if (session != null) {
			session.invalidate();
		}
	}
}
