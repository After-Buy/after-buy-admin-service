package com.After_Buy.AdminService.Repository;

import com.After_Buy.AdminService.Entity.AdminLoginLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 관리자 로그인 감사 로그 리포지토리
 * admin_db.admin_login_logs 테이블에 접근합니다.
 *
 * @author 최준혁
 * @since 2026.03.30
 * @version 0.0.1
 */
public interface AdminLoginLogRepository extends JpaRepository<AdminLoginLog, Long> {

	/**
	 * 전체 로그인 내역을 최신순으로 페이징 조회합니다.
	 *
	 * @param pageable 페이징 정보 (page, size)
	 * @return 페이징된 로그 목록
	 */
	Page<AdminLoginLog> findAllByOrderByLoginAtDesc(Pageable pageable);

	/**
	 * session_id로 로그인 로그를 조회합니다.
	 * 로그아웃 시 해당 세션의 logout_at 업데이트에 사용됩니다.
	 *
	 * @param sessionId 세션 쿠키 ID
	 * @return 해당 세션의 로그인 로그 (없으면 empty)
	 */
	Optional<AdminLoginLog> findBySessionId(String sessionId);
}
