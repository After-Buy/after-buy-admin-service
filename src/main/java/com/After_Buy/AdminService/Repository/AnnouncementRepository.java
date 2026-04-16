package com.After_Buy.AdminService.Repository;

import com.After_Buy.AdminService.Entity.Announcement;
import com.After_Buy.AdminService.Entity.AnnouncementCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 공지사항 리포지터리
 * announcements 테이블에 대한 CRUD 및 커스텀 쿼리를 제공합니다.
 *
 * @since : 2026.04.10
 * @version : 0.0.1
 * @author : 최준혁
 */
@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    @Query("SELECT a FROM Announcement a WHERE a.isPinned = 1 " +
           "AND (:category IS NULL OR a.category = :category) " +
           "ORDER BY a.createdAt DESC")
    List<Announcement> findPinnedAnnouncements(@Param("category") AnnouncementCategory category);

    @Query("SELECT a FROM Announcement a WHERE a.isPinned = 0 " +
           "AND (:category IS NULL OR a.category = :category) " +
           "AND (:keyword IS NULL OR a.title LIKE %:keyword% OR a.content LIKE %:keyword%)")
    Page<Announcement> findNormalAnnouncements(@Param("category") AnnouncementCategory category, @Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT a FROM Announcement a WHERE " +
           "(:category IS NULL OR a.category = :category) " +
           "AND (:keyword IS NULL OR a.title LIKE %:keyword% OR a.content LIKE %:keyword%)")
    Page<Announcement> findAllByKeyword(@Param("category") AnnouncementCategory category, @Param("keyword") String keyword, Pageable pageable);

    /**
     * 특정 날짜 이후 작성된 최신 공지사항 N건 조회 (대시보드용)
     */
    List<Announcement> findTop5ByCreatedAtAfterOrderByCreatedAtDesc(java.time.LocalDateTime date);
}
