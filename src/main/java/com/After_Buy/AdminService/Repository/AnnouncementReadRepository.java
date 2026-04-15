package com.After_Buy.AdminService.Repository;

import com.After_Buy.AdminService.Entity.AnnouncementRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;

/**
 * 공지사항 읽음 이력 리포지터리
 * announcement_reads 테이블에 대한 CRUD 및 커스텀 쿼리를 제공합니다.
 *
 * @since : 2026.04.10
 * @version : 0.0.1
 * @author : 최준혁
 */
@Repository
public interface AnnouncementReadRepository extends JpaRepository<AnnouncementRead, Long> {
	List<AnnouncementRead> findByUserIdAndAnnouncement_AnnouncementIdIn(Long userId, List<Long> announcementIds);
	boolean existsByUserIdAndAnnouncement_AnnouncementId(Long userId, Long announcementId);
	AnnouncementRead findByUserIdAndAnnouncement_AnnouncementId(Long userId, Long announcementId);

	/**
	 * 공지사항에 연관된 모든 읽음 기록 삭제
	 *
	 * @param announcementId : 공지사항 ID
	 */
	@Modifying
	void deleteByAnnouncement_AnnouncementId(Long announcementId);
}

