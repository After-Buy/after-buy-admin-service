package com.After_Buy.AdminService.Controller;

import com.After_Buy.AdminService.Dto.Request.ErrorLogReceiveRequest;
import com.After_Buy.AdminService.Service.ErrorLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 에러 로그 내부 수신 컨트롤러
 * 타 마이크로서비스(Auth, Device, Notification)의 GlobalExceptionHandler에서
 * 500급 서버 예외 발생 시 비동기로 호출되는 내부 전용 엔드포인트입니다.
 *
 * POST /internal/error-logs — 에러 로그 중앙 수집 (내부 전용)
 *
 * 보안: X-Internal-Secret 헤더 검증은 InternalSecretAuthFilter에서 필터 레벨로 처리합니다.
 * 처리 방식: 수신 즉시 DB 저장 (동기 처리, 그러나 호출 측은 비동기 Fire & Forget)
 *
 * @since : 2026.04.26
 * @version : 0.0.2
 * @author : 신태훈
 */
@Slf4j
@Tag(name = "Internal Error Logs", description = "에러 로그 내부 수신 API (MSA 내부 전용)")
@RestController
@RequestMapping("/internal/error-logs")
@RequiredArgsConstructor
public class InternalErrorLogController {

	private final ErrorLogService errorLogService;

	/**
	 * 타 마이크로서비스 500급 에러 로그 수신
	 * GlobalExceptionHandler의 handleException(@ExceptionHandler(Exception.class))
	 * 블록에서만 호출됩니다.
	 * 4xx 클라이언트 귀책 에러는 호출 대상에서 분기 제외되어야 합니다.
	 *
	 * X-Internal-Secret 검증은 InternalSecretAuthFilter에서 사전 처리됩니다.
	 *
	 * @param request : 에러 로그 수신 DTO
	 * @return 201 Created { "logged": true }
	 * @since : 2026.04.26
	 * @version : 0.0.2
	 * @author : 신태훈
	 */
	@Operation(summary = "에러 로그 중앙 수집 (내부 전용)", description = "각 서비스의 GlobalExceptionHandler에서 500급 예외 발생 시 Admin Service로 에러 로그를 전송합니다.")
	@PostMapping
	public ResponseEntity<Map<String, Boolean>> receiveErrorLog(
			@RequestBody ErrorLogReceiveRequest request) {

		errorLogService.saveFromInternal(request);

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(Map.of("logged", true));
	}
}
