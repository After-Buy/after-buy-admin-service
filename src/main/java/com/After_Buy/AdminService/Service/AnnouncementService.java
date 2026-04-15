package com.After_Buy.AdminService.Service;

import com.After_Buy.AdminService.Dto.Request.AnnouncementCreateRequest;
import com.After_Buy.AdminService.Dto.Response.AnnouncementCreateResponse;
import com.After_Buy.AdminService.Dto.Response.AnnouncementDetailResponse;
import com.After_Buy.AdminService.Dto.Response.AnnouncementListResponse;
import com.After_Buy.AdminService.Dto.Response.AnnouncementReadResponse;
import com.After_Buy.AdminService.Dto.Response.AnnouncementUpdateResponse;

/**
 * 공지사항 서비스 인터페이스
 *
 * @author : 최준혁
 */
public interface AnnouncementService {

	/**
	 * 공지사항 등록 및 푸시 알림 발송
	 *
	 * @param request : 공지사항 등록 요청 정보
	 * @param adminId : 작성자 관리자 ID
	 * @return : 등록 완료 응답
	 */
	AnnouncementCreateResponse createAnnouncement(AnnouncementCreateRequest request, Long adminId);

	/**
	 * 공지사항 목록 조회 (사용자/관리자 공용)
	 *
	 * @param category : 카테고리 필터 (NOTICE 등, null이면 전체)
	 * @param keyword  : 검색어 (null이면 전체)
	 * @param page     : 조회할 페이지 (1-based)
	 * @param size     : 페이지 크기
	 * @param userId   : 요청한 사용자 ID (관리자인 경우 null)
	 * @return : 공지사항 목록 응답 DTO
	 */
	AnnouncementListResponse getAnnouncementList(String category, String keyword, int page, int size, Long userId);

	/**
	 * 공지사항 상세 조회 (관리자/사용자 공용)
	 *
	 * @param announcementId : 조회할 공지사항 ID
	 * @return : 공지사항 상세 응답 DTO
	 * @throws com.After_Buy.AdminService.Exception.CustomException : 공지사항 미존재 시 ANNOUNCEMENT_NOT_FOUND
	 */
	AnnouncementDetailResponse getAnnouncementDetail(Long announcementId);

	/**
	 * 공지사항 읽음 처리 (사용자 전용)
	 * UNIQUE 제약조건에 따라 이미 읽은 경우 멱등성(idempotent) 처리하여 기존 read_at을 반환합니다.
	 *
	 * @param announcementId : 공지사항 ID
	 * @param userId         : JWT 인증 사용자 ID
	 * @return : 읽음 처리 결과 (read_at)
	 * @throws com.After_Buy.AdminService.Exception.CustomException : 공지사항 미존재 시 ANNOUNCEMENT_NOT_FOUND
	 */
	AnnouncementReadResponse readAnnouncement(Long announcementId, Long userId);

	/**
	 * 공지사항 수정 (관리자 전용)
	 * 제목, 카테고리, 본문, 상단 고정 여부를 수정합니다.
	 *
	 * @param announcementId : 수정할 공지사항 ID
	 * @param request        : 수정 요청 정보 (POST와 동일한 구조)
	 * @return : 수정 완료된 공지사항 전체 응답
	 * @throws com.After_Buy.AdminService.Exception.CustomException : 공지사항 미존재 시 ANNOUNCEMENT_NOT_FOUND
	 */
	AnnouncementUpdateResponse updateAnnouncement(Long announcementId, AnnouncementCreateRequest request);

	/**
	 * 공지사항 삭제 (관리자 전용)
	 * 해당 공지사항과 연관된 읽음 이력(announcement_reads)은 DB CASCADE로 처리됩니다.
	 *
	 * @param announcementId : 삭제할 공지사항 ID
	 * @throws com.After_Buy.AdminService.Exception.CustomException : 공지사항 미존재 시 ANNOUNCEMENT_NOT_FOUND
	 */
	void deleteAnnouncement(Long announcementId);
}

