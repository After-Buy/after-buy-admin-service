package com.After_Buy.AdminService.Service;

import com.After_Buy.AdminService.Dto.Request.FaqCreateRequest;
import com.After_Buy.AdminService.Dto.Response.FaqCreateResponse;
import com.After_Buy.AdminService.Dto.Response.FaqDetailResponse;
import com.After_Buy.AdminService.Dto.Response.FaqListResponse;
import com.After_Buy.AdminService.Dto.Response.FaqUpdateResponse;
import com.After_Buy.AdminService.Entity.Faq;
import com.After_Buy.AdminService.Exception.CustomException;
import com.After_Buy.AdminService.Exception.ErrorCode;
import com.After_Buy.AdminService.Repository.FaqRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 이용 안내(FAQ) 서비스 구현체
 *
 * @since : 2026.04.26
 * @version : 0.0.1
 * @author : 신태훈
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FaqServiceImpl implements FaqService {

	private final FaqRepository faqRepository;

	/**
	 * FAQ 등록 (관리자 전용)
	 * Faq 엔티티를 빌드하여 저장 후 등록 응답 DTO를 반환합니다.
	 *
	 * @param request : 등록 요청 DTO
	 * @param adminId : 등록한 관리자 ID
	 * @return 등록된 FAQ 응답 DTO
	 * @since : 2026.04.26
	 * @version : 0.0.1
	 * @author : 신태훈
	 */
	@Override
	@Transactional
	public FaqCreateResponse createFaq(FaqCreateRequest request, Long adminId) {
		Faq faq = Faq.builder()
				.title(request.getTitle())
				.content(request.getContent())
				.createdBy(adminId)
				.build();

		Faq saved = faqRepository.save(faq);
		return FaqCreateResponse.from(saved);
	}

	/**
	 * FAQ 목록 조회
	 * 키워드가 있으면 제목/본문 검색, 없으면 전체 목록을 최신순으로 조회합니다.
	 *
	 * @param keyword : 제목/본문 검색어 (없으면 전체 조회)
	 * @param page    : 페이지 번호 (1-based)
	 * @param size    : 페이지당 항목 수
	 * @return FAQ 목록 및 페이지네이션 응답 DTO
	 * @since : 2026.04.26
	 * @version : 0.0.1
	 * @author : 신태훈
	 */
	@Override
	@Transactional(readOnly = true)
	public FaqListResponse getFaqList(String keyword, int page, int size) {
		Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

		Page<Faq> faqPage;
		if (StringUtils.hasText(keyword)) {
			/* 키워드 있는 경우: 제목 또는 본문 검색 */
			faqPage = faqRepository.findByTitleContainingOrContentContaining(keyword, keyword, pageable);
		} else {
			/* 키워드 없는 경우: 전체 최신순 조회 */
			faqPage = faqRepository.findAll(pageable);
		}

		List<FaqListResponse.FaqItem> faqItems = faqPage.getContent().stream()
				.map(FaqListResponse.FaqItem::from)
				.collect(Collectors.toList());

		FaqListResponse.Pagination pagination = FaqListResponse.Pagination.builder()
				.currentPage(faqPage.getNumber() + 1)
				.totalPages(faqPage.getTotalPages())
				.totalCount(faqPage.getTotalElements())
				.size(faqPage.getSize())
				.build();

		return FaqListResponse.builder()
				.faqs(faqItems)
				.pagination(pagination)
				.build();
	}

	/**
	 * FAQ 상세 조회
	 * 존재하지 않는 ID 요청 시 FAQ_NOT_FOUND 예외를 발생시킵니다.
	 *
	 * @param faqId : 조회할 FAQ ID
	 * @return FAQ 상세 응답 DTO
	 * @throws CustomException : FAQ 미존재 시 FAQ_NOT_FOUND (ADMIN-004)
	 * @since : 2026.04.26
	 * @version : 0.0.1
	 * @author : 신태훈
	 */
	@Override
	@Transactional(readOnly = true)
	public FaqDetailResponse getFaqDetail(Long faqId) {
		/* FAQ 존재 여부 확인 - 없으면 FAQ_NOT_FOUND 예외 발생 */
		Faq faq = faqRepository.findById(faqId)
				.orElseThrow(() -> new CustomException(ErrorCode.FAQ_NOT_FOUND));

		return FaqDetailResponse.from(faq);
	}

	/**
	 * FAQ 수정 (관리자 전용)
	 * find 후 엔티티 update() 호출 → Dirty Checking으로 UPDATE 쿼리 자동 실행
	 * @UpdateTimestamp에 의해 updated_at이 자동 갱신됩니다.
	 *
	 * @param faqId   : 수정할 FAQ ID
	 * @param request : 수정 요청 DTO
	 * @return 수정 완료된 FAQ 응답 DTO
	 * @throws CustomException : FAQ 미존재 시 FAQ_NOT_FOUND (ADMIN-004)
	 * @since : 2026.04.26
	 * @version : 0.0.1
	 * @author : 신태훈
	 */
	@Override
	@Transactional
	public FaqUpdateResponse updateFaq(Long faqId, FaqCreateRequest request) {
		/* FAQ 존재 여부 확인 - 없으면 FAQ_NOT_FOUND 예외 발생 */
		Faq faq = faqRepository.findById(faqId)
				.orElseThrow(() -> new CustomException(ErrorCode.FAQ_NOT_FOUND));

		/* 엔티티 Dirty Checking으로 UPDATE 처리 */
		faq.update(request.getTitle(), request.getContent());

		return FaqUpdateResponse.from(faq);
	}

	/**
	 * FAQ 삭제 (관리자 전용)
	 *
	 * @param faqId : 삭제할 FAQ ID
	 * @throws CustomException : FAQ 미존재 시 FAQ_NOT_FOUND (ADMIN-004)
	 * @since : 2026.04.26
	 * @version : 0.0.1
	 * @author : 신태훈
	 */
	@Override
	@Transactional
	public void deleteFaq(Long faqId) {
		/* FAQ 존재 여부 확인 - 없으면 FAQ_NOT_FOUND 예외 발생 */
		Faq faq = faqRepository.findById(faqId)
				.orElseThrow(() -> new CustomException(ErrorCode.FAQ_NOT_FOUND));

		faqRepository.delete(faq);
	}
}
