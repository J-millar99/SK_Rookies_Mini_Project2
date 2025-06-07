package com.miniproject.rookiejangter.controller;

import com.miniproject.rookiejangter.controller.dto.ProductDTO;
import com.miniproject.rookiejangter.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    // 상품 등록
    @PostMapping(value = "/products", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<ProductDTO.Response> createProduct(
            @Valid @ModelAttribute ProductDTO.Request request,
            Authentication authentication) {

        Long userId = Long.parseLong(authentication.getName());
        ProductDTO.Response response = productService.createProduct(request, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 현재 유저가 등록한 모든 상품 조회
    @GetMapping("/products")
    public ResponseEntity<ProductDTO.ProductListData> getUserProducts(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        ProductDTO.ProductListData response = productService.getProductsByUser(userId, pageable, userId);
        return ResponseEntity.ok(response);
    }

    // 현재 유저가 등록한 특정 상품 상세 조회
    @GetMapping("/products/{product_id}")
    public ResponseEntity<ProductDTO.Response> getUserProduct(
            @PathVariable("product_id") Long productId,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        ProductDTO.Response response = productService.getUserProductById(productId, userId);
        return ResponseEntity.ok(response);
    }

    // 현재 유저가 등록한 상품 수정
    @PutMapping("/products/{product_id}")
    public ResponseEntity<ProductDTO.Response> updateUserProduct(
            @PathVariable("product_id") Long productId,
            @Valid @RequestBody ProductDTO.UpdateRequest request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        ProductDTO.Response response = productService.updateProduct(productId, request, userId);
        return ResponseEntity.ok(response);
    }

    // 현재 유저가 등록한 상품 삭제
    @DeleteMapping("/products/{product_id}")
    public ResponseEntity<Void> deleteUserProduct(
            @PathVariable("product_id") Long productId,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        productService.deleteProduct(productId, userId);
        return ResponseEntity.noContent().build();
    }

    // 현재 유저가 등록한 상품 상태 변경 (예약중/판매완료)
    @PutMapping("/products/{product_id}/status")
    public ResponseEntity<Void> updateProductStatus(
            @PathVariable("product_id") Long productId,
            @RequestBody ProductDTO.StatusUpdateRequest request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        productService.updateProductStatus(productId, request.getIsReserved(), request.getIsCompleted(), userId);
        return ResponseEntity.ok().build();
    }
}
