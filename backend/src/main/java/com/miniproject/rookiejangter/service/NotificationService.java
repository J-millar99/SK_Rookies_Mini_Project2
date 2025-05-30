package com.miniproject.rookiejangter.service;

import com.miniproject.rookiejangter.controller.dto.NotificationDTO;
import com.miniproject.rookiejangter.entity.Notification;
import com.miniproject.rookiejangter.entity.User;
import com.miniproject.rookiejangter.exception.BusinessException;
import com.miniproject.rookiejangter.exception.ErrorCode;
import com.miniproject.rookiejangter.repository.NotificationRepository;
import com.miniproject.rookiejangter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * 알림 생성
     * @param userId
     * @param entityId
     * @param entityType
     * @param message
     * @return
     */
    public NotificationDTO.Response createNotification(Long userId, Long entityId, String entityType, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, userId));

        Notification notification = Notification.builder()
                .user(user)
                .entityId(entityId)
                .entityType(entityType)
                .message(message)
                .sentAt(LocalDateTime.now())
                .isRead(false)
                .build();

        Notification savedNotification = notificationRepository.save(notification);
        return NotificationDTO.Response.fromEntity(savedNotification);
    }

    /**
     * 알림 ID로 조회
     * @param notificationId
     * @return
     */
    public NotificationDTO.Response getNotificationById(Long notificationId) {
        Notification notification = notificationRepository.findByNotificationId(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Notification", notificationId));

        return NotificationDTO.Response.fromEntity(notification);
    }

    /**
     * 유저 ID로 알림 목록 조회
     * @param userId
     * @return
     */
    public List<NotificationDTO.Response> getNotificationsByUserId(Long userId) {
        List<Notification> notifications = notificationRepository.findByUser_UserId(userId);

        return notifications.stream()
                .map(NotificationDTO.Response::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 엔티티 ID로 알림 목록 조회
     * @param entityId
     * @return
     */
    public List<NotificationDTO.Response> getNotificationsByEntityId(Long entityId) {
        List<Notification> notifications = notificationRepository.findByEntityId(entityId);

        return notifications.stream()
                .map(NotificationDTO.Response::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 엔티티 타입으로 알림 목록 조회
     * @param entityType
     * @return
     */
    public List<NotificationDTO.Response> getNotificationsByEntityType(String entityType) {
        List<Notification> notifications = notificationRepository.findByEntityType(entityType);

        return notifications.stream()
                .map(NotificationDTO.Response::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 읽지 않은 알림 목록 조회
     * @return
     */
    public List<NotificationDTO.Response> getUnreadNotifications() {
        List<Notification> notifications = notificationRepository.findByIsRead(false);

        return notifications.stream()
                .map(NotificationDTO.Response::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 알림 읽음 처리
     * @param notificationId
     */
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findByNotificationId(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Notification", notificationId));

        notification.setIsRead(true);
    }

    /**
     * 알림 삭제
     * @param notificationId
     */
    public void deleteNotification(Long notificationId) {
        Notification notification = notificationRepository.findByNotificationId(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Notification", notificationId));

        notificationRepository.delete(notification);
    }
}