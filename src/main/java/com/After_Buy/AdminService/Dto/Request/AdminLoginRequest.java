package com.After_Buy.AdminService.Dto.Request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 관리자 로그인 요청 DTO
 * POST /api/admin/auth/login 요청 바디
 *
 * @author 최준혁
 * @since 2026.03.26
 * @version 0.0.1
 */
@Getter
@NoArgsConstructor
public class AdminLoginRequest {

	/** 사전 지급된 관리자 로그인 아이디 */
	@NotBlank(message = "아이디는 필수 입력값입니다.")
	@JsonProperty("admin_account")
	private String adminAccount;

	/** 관리자 비밀번호 (BCrypt 검증용) */
	@NotBlank(message = "비밀번호는 필수 입력값입니다.")
	@JsonProperty("password")
	private String password;
}
