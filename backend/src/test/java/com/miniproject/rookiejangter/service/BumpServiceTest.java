package com.miniproject.rookiejangter.service;

import com.miniproject.rookiejangter.controller.dto.BumpDTO;
import com.miniproject.rookiejangter.entity.Bump;
import com.miniproject.rookiejangter.entity.Product;
import com.miniproject.rookiejangter.exception.BusinessException;
import com.miniproject.rookiejangter.exception.ErrorCode;
import com.miniproject.rookiejangter.repository.BumpRepository;
import com.miniproject.rookiejangter.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BumpServiceTest {

    @Mock
    private BumpRepository bumpRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private BumpService bumpService;

    private final int MAX_BUMPS_PER_DAY = 5;

    @Test
    @DisplayName("게시글 끌어올리기 성공 테스트")
    void bumpProductSuccessTest() {
        // Given
        Long productId = 1L;
        Product product = Product.builder().productId(productId).isBumped(false).build();
        Bump savedBump = Bump.builder()
                .bumpId(1L)
                .product(product)
                .bumpedAt(LocalDateTime.now())
                .bumpCount(1)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(bumpRepository.countByProduct_ProductIdAndBumpedAtBetween(eq(productId), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(0L);
        when(bumpRepository.findTopByProduct_ProductIdOrderByBumpedAtDesc(productId)).thenReturn(Optional.empty());
        when(bumpRepository.save(any(Bump.class))).thenReturn(savedBump);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        BumpDTO.Response response = bumpService.bumpProduct(productId);

        // Then
        assertThat(response.getBumpCount()).isEqualTo(1);
        assertThat(response.getProductId()).isEqualTo(productId);
        verify(productRepository, times(1)).findById(productId);
        verify(bumpRepository, times(1)).countByProduct_ProductIdAndBumpedAtBetween(eq(productId), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(bumpRepository, times(1)).findTopByProduct_ProductIdOrderByBumpedAtDesc(productId);
        verify(bumpRepository, times(1)).save(any(Bump.class));
        verify(productRepository, times(1)).save(product);
        assertThat(product.getIsBumped()).isTrue();
    }

    @Test
    @DisplayName("게시글 끌어올리기 실패 테스트 (게시글 없음)")
    void bumpProductNotFoundFailTest() {
        // Given
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> bumpService.bumpProduct(productId));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND.formatMessage(productId));
        verify(productRepository, times(1)).findById(productId);
        verify(bumpRepository, never()).countByProduct_ProductIdAndBumpedAtBetween(anyLong(), any(), any());
        verify(bumpRepository, never()).findTopByProduct_ProductIdOrderByBumpedAtDesc(anyLong());
        verify(bumpRepository, never()).save(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("게시글 끌어올리기 실패 테스트 (오늘 최대 끌어올리기 횟수 초과)")
    void bumpProductMaxBumpsExceededFailTest() {
        // Given
        Long productId = 1L;
        Product product = Product.builder().productId(productId).build();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(bumpRepository.countByProduct_ProductIdAndBumpedAtBetween(eq(productId), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn((long) MAX_BUMPS_PER_DAY);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> bumpService.bumpProduct(productId));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_CANNOT_BUMP);
        verify(productRepository, times(1)).findById(productId);
        verify(bumpRepository, times(1)).countByProduct_ProductIdAndBumpedAtBetween(eq(productId), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(bumpRepository, never()).findTopByProduct_ProductIdOrderByBumpedAtDesc(anyLong());
        verify(bumpRepository, never()).save(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("특정 게시글의 최신 끌어올리기 정보 조회 성공 테스트 (존재)")
    void getLatestBumpForProductSuccessTest() {
        // Given
        Long productId = 1L;
        Product product = Product.builder().productId(productId).build();
        Bump latestBump = Bump.builder()
                .bumpId(1L)
                .product(product)
                .bumpedAt(LocalDateTime.now())
                .bumpCount(2)
                .build();
        when(bumpRepository.findTopByProduct_ProductIdOrderByBumpedAtDesc(productId)).thenReturn(Optional.of(latestBump));

        // When
        Optional<BumpDTO.Response> responseOptional = bumpService.getLatestBumpForProduct(productId);

        // Then
        assertThat(responseOptional).isPresent();
        BumpDTO.Response response = responseOptional.get();
        assertThat(response.getBumpId()).isEqualTo(1L);
        assertThat(response.getProductId()).isEqualTo(productId);
        assertThat(response.getBumpCount()).isEqualTo(2);
        verify(bumpRepository, times(1)).findTopByProduct_ProductIdOrderByBumpedAtDesc(productId);
    }

    @Test
    @DisplayName("특정 게시글의 최신 끌어올리기 정보 조회 성공 테스트 (없음)")
    void getLatestBumpForProductNotFoundTest() {
        // Given
        Long productId = 1L;
        when(bumpRepository.findTopByProduct_ProductIdOrderByBumpedAtDesc(productId)).thenReturn(Optional.empty());

        // When
        Optional<BumpDTO.Response> responseOptional = bumpService.getLatestBumpForProduct(productId);

        // Then
        assertThat(responseOptional).isEmpty();
        verify(bumpRepository, times(1)).findTopByProduct_ProductIdOrderByBumpedAtDesc(productId);
    }

    @Test
    @DisplayName("특정 게시글의 모든 끌어올리기 기록 조회 성공 테스트")
    void getBumpsForProductSuccessTest() {
        // Given
        Long productId = 1L;
        Product product = Product.builder().productId(productId).build();
        List<Bump> bumps = Arrays.asList(
                Bump.builder().bumpId(1L).product(product).bumpedAt(LocalDateTime.now().minusHours(2)).bumpCount(1).build(),
                Bump.builder().bumpId(2L).product(product).bumpedAt(LocalDateTime.now().minusHours(1)).bumpCount(2).build()
        );
        when(productRepository.existsById(productId)).thenReturn(true);
        when(bumpRepository.findByProduct_ProductId(productId)).thenReturn(bumps);

        // When
        List<BumpDTO.Response> responses = bumpService.getBumpsForProduct(productId);

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getProductId()).isEqualTo(productId);
        assertThat(responses.get(1).getProductId()).isEqualTo(productId);
        verify(productRepository, times(1)).existsById(productId);
        verify(bumpRepository, times(1)).findByProduct_ProductId(productId);
    }

    @Test
    @DisplayName("특정 게시글의 모든 끌어올리기 기록 조회 실패 테스트 (게시글 없음)")
    void getBumpsForProductNotFoundFailTest() {
        // Given
        Long productId = 1L;
        when(productRepository.existsById(productId)).thenReturn(false);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> bumpService.getBumpsForProduct(productId));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND.formatMessage(productId));
        verify(productRepository, times(1)).existsById(productId);
        verify(bumpRepository, never()).findByProduct_ProductId(anyLong());
    }

    @Test
    @DisplayName("특정 게시글의 오늘 끌어올린 횟수 조회 성공 테스트")
    void getTodaysBumpCountForProductSuccessTest() {
        // Given
        Long productId = 1L;
        when(productRepository.existsById(productId)).thenReturn(true);
        when(bumpRepository.countByProduct_ProductIdAndBumpedAtBetween(eq(productId), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(3L);

        // When
        Long count = bumpService.getTodaysBumpCountForProduct(productId);

        // Then
        assertThat(count).isEqualTo(3L);
        verify(productRepository, times(1)).existsById(productId);
        verify(bumpRepository, times(1)).countByProduct_ProductIdAndBumpedAtBetween(eq(productId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("특정 게시글의 오늘 끌어올린 횟수 조회 실패 테스트 (게시글 없음)")
    void getTodaysBumpCountForProductNotFoundFailTest() {
        // Given
        Long productId = 1L;
        when(productRepository.existsById(productId)).thenReturn(false);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> bumpService.getTodaysBumpCountForProduct(productId));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND.formatMessage(productId));
        verify(productRepository, times(1)).existsById(productId);
        verify(bumpRepository, never()).countByProduct_ProductIdAndBumpedAtBetween(anyLong(), any(), any());
    }
}
