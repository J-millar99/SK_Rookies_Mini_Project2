package com.miniproject.rookiejangter.controller.dto;

import com.miniproject.rookiejangter.entity.Image;
import com.miniproject.rookiejangter.entity.Product;
import com.miniproject.rookiejangter.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

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

        private List<String> images; // 이미지 URL 목록 (요청 시)
    }
    // 역할: 상품 등록 API의 요청 데이터를 담는 DTO입니다.

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

        private List<String> images; // 이미지 URL 목록 (수정 시)
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
    public static class ImageResponse {
        private Long imageId;
        private String imageUrl;

        public static ImageResponse fromEntity(Image image) {
            return ImageResponse.builder()
                    .imageId(image.getImageId())
                    .imageUrl(image.getImageUrl())
                    .build();
        }
    }
    // 역할: 이미지 정보를 응답하는 DTO입니다.

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
        private String status;
        private List<ImageResponse> images;
        private SellerInfo seller;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
        private Integer viewCount;
        private Integer likeCount;
        private Boolean isLiked;

        public static Response fromEntity(Product product, List<Image> imageList) {
            List<ImageResponse> imageResponses = imageList.stream()
                    .map(ImageResponse::fromEntity)
                    .collect(Collectors.toList());

            return Response.builder()
                    .id(product.getProductId())
                    .title(product.getTitle())
                    .content(product.getContent())
                    .price(product.getPrice())
                    .categoryName(product.getCategory() != null ? product.getCategory().getCategoryName() : null)
                    .status(product.getIsCompleted() != null && product.getIsCompleted() ? "COMPLETED" :
                            product.getIsReserved() != null && product.getIsReserved() ? "RESERVED" : "SALE")
                    .images(imageResponses)
                    .seller(SellerInfo.fromEntity(product.getUser()))
                    .createdAt(product.getCreatedAt().atOffset(ZoneOffset.UTC))
                    .updatedAt(product.getUpdatedAt() != null ? product.getUpdatedAt().atOffset(ZoneOffset.UTC) : null)
                    .viewCount(product.getViewCount())
                    .likeCount(null) // 실제 구현 필요
                    .isLiked(false) // 실제 구현 필요 (로그인 시)
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
}