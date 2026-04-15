package com.After_Buy.AdminService.Service;

import com.After_Buy.AdminService.Client.NotificationInternalClient;
import com.After_Buy.AdminService.Dto.Request.AnnouncementCreateRequest;
import com.After_Buy.AdminService.Dto.Response.AnnouncementCreateResponse;
import com.After_Buy.AdminService.Dto.Response.AnnouncementDetailResponse;
import com.After_Buy.AdminService.Dto.Response.AnnouncementListResponse;
import com.After_Buy.AdminService.Dto.Response.AnnouncementReadResponse;
import com.After_Buy.AdminService.Dto.Response.AnnouncementUpdateResponse;
import com.After_Buy.AdminService.Entity.Announcement;
import com.After_Buy.AdminService.Entity.AnnouncementCategory;
import com.After_Buy.AdminService.Entity.AnnouncementRead;
import com.After_Buy.AdminService.Exception.CustomException;
import com.After_Buy.AdminService.Exception.ErrorCode;
import com.After_Buy.AdminService.Repository.AnnouncementReadRepository;
import com.After_Buy.AdminService.Repository.AnnouncementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 공지사항 서비스 구현체
 *
 * @author 최준혁
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final AnnouncementReadRepository announcementReadRepository;
    private final NotificationInternalClient notificationClient;

    @Override
    @Transactional
    public AnnouncementCreateResponse createAnnouncement(AnnouncementCreateRequest request, Long adminId) {
        Announcement announcement = Announcement.builder()
                .title(request.getTitle())
                .category(request.getCategory())
                .content(request.getContent())
                .isPinned(request.getIsPinned())
                .createdBy(adminId)
                .build();

        Announcement saved = announcementRepository.save(announcement);

        // 비동기로 푸시 발송 요청 (Notification Service)
        String deepLink = "afterbuy://announcements/" + saved.getAnnouncementId();
        notificationClient.broadcastPushAsync(
                "새 공지사항이 등록되었습니다.",
                saved.getTitle(),
                deepLink,
                saved.getAnnouncementId());

        // 명세서에 따라 응답 시 push_sent 는 true로 고정하여 반환 (비동기이므로 요청 발송 여부만 알림)
        return AnnouncementCreateResponse.from(saved, true);
    }

    @Override
    @Transactional(readOnly = true)
    public AnnouncementListResponse getAnnouncementList(String categoryStr, String keyword, int page, int size,
            Long userId) {
        AnnouncementCategory category = null;
        if (StringUtils.hasText(categoryStr) && !"ALL".equalsIgnoreCase(categoryStr)) {
            category = AnnouncementCategory.valueOf(categoryStr.toUpperCase());
        }
        String searchKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        List<Announcement> pinnedList = new ArrayList<>();
        Page<Announcement> normalPage;

        // 키워드가 있으면 상단 고정 여부 무관하게 전체 조건에서 검색, 없으면 상단고정 분리
        if (searchKeyword != null) {
            normalPage = announcementRepository.findAllByKeyword(category, searchKeyword, pageable);
        } else {
            // 키워드가 없으므로, 1페이지일 때만 고정 공지를 상단에 표시하거나 항상 표시
            // 명세서 상 "상단 고정 공지는 검색어 입력 시 결과 대상에서 제외합니다" 라고 되어 있음
            pinnedList = announcementRepository.findPinnedAnnouncements(category);
            normalPage = announcementRepository.findNormalAnnouncements(category, null, pageable);
        }

        List<Announcement> allFetched = new ArrayList<>();
        allFetched.addAll(pinnedList);
        allFetched.addAll(normalPage.getContent());

        // 사용자인 경우에만 read 처리
        List<Long> readAnnouncementIds = Collections.emptyList();
        if (userId != null && !allFetched.isEmpty()) {
            List<Long> ids = allFetched.stream().map(Announcement::getAnnouncementId).collect(Collectors.toList());
            readAnnouncementIds = announcementReadRepository.findByUserIdAndAnnouncement_AnnouncementIdIn(userId, ids)
                    .stream()
                    .map(r -> r.getAnnouncement().getAnnouncementId())
                    .collect(Collectors.toList());
        }

        List<Long> finalReadIds = readAnnouncementIds;
        LocalDate today = LocalDate.now();

        List<AnnouncementListResponse.AnnouncementItem> pinnedItems = pinnedList.stream()
                .map(a -> mapToDto(a, today, finalReadIds, userId))
                .collect(Collectors.toList());

        List<AnnouncementListResponse.AnnouncementItem> normalItems = normalPage.getContent().stream()
                .map(a -> mapToDto(a, today, finalReadIds, userId))
                .collect(Collectors.toList());

        AnnouncementListResponse.Pagination pagination = AnnouncementListResponse.Pagination.builder()
                .currentPage(normalPage.getNumber() + 1)
                .totalPages(normalPage.getTotalPages())
                .totalCount(normalPage.getTotalElements())
                .size(normalPage.getSize())
                .build();

        return AnnouncementListResponse.builder()
                .pinnedAnnouncements(pinnedItems)
                .announcements(normalItems)
                .pagination(pagination)
                .build();
    }

    /**
     * 공지사항 상세 조회 (관리자/사용자 공용)
     * 존재하지 않는 ID 요청 시 ANNOUNCEMENT_NOT_FOUND 예외를 발생시킵니다.
     *
     * @param announcementId 조회할 공지사항 ID
     * @return 공지사항 상세 응답 DTO
     * @since 2026.04.15
     * @author 최준혁
     */
    @Override
    @Transactional(readOnly = true)
    public AnnouncementDetailResponse getAnnouncementDetail(Long announcementId) {
        /* 공지사항 DB 조회 - 없으면 ANNOUNCEMENT_NOT_FOUND 예외 발생 */
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new CustomException(ErrorCode.ANNOUNCEMENT_NOT_FOUND));

        LocalDate today = LocalDate.now();

        return AnnouncementDetailResponse.from(announcement, today);
    }

    private AnnouncementListResponse.AnnouncementItem mapToDto(Announcement a, LocalDate today, List<Long> readIds,
            Long userId) {
        /* is_new: 오늘 날짜인지 비교하여 서버에서 계산 */
        boolean isNew = a.getCreatedAt().toLocalDate().isEqual(today);
        /* userId가 있으면 읽음 여부 세팅, 관리자(null)면 false로 처리 */
        Boolean isRead = (userId != null) ? readIds.contains(a.getAnnouncementId()) : false;

        return AnnouncementListResponse.AnnouncementItem.builder()
                .announcementId(a.getAnnouncementId())
                .title(a.getTitle())
                .category(a.getCategory())
                .isPinned(a.getIsPinned())
                .createdAt(a.getCreatedAt())
                .isNew(isNew)
                .isRead(isRead)
                .build();
    }

    /**
     * 공지사항 읽음 처리 (사용자 전용)
     * announcement_reads 테이블에 (user_id, announcement_id) 레코드를 삽입합니다.
     * UNIQUE 제약조건으로 중복 삽입을 방지하며, 이미 읽은 경우 기존 read_at을 반환합니다. (idempotent)
     *
     * @param announcementId : 공지사항 ID
     * @param userId         : JWT 인증 사용자 ID
     * @return : 읽음 처리 결과 (read_at)
     * @throws CustomException : 공지사항 미존재 시 ANNOUNCEMENT_NOT_FOUND
     * @since : 2026.04.15
     * @author : 최준혁
     */
    @Override
    @Transactional
    public AnnouncementReadResponse readAnnouncement(Long announcementId, Long userId) {
        // 공지사항 존재 여부 확인
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new CustomException(ErrorCode.ANNOUNCEMENT_NOT_FOUND));

        // 이미 읽음 처리된 내역이 있는지 확인 (idempotent)
        AnnouncementRead existingRead = announcementReadRepository.findByUserIdAndAnnouncement_AnnouncementId(userId,
                announcementId);

        if (existingRead != null) {
            return AnnouncementReadResponse.builder()
                    .readAt(existingRead.getReadAt())
                    .build();
        }

        // 새 읽음 기록 생성
        AnnouncementRead newRead = AnnouncementRead.builder()
                .userId(userId)
                .announcement(announcement)
                .build();

        AnnouncementRead saved = announcementReadRepository.save(newRead);

        return AnnouncementReadResponse.builder()
                .readAt(saved.getReadAt())
                .build();
    }

    /**
     * 공지사항 수정 (관리자 전용)
     * find 후 엔티티 update() 호출 → dirty checking으로 UPDATE 쿼리 자동 실행
     * @UpdateTimestamp에 의해 updated_at이 자동 갱신됩니다.
     *
     * @param announcementId : 수정할 공지사항 ID
     * @param request        : 수정 요청 정보
     * @return : 수정 완료된 공지사항 응답 DTO
     * @throws CustomException : 공지사항 미존재 시 ANNOUNCEMENT_NOT_FOUND
     * @since : 2026.04.15
     * @author : 최준혁
     */
    @Override
    @Transactional
    public AnnouncementUpdateResponse updateAnnouncement(Long announcementId, AnnouncementCreateRequest request) {
        /* 공지사항 존재 여부 확인 - 없으면 ANNOUNCEMENT_NOT_FOUND 예외 발생 */
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new CustomException(ErrorCode.ANNOUNCEMENT_NOT_FOUND));

        /* 엔티티 Dirty Checking으로 UPDATE 처리 */
        announcement.update(
                request.getTitle(),
                request.getCategory(),
                request.getContent(),
                request.getIsPinned()
        );

        return AnnouncementUpdateResponse.from(announcement);
    }

    /**
     * 공지사항 삭제 (관리자 전용)
     * 연관된 announcement_reads 레코드는 DB FK CASCADE DELETE로 자동 처리됩니다.
     *
     * @param announcementId : 삭제할 공지사항 ID
     * @throws CustomException : 공지사항 미존재 시 ANNOUNCEMENT_NOT_FOUND
     * @since : 2026.04.15
     * @author : 최준혁
     */
    @Override
    @Transactional
    public void deleteAnnouncement(Long announcementId) {
        /* 공지사항 존재 여부 확인 - 없으면 ANNOUNCEMENT_NOT_FOUND 예외 발생 */
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new CustomException(ErrorCode.ANNOUNCEMENT_NOT_FOUND));

        announcementRepository.delete(announcement);
    }
}
