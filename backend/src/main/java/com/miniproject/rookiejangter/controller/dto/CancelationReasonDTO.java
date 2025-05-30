package com.miniproject.rookiejangter.controller.dto;

import com.miniproject.rookiejangter.entity.CancelationReason;
import jakarta.validation.constraints.Size;
import lombok.*;

public class CancelationReasonDTO {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @Size(max = 50, message = "취소 사유 타입은 최대 50자까지 가능합니다.")
        private String cancelationReasonType;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Integer cancelationReasonId;
        private String cancelationReasonType;

        public static Response fromEntity(CancelationReason cancelationReason) {
            return Response.builder()
                    .cancelationReasonId(cancelationReason.getCancelationReasonId())
                    .cancelationReasonType(cancelationReason.getCancelationReasonType())
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