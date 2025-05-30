package com.miniproject.rookiejangter.service;

import com.miniproject.rookiejangter.controller.dto.NotificationDTO;
import com.miniproject.rookiejangter.entity.Notification;
import com.miniproject.rookiejangter.entity.User;
import com.miniproject.rookiejangter.exception.BusinessException;
import com.miniproject.rookiejangter.repository.NotificationRepository;
import com.miniproject.rookiejangter.repository.UserRepository;
import com.miniproject.rookiejangter.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User user;
    private Notification notification;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userId(1L)
                .build();

        notification = Notification.builder()
                .notificationId(10L)
                .user(user)
                .entityId(100L)
                .entityType("Product")
                .message("Test Notification")
                .sentAt(LocalDateTime.now())
                .isRead(false)
                .build();
    }

    @Test
    void createNotification_성공() {
        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        NotificationDTO.Response response = notificationService.createNotification(user.getUserId(), notification.getEntityId(), notification.getEntityType(), notification.getMessage());

        assertNotNull(response);
        assertEquals(notification.getNotificationId(), response.getNotificationId());
        assertEquals(user.getUserId(), response.getUserId());
    }

    @Test
    void createNotification_실패_USER_NOT_FOUND() {
        when(userRepository.findById(user.getUserId())).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> notificationService.createNotification(user.getUserId(), notification.getEntityId(), notification.getEntityType(), notification.getMessage()));
    }

    @Test
    void getNotificationById_성공() {
        when(notificationRepository.findByNotificationId(notification.getNotificationId())).thenReturn(Optional.of(notification));

        NotificationDTO.Response response = notificationService.getNotificationById(notification.getNotificationId());

        assertNotNull(response);
        assertEquals(notification.getNotificationId(), response.getNotificationId());
    }

    @Test
    void getNotificationById_실패_NOTIFICATION_NOT_FOUND() {
        when(notificationRepository.findByNotificationId(notification.getNotificationId())).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> notificationService.getNotificationById(notification.getNotificationId()));
    }

    @Test
    void getNotificationsByUserId_성공() {
        when(notificationRepository.findByUser_UserId(user.getUserId())).thenReturn(Arrays.asList(notification));

        List<NotificationDTO.Response> responses = notificationService.getNotificationsByUserId(user.getUserId());

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(notification.getNotificationId(), responses.get(0).getNotificationId());
    }

    @Test
    void getNotificationsByEntityId_성공() {
        when(notificationRepository.findByEntityId(notification.getEntityId())).thenReturn(Arrays.asList(notification));

        List<NotificationDTO.Response> responses = notificationService.getNotificationsByEntityId(notification.getEntityId());

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(notification.getNotificationId(), responses.get(0).getNotificationId());
    }

    @Test
    void getNotificationsByEntityType_성공() {
        when(notificationRepository.findByEntityType(notification.getEntityType())).thenReturn(Arrays.asList(notification));

        List<NotificationDTO.Response> responses = notificationService.getNotificationsByEntityType(notification.getEntityType());

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(notification.getNotificationId(), responses.get(0).getNotificationId());
    }

    @Test
    void getUnreadNotifications_성공() {
        when(notificationRepository.findByIsRead(false)).thenReturn(Arrays.asList(notification));

        List<NotificationDTO.Response> responses = notificationService.getUnreadNotifications();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(notification.getNotificationId(), responses.get(0).getNotificationId());
    }

    @Test
    void markAsRead_성공() {
        when(notificationRepository.findByNotificationId(notification.getNotificationId())).thenReturn(Optional.of(notification));

        notificationService.markAsRead(notification.getNotificationId());

        assertTrue(notification.getIsRead());
    }

    @Test
    void markAsRead_실패_NOTIFICATION_NOT_FOUND() {
        when(notificationRepository.findByNotificationId(notification.getNotificationId())).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> notificationService.markAsRead(notification.getNotificationId()));
    }

    @Test
    void deleteNotification_성공() {
        when(notificationRepository.findByNotificationId(notification.getNotificationId())).thenReturn(Optional.of(notification));

        notificationService.deleteNotification(notification.getNotificationId());

        verify(notificationRepository).delete(notification);
    }

    @Test
    void deleteNotification_실패_NOTIFICATION_NOT_FOUND() {
        when(notificationRepository.findByNotificationId(notification.getNotificationId())).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> notificationService.deleteNotification(notification.getNotificationId()));
    }
}