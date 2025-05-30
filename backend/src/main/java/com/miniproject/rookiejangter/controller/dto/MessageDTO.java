package com.miniproject.rookiejangter.controller.dto;

import com.miniproject.rookiejangter.entity.Message;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class MessageDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotBlank(message = "내용은 필수입니다.")
        @Size(max = 255, message = "내용은 최대 255자까지 가능합니다.")
        private String content; // 메시지 전송 API 요청 바디
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long messageId;
        private Long chatRoomId;
        private Long senderId;
        private String content;
        private OffsetDateTime sentAt;
        private Boolean isRead;

        public static Response fromEntity(Message message, Long chatRoomId) {
            return Response.builder()
                    .messageId(message.getMessageId())
                    .chatRoomId(chatRoomId)
                    .senderId(message.getUser().getUserId())
                    .content(message.getContent())
                    .sentAt(message.getSentAt() != null ? message.getSentAt().atOffset(ZoneOffset.UTC) : null)
                    .isRead(false)
                    .build();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MessageListResponse {
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
        private java.util.List<MessageResponse> content;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class MessageResponse {
            private Long messageId;
            private Long senderId;
            private String content;
            private OffsetDateTime sentAt;
            private Boolean isRead;

            public static MessageResponse fromEntity(Message message) {
                return MessageResponse.builder()
                        .messageId(message.getMessageId())
                        .senderId(message.getUser().getUserId())
                        .content(message.getContent())
                        .sentAt(message.getSentAt() != null ? message.getSentAt().atOffset(ZoneOffset.UTC) : null)
                        .isRead(false)
                        .build();
            }
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
        private String content;
        private OffsetDateTime timestamp = OffsetDateTime.now(ZoneOffset.UTC);
        private String requestId;
    }
}