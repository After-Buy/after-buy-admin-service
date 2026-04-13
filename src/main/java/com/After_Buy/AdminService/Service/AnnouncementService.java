package com.After_Buy.AdminService.Service;

import com.After_Buy.AdminService.Dto.Request.AnnouncementCreateRequest;
import com.After_Buy.AdminService.Dto.Response.AnnouncementCreateResponse;

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
}
