package com.After_Buy.AdminService.Controller;

import com.After_Buy.AdminService.Dto.Response.DashboardResponse;
import com.After_Buy.AdminService.Dto.Response.DashboardUserStatsResponse;
import com.After_Buy.AdminService.Service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 대시보드 및 통계 컨트롤러
 *
 * @since : 2026.04.15
 * @version : 0.0.1
 * @author : 최준혁
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class StatsController {

	private final StatsService statsService;

	/**
	 * 메인 대시보드 운영 지표 통합 조회
	 */
	@GetMapping("/dashboard")
	public ResponseEntity<Map<String, Object>> getDashboard() {
		DashboardResponse dashboardData = statsService.getDashboardData();
		return ResponseEntity.ok(Map.of(
				"success", true,
				"data", dashboardData
		));
	}

	/**
	 * 사용자 통계 단일 조회
	 */
	@GetMapping("/users/stats")
	public ResponseEntity<Map<String, Object>> getUserStats() {
		DashboardUserStatsResponse userStats = statsService.getUserStats();
		return ResponseEntity.ok(Map.of(
				"success", true,
				"data", userStats == null ? Map.of() : userStats
		));
	}
}
