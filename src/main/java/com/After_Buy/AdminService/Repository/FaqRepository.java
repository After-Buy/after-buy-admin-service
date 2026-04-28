package com.After_Buy.AdminService.Repository;

import com.After_Buy.AdminService.Entity.Faq;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 이용 안내(FAQ) 레포지토리
 *
 * @since : 2026.04.26
 * @version : 0.0.1
 * @author : 신태훈
 */
@Repository
public interface FaqRepository extends JpaRepository<Faq, Long> {

	/**
	 * 제목 또는 본문에 키워드가 포함된 FAQ 페이징 조회
	 *
	 * @param titleKeyword   : 제목 검색어
	 * @param contentKeyword : 본문 검색어
	 * @param pageable       : 페이징 조건
	 * @return 키워드 일치 FAQ 페이지
	 */
	Page<Faq> findByTitleContainingOrContentContaining(
			String titleKeyword, String contentKeyword, Pageable pageable);
}
