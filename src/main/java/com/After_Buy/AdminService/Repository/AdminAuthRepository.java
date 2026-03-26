package com.After_Buy.AdminService.Repository;

import com.After_Buy.AdminService.Entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 관리자 계정 레포지토리
 * admin_db.admins 테이블과 매핑됩니다.
 *
 * @author 최준혁
 * @since 2026.03.26
 * @version 0.0.1
 */
public interface AdminAuthRepository extends JpaRepository<Admin, Long> {

	/**
	 * 관리자 아이디로 계정을 조회합니다.
	 * 로그인 시 admin_account로 admins 테이블에서 조회합니다.
	 *
	 * @param adminAccount 관리자 아이디
	 * @return 관리자 엔티티 Optional
	 */
	Optional<Admin> findByAdminAccount(String adminAccount);
}
