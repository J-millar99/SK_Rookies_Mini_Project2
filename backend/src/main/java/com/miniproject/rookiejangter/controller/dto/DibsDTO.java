package com.miniproject.rookiejangter.controller.dto;

import com.miniproject.rookiejangter.entity.Dibs;
import com.miniproject.rookiejangter.entity.Product;
import lombok.*;
import org.springframework.data.domain.Page;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

public class DibsDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        // 찜하기 추가/제거 API의 요청 데이터는 path parameter {product_id} 뿐이므로
        // 별도의 Request Body DTO는 필요하지 않습니다.
        private Long productId; // Path parameter로 받음
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long productId;
        private boolean isLiked;  // 요청하는 사용자 ID와 상품 ID를 기반으로 Dibs 테이블에 해당 레코드가 존재하는지 확인합니다.
        private OffsetDateTime likedAt;

        public static Response fromEntity(Dibs dibs, boolean liked) {
            return Response.builder()
                    .productId(dibs.getProduct().getProductId())
                    .isLiked(liked)
                    .likedAt(dibs.getAddedAt() != null ? dibs.getAddedAt().atOffset(ZoneOffset.UTC) : null)
                    .build();
            }
        }
        // 찜한 상품 목록 조회를 위한 응답
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DibbedProduct {
        private Long productId;
        private String title;
        private Integer price;
        private OffsetDateTime likedAt;

        public static DibbedProduct fromEntity(Dibs dibs) {
            Product product = dibs.getProduct();

            return DibbedProduct.builder()
                .productId(product.getProductId())
                .title(product.getTitle())
                .price(product.getPrice())
                .likedAt(dibs.getAddedAt() != null ? dibs.getAddedAt().atOffset(ZoneOffset.UTC) : null)
                .build();
        }
    }
    

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DibsListResponse { 
        private List<DibbedProduct> content; 
        private PaginationInfo pagination;

        public static DibsListResponse fromPage(Page<Dibs> dibsPage) {
            List<DibbedProduct> contentList = dibsPage.getContent().stream()
                    .map(DibbedProduct::fromEntity)
                    .collect(Collectors.toList());

            PaginationInfo paginationInfo = PaginationInfo.builder()
                    .page(dibsPage.getNumber())
                    .size(dibsPage.getSize())
                    .totalElements(dibsPage.getTotalElements())
                    .totalPages(dibsPage.getTotalPages())
                    .first(dibsPage.isFirst())
                    .last(dibsPage.isLast())
                    .build();
            return new DibsListResponse(contentList, paginationInfo);
        }
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaginationInfo {
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
    public static class ApiResponseWrapper<T> {
        private boolean success;
        private T data;
        private Object error;
        private String message;
        private OffsetDateTime timestamp = OffsetDateTime.now(ZoneOffset.UTC);
        private String requestId;
    }
}
