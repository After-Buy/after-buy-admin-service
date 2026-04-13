package com.After_Buy.AdminService.Service;

import com.After_Buy.AdminService.Dto.Request.AnnouncementCreateRequest;
import com.After_Buy.AdminService.Dto.Response.AnnouncementCreateResponse;

import com.After_Buy.AdminService.Dto.Response.AnnouncementListResponse;

/**
 * 공지사항 서비스 인터페이스
 *
 * @author 최준혁
 */
public interface AnnouncementService {
    
    /**
     * 공지사항 등록 및 푸시 알림 발송
     *
     * @param request 공지사항 등록 요청 정보
     * @param adminId 작성자 관리자 ID
     * @return 등록 완료 응답
     */
    AnnouncementCreateResponse createAnnouncement(AnnouncementCreateRequest request, Long adminId);

    /**
     * 공지사항 목록 조회 (사용자/관리자 공용)
     *
     * @param category 카테고리 필터 (NOTICE 등, null이면 전체)
     * @param keyword 검색어 (null이면 전체)
     * @param page 조회할 페이지 (1-based)
     * @param size 페이지 크기
     * @param userId 요청한 사용자 ID (관리자인 경우 null)
     * @return 공지사항 목록 응답 DTO
     */
    AnnouncementListResponse getAnnouncementList(String category, String keyword, int page, int size, Long userId);
}
