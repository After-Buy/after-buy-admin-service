package com.After_Buy.AdminService.Service;

import com.After_Buy.AdminService.Dto.Request.AnnouncementCreateRequest;
import com.After_Buy.AdminService.Dto.Response.AnnouncementCreateResponse;
import com.After_Buy.AdminService.Entity.Announcement;
import com.After_Buy.AdminService.Repository.AnnouncementRepository;
import com.After_Buy.AdminService.Client.NotificationInternalClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                saved.getAnnouncementId()
        );

        // 명세서에 따라 응답 시 push_sent 는 true로 고정하여 반환 (비동기이므로 요청 발송 여부만 알림)
        return AnnouncementCreateResponse.from(saved, true);
    }
}
