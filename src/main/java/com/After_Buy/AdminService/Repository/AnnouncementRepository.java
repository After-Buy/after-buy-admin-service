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
 * 공지사항 리포지토리
 *
 * @author 최준혁
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
}
