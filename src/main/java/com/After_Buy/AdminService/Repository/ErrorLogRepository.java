package com.After_Buy.AdminService.Repository;

import com.After_Buy.AdminService.Entity.ErrorLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 에러 로그 레포지토리
 * 대시보드 및 로그 관리에서 사용됩니다.
 *
 * @since : 2026.04.15
 * @version : 0.0.2
 * @author : 최준혁
 * @author : 신태훈 (2026.04.26 — 페이징/필터 쿼리 추가)
 */
@Repository
public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long> {

	/**
	 * 미해결 혹은 해결된 특정 상태의 최신 에러 로그 5건 조회 (대시보드용)
	 */
	List<ErrorLog> findTop5ByIsResolvedOrderByCreatedAtDesc(Integer isResolved);

	/**
	 * 특정 상태의 에러 로그 총 개수 조회
	 */
	Long countByIsResolved(Integer isResolved);

	/**
	 * 전체 에러 로그 최신순 페이징 조회 (error_type 필터 없음)
	 * @since : 2026.04.26
	 * @author : 신태훈
	 */
	Page<ErrorLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

	/**
	 * error_type 필터 적용 최신순 페이징 조회
	 * @since : 2026.04.26
	 * @author : 신태훈
	 */
	Page<ErrorLog> findByErrorTypeOrderByCreatedAtDesc(ErrorLog.ErrorType errorType, Pageable pageable);
}

