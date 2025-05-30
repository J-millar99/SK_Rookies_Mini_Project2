package com.miniproject.rookiejangter.controller.dto;

import com.miniproject.rookiejangter.entity.Review;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class ReviewDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @Min(value = 1, message = "평점은 1점 이상이어야 합니다.")
        @Max(value = 5, message = "평점은 5점 이하여야 합니다.")
        private Integer rating;

        @Size(max = 255, message = "리뷰 내용은 최대 255자까지 가능합니다.")
        private String content;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long reviewId;
        private Long tradeId;
        private Long buyerId;
        private Long sellerId;
        private Integer rating;
        private String content;
        private OffsetDateTime createdAt;

        public static Response fromEntity(Review review) {
            return Response.builder()
                    .reviewId(review.getReviewId())
                    .tradeId(review.getComplete().getCompleteId()) // Complete ID를 tradeId로 사용
                    .buyerId(review.getComplete().getBuyer().getUserId())
                    .sellerId(review.getComplete().getSeller().getUserId())
                    .rating(review.getRating())
                    .content(review.getContent())
                    .createdAt(review.getCreatedAt() != null ? review.getCreatedAt().atOffset(ZoneOffset.UTC) : null)
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