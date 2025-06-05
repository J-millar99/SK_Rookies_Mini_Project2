//package com.miniproject.rookiejangter.service;
//
//import com.miniproject.rookiejangter.controller.dto.DibsDTO;
//import com.miniproject.rookiejangter.entity.Dibs;
//import com.miniproject.rookiejangter.entity.Product;
//import com.miniproject.rookiejangter.entity.User;
//import com.miniproject.rookiejangter.exception.BusinessException;
//import com.miniproject.rookiejangter.exception.ErrorCode;
//import com.miniproject.rookiejangter.repository.DibsRepository;
//import com.miniproject.rookiejangter.repository.ProductRepository;
//import com.miniproject.rookiejangter.repository.UserRepository;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDateTime;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//public class DibsServiceTest {
//
//    @Mock
//    private DibsRepository dibsRepository;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private ProductRepository productRepository;
//
//    @InjectMocks
//    private DibsService dibsService;
//
//    @Test
//    @DisplayName("찜 추가 성공 테스트")
//    void addDibsSuccessTest() {
//        // Given
//        Long userId = 1L;
//        Long productId = 10L;
//        User user = User.builder().userId(userId).build();
//        Product product = Product.builder().productId(productId).title("테스트 상품").price(10000).build();
//        Dibs savedDibs = Dibs.builder()
//                .dibsId(100L)
//                .user(user)
//                .product(product)
//                .addedAt(LocalDateTime.now())
//                .build();
//
//        when(dibsRepository.existsByUser_UserIdAndProduct_ProductId(userId, productId)).thenReturn(false);
//        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
//        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
//        when(dibsRepository.save(any(Dibs.class))).thenReturn(savedDibs);
//
//        // When
//        DibsDTO.Response response = dibsService.addDibs(userId, productId);
//
//        // Then
//        assertThat(response.getProductId()).isEqualTo(productId);
//        assertThat(response.getIsLiked()).isTrue();
//        assertThat(response.getLikedAt()).isNotNull();
//        verify(dibsRepository, times(1)).existsByUser_UserIdAndProduct_ProductId(userId, productId);
//        verify(userRepository, times(1)).findById(userId);
//        verify(productRepository, times(1)).findById(productId);
//        verify(dibsRepository, times(1)).save(any(Dibs.class));
//    }
//
//    @Test
//    @DisplayName("찜 추가 실패 테스트 - 이미 찜한 상품")
//    void addDibsAlreadyExistsFailTest() {
//        // Given
//        Long userId = 1L;
//        Long productId = 10L;
//        when(dibsRepository.existsByUser_UserIdAndProduct_ProductId(userId, productId)).thenReturn(true);
//
//        // When & Then
//        BusinessException exception = assertThrows(BusinessException.class, () -> dibsService.addDibs(userId, productId));
//        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DIBS_ALREADY_EXISTS);
//        verify(dibsRepository, times(1)).existsByUser_UserIdAndProduct_ProductId(userId, productId);
//        verify(userRepository, never()).findById(anyLong());
//        verify(productRepository, never()).findById(anyLong());
//        verify(dibsRepository, never()).save(any(Dibs.class));
//    }
//
//    @Test
//    @DisplayName("찜 추가 실패 테스트 - 사용자 없음")
//    void addDibsUserNotFoundFailTest() {
//        // Given
//        Long userId = 1L;
//        Long productId = 10L;
//        when(dibsRepository.existsByUser_UserIdAndProduct_ProductId(userId, productId)).thenReturn(false);
//        when(userRepository.findById(userId)).thenReturn(Optional.empty());
//
//        // When & Then
//        BusinessException exception = assertThrows(BusinessException.class, () -> dibsService.addDibs(userId, productId));
//        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
//        assertThat(exception.getMessage()).isEqualTo(ErrorCode.USER_NOT_FOUND.formatMessage(userId));
//        verify(dibsRepository, times(1)).existsByUser_UserIdAndProduct_ProductId(userId, productId);
//        verify(userRepository, times(1)).findById(userId);
//        verify(productRepository, never()).findById(anyLong());
//        verify(dibsRepository, never()).save(any(Dibs.class));
//    }
//
//    @Test
//    @DisplayName("찜 추가 실패 테스트 - 상품 없음")
//    void addDibsProductNotFoundFailTest() {
//        // Given
//        Long userId = 1L;
//        Long productId = 10L;
//        User user = User.builder().userId(userId).build();
//        when(dibsRepository.existsByUser_UserIdAndProduct_ProductId(userId, productId)).thenReturn(false);
//        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
//        when(productRepository.findById(productId)).thenReturn(Optional.empty());
//
//        // When & Then
//        BusinessException exception = assertThrows(BusinessException.class, () -> dibsService.addDibs(userId, productId));
//        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
//        assertThat(exception.getMessage()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND.formatMessage(productId));
//        verify(dibsRepository, times(1)).existsByUser_UserIdAndProduct_ProductId(userId, productId);
//        verify(userRepository, times(1)).findById(userId);
//        verify(productRepository, times(1)).findById(productId);
//        verify(dibsRepository, never()).save(any(Dibs.class));
//    }
//
//    @Test
//    @DisplayName("찜 제거 성공 테스트")
//    void removeDibsSuccessTest() {
//        // Given
//        Long userId = 1L;
//        Long productId = 10L;
//        when(dibsRepository.existsByUser_UserIdAndProduct_ProductId(userId, productId)).thenReturn(true);
//        doNothing().when(dibsRepository).deleteByUser_UserIdAndProduct_ProductId(userId, productId);
//
//        // When
//        dibsService.removeDibs(userId, productId);
//
//        // Then
//        verify(dibsRepository, times(1)).existsByUser_UserIdAndProduct_ProductId(userId, productId);
//        verify(dibsRepository, times(1)).deleteByUser_UserIdAndProduct_ProductId(userId, productId);
//    }
//
//    @Test
//    @DisplayName("찜 제거 실패 테스트 - 찜 내역 없음")
//    void removeDibsNotFoundFailTest() {
//        // Given
//        Long userId = 1L;
//        Long productId = 10L;
//        when(dibsRepository.existsByUser_UserIdAndProduct_ProductId(userId, productId)).thenReturn(false);
//
//        // When & Then
//        BusinessException exception = assertThrows(BusinessException.class, () -> dibsService.removeDibs(userId, productId));
//        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DIBS_NOT_FOUND);
//        verify(dibsRepository, times(1)).existsByUser_UserIdAndProduct_ProductId(userId, productId);
//        verify(dibsRepository, never()).deleteByUser_UserIdAndProduct_ProductId(anyLong(), anyLong());
//    }
//
//    @Test
//    @DisplayName("찜 상태 조회 - 찜 O")
//    void getDibsStatusLikedTest() {
//        // Given
//        Long userId = 1L;
//        Long productId = 10L;
//        User user = User.builder().userId(userId).build();
//        Product product = Product.builder().productId(productId).title("테스트 상품").price(10000).build();
//        Dibs dibs = Dibs.builder().dibsId(100L).user(user).product(product).addedAt(LocalDateTime.now()).build();
//
//        when(dibsRepository.existsByUser_UserIdAndProduct_ProductId(userId, productId)).thenReturn(true);
//        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
//        when(dibsRepository.findByProduct_ProductId(productId)).thenReturn(List.of(dibs));
//
//        // When
//        DibsDTO.Response response = dibsService.getDibsStatus(userId, productId);
//
//        // Then
//        assertThat(response.getProductId()).isEqualTo(productId);
//        assertThat(response.getIsLiked()).isTrue();
//        assertThat(response.getLikedAt()).isNotNull();
//        verify(dibsRepository, times(1)).existsByUser_UserIdAndProduct_ProductId(userId, productId);
//        verify(productRepository, times(1)).findById(productId);
//        verify(dibsRepository, times(1)).findByProduct_ProductId(productId);
//    }
//
//    @Test
//    @DisplayName("찜 상태 조회 - 찜 X")
//    void getDibsStatusNotLikedTest() {
//        // Given
//        Long userId = 1L;
//        Long productId = 10L;
//        Product product = Product.builder().productId(productId).title("테스트 상품").price(10000).build();
//
//        when(dibsRepository.existsByUser_UserIdAndProduct_ProductId(userId, productId)).thenReturn(false);
//        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
//
//        // When
//        DibsDTO.Response response = dibsService.getDibsStatus(userId, productId);
//
//        // Then
//        assertThat(response.getProductId()).isEqualTo(productId);
//        assertThat(response.getIsLiked()).isFalse();
//        assertThat(response.getLikedAt()).isNull();
//        verify(dibsRepository, times(1)).existsByUser_UserIdAndProduct_ProductId(userId, productId);
//        verify(productRepository, times(1)).findById(productId);
//        verify(dibsRepository, never()).findByProduct_ProductId(anyLong());
//    }
//
//    @Test
//    @DisplayName("찜 상태 조회 실패 테스트 - 상품 없음")
//    void getDibsStatusProductNotFoundFailTest() {
//        // Given
//        Long userId = 1L;
//        Long productId = 10L;
//        when(productRepository.findById(productId)).thenReturn(Optional.empty());
//
//        // When & Then
//        BusinessException exception = assertThrows(BusinessException.class, () -> dibsService.getDibsStatus(userId, productId));
//        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
//        assertThat(exception.getMessage()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND.formatMessage(productId));
//        verify(dibsRepository, never()).existsByUser_UserIdAndProduct_ProductId(anyLong(), anyLong());
//        verify(productRepository, times(1)).findById(productId);
//        verify(dibsRepository, never()).findByProduct_ProductId(anyLong());
//    }
//
//    @Test
//    @DisplayName("사용자 찜 목록 조회 성공 테스트")
//    void getUserDibsListSuccessTest() {
//        // Given
//        Long userId = 1L;
//        User user = User.builder().userId(userId).build();
//        Product product1 = Product.builder().productId(10L).title("상품1").price(10000).build();
//        Product product2 = Product.builder().productId(11L).title("상품2").price(20000).build();
//        List<Dibs> dibsList = Arrays.asList(
//                Dibs.builder().dibsId(100L).user(user).product(product1).addedAt(LocalDateTime.now()).build(),
//                Dibs.builder().dibsId(101L).user(user).product(product2).addedAt(LocalDateTime.now().minusHours(1)).build()
//        );
//
//        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
//        when(dibsRepository.findByUser_UserId(userId)).thenReturn(dibsList);
//
//        // When
//        List<DibsDTO.Response.DibbedProduct> result = dibsService.getUserDibsList(userId);
//
//        // Then
//        assertThat(result).hasSize(2);
//        assertThat(result.get(0).getProductId()).isEqualTo(10L);
//        assertThat(result.get(1).getProductId()).isEqualTo(11L);
//        verify(userRepository, times(1)).findById(userId);
//        verify(dibsRepository, times(1)).findByUser_UserId(userId);
//    }
//
//    @Test
//    @DisplayName("사용자 찜 목록 조회 실패 테스트 - 사용자 없음")
//    void getUserDibsListUserNotFoundFailTest() {
//        // Given
//        Long userId = 1L;
//        when(userRepository.findById(userId)).thenReturn(Optional.empty());
//
//        // When & Then
//        BusinessException exception = assertThrows(BusinessException.class, () -> dibsService.getUserDibsList(userId));
//        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
//        assertThat(exception.getMessage()).isEqualTo(ErrorCode.USER_NOT_FOUND.formatMessage(userId));
//        verify(userRepository, times(1)).findById(userId);
//        verify(dibsRepository, never()).findByUser_UserId(anyLong());
//    }
//
//    @Test
//    @DisplayName("상품 찜 개수 조회 성공 테스트")
//    void getDibsCountForProductSuccessTest() {
//        // Given
//        Long productId = 10L;
//        Product product = Product.builder().productId(productId).build();
//        List<Dibs> dibsList = Arrays.asList(
//                Dibs.builder().dibsId(100L).product(product).addedAt(LocalDateTime.now()).build(),
//                Dibs.builder().dibsId(101L).product(product).addedAt(LocalDateTime.now().minusHours(1)).build()
//        );
//
//        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
//        when(dibsRepository.findByProduct_ProductId(productId)).thenReturn(dibsList);
//
//        // When
//        long count = dibsService.getDibsCountForProduct(productId);
//
//        // Then
//        assertThat(count).isEqualTo(2);
//        verify(productRepository, times(1)).findById(productId);
//        verify(dibsRepository, times(1)).findByProduct_ProductId(productId);
//    }
//
//    @Test
//    @DisplayName("상품 찜 개수 조회 실패 테스트 - 상품 없음")
//    void getDibsCountForProductNotFoundFailTest() {
//        // Given
//        Long productId = 10L;
//        when(productRepository.findById(productId)).thenReturn(Optional.empty());
//
//        // When & Then
//        BusinessException exception = assertThrows(BusinessException.class, () -> dibsService.getDibsCountForProduct(productId));
//        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
//        assertThat(exception.getMessage()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND.formatMessage(productId));
//        verify(productRepository, times(1)).findById(productId);
//        verify(dibsRepository, never()).findByProduct_ProductId(anyLong());
//    }
//}
