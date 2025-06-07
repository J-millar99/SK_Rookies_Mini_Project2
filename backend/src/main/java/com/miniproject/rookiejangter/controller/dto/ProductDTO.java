package com.miniproject.rookiejangter.controller.dto;

import com.miniproject.rookiejangter.entity.Product;
import com.miniproject.rookiejangter.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class ProductDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 50, message = "제목은 최대 50자까지 가능합니다.")
        private String title;

        @NotBlank(message = "내용은 필수입니다.")
        @Size(max = 255, message = "내용은 최대 255자까지 가능합니다.")
        private String content;

        @NotNull(message = "가격은 필수입니다.")
        private Integer price;

        @NotNull(message = "카테고리 ID는 필수입니다.")
        private Integer categoryId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        @Size(max = 50, message = "제목은 최대 50자까지 가능합니다.")
        private String title;

        @Size(max = 255, message = "내용은 최대 255자까지 가능합니다.")
        private String content;

        private Integer price;

        private Integer categoryId;
    }
    // 역할: 상품 수정 API의 요청 데이터를 담는 DTO입니다.

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SellerInfo {
        private Long id;
        private String userName;
        private UserDTO.AreaInfo area;

        public static SellerInfo fromEntity(User user) {
            if (user == null) {
                return null;
            }
            return SellerInfo.builder()
                    .id(user.getUserId())
                    .userName(user.getUserName())
                    .area(UserDTO.AreaInfo.fromEntity(user.getArea()))
                    .build();
        }
    }
    // 역할: 상품 목록 및 상세 조회 시 판매자 정보를 담아 응답하는 DTO입니다.

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String title;
        private String content;
        private Integer price;
        private String categoryName;
        private SellerInfo seller;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
        private Integer viewCount;
        private Boolean isBumped;
        private Boolean isReserved;
        private Boolean isCompleted;

        public static Response fromEntity(Product product) {

            return Response.builder()
                    .id(product.getProductId())
                    .title(product.getTitle())
                    .content(product.getContent())
                    .price(product.getPrice())
                    .categoryName(product.getCategory() != null ? product.getCategory().getCategoryName() : null)
                    .seller(SellerInfo.fromEntity(product.getUser()))
                    .createdAt(product.getCreatedAt().atOffset(ZoneOffset.UTC))
                    .updatedAt(product.getUpdatedAt() != null ? product.getUpdatedAt().atOffset(ZoneOffset.UTC) : null)
                    .viewCount(product.getViewCount())
                    .isBumped(product.getIsBumped() != null ? product.getIsBumped() : false)
                    .isReserved(product.getIsReserved() != null ? product.getIsReserved() : false)
                    .isCompleted(product.getIsCompleted() != null ? product.getIsCompleted() : false)
                    .build();
        }
    }
    // 역할: 상품 목록 조회 및 상세 조회 API의 응답 데이터를 담는 DTO입니다.
    // 모든 가능한 필드를 포함합니다.

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductListPagination {
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductListData {
        private ProductListPagination pagination;
        private List<Response> content;
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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatusUpdateRequest {
        private Boolean isReserved;  // 예약중 상태
        private Boolean isCompleted; // 판매완료 상태
    }

}