package com.After_Buy.AdminService.Controller;

import com.After_Buy.AdminService.Dto.Response.DashboardResponse;
import com.After_Buy.AdminService.Dto.Response.DashboardUserStatsResponse;
import com.After_Buy.AdminService.Dto.Response.OcrStatsDetailResponse;
import com.After_Buy.AdminService.Service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 대시보드 및 통계 컨트롤러
 *
 * @since : 2026.04.15
 * @version : 0.0.1
 * @author : 최준혁
 */
@Tag(name = "Admin DashBoard & Stats", description = "대시보드 및 통계 관리 (관리자)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class StatsController {

	private final StatsService statsService;

	/**
	 * 메인 대시보드 운영 지표 통합 조회
	 */
	@Operation(summary = "메인 대시보드 데이터 조회", description = "대시보드 상단 요약 데이터(사용자, OCR 현황 등)와 최근 공지사항, 미해결 에러 로그 목록을 통합 반환합니다.")
	@GetMapping("/dashboard")
	public ResponseEntity<Map<String, Object>> getDashboard() {
		DashboardResponse dashboardData = statsService.getDashboardData();
		return ResponseEntity.ok(Map.of(
				"success", true,
				"data", dashboardData));
	}

	/**
	 * 사용자 통계 단일 조회
	 */
	@Operation(summary = "사용자 통계 조회", description = "대시보드 내 전체 사용자 및 최근 7일 신규 가입자 증감률 통계를 반환합니다.")
	@GetMapping("/users/stats")
	public ResponseEntity<Map<String, Object>> getUserStats() {
		DashboardUserStatsResponse userStats = statsService.getUserStats();
		return ResponseEntity.ok(Map.of(
				"success", true,
				"data", userStats == null ? Map.of() : userStats));
	}

	/**
	 * OCR 통계 조회
	 * API 명세서에 따라 클라이언트에게 period별 정제된 OCR 통계를 반환합니다.
	 *
	 * @param period : 조회 기간 단위 (WEEK, MONTH, ALL), 기본값은 MONTH
	 */
	@Operation(summary = "OCR 관리 페이지 통계 조회", description = "선택된 기간(WEEK, MONTH, ALL)에 해당하는 OCR 오인식 집계, 항목별 통계, 실패 추이, 날짜별 결과 데이터를 반환합니다.")
	@GetMapping("/ocr-stats")
	public ResponseEntity<Map<String, Object>> getOcrStats(
			@RequestParam(defaultValue = "MONTH") String period) {
		OcrStatsDetailResponse ocrStats = statsService.getOcrStatsDetail(period);
		return ResponseEntity.ok(Map.of(
				"success", true,
				"data", ocrStats == null ? Map.of() : ocrStats));
	}
}
