package com.miniproject.rookiejangter.controller.dto;

import com.miniproject.rookiejangter.entity.Reservation;
import lombok.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class ReservationDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TradeRequest {
        private Long productId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatusUpdateRequest {
        private Reservation.TradeStatus status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long tradeId;
        private Long productId;
        private Long buyerId;
        private Long sellerId;
        private Reservation.TradeStatus status;
        private OffsetDateTime requestedAt;
        private OffsetDateTime updatedAt;

        public static Response fromEntity(Reservation reservation) {
            return Response.builder()
                    .tradeId(reservation.getReservationId())
                    .productId(reservation.getProduct().getProductId())
                    .buyerId(reservation.getBuyer().getUserId())
                    .sellerId(reservation.getSeller().getUserId())
                    .status(reservation.getStatus())
                    .requestedAt(reservation.getCreatedAt() != null ? reservation.getCreatedAt().atOffset(ZoneOffset.UTC) : null)
                    .updatedAt(reservation.getUpdatedAt() != null ? reservation.getUpdatedAt().atOffset(ZoneOffset.UTC) : null)
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
        private OffsetDateTime timestamp = OffsetDateTime.now(ZoneOffset.UTC);
        private String requestId;
    }
}