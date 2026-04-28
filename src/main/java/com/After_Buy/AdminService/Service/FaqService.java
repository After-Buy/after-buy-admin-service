package com.After_Buy.AdminService.Service;

import com.After_Buy.AdminService.Dto.Request.FaqCreateRequest;
import com.After_Buy.AdminService.Dto.Response.FaqCreateResponse;
import com.After_Buy.AdminService.Dto.Response.FaqDetailResponse;
import com.After_Buy.AdminService.Dto.Response.FaqListResponse;
import com.After_Buy.AdminService.Dto.Response.FaqUpdateResponse;

/**
 * 이용 안내(FAQ) 서비스 인터페이스
 *
 * @since : 2026.04.26
 * @version : 0.0.1
 * @author : 신태훈
 */
public interface FaqService {

	/**
	 * FAQ를 등록합니다.
	 *
	 * @param request : 등록 요청 DTO (title, content)
	 * @param adminId : 등록한 관리자 ID
	 * @return 등록된 FAQ 정보
	 */
	FaqCreateResponse createFaq(FaqCreateRequest request, Long adminId);

	/**
	 * FAQ 목록을 최신순으로 페이징 조회합니다.
	 *
	 * @param keyword : 제목/본문 검색어 (없으면 전체 조회)
	 * @param page    : 페이지 번호 (1-based)
	 * @param size    : 페이지당 항목 수
	 * @return FAQ 목록 및 페이지네이션 정보
	 */
	FaqListResponse getFaqList(String keyword, int page, int size);

	/**
	 * FAQ 상세 정보를 조회합니다.
	 *
	 * @param faqId : 조회할 FAQ ID
	 * @return FAQ 상세 정보
	 * @throws com.After_Buy.AdminService.Exception.CustomException : FAQ 미존재 시 FAQ_NOT_FOUND (ADMIN-004)
	 */
	FaqDetailResponse getFaqDetail(Long faqId);

	/**
	 * FAQ를 수정합니다.
	 *
	 * @param faqId   : 수정할 FAQ ID
	 * @param request : 수정 요청 DTO (title, content)
	 * @return 수정된 FAQ 정보
	 * @throws com.After_Buy.AdminService.Exception.CustomException : FAQ 미존재 시 FAQ_NOT_FOUND (ADMIN-004)
	 */
	FaqUpdateResponse updateFaq(Long faqId, FaqCreateRequest request);

	/**
	 * FAQ를 삭제합니다.
	 *
	 * @param faqId : 삭제할 FAQ ID
	 * @throws com.After_Buy.AdminService.Exception.CustomException : FAQ 미존재 시 FAQ_NOT_FOUND (ADMIN-004)
	 */
	void deleteFaq(Long faqId);
}
