package com.miniproject.rookiejangter.repository;

import com.miniproject.rookiejangter.entity.Notification;
import com.miniproject.rookiejangter.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User user1;
    private User user2;
    private Notification notification1;
    private Notification notification2;
    private Notification notification3;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .userName("사용자1")
                .loginId("user1")
                .password("password")
                .phone("010-1234-1234")
                .build();
        entityManager.persist(user1);

        user2 = User.builder()
                .userName("사용자2")
                .loginId("user2")
                .password("password")
                .phone("010-5678-5678")
                .build();
        entityManager.persist(user2);
        entityManager.flush();

        notification1 = Notification.builder()
                .user(user1)
                .message("새로운 채팅이 도착했습니다.")
                .entityType("Chat")
                .entityId(1L)
                .sentAt(LocalDateTime.now())
                .isRead(false)
                .build();
        entityManager.persist(notification1);

        notification2 = Notification.builder()
                .user(user1)
                .message("예약이 완료되었습니다.")
                .entityType("Reservation")
                .entityId(2L)
                .sentAt(LocalDateTime.now().plusMinutes(5))
                .isRead(false)
                .build();
        entityManager.persist(notification2);

        notification3 = Notification.builder()
                .user(user2)
                .message("관심 목록에 추가되었습니다.")
                .entityType("Dibs")
                .entityId(3L)
                .sentAt(LocalDateTime.now().plusMinutes(10))
                .isRead(false)
                .build();
        entityManager.persist(notification3);
        entityManager.flush();
    }

    @Test
    void saveNotification() {
        Notification newNotification = Notification.builder()
                .user(user2)
                .message("리뷰가 작성되었습니다.")
                .entityType("Review")
                .entityId(4L)
                .sentAt(LocalDateTime.now().plusHours(1))
                .isRead(true)
                .build();
        Notification savedNotification = notificationRepository.save(newNotification);

        Optional<Notification> foundNotification = notificationRepository.findById(savedNotification.getNotificationId());
        assertThat(foundNotification).isPresent();
        assertThat(foundNotification.get().getMessage()).isEqualTo("리뷰가 작성되었습니다.");
        assertThat(foundNotification.get().getUser().getUserId()).isEqualTo(user2.getUserId());
        assertThat(foundNotification.get().getEntityType()).isEqualTo("Review");
        assertThat(foundNotification.get().getEntityId()).isEqualTo(4L);
        assertThat(foundNotification.get().getIsRead()).isTrue();
    }

    @Test
    void findByNotificationId() {
        Optional<Notification> foundNotification = notificationRepository.findById(notification1.getNotificationId());
        assertThat(foundNotification).isPresent();
        assertThat(foundNotification.get().getMessage()).isEqualTo("새로운 채팅이 도착했습니다.");
        assertThat(foundNotification.get().getUser().getUserId()).isEqualTo(user1.getUserId());
    }

    @Test
    void findByUserId() {
        List<Notification> foundNotifications = notificationRepository.findByUser_UserId(user1.getUserId());
        assertThat(foundNotifications).hasSize(2);
        assertThat(foundNotifications).extracting(Notification::getMessage)
                .containsExactlyInAnyOrder("새로운 채팅이 도착했습니다.", "예약이 완료되었습니다.");
    }

    @Test
    void updateIsRead() {
        Notification unreadNotification = Notification.builder()
                .user(user1)
                .message("새로운 알림입니다.")
                .entityType("Report")
                .entityId(5L)
                .sentAt(LocalDateTime.now().plusMinutes(15))
                .isRead(false)
                .build();
        Notification savedUnreadNotification = entityManager.persist(unreadNotification);
        entityManager.flush();

        notificationRepository.updateIsReadByNotificationId(true, savedUnreadNotification.getNotificationId());
        entityManager.clear();

        Optional<Notification> updatedNotification = notificationRepository.findById(savedUnreadNotification.getNotificationId());
        assertThat(updatedNotification).isPresent();
        assertThat(updatedNotification.get().getIsRead()).isTrue();
    }

    @Test
    void findByIsRead() {
        Notification unreadNotification = Notification.builder()
                .user(user1)
                .message("읽음 여부 테스트용 알림입니다")
                .entityType("Report")
                .entityId(6L)
                .sentAt(LocalDateTime.now().plusMinutes(30))
                .isRead(false)
                .build();
        Notification savedUnreadNotification = entityManager.persist(unreadNotification);
        entityManager.flush();

        Optional<Notification> updatedNotification = notificationRepository.findById(savedUnreadNotification.getNotificationId());
        assertThat(updatedNotification).isPresent();
        assertThat(updatedNotification.get().getIsRead()).isFalse();

    }
}