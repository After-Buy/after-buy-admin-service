package com.After_Buy.AdminService.Service;

import com.After_Buy.AdminService.Dto.Request.ErrorLogReceiveRequest;
import com.After_Buy.AdminService.Dto.Response.ErrorLogListResponse;
import com.After_Buy.AdminService.Dto.Response.ErrorLogResolveResponse;
import com.After_Buy.AdminService.Entity.ErrorLog;
import com.After_Buy.AdminService.Exception.CustomException;
import com.After_Buy.AdminService.Exception.ErrorCode;
import com.After_Buy.AdminService.Repository.ErrorLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 에러 로그 서비스 구현체
 *
 * @since : 2026.04.26
 * @version : 0.0.1
 * @author : 신태훈
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ErrorLogServiceImpl implements ErrorLogService {

	private final ErrorLogRepository errorLogRepository;

	/**
	 * 에러 로그 목록 조회
	 * error_type이 ALL이면 전체 조회, 그 외 해당 타입만 필터링합니다.
	 * 응답에 미해결(is_resolved=0) 총 카운트를 함께 포함합니다.
	 *
	 * @param errorType : 필터 타입 ("WARNING", "ERROR", "ALL")
	 * @param page      : 페이지 번호 (1-based)
	 * @param size      : 페이지당 항목 수
	 * @return 에러 로그 목록, 미해결 카운트, 페이지네이션
	 * @since : 2026.04.26
	 * @version : 0.0.1
	 * @author : 신태훈
	 */
	@Override
	@Transactional(readOnly = true)
	public ErrorLogListResponse getErrorLogs(String errorType, int page, int size) {
		Pageable pageable = PageRequest.of(page - 1, size);

		Page<ErrorLog> logPage;
		if (!StringUtils.hasText(errorType) || "ALL".equalsIgnoreCase(errorType)) {
			/* error_type 필터 없음: 전체 최신순 조회 */
			logPage = errorLogRepository.findAllByOrderByCreatedAtDesc(pageable);
		} else {
			/* error_type 필터 적용: WARNING 또는 ERROR */
			ErrorLog.ErrorType type = ErrorLog.ErrorType.valueOf(errorType.toUpperCase());
			logPage = errorLogRepository.findByErrorTypeOrderByCreatedAtDesc(type, pageable);
		}

		List<ErrorLogListResponse.ErrorLogItem> items = logPage.getContent().stream()
				.map(ErrorLogListResponse.ErrorLogItem::from)
				.collect(Collectors.toList());

		/* 미해결 에러 총 카운트 */
		Long unresolvedCount = errorLogRepository.countByIsResolved(0);

		ErrorLogListResponse.Pagination pagination = ErrorLogListResponse.Pagination.builder()
				.currentPage(logPage.getNumber() + 1)
				.totalPages(logPage.getTotalPages())
				.totalCount(logPage.getTotalElements())
				.size(logPage.getSize())
				.build();

		return ErrorLogListResponse.builder()
				.errorLogs(items)
				.unresolvedCount(unresolvedCount)
				.pagination(pagination)
				.build();
	}

	/**
	 * 에러 로그 해결 상태 토글
	 * is_resolved=0 이면 resolve(), is_resolved=1 이면 unresolve() 호출.
	 * resolve 시 resolved_at=NOW(), unresolve 시 resolved_at=null.
	 *
	 * @param logId : 토글할 에러 로그 ID
	 * @return 변경 후 log_id, is_resolved, resolved_at
	 * @throws CustomException : 로그 미존재 시 ERROR_LOG_NOT_FOUND (ADMIN-005)
	 * @since : 2026.04.26
	 * @version : 0.0.1
	 * @author : 신태훈
	 */
	@Override
	@Transactional
	public ErrorLogResolveResponse toggleResolve(Long logId) {
		/* 에러 로그 존재 여부 확인 - 없으면 ERROR_LOG_NOT_FOUND 예외 발생 */
		ErrorLog errorLog = errorLogRepository.findById(logId)
				.orElseThrow(() -> new CustomException(ErrorCode.ERROR_LOG_NOT_FOUND));

		/* is_resolved 현재 값에 따라 resolve / unresolve 토글 */
		if (errorLog.getIsResolved() == 0) {
			errorLog.resolve();
		} else {
			errorLog.unresolve();
		}

		return ErrorLogResolveResponse.builder()
				.logId(errorLog.getLogId())
				.isResolved(errorLog.getIsResolved())
				.resolvedAt(errorLog.getResolvedAt())
				.build();
	}

	/**
	 * 타 마이크로서비스에서 전송된 500급 에러 로그를 DB에 저장합니다.
	 * POST /internal/error-logs 엔드포인트에서 호출됩니다.
	 * error_type은 "ERROR"로 고정 저장합니다. (500급 서버 크래시만 수신하므로)
	 *
	 * @param request : 에러 로그 수신 DTO
	 * @since : 2026.04.26
	 * @version : 0.0.1
	 * @author : 신태훈
	 */
	@Override
	@Transactional
	public void saveFromInternal(ErrorLogReceiveRequest request) {
		/* 에러 메시지 길이 제한 (DB 컬럼 VARCHAR(500)) */
		String errorMessage = request.getErrorMessage();
		if (errorMessage != null && errorMessage.length() > 500) {
			errorMessage = errorMessage.substring(0, 497) + "...";
		}

		ErrorLog errorLog = ErrorLog.builder()
				.serviceName(request.getServiceName())
				.endpointPath(request.getEndpointPath())
				.errorType(ErrorLog.ErrorType.ERROR)
				.errorMessage(errorMessage != null ? errorMessage : request.getErrorType())
				.fullMessage(null)
				.build();

		errorLogRepository.save(errorLog);

		log.info("[ErrorLogService] 내부 에러 로그 저장 완료: service={}, endpoint={}",
				request.getServiceName(), request.getEndpointPath());
	}
}
