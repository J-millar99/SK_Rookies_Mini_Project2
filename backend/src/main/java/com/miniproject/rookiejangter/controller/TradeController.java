package com.miniproject.rookiejangter.controller;

import com.miniproject.rookiejangter.controller.dto.ReviewDTO;
import com.miniproject.rookiejangter.service.ProductService;
import com.miniproject.rookiejangter.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
public class TradeController {

    private final ProductService productService;
    private final ReviewService reviewService;

    @PutMapping("/{productId}/complete")
    public ResponseEntity<Void> completeTrade(@PathVariable Long productId, Authentication authentication) {
        Long sellerId = Long.parseLong(authentication.getName());
        productService.updateProductStatus(productId, null, true, sellerId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{completeId}/review")
    public ResponseEntity<ReviewDTO.Response> createReview(
            @PathVariable Long completeId,
            @Valid @RequestBody ReviewDTO.Request request,
            Authentication authentication
    ) {
        Long buyerId = Long.parseLong(authentication.getName());
        ReviewDTO.Response response = reviewService.createReview(completeId, buyerId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
