package com.After_Buy.AdminService.Repository;

import com.After_Buy.AdminService.Entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 공지사항 리포지토리
 *
 * @author 최준혁
 */
@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
}
