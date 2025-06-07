package com.miniproject.rookiejangter.controller.dto;

import com.miniproject.rookiejangter.entity.Chat;
import lombok.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class ChatDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private Long sellerId;
        private Long productId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long chatRoomId;
        private Long productId;
        private Long buyerId;
        private Long sellerId;
        private OffsetDateTime createdAt;

        public static Response fromEntity(Chat chat) {
            return Response.builder()
                    .chatRoomId(chat.getChatId())
                    .productId(chat.getProduct().getProductId())
                    .buyerId(chat.getBuyer().getUserId())
                    .sellerId(chat.getSeller().getUserId())
                    .createdAt(chat.getCreatedAt().atOffset(ZoneOffset.UTC))
                    .build();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChatListResponse {
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
        private List<ChatInfo> content;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class ChatInfo {
            private Long chatRoomId;
            private Long productId;
            private String lastMessage;
            private Integer unreadCount;
            private OffsetDateTime updatedAt;

            public static ChatInfo fromEntity(Chat chat, String lastMessage, Integer unreadCount) {
                return ChatInfo.builder()
                        .chatRoomId(chat.getChatId())
                        .productId(chat.getProduct().getProductId())
                        .lastMessage(lastMessage)
                        .unreadCount(unreadCount)
                        .updatedAt(chat.getUpdatedAt() != null ? chat.getUpdatedAt().atOffset(ZoneOffset.UTC) : chat.getCreatedAt().atOffset(ZoneOffset.UTC)) // updatedAt 없을 경우 createdAt 사용
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
        private String message;
        private OffsetDateTime timestamp = OffsetDateTime.now(ZoneOffset.UTC);
        private String requestId;
    }
}