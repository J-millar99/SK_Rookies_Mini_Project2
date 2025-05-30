package com.miniproject.rookiejangter.service;

import com.miniproject.rookiejangter.controller.dto.ReservationDTO;
import com.miniproject.rookiejangter.entity.Product;
import com.miniproject.rookiejangter.entity.Reservation;
import com.miniproject.rookiejangter.entity.User;
import com.miniproject.rookiejangter.repository.ProductRepository;
import com.miniproject.rookiejangter.repository.ReservationRepository;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductService productService;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    @DisplayName("예약 생성 성공 테스트")
    void createReservationSuccessTest() {
        // Given
        Long buyerId = 1L;
        Long productId = 10L;
        User buyer = User.builder().userId(buyerId).build();
        User seller = User.builder().userId(2L).build();
        Product product = Product.builder().productId(productId).user(seller).isCompleted(false).isReserved(false).build();
        Reservation savedReservation = Reservation.builder()
                .reservationId(100L)
                .buyer(buyer)
                .seller(seller)
                .product(product)
                .status(Reservation.TradeStatus.REQUESTED)
                .isCanceled(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(reservationRepository.existsByBuyer_UserIdAndProduct_ProductId(buyerId, productId)).thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);
        doNothing().when(productService).updateProductStatus(productId, true, null, seller.getUserId());

        // When
        ReservationDTO.Response response = reservationService.createReservation(buyerId, productId);

        // Then
        assertThat(response.getTradeId()).isEqualTo(100L);
        assertThat(response.getBuyerId()).isEqualTo(buyerId);
        assertThat(response.getProductId()).isEqualTo(productId);
        assertThat(response.getStatus()).isEqualTo(Reservation.TradeStatus.REQUESTED);
        verify(userRepository, times(1)).findById(buyerId);
        verify(productRepository, times(1)).findById(productId);
        verify(reservationRepository, times(1)).existsByBuyer_UserIdAndProduct_ProductId(buyerId, productId);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
        verify(productService, times(1)).updateProductStatus(productId, true, null, seller.getUserId());
    }

    @Test
    @DisplayName("예약 생성 실패 테스트 - 구매자 없음")
    void createReservationBuyerNotFoundFailTest() {
        // Given
        Long buyerId = 1L;
        Long productId = 10L;
        when(userRepository.findById(buyerId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> reservationService.createReservation(buyerId, productId));
        verify(userRepository, times(1)).findById(buyerId);
        verify(productRepository, never()).findById(anyLong());
        verify(reservationRepository, never()).existsByBuyer_UserIdAndProduct_ProductId(anyLong(), anyLong());
        verify(reservationRepository, never()).save(any());
        verify(productService, never()).updateProductStatus(anyLong(), anyBoolean(), any(), anyLong());
    }

    @Test
    @DisplayName("예약 생성 실패 테스트 - 게시글 없음")
    void createReservationProductNotFoundFailTest() {
        // Given
        Long buyerId = 1L;
        Long productId = 10L;
        User buyer = User.builder().userId(buyerId).build();
        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> reservationService.createReservation(buyerId, productId));
        verify(userRepository, times(1)).findById(buyerId);
        verify(productRepository, times(1)).findById(productId);
        verify(reservationRepository, never()).existsByBuyer_UserIdAndProduct_ProductId(anyLong(), anyLong());
        verify(reservationRepository, never()).save(any());
        verify(productService, never()).updateProductStatus(anyLong(), anyBoolean(), any(), anyLong());
    }

    @Test
    @DisplayName("예약 생성 실패 테스트 - 이미 판매 완료된 게시글")
    void createReservationAlreadyCompletedFailTest() {
        // Given
        Long buyerId = 1L;
        Long productId = 10L;
        User buyer = User.builder().userId(buyerId).build();
        User seller = User.builder().userId(2L).build();
        Product product = Product.builder().productId(productId).user(seller).isCompleted(true).isReserved(false).build();
        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // When & Then
        assertThrows(IllegalStateException.class, () -> reservationService.createReservation(buyerId, productId));
        verify(userRepository, times(1)).findById(buyerId);
        verify(productRepository, times(1)).findById(productId);
        verify(reservationRepository, never()).existsByBuyer_UserIdAndProduct_ProductId(anyLong(), anyLong());
        verify(reservationRepository, never()).save(any());
        verify(productService, never()).updateProductStatus(anyLong(), anyBoolean(), any(), anyLong());
    }

    @Test
    @DisplayName("예약 생성 실패 테스트 - 이미 다른 사용자에 의해 예약된 게시글")
    void createReservationAlreadyReservedByOtherFailTest() {
        // Given
        Long buyerId = 1L;
        Long productId = 10L;
        Long otherBuyerId = 3L;
        User buyer = User.builder().userId(buyerId).build();
        User seller = User.builder().userId(2L).build();
        Product product = Product.builder().productId(productId).user(seller).isCompleted(false).isReserved(true).build();
        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // When & Then
        assertThrows(IllegalStateException.class, () -> reservationService.createReservation(buyerId, productId));
        verify(userRepository, times(1)).findById(buyerId);
        verify(productRepository, times(1)).findById(productId);
        verify(reservationRepository, never()).existsByBuyer_UserIdAndProduct_ProductId(anyLong(), anyLong());
        verify(reservationRepository, never()).save(any());
        verify(productService, never()).updateProductStatus(anyLong(), anyBoolean(), any(), anyLong());
    }

    @Test
    @DisplayName("예약 생성 실패 테스트 - 자신의 게시글 예약 시도")
    void createReservationOwnProductFailTest() {
        // Given
        Long userId = 1L;
        Long productId = 10L;
        User user = User.builder().userId(userId).build();
        Product product = Product.builder().productId(productId).user(user).isCompleted(false).isReserved(false).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> reservationService.createReservation(userId, productId));
        verify(userRepository, times(1)).findById(userId);
        verify(productRepository, times(1)).findById(productId);
        verify(reservationRepository, never()).existsByBuyer_UserIdAndProduct_ProductId(anyLong(), anyLong());
        verify(reservationRepository, never()).save(any());
        verify(productService, never()).updateProductStatus(anyLong(), anyBoolean(), any(), anyLong());
    }

    @Test
    @DisplayName("예약 생성 실패 테스트 - 이미 예약 요청이 존재")
    void createReservationAlreadyRequestedFailTest() {
        // Given
        Long buyerId = 1L;
        Long productId = 10L;
        User buyer = User.builder().userId(buyerId).build();
        User seller = User.builder().userId(2L).build();
        Product product = Product.builder().productId(productId).user(seller).isCompleted(false).isReserved(false).build();
        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(reservationRepository.existsByBuyer_UserIdAndProduct_ProductId(buyerId, productId)).thenReturn(true);

        // When & Then
        assertThrows(IllegalStateException.class, () -> reservationService.createReservation(buyerId, productId));
        verify(userRepository, times(1)).findById(buyerId);
        verify(productRepository, times(1)).findById(productId);
        verify(reservationRepository, times(1)).existsByBuyer_UserIdAndProduct_ProductId(buyerId, productId);
        verify(reservationRepository, never()).save(any());
        verify(productService, never()).updateProductStatus(anyLong(), anyBoolean(), any(), anyLong());
    }

    @Test
    @DisplayName("구매자 ID로 예약 목록 조회 성공 테스트")
    void getReservationsByBuyerSuccessTest() {
        // Given
        Long buyerId = 1L;
        User buyer = User.builder().userId(buyerId).build();
        User seller = User.builder().userId(2L).build();
        Product product1 = Product.builder().productId(10L).user(seller).build();
        Product product2 = Product.builder().productId(11L).user(seller).build();
        Reservation reservation1 = Reservation.builder().reservationId(100L).buyer(buyer).seller(seller).product(product1).status(Reservation.TradeStatus.REQUESTED).createdAt(LocalDateTime.now()).build();
        Reservation reservation2 = Reservation.builder().reservationId(101L).buyer(buyer).seller(seller).product(product2).status(Reservation.TradeStatus.ACCEPTED).createdAt(LocalDateTime.now()).build();
        List<Reservation> reservations = Arrays.asList(reservation1, reservation2);

        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
        when(reservationRepository.findByBuyer_UserId(buyerId)).thenReturn(reservations);

        // When
        List<ReservationDTO.Response> responses = reservationService.getReservationsByBuyer(buyerId);

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getBuyerId()).isEqualTo(buyerId);
        assertThat(responses.get(1).getBuyerId()).isEqualTo(buyerId);
        verify(userRepository, times(1)).findById(buyerId);
        verify(reservationRepository, times(1)).findByBuyer_UserId(buyerId);
    }

    @Test
    @DisplayName("구매자 ID로 예약 목록 조회 실패 테스트 - 구매자 없음")
    void getReservationsByBuyerNotFoundFailTest() {
        // Given
        Long buyerId = 1L;
        when(userRepository.findById(buyerId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> reservationService.getReservationsByBuyer(buyerId));
        verify(userRepository, times(1)).findById(buyerId);
        verify(reservationRepository, never()).findByBuyer_UserId(anyLong());
    }

    @Test
    @DisplayName("판매자 ID로 예약 목록 조회 성공 테스트")
    void getReservationsBySellerSuccessTest() {
        // Given
        Long sellerId = 2L;
        User buyer1 = User.builder().userId(1L).build();
        User buyer2 = User.builder().userId(3L).build();
        User seller = User.builder().userId(sellerId).build();
        Product product1 = Product.builder().productId(10L).user(seller).build();
        Product product2 = Product.builder().productId(11L).user(seller).build();
        Reservation reservation1 = Reservation.builder().reservationId(100L).seller(seller).buyer(buyer1).product(product1).status(Reservation.TradeStatus.REQUESTED).createdAt(LocalDateTime.now()).build();
        Reservation reservation2 = Reservation.builder().reservationId(101L).seller(seller).buyer(buyer2).product(product2).status(Reservation.TradeStatus.ACCEPTED).createdAt(LocalDateTime.now()).build();
        List<Reservation> reservations = Arrays.asList(reservation1, reservation2);

        when(userRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(reservationRepository.findBySeller_UserId(sellerId)).thenReturn(reservations);

        // When
        List<ReservationDTO.Response> responses = reservationService.getReservationsBySeller(sellerId);

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getSellerId()).isEqualTo(sellerId);
        assertThat(responses.get(1).getSellerId()).isEqualTo(sellerId);
        verify(userRepository, times(1)).findById(sellerId);
        verify(reservationRepository, times(1)).findBySeller_UserId(sellerId);
    }

    @Test
    @DisplayName("판매자 ID로 예약 목록 조회 실패 테스트 - 판매자 없음")
    void getReservationsBySellerNotFoundFailTest() {
        // Given
        Long sellerId = 2L;
        when(userRepository.findById(sellerId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> reservationService.getReservationsBySeller(sellerId));
        verify(userRepository, times(1)).findById(sellerId);
        verify(reservationRepository, never()).findBySeller_UserId(anyLong());
    }

    @Test
    @DisplayName("상품 ID로 예약 목록 조회 성공 테스트")
    void getReservationsByProductSuccessTest() {
        // Given
        Long productId = 10L;
        User buyer1 = User.builder().userId(1L).build();
        User buyer2 = User.builder().userId(3L).build();
        User seller = User.builder().userId(2L).build();
        Product product = Product.builder().productId(productId).user(seller).build();
        Reservation reservation1 = Reservation.builder().reservationId(100L).product(product).buyer(buyer1).seller(seller).status(Reservation.TradeStatus.REQUESTED).createdAt(LocalDateTime.now()).build();
        Reservation reservation2 = Reservation.builder().reservationId(101L).product(product).buyer(buyer2).seller(seller).status(Reservation.TradeStatus.ACCEPTED).createdAt(LocalDateTime.now()).build();
        List<Reservation> reservations = Arrays.asList(reservation1, reservation2);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(reservationRepository.findByProduct_ProductId(productId)).thenReturn(reservations);

        // When
        List<ReservationDTO.Response> responses = reservationService.getReservationsByProduct(productId);

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getProductId()).isEqualTo(productId);
        assertThat(responses.get(1).getProductId()).isEqualTo(productId);
        verify(productRepository, times(1)).findById(productId);
        verify(reservationRepository, times(1)).findByProduct_ProductId(productId);
    }

    @Test
    @DisplayName("상품 ID로 예약 목록 조회 실패 테스트 - 게시글 없음")
    void getReservationsByProductNotFoundFailTest() {
        // Given
        Long productId = 10L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> reservationService.getReservationsByProduct(productId));
        verify(productRepository, times(1)).findById(productId);
        verify(reservationRepository, never()).findByProduct_ProductId(anyLong());
    }

    @Test
    @DisplayName("예약 ID로 예약 정보 조회 성공 테스트")
    void getReservationByIdSuccessTest() {
        // Given
        Long reservationId = 100L;
        User buyer = User.builder().userId(1L).build();
        User seller = User.builder().userId(2L).build();
        Product product = Product.builder().productId(10L).user(seller).build();
        Reservation reservation = Reservation.builder()
                .reservationId(reservationId)
                .buyer(buyer)
                .seller(seller)
                .product(product)
                .status(Reservation.TradeStatus.REQUESTED)
                .createdAt(LocalDateTime.now())
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        // When
        ReservationDTO.Response response = reservationService.getReservationById(reservationId);

        // Then
        assertThat(response.getTradeId()).isEqualTo(reservationId);
        assertThat(response.getBuyerId()).isEqualTo(buyer.getUserId());
        assertThat(response.getProductId()).isEqualTo(product.getProductId());
        assertThat(response.getStatus()).isEqualTo(Reservation.TradeStatus.REQUESTED);
        verify(reservationRepository, times(1)).findById(reservationId);
    }

    @Test
    @DisplayName("예약 ID로 예약 정보 조회 실패 테스트 - 예약 정보 없음")
    void getReservationByIdNotFoundFailTest() {
        // Given
        Long reservationId = 100L;
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> reservationService.getReservationById(reservationId));
        verify(reservationRepository, times(1)).findById(reservationId);
    }

    @Test
    @DisplayName("예약 상태 업데이트 성공 테스트 - 수락")
    void updateReservationStatusAcceptedTest() {
        // Given
        Long reservationId = 100L;
        Long sellerId = 2L;
        User buyer = User.builder().userId(1L).build();
        User seller = User.builder().userId(sellerId).build();
        Product product = Product.builder().productId(10L).user(seller).isReserved(false).isCompleted(false).build();
        Reservation reservation = Reservation.builder()
                .reservationId(reservationId)
                .buyer(buyer)
                .seller(seller)
                .product(product)
                .status(Reservation.TradeStatus.REQUESTED)
                .build();
        Reservation updatedReservation = Reservation.builder()
                .reservationId(reservationId)
                .buyer(buyer)
                .seller(seller)
                .product(product)
                .status(Reservation.TradeStatus.ACCEPTED)
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(userRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(updatedReservation);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        ReservationDTO.Response response = reservationService.updateReservationStatus(reservationId, Reservation.TradeStatus.ACCEPTED, sellerId);

        // Then
        assertThat(response.getStatus()).isEqualTo(Reservation.TradeStatus.ACCEPTED);
        assertTrue(product.getIsReserved());
        verify(reservationRepository, times(1)).findById(reservationId);
        verify(userRepository, times(1)).findById(sellerId);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("예약 상태 업데이트 실패 테스트 - 수락 권한 없음")
    void updateReservationStatusAcceptNoPermissionFailTest() {
        // Given
        Long reservationId = 100L;
        Long buyerId = 1L;
        User buyer = User.builder().userId(buyerId).build();
        User seller = User.builder().userId(2L).build();
        Product product = Product.builder().productId(10L).user(seller).isReserved(false).isCompleted(false).build();
        Reservation reservation = Reservation.builder()
                .reservationId(reservationId)
                .buyer(buyer)
                .seller(seller)
                .product(product)
                .status(Reservation.TradeStatus.REQUESTED)
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));

        // When & Then
        assertThrows(SecurityException.class, () -> reservationService.updateReservationStatus(reservationId, Reservation.TradeStatus.ACCEPTED, buyerId));
        verify(reservationRepository, times(1)).findById(reservationId);
        verify(userRepository, times(1)).findById(buyerId);
        verify(reservationRepository, never()).save(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("예약 상태 업데이트 성공 테스트 - 거절")
    void updateReservationStatusDeclinedTest() {
        // Given
        Long reservationId = 100L;
        Long sellerId = 2L;
        User buyer = User.builder().userId(1L).build();
        User seller = User.builder().userId(sellerId).build();
        Product product = Product.builder().productId(10L).user(seller).isReserved(true).isCompleted(false).build();
        Reservation reservation = Reservation.builder()
                .reservationId(reservationId)
                .buyer(buyer)
                .seller(seller)
                .product(product)
                .status(Reservation.TradeStatus.REQUESTED)
                .build();
        Reservation updatedReservation = Reservation.builder()
                .reservationId(reservationId)
                .buyer(buyer)
                .seller(seller)
                .product(product)
                .status(Reservation.TradeStatus.DECLINED)
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(userRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(updatedReservation);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        ReservationDTO.Response response = reservationService.updateReservationStatus(reservationId, Reservation.TradeStatus.DECLINED, sellerId);

        // Then
        assertThat(response.getStatus()).isEqualTo(Reservation.TradeStatus.DECLINED);
        assertFalse(product.getIsReserved());
        verify(reservationRepository, times(1)).findById(reservationId);
        verify(userRepository, times(1)).findById(sellerId);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("예약 상태 업데이트 실패 테스트 - 거절 권한 없음")
    void updateReservationStatusDeclinedNoPermissionFailTest() {
        // Given
        Long reservationId = 100L;
        Long buyerId = 1L;
        User buyer = User.builder().userId(buyerId).build();
        User seller = User.builder().userId(2L).build();
        Product product = Product.builder().productId(10L).user(seller).isReserved(false).isCompleted(false).build();
        Reservation reservation = Reservation.builder()
                .reservationId(reservationId)
                .buyer(buyer)
                .seller(seller)
                .product(product)
                .status(Reservation.TradeStatus.REQUESTED)
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));

        // When & Then
        assertThrows(SecurityException.class, () -> reservationService.updateReservationStatus(reservationId, Reservation.TradeStatus.DECLINED, buyerId));
        verify(reservationRepository, times(1)).findById(reservationId);
        verify(userRepository, times(1)).findById(buyerId);
        verify(reservationRepository, never()).save(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("예약 상태 업데이트 성공 테스트 - 취소 (구매자, REQUESTED)")
    void updateReservationStatusCancelledByBuyerRequestedTest() {
        // Given
        Long reservationId = 100L;
        Long buyerId = 1L;
        User buyer = User.builder().userId(buyerId).build();
        User seller = User.builder().userId(2L).build();
        Product product = Product.builder().productId(10L).user(seller).isReserved(false).isCompleted(false).build();
        Reservation reservation = Reservation.builder()
                .reservationId(reservationId)
                .buyer(buyer)
                .seller(seller)
                .product(product)
                .status(Reservation.TradeStatus.REQUESTED)
                .isCanceled(false)
                .build();
        Reservation updatedReservation = Reservation.builder()
                .reservationId(reservationId)
                .buyer(buyer)
                .seller(seller)
                .product(product)
                .status(Reservation.TradeStatus.CANCELLED)
                .isCanceled(true)
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(updatedReservation);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        ReservationDTO.Response response = reservationService.updateReservationStatus(reservationId, Reservation.TradeStatus.CANCELLED, buyerId);

        // Then
        assertThat(response.getStatus()).isEqualTo(Reservation.TradeStatus.CANCELLED);
        assertTrue(reservation.getIsCanceled());
        assertFalse(product.getIsReserved());
        verify(reservationRepository, times(1)).findById(reservationId);
        verify(userRepository, times(1)).findById(buyerId);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("예약 상태 업데이트 성공 테스트 - 취소 (구매자, ACCEPTED)")
    void updateReservationStatusCancelledByBuyerAcceptedTest() {
        // Given
        Long reservationId = 100L;
        Long buyerId = 1L;
        User buyer = User.builder().userId(buyerId).build();
        User seller = User.builder().userId(2L).build();
        Product product = Product.builder().productId(10L).user(seller).isReserved(true).isCompleted(false).build();
        Reservation reservation = Reservation.builder()
                .reservationId(reservationId)
                .buyer(buyer)
                .seller(seller)
                .product(product)
                .status(Reservation.TradeStatus.ACCEPTED)
                .isCanceled(false)
                .build();
        Reservation updatedReservation = Reservation.builder()
                .reservationId(reservationId)
                .buyer(buyer)
                .seller(seller)
                .product(product)
                .status(Reservation.TradeStatus.CANCELLED)
                .isCanceled(true)
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(updatedReservation);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        ReservationDTO.Response response = reservationService.updateReservationStatus(reservationId, Reservation.TradeStatus.CANCELLED, buyerId);

        // Then
        assertThat(response.getStatus()).isEqualTo(Reservation.TradeStatus.CANCELLED);
        assertTrue(reservation.getIsCanceled());
        assertFalse(product.getIsReserved());
        verify(reservationRepository, times(1)).findById(reservationId);
        verify(userRepository, times(1)).findById(buyerId);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("예약 상태 업데이트 성공 테스트 - 취소 (판매자, ACCEPTED)")
    void updateReservationStatusCancelledBySellerAcceptedTest() {
        // Given
        Long reservationId = 100L;
        Long sellerId = 2L;
        User buyer = User.builder().userId(1L).build();
        User seller = User.builder().userId(sellerId).build();
        Product product = Product.builder().productId(10L).user(seller).isReserved(true).isCompleted(false).build();
        Reservation reservation = Reservation.builder()
                .reservationId(reservationId)
                .buyer(buyer)
                .seller(seller)
                .product(product)
                .status(Reservation.TradeStatus.ACCEPTED)
                .isCanceled(false)
                .build();
        Reservation updatedReservation = Reservation.builder()
                .reservationId(reservationId)
                .buyer(buyer)
                .seller(seller)
                .product(product)
                .status(Reservation.TradeStatus.CANCELLED)
                .isCanceled(true)
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(userRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(updatedReservation);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        ReservationDTO.Response response = reservationService.updateReservationStatus(reservationId, Reservation.TradeStatus.CANCELLED, sellerId);

        // Then
        assertThat(response.getStatus()).isEqualTo(Reservation.TradeStatus.CANCELLED);
        assertTrue(reservation.getIsCanceled());
        assertFalse(product.getIsReserved());
        verify(reservationRepository, times(1)).findById(reservationId);
        verify(userRepository, times(1)).findById(sellerId);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("예약 상태 업데이트 실패 테스트 - 취소 권한 없음")
    void updateReservationStatusCancelledNoPermissionFailTest() {
        // Given
        Long reservationId = 100L;
        Long otherUserId = 3L;
        User buyer = User.builder().userId(1L).build();
        User seller = User.builder().userId(2L).build();
        Product product = Product.builder().productId(10L).user(seller).isReserved(false).isCompleted(false).build();
        Reservation reservation = Reservation.builder()
                .reservationId(reservationId)
                .buyer(buyer)
                .seller(seller)
                .product(product)
                .status(Reservation.TradeStatus.REQUESTED)
                .isCanceled(false)
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(userRepository.findById(otherUserId)).thenReturn(Optional.of(User.builder().userId(otherUserId).build()));

        // When & Then
        assertThrows(SecurityException.class, () -> reservationService.updateReservationStatus(reservationId, Reservation.TradeStatus.CANCELLED, otherUserId));
        verify(reservationRepository, times(1)).findById(reservationId);
        verify(userRepository, times(1)).findById(otherUserId);
        verify(reservationRepository, never()).save(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("예약 상태 업데이트 실패 테스트 - 취소 불가 상태")
    void updateReservationStatusCancelledIllegalStateFailTest() {
        // Given
        Long reservationId = 100L;
        Long buyerId = 1L;
        User buyer = User.builder().userId(buyerId).build();
        User seller = User.builder().userId(2L).build();
        Product product = Product.builder().productId(10L).user(seller).isReserved(false).isCompleted(false).build();
        Reservation reservation = Reservation.builder()
                .reservationId(reservationId)
                .buyer(buyer)
                .seller(seller)
                .product(product)
                .status(Reservation.TradeStatus.COMPLETED)
                .isCanceled(false)
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));

        // When & Then
        assertThrows(IllegalStateException.class, () -> reservationService.updateReservationStatus(reservationId, Reservation.TradeStatus.CANCELLED, buyerId));
        verify(reservationRepository, times(1)).findById(reservationId);
        verify(userRepository, times(1)).findById(buyerId);
        verify(reservationRepository, never()).save(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("예약 상태 업데이트 성공 테스트 - 완료")
    void updateReservationStatusCompletedTest() {
        // Given
        Long reservationId = 100L;
        Long sellerId = 2L;
        User buyer = User.builder().userId(1L).build();
        User seller = User.builder().userId(sellerId).build();
        Product product = Product.builder().productId(10L).user(seller).isReserved(true).isCompleted(false).build();
        Reservation reservation = Reservation.builder()
                .reservationId(reservationId)
                .buyer(buyer)
                .seller(seller)
                .product(product)
                .status(Reservation.TradeStatus.ACCEPTED)
                .build();
        Reservation updatedReservation = Reservation.builder()
                .reservationId(reservationId)
                .buyer(buyer)
                .seller(seller)
                .product(product)
                .status(Reservation.TradeStatus.COMPLETED)
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(userRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(updatedReservation);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        ReservationDTO.Response response = reservationService.updateReservationStatus(reservationId, Reservation.TradeStatus.COMPLETED, sellerId);

        // Then
        assertThat(response.getStatus()).isEqualTo(Reservation.TradeStatus.COMPLETED);
        assertTrue(product.getIsCompleted());
        assertFalse(product.getIsReserved());
        verify(reservationRepository, times(1)).findById(reservationId);
        verify(userRepository, times(1)).findById(sellerId);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("예약 상태 업데이트 실패 테스트 - 완료 권한 없음")
    void updateReservationStatusCompletedNoPermissionFailTest() {
        // Given
        Long reservationId = 100L;
        Long buyerId = 1L;
        User buyer = User.builder().userId(buyerId).build();
        User seller = User.builder().userId(2L).build();
        Product product = Product.builder().productId(10L).user(seller).isReserved(true).isCompleted(false).build();
        Reservation reservation = Reservation.builder()
                .reservationId(reservationId)
                .buyer(buyer)
                .seller(seller)
                .product(product)
                .status(Reservation.TradeStatus.ACCEPTED)
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));

        // When & Then
        assertThrows(SecurityException.class, () -> reservationService.updateReservationStatus(reservationId, Reservation.TradeStatus.COMPLETED, buyerId));
        verify(reservationRepository, times(1)).findById(reservationId);
        verify(userRepository, times(1)).findById(buyerId);
        verify(reservationRepository, never()).save(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("예약 상태 업데이트 실패 테스트 - 완료 불가 상태")
    void updateReservationStatusCompletedIllegalStateFailTest() {
        // Given
        Long reservationId = 100L;
        Long sellerId = 2L;
        User buyer = User.builder().userId(1L).build();
        User seller = User.builder().userId(sellerId).build();
        Product product = Product.builder().productId(10L).user(seller).isReserved(false).isCompleted(false).build();
        Reservation reservation = Reservation.builder()
                .reservationId(reservationId)
                .buyer(buyer)
                .seller(seller)
                .product(product)
                .status(Reservation.TradeStatus.REQUESTED)
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(userRepository.findById(sellerId)).thenReturn(Optional.of(seller));

        // When & Then
        assertThrows(IllegalStateException.class, () -> reservationService.updateReservationStatus(reservationId, Reservation.TradeStatus.COMPLETED, sellerId));
        verify(reservationRepository, times(1)).findById(reservationId);
        verify(userRepository, times(1)).findById(sellerId);
        verify(reservationRepository, never()).save(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("예약 삭제 성공 테스트 (구매자, REQUESTED)")
    void deleteReservationSuccessByBuyerRequestedTest() {
        // Given
        Long reservationId = 100L;
        Long buyerId = 1L;
        User buyer = User.builder().userId(buyerId).build();
        User seller = User.builder().userId(2L).build();
        Product product = Product.builder().productId(10L).user(seller).isReserved(false).isCompleted(false).build();
        Reservation reservation = Reservation.builder()
                .reservationId(reservationId)
                .buyer(buyer)
                .seller(seller)
                .product(product)
                .status(Reservation.TradeStatus.REQUESTED)
                .isCanceled(false)
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
        doNothing().when(reservationRepository).delete(reservation);

        // When
        reservationService.deleteReservation(reservationId, buyerId);

        // Then
        verify(reservationRepository, times(1)).findById(reservationId);
        verify(userRepository, times(1)).findById(buyerId);
        verify(reservationRepository, times(1)).delete(reservation);
    }

    @Test
    @DisplayName("예약 삭제 성공 테스트 (구매자, DECLINED)")
    void deleteReservationSuccessByBuyerDeclinedTest() {
        // Given
        Long reservationId = 100L;
        Long buyerId = 1L;
        User buyer = User.builder().userId(buyerId).build();
        User seller = User.builder().userId(2L).build();
        Product product = Product.builder().productId(10L).user(seller).isReserved(false).isCompleted(false).build();
        Reservation reservation = Reservation.builder()
                .reservationId(reservationId)
                .buyer(buyer)
                .seller(seller)
                .product(product)
                .status(Reservation.TradeStatus.DECLINED)
                .isCanceled(false)
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
        doNothing().when(reservationRepository).delete(reservation);

        // When
        reservationService.deleteReservation(reservationId, buyerId);

        // Then
        verify(reservationRepository, times(1)).findById(reservationId);
        verify(userRepository, times(1)).findById(buyerId);
        verify(reservationRepository, times(1)).delete(reservation);
    }

    @Test
    @DisplayName("예약 삭제 성공 테스트 (구매자, CANCELLED)")
    void deleteReservationSuccessByBuyerCancelledTest() {
        // Given
        Long reservationId = 100L;
        Long buyerId = 1L;
        User buyer = User.builder().userId(buyerId).build();
        User seller = User.builder().userId(2L).build();
        Product product = Product.builder().productId(10L).user(seller).isReserved(false).isCompleted(false).build();
        Reservation reservation = Reservation.builder()
                .reservationId(reservationId)
                .buyer(buyer)
                .seller(seller)
                .product(product)
                .status(Reservation.TradeStatus.CANCELLED)
                .isCanceled(true)
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
        doNothing().when(reservationRepository).delete(reservation);

        // When
        reservationService.deleteReservation(reservationId, buyerId);

        // Then
        verify(reservationRepository, times(1)).findById(reservationId);
        verify(userRepository, times(1)).findById(buyerId);
        verify(reservationRepository, times(1)).delete(reservation);
    }

    @Test
    @DisplayName("예약 삭제 실패 테스트 - 예약 정보 없음")
    void deleteReservationNotFoundFailTest() {
        // Given
        Long reservationId = 100L;
        Long buyerId = 1L;
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> reservationService.deleteReservation(reservationId, buyerId));
        verify(reservationRepository, times(1)).findById(reservationId);
        verify(reservationRepository, never()).delete(any());
    }

    @Test
    @DisplayName("예약 삭제 실패 테스트 - 권한 없음")
    void deleteReservationNoPermissionFailTest() {
        // Given
        Long reservationId = 100L;
        Long buyerId = 1L;
        Long otherUserId = 3L;
        User buyer = User.builder().userId(buyerId).build();
        User seller = User.builder().userId(2L).build();
        Product product = Product.builder().productId(10L).user(seller).isReserved(false).isCompleted(false).build();
        Reservation reservation = Reservation.builder()
                .reservationId(reservationId)
                .buyer(buyer)
                .seller(seller)
                .product(product)
                .status(Reservation.TradeStatus.REQUESTED)
                .isCanceled(false)
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(userRepository.findById(otherUserId)).thenReturn(Optional.of(User.builder().userId(otherUserId).build()));

        // When & Then
        assertThrows(SecurityException.class, () -> reservationService.deleteReservation(reservationId, otherUserId));
        verify(reservationRepository, times(1)).findById(reservationId);
        verify(userRepository, times(1)).findById(otherUserId);
        verify(reservationRepository, never()).delete(any());
    }

    @Test
    @DisplayName("예약 삭제 실패 테스트 - 삭제 불가 상태")
    void deleteReservationIllegalStateFailTest() {
        // Given
        Long reservationId = 100L;
        Long buyerId = 1L;
        User buyer = User.builder().userId(buyerId).build();
        User seller = User.builder().userId(2L).build();
        Product product = Product.builder().productId(10L).user(seller).isReserved(true).isCompleted(false).build();
        Reservation reservation = Reservation.builder()
                .reservationId(reservationId)
                .buyer(buyer)
                .seller(seller)
                .product(product)
                .status(Reservation.TradeStatus.ACCEPTED)
                .isCanceled(false)
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));

        // When & Then
        assertThrows(IllegalStateException.class, () -> reservationService.deleteReservation(reservationId, buyerId));
        verify(reservationRepository, times(1)).findById(reservationId);
        verify(userRepository, times(1)).findById(buyerId);
        verify(reservationRepository, never()).delete(any());
    }
}