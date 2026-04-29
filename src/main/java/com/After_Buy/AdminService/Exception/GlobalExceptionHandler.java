package com.After_Buy.AdminService.Exception;

import com.After_Buy.AdminService.Dto.Response.ErrorResponse;
import com.After_Buy.AdminService.Entity.ErrorLog;
import com.After_Buy.AdminService.Repository.ErrorLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 전역 예외 처리 핸들러
 * 예외 처리 방법.md 의 설계 방식에 따라 @RestControllerAdvice로 전역 예외를 처리합니다.
 *
 * 처리 항목:
 * 1. CustomException - 비즈니스 예외 (의도된 예외)
 * 2. MethodArgumentNotValidException - @Valid 유효성 검사 실패
 * 3. Exception - 비처리 서버 오류 (500) → error_logs 테이블에 자동 저장
 *
 * @author 최준혁
 * @author 신태훈 (2026.04.26 — handleException error_logs 자동 저장 로직 추가)
 * @since 2026.03.26
 * @version 0.0.2
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

	private final ErrorLogRepository errorLogRepository;

	/**
	 * 비즈니스 커스텀 예외 처리
	 * Service에서 throw new CustomException(ErrorCode.XXX) 으로 발생시킨 예외를 처리합니다.
	 *
	 * @param e       발생한 CustomException
	 * @param request 현재 HTTP 요청 (path 추출용)
	 * @return 표준 에러 응답 JSON
	 */
	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ErrorResponse> handleCustomException(
			CustomException e,
			HttpServletRequest request
	) {
		ErrorCode errorCode = e.getErrorCode();
		log.warn("[CustomException] code={}, message={}, path={}",
				errorCode.getCode(), errorCode.getMessage(), request.getRequestURI());

		ErrorResponse response = ErrorResponse.builder()
				.timestamp(LocalDateTime.now())
				.status(errorCode.getHttpStatus().value())
				.code(errorCode.getCode())
				.message(errorCode.getMessage())
				.errors(List.of())
				.path(request.getRequestURI())
				.build();

		return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
	}

	/**
	 * @Valid 유효성 검사 실패 예외 처리
	 * RequestBody DTO 필드 검증 실패 시 필드별 오류 목록을 errors 배열로 반환합니다.
	 *
	 * @param e       MethodArgumentNotValidException
	 * @param request 현재 HTTP 요청 (path 추출용)
	 * @return 유효성 오류 목록이 포함된 표준 에러 응답 JSON
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(
			MethodArgumentNotValidException e,
			HttpServletRequest request
	) {
		BindingResult bindingResult = e.getBindingResult();

		// 필드별 검증 오류를 FieldError 목록으로 변환
		List<ErrorResponse.FieldError> fieldErrors = bindingResult.getFieldErrors().stream()
				.map(fieldError -> ErrorResponse.FieldError.builder()
						.field(fieldError.getField())
						.value(fieldError.getRejectedValue() != null
								? fieldError.getRejectedValue().toString() : "")
						.reason(fieldError.getDefaultMessage())
						.build())
				.toList();

		log.warn("[ValidationException] path={}, errors={}", request.getRequestURI(), fieldErrors);

		ErrorResponse response = ErrorResponse.builder()
				.timestamp(LocalDateTime.now())
				.status(ErrorCode.INVALID_INPUT_VALUE.getHttpStatus().value())
				.code(ErrorCode.INVALID_INPUT_VALUE.getCode())
				.message(ErrorCode.INVALID_INPUT_VALUE.getMessage())
				.errors(fieldErrors)
				.path(request.getRequestURI())
				.build();

		return ResponseEntity.badRequest().body(response);
	}

	/**
	 * 처리되지 않은 서버 내부 예외 처리 (최종 디펜스 라인)
	 * NullPointerException, DB 타임아웃 등 예상치 못한 500급 예외를 처리합니다.
	 * Admin Service 자체 발생 예외도 error_logs 테이블에 자동 저장됩니다. (service_name = "ADMIN")
	 *
	 * @param e       발생한 예외
	 * @param request 현재 HTTP 요청 (path 추출용)
	 * @return 500 Internal Server Error 응답 JSON
	 * @since : 2026.04.26
	 * @author : 신태훈
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(
			Exception e,
			HttpServletRequest request
	) {
		log.error("[UnhandledException] path={}, message={}", request.getRequestURI(), e.getMessage(), e);

		/* Admin Service 자체 500 예외를 error_logs 테이블에 저장 */
		try {
			String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
			if (errorMessage.length() > 500) {
				errorMessage = errorMessage.substring(0, 497) + "...";
			}

			/* 스택트레이스를 문자열로 변환 */
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String fullMessage = sw.toString();

			ErrorLog errorLog = ErrorLog.builder()
					.serviceName("ADMIN")
					.endpointPath(request.getRequestURI())
					.errorType(ErrorLog.ErrorType.ERROR)
					.errorMessage(errorMessage)
					.fullMessage(fullMessage)
					.build();
			errorLogRepository.save(errorLog);

		} catch (Exception saveException) {
			/* 에러 로그 저장 실패 시 콘솔 로그만 출력 (2차 장애 방지) */
			log.error("[UnhandledException] error_logs 저장 실패: {}", saveException.getMessage());
		}

		ErrorResponse response = ErrorResponse.builder()
				.timestamp(LocalDateTime.now())
				.status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus().value())
				.code(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
				.message(ErrorCode.INTERNAL_SERVER_ERROR.getMessage())
				.errors(List.of())
				.path(request.getRequestURI())
				.build();

		return ResponseEntity.internalServerError().body(response);
	}
}
