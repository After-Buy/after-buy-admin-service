package com.After_Buy.AdminService.Service;

import com.After_Buy.AdminService.Dto.Response.DashboardResponse;
import com.After_Buy.AdminService.Dto.Response.DashboardUserStatsResponse;
import com.After_Buy.AdminService.Dto.Response.OcrStatsDetailResponse;

/**
 * 대시보드 및 통계 서비스 인터페이스
 *
 * @since : 2026.04.15
 * @version : 0.0.1
 * @author : 최준혁
 */
public interface StatsService {

	/**
	 * 메인 대시보드 통합 데이터 조회
	 */
	DashboardResponse getDashboardData();

	/**
	 * 사용자 통계 조회 (관리자용)
	 */
	DashboardUserStatsResponse getUserStats();

	/**
	 * OCR 통계 전체 정보 조회 (관리자용)
	 *
	 * @param period : 조회 기간 단위 (WEEK, MONTH, ALL)
	 */
	OcrStatsDetailResponse getOcrStatsDetail(String period);
}
