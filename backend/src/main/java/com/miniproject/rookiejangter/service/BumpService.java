package com.miniproject.rookiejangter.service;

import com.miniproject.rookiejangter.controller.dto.BumpDTO;
import com.miniproject.rookiejangter.entity.Bump;
import com.miniproject.rookiejangter.entity.Product;
import com.miniproject.rookiejangter.repository.BumpRepository;
import com.miniproject.rookiejangter.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BumpService {

    private final BumpRepository bumpRepository;
    private final ProductRepository productRepository;
    private static final int MAX_BUMPS_PER_DAY = 5; // 일일 최대 끌어올리기 횟수 (예시)

    @Transactional
    public BumpDTO.Response bumpProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다: " + productId));

        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        long bumpsToday = bumpRepository.countByProduct_ProductIdAndBumpedAtBetween(productId, todayStart, todayEnd); //
        if (bumpsToday >= MAX_BUMPS_PER_DAY) {
            throw new IllegalStateException("오늘 해당 게시글의 끌어올리기 가능 횟수를 초과했습니다.");
        }

        Optional<Bump> latestBumpOpt = bumpRepository.findTopByProduct_ProductIdOrderByBumpedAtDesc(productId); //
        int nextBumpCount = latestBumpOpt.map(bump -> bump.getBumpCount() + 1).orElse(1);

        Bump newBump = Bump.builder()
                .product(product)
                .bumpedAt(LocalDateTime.now())
                .bumpCount(nextBumpCount)
                .build();
        Bump savedBump = bumpRepository.save(newBump);

        product.setIsBumped(true);
        // product.setLastBumpedAt(savedBump.getBumpedAt()); // Product 엔티티에 lastBumpedAt 필드가 있다면 설정
        productRepository.save(product);

        return BumpDTO.Response.fromEntity(savedBump);
    }

    @Transactional(readOnly = true)
    public Optional<BumpDTO.Response> getLatestBumpForProduct(Long productId) {
        return bumpRepository.findTopByProduct_ProductIdOrderByBumpedAtDesc(productId) //
                .map(BumpDTO.Response::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<BumpDTO.Response> getBumpsForProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new EntityNotFoundException("게시글을 찾을 수 없습니다: " + productId);
        }
        return bumpRepository.findByProduct_ProductId(productId).stream() //
                .map(BumpDTO.Response::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Long getTodaysBumpCountForProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new EntityNotFoundException("게시글을 찾을 수 없습니다: " + productId);
        }
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        return bumpRepository.countByProduct_ProductIdAndBumpedAtBetween(productId, todayStart, todayEnd); //
    }
}