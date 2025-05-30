package com.miniproject.rookiejangter.controller.dto;

import com.miniproject.rookiejangter.entity.Notification;
import lombok.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class NotificationDTO {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long notificationId;
        private Long userId;
        private Long entityId;
        private String entityType;
        private String message;
        private OffsetDateTime sentAt;
        private Boolean isRead;

        public static Response fromEntity(Notification notification) {
            return Response.builder()
                    .notificationId(notification.getNotificationId())
                    .userId(notification.getUser().getUserId())
                    .entityId(notification.getEntityId())
                    .entityType(notification.getEntityType())
                    .message(notification.getMessage())
                    .sentAt(notification.getSentAt() != null ? notification.getSentAt().atOffset(ZoneOffset.UTC) : null)
                    .isRead(notification.getIsRead())
                    .build();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApiResponseWrapper<T> {
        private boolean success;
        private T data;
        private Object error;
        private String message;
        private java.time.OffsetDateTime timestamp = java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC);
        private String requestId;
    }
}