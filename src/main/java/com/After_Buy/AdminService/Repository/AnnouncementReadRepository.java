package com.After_Buy.AdminService.Repository;

import com.After_Buy.AdminService.Entity.AnnouncementRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 읽음 여부 리포지토리
 *
 * @author 최준혁
 */
@Repository
public interface AnnouncementReadRepository extends JpaRepository<AnnouncementRead, Long> {
    List<AnnouncementRead> findByUserIdAndAnnouncement_AnnouncementIdIn(Long userId, List<Long> announcementIds);
    boolean existsByUserIdAndAnnouncement_AnnouncementId(Long userId, Long announcementId);
}
