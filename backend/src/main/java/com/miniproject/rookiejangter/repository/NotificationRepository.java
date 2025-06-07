package com.miniproject.rookiejangter.repository;

import com.miniproject.rookiejangter.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Optional<Notification> findByNotificationId(Long notificationId);
    Page<Notification> findByUser_UserId(Long userUserId, Pageable pageable);
    List<Notification> findByEntityId(Long entityId);
    List<Notification> findByEntityType(String entityType);
    List<Notification> findByIsRead(Boolean isRead);
    long countByUser_UserIdAndIsRead(Long userUserId, Boolean isRead);

    @Transactional
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = :isRead WHERE n.notificationId = :notificationId")
    void updateIsReadByNotificationId(Boolean isRead, Long notificationId);
}