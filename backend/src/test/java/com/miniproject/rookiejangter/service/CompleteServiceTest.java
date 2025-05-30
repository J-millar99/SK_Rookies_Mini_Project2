package com.miniproject.rookiejangter.service;

import com.miniproject.rookiejangter.controller.dto.CompleteDTO;
import com.miniproject.rookiejangter.entity.Complete;
import com.miniproject.rookiejangter.entity.Product;
import com.miniproject.rookiejangter.entity.User;
import com.miniproject.rookiejangter.repository.CompleteRepository;
import com.miniproject.rookiejangter.repository.ProductRepository;
import com.miniproject.rookiejangter.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CompleteServiceTest {

    @Mock
    private CompleteRepository completeRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CompleteService completeService;

    @Test
    @DisplayName("거래 완료 생성 성공 테스트")
    void createCompleteSuccessTest() {
        // Given
        Long productId = 10L;
        Long buyerId = 2L;
        Long sellerId = 1L;
        Product product = Product.builder().productId(productId).title("테스트 상품").price(10000).build();
        User buyer = User.builder().userId(buyerId).build();
        User seller = User.builder().userId(sellerId).build();
        Complete savedComplete = Complete.builder()
                .completeId(productId)
                .product(product)
                .buyer(buyer)
                .seller(seller)
                .completedAt(LocalDateTime.now())
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
        when(userRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(completeRepository.findByProduct_ProductId(productId)).thenReturn(Optional.empty());
        when(completeRepository.save(any(Complete.class))).thenReturn(savedComplete);

        // When
        CompleteDTO.Response response = completeService.createComplete(productId, buyerId, sellerId);

        // Then
        assertThat(response.getProductId()).isEqualTo(productId);
        assertThat(response.getBuyerId()).isEqualTo(buyerId);
        assertThat(response.getSellerId()).isEqualTo(sellerId);
        assertThat(response.getCompletedAt()).isNotNull();
        verify(productRepository, times(1)).findById(productId);
        verify(userRepository, times(1)).findById(buyerId);
        verify(userRepository, times(1)).findById(sellerId);
        verify(completeRepository, times(1)).findByProduct_ProductId(productId);
        verify(completeRepository, times(1)).save(any(Complete.class));
    }

    @Test
    @DisplayName("거래 완료 생성 실패 테스트 - 게시글 없음")
    void createCompleteProductNotFoundFailTest() {
        // Given
        Long productId = 10L;
        Long buyerId = 2L;
        Long sellerId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> completeService.createComplete(productId, buyerId, sellerId));
        verify(productRepository, times(1)).findById(productId);
        verify(userRepository, never()).findById(anyLong());
        verify(completeRepository, never()).findByProduct_ProductId(anyLong());
        verify(completeRepository, never()).save(any());
    }

    @Test
    @DisplayName("거래 완료 생성 실패 테스트 - 구매자 없음")
    void createCompleteBuyerNotFoundFailTest() {
        // Given
        Long productId = 10L;
        Long buyerId = 2L;
        Long sellerId = 1L;
        Product product = Product.builder().productId(productId).build();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(userRepository.findById(buyerId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> completeService.createComplete(productId, buyerId, sellerId));
        verify(productRepository, times(1)).findById(productId);
        verify(userRepository, times(1)).findById(buyerId);
        verify(userRepository, never()).findById(sellerId);
        verify(completeRepository, never()).findByProduct_ProductId(anyLong());
        verify(completeRepository, never()).save(any());
    }

    @Test
    @DisplayName("거래 완료 생성 실패 테스트 - 판매자 없음")
    void createCompleteSellerNotFoundFailTest() {
        // Given
        Long productId = 10L;
        Long buyerId = 2L;
        Long sellerId = 1L;
        Product product = Product.builder().productId(productId).build();
        User buyer = User.builder().userId(buyerId).build();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
        when(userRepository.findById(sellerId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> completeService.createComplete(productId, buyerId, sellerId));
        verify(productRepository, times(1)).findById(productId);
        verify(userRepository, times(1)).findById(buyerId);
        verify(userRepository, times(1)).findById(sellerId);
        verify(completeRepository, never()).findByProduct_ProductId(anyLong());
        verify(completeRepository, never()).save(any());
    }

    @Test
    @DisplayName("거래 완료 생성 실패 테스트 - 이미 거래 완료된 게시글")
    void createCompleteAlreadyCompletedFailTest() {
        // Given
        Long productId = 10L;
        Long buyerId = 2L;
        Long sellerId = 1L;
        Product product = Product.builder().productId(productId).build();
        User buyer = User.builder().userId(buyerId).build();
        User seller = User.builder().userId(sellerId).build();
        Complete existingComplete = Complete.builder().completeId(productId).product(product).buyer(buyer).seller(seller).completedAt(LocalDateTime.now()).build();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
        when(userRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(completeRepository.findByProduct_ProductId(productId)).thenReturn(Optional.of(existingComplete));

        // When & Then
        assertThrows(IllegalStateException.class, () -> completeService.createComplete(productId, buyerId, sellerId));
        verify(productRepository, times(1)).findById(productId);
        verify(userRepository, times(1)).findById(buyerId);
        verify(userRepository, times(1)).findById(sellerId);
        verify(completeRepository, times(1)).findByProduct_ProductId(productId);
        verify(completeRepository, never()).save(any());
    }

    @Test
    @DisplayName("상품 ID로 거래 완료 정보 조회 성공 테스트")
    void getCompleteByProductIdSuccessTest() {
        // Given
        Long productId = 10L;
        Product product = Product.builder().productId(productId).title("테스트 상품").price(10000).build();
        User buyer = User.builder().userId(2L).build();
        User seller = User.builder().userId(1L).build();
        Complete complete = Complete.builder()
                .completeId(productId)
                .product(product)
                .buyer(buyer)
                .seller(seller)
                .completedAt(LocalDateTime.now())
                .build();
        when(completeRepository.findByProduct_ProductId(productId)).thenReturn(Optional.of(complete));

        // When
        CompleteDTO.Response response = completeService.getCompleteByProductId(productId);

        // Then
        assertThat(response.getProductId()).isEqualTo(productId);
        assertThat(response.getBuyerId()).isEqualTo(2L);
        assertThat(response.getSellerId()).isEqualTo(1L);
        assertThat(response.getCompletedAt()).isNotNull();
        verify(completeRepository, times(1)).findByProduct_ProductId(productId);
    }

    @Test
    @DisplayName("상품 ID로 거래 완료 정보 조회 실패 테스트 - 정보 없음")
    void getCompleteByProductIdNotFoundFailTest() {
        // Given
        Long productId = 10L;
        when(completeRepository.findByProduct_ProductId(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> completeService.getCompleteByProductId(productId));
        verify(completeRepository, times(1)).findByProduct_ProductId(productId);
    }

    @Test
    @DisplayName("구매자 ID로 거래 완료 목록 조회 성공 테스트")
    void getCompletesByBuyerIdSuccessTest() {
        // Given
        Long buyerId = 2L;
        User buyer = User.builder().userId(buyerId).build();
        Product product1 = Product.builder().productId(10L).title("상품1").price(10000).build();
        Product product2 = Product.builder().productId(11L).title("상품2").price(20000).build();
        User seller1 = User.builder().userId(1L).build();
        User seller2 = User.builder().userId(3L).build();
        List<Complete> completes = Arrays.asList(
                Complete.builder().completeId(10L).product(product1).buyer(buyer).seller(seller1).completedAt(LocalDateTime.now()).build(),
                Complete.builder().completeId(11L).product(product2).buyer(buyer).seller(seller2).completedAt(LocalDateTime.now().minusHours(1)).build()
        );
        when(completeRepository.findByBuyer_UserId(buyerId)).thenReturn(completes);

        // When
        List<CompleteDTO.Response> responses = completeService.getCompletesByBuyerId(buyerId);

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getBuyerId()).isEqualTo(buyerId);
        assertThat(responses.get(1).getBuyerId()).isEqualTo(buyerId);
        verify(completeRepository, times(1)).findByBuyer_UserId(buyerId);
    }

    @Test
    @DisplayName("판매자 ID로 거래 완료 목록 조회 성공 테스트")
    void getCompletesBySellerIdSuccessTest() {
        // Given
        Long sellerId = 1L;
        User seller = User.builder().userId(sellerId).build();
        Product product1 = Product.builder().productId(10L).title("상품1").price(10000).build();
        Product product2 = Product.builder().productId(11L).title("상품2").price(20000).build();
        User buyer1 = User.builder().userId(2L).build();
        User buyer2 = User.builder().userId(3L).build();
        List<Complete> completes = Arrays.asList(
                Complete.builder().completeId(10L).product(product1).buyer(buyer1).seller(seller).completedAt(LocalDateTime.now()).build(),
                Complete.builder().completeId(11L).product(product2).buyer(buyer2).seller(seller).completedAt(LocalDateTime.now().minusHours(1)).build()
        );
        when(completeRepository.findBySeller_UserId(sellerId)).thenReturn(completes);

        // When
        List<CompleteDTO.Response> responses = completeService.getCompletesBySellerId(sellerId);

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getSellerId()).isEqualTo(sellerId);
        assertThat(responses.get(1).getSellerId()).isEqualTo(sellerId);
        verify(completeRepository, times(1)).findBySeller_UserId(sellerId);
    }

    @Test
    @DisplayName("모든 거래 완료 목록 조회 성공 테스트")
    void getAllCompletesSuccessTest() {
        // Given
        Product product1 = Product.builder().productId(10L).title("상품1").price(10000).build();
        Product product2 = Product.builder().productId(11L).title("상품2").price(20000).build();
        User buyer1 = User.builder().userId(2L).build();
        User seller1 = User.builder().userId(1L).build();
        User buyer2 = User.builder().userId(3L).build();
        User seller2 = User.builder().userId(1L).build();
        List<Complete> completes = Arrays.asList(
                Complete.builder().completeId(10L).product(product1).buyer(buyer1).seller(seller1).completedAt(LocalDateTime.now()).build(),
                Complete.builder().completeId(11L).product(product2).buyer(buyer2).seller(seller2).completedAt(LocalDateTime.now().minusHours(1)).build()
        );
        when(completeRepository.findAll()).thenReturn(completes);

        // When
        List<CompleteDTO.Response> responses = completeService.getAllCompletes();

        // Then
        assertThat(responses).hasSize(2);
        verify(completeRepository, times(1)).findAll();
    }
}