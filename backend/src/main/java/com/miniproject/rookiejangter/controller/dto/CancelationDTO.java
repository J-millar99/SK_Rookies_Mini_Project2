package com.miniproject.rookiejangter.controller.dto;

import com.miniproject.rookiejangter.entity.Cancelation;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class CancelationDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private Integer cancelationReasonId;

        @Size(max = 255, message = "취소 상세는 최대 255자까지 가능합니다.")
        private String cancelationDetail;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long cancelationId;
        private Long reservationId;
        private Integer cancelationReasonId;
        private String cancelationReasonType;
        private String cancelationDetail;
        private OffsetDateTime canceledAt;

        public static Response fromEntity(Cancelation cancelation) {
            return Response.builder()
                    .cancelationId(cancelation.getCancelationId())
                    .reservationId(cancelation.getReservation().getReservationId())
                    .cancelationReasonId(cancelation.getCancelationReason().getCancelationReasonId())
                    .cancelationReasonType(cancelation.getCancelationReason().getCancelationReasonType())
                    .cancelationDetail(cancelation.getCancelationDetail())
                    .canceledAt(cancelation.getCanceledAt() != null ? cancelation.getCanceledAt().atOffset(ZoneOffset.UTC) : null)
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