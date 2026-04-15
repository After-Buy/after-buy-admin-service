package com.After_Buy.AdminService.Repository;

import com.After_Buy.AdminService.Entity.ErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 에러 로그 레포지토리
 * 대시보드 및 로그 관리에서 사용됩니다.
 *
 * @since : 2026.04.15
 * @version : 0.0.1
 * @author : 최준혁
 */
@Repository
public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long> {

	/**
	 * 미해결 혹은 해결된 특정 상태의 최신 에러 로그 5건 조회
	 */
	List<ErrorLog> findTop5ByIsResolvedOrderByCreatedAtDesc(Integer isResolved);

	/**
	 * 특정 상태의 에러 로그 총 개수 조회
	 */
	Long countByIsResolved(Integer isResolved);
}
