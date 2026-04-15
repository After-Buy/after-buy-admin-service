package com.After_Buy.AdminService.Dto.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 대시보드 및 사용자 통계 요약 응답 DTO
 *
 * @since : 2026.04.15
 * @version : 0.0.1
 * @author : 최준혁
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DashboardUserStatsResponse {

	@JsonProperty("total_users")
	private Long totalUsers;

	@JsonProperty("new_users_7d")
	private Long newUsers7d;

	@JsonProperty("new_users_prev_7d")
	private Long newUsersPrev7d;

	@JsonProperty("change_count")
	private Long changeCount;

	@JsonProperty("change_rate")
	private Double changeRate;

	@JsonProperty("change_direction")
	private String changeDirection;

	@JsonProperty("new_users_7d_change_rate")
	private Double newUsers7dChangeRate;

	@Builder
	public DashboardUserStatsResponse(Long totalUsers, Long newUsers7d, Long newUsersPrev7d, Long changeCount, Double changeRate, String changeDirection, Double newUsers7dChangeRate) {
		this.totalUsers = totalUsers;
		this.newUsers7d = newUsers7d;
		this.newUsersPrev7d = newUsersPrev7d;
		this.changeCount = changeCount;
		this.changeRate = changeRate;
		this.changeDirection = changeDirection;
		this.newUsers7dChangeRate = newUsers7dChangeRate;
	}
}
