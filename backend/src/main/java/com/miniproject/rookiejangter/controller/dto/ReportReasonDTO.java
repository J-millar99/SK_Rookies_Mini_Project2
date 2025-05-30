package com.miniproject.rookiejangter.controller.dto;

import com.miniproject.rookiejangter.entity.ReportReason;
import jakarta.validation.constraints.Size;
import lombok.*;

public class ReportReasonDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @Size(max = 50, message = "신고 사유 타입은 최대 50자까지 가능합니다.")
        private String reportReasonType;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Integer reportReasonId;
        private String reportReasonType;

        public static Response fromEntity(ReportReason reportReason) {
            return Response.builder()
                    .reportReasonId(reportReason.getReportReasonId())
                    .reportReasonType(reportReason.getReportReasonType())
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