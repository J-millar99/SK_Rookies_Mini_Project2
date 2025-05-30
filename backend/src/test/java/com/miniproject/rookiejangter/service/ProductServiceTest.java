package com.miniproject.rookiejangter.service;

import com.miniproject.rookiejangter.controller.dto.ProductDTO;
import com.miniproject.rookiejangter.entity.*;
import com.miniproject.rookiejangter.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ImageRepository imageRepository;
    @Mock
    private DibsRepository dibsRepository;
    @Mock
    private BumpRepository bumpRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("상품 생성 성공 테스트")
    void createProductSuccessTest() {
        // Given
        Long userId = 1L;
        ProductDTO.Request requestDto = ProductDTO.Request.builder()
                .title("테스트 상품")
                .content("테스트 내용")
                .price(10000)
                .categoryId(1)
                .images(List.of("url1", "url2"))
                .build();
        User user = User.builder().userId(userId).build();
        Category category = Category.builder().categoryId(1).categoryName("전자기기").build();
        Product savedProduct = Product.builder()
                .productId(10L)
                .user(user)
                .category(category)
                .title("테스트 상품")
                .content("테스트 내용")
                .price(10000)
                .viewCount(0)
                .isBumped(false)
                .isReserved(false)
                .isCompleted(false)
                .createdAt(LocalDateTime.now())
                .build();
        Image image1 = Image.builder().imageId(100L).product(savedProduct).imageUrl("url1").build();
        Image image2 = Image.builder().imageId(101L).product(savedProduct).imageUrl("url2").build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        when(imageRepository.save(any(Image.class))).thenReturn(image1, image2);

        // When
        ProductDTO.Response response = productService.createProduct(requestDto, userId);

        // Then
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getTitle()).isEqualTo("테스트 상품");
        assertThat(response.getImages()).hasSize(2);
        verify(userRepository, times(1)).findById(userId);
        verify(categoryRepository, times(1)).findById(1);
        verify(productRepository, times(1)).save(any(Product.class));
        verify(imageRepository, times(2)).save(any(Image.class));
    }

    @Test
    @DisplayName("상품 생성 실패 테스트 - 사용자 없음")
    void createProductUserNotFoundFailTest() {
        // Given
        Long userId = 1L;
        ProductDTO.Request requestDto = ProductDTO.Request.builder().categoryId(1).build();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> productService.createProduct(requestDto, userId));
        verify(userRepository, times(1)).findById(userId);
        verify(categoryRepository, never()).findById(anyInt());
        verify(productRepository, never()).save(any());
        verify(imageRepository, never()).save(any());
    }

    @Test
    @DisplayName("상품 생성 실패 테스트 - 카테고리 없음")
    void createProductCategoryNotFoundFailTest() {
        // Given
        Long userId = 1L;
        ProductDTO.Request requestDto = ProductDTO.Request.builder().categoryId(1).build();
        User user = User.builder().userId(userId).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> productService.createProduct(requestDto, userId));
        verify(userRepository, times(1)).findById(userId);
        verify(categoryRepository, times(1)).findById(1);
        verify(productRepository, never()).save(any());
        verify(imageRepository, never()).save(any());
    }

    @Test
    @DisplayName("상품 ID로 조회 성공 테스트")
    void getProductByIdSuccessTest() {
        // Given
        Long productId = 10L;
        Long currentUserId = 2L;
        User seller = User.builder().userId(1L).userName("판매자").build();
        Category category = Category.builder().categoryId(1).categoryName("전자기기").build();
        Product product = Product.builder()
                .productId(productId)
                .user(seller)
                .category(category)
                .title("테스트 상품")
                .content("테스트 내용")
                .price(10000)
                .viewCount(0)
                .createdAt(LocalDateTime.now())
                .build();
        Image image1 = Image.builder().imageId(100L).product(product).imageUrl("url1").build();
        Image image2 = Image.builder().imageId(101L).product(product).imageUrl("url2").build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(imageRepository.findByProduct_ProductId(productId)).thenReturn(List.of(image1, image2));
        when(dibsRepository.findByProduct_ProductId(productId)).thenReturn(Collections.emptyList());
        when(dibsRepository.existsByUser_UserIdAndProduct_ProductId(currentUserId, productId)).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product); // 조회 시 viewCount 증가 후 저장

        // When
        ProductDTO.Response response = productService.getProductById(productId, currentUserId);

        // Then
        assertThat(response.getId()).isEqualTo(productId);
        assertThat(response.getViewCount()).isEqualTo(1);
        assertThat(response.getImages()).hasSize(2);
        verify(productRepository, times(1)).findById(productId);
        verify(imageRepository, times(1)).findByProduct_ProductId(productId);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("상품 ID로 조회 실패 테스트 - 상품 없음")
    void getProductByIdNotFoundFailTest() {
        // Given
        Long productId = 10L;
        Long currentUserId = 2L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> productService.getProductById(productId, currentUserId));
        verify(productRepository, times(1)).findById(productId);
        verify(imageRepository, never()).findByProduct_ProductId(anyLong());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("상품 수정 성공 테스트")
    void updateProductSuccessTest() {
        // Given
        Long productId = 10L;
        Long userId = 1L;
        ProductDTO.UpdateRequest requestDto = ProductDTO.UpdateRequest.builder()
                .title("수정된 제목")
                .content("수정된 내용")
                .price(20000)
                .categoryId(2)
                .images(List.of("updatedUrl1"))
                .build();
        User user = User.builder().userId(userId).build();
        Category originalCategory = Category.builder().categoryId(1).categoryName("전자기기").build();
        Category updatedCategory = Category.builder().categoryId(2).categoryName("의류").build();
        Product originalProduct = Product.builder()
                .productId(productId)
                .user(user)
                .category(originalCategory)
                .title("테스트 상품")
                .content("테스트 내용")
                .price(10000)
                .createdAt(LocalDateTime.now())
                .build();
        Image originalImage = Image.builder().imageId(100L).product(originalProduct).imageUrl("url1").build();
        Image updatedImage = Image.builder().imageId(102L).product(originalProduct).imageUrl("updatedUrl1").build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(originalProduct));
        when(categoryRepository.findById(2)).thenReturn(Optional.of(updatedCategory));
        doNothing().when(imageRepository).deleteAll(anyList());
        when(imageRepository.save(any(Image.class))).thenReturn(updatedImage);
        when(productRepository.save(any(Product.class))).thenReturn(originalProduct); // 수정된 product 반환하도록

        // When
        ProductDTO.Response response = productService.updateProduct(productId, requestDto, userId);

        // Then
        assertThat(response.getId()).isEqualTo(productId);
        assertThat(response.getTitle()).isEqualTo("수정된 제목");
        assertThat(response.getContent()).isEqualTo("수정된 내용");
        assertThat(response.getPrice()).isEqualTo(20000);
        assertThat(response.getCategoryName()).isEqualTo("의류");
        assertThat(response.getImages()).hasSize(1);
        assertThat(response.getImages().get(0).getImageUrl()).isEqualTo("updatedUrl1");
        verify(productRepository, times(1)).findById(productId);
        verify(categoryRepository, times(1)).findById(2);
        verify(imageRepository, times(1)).deleteAll(anyList());
        verify(imageRepository, times(1)).save(any(Image.class));
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("상품 수정 실패 테스트 - 상품 없음")
    void updateProductNotFoundFailTest() {
        // Given
        Long productId = 10L;
        Long userId = 1L;
        ProductDTO.UpdateRequest requestDto = ProductDTO.UpdateRequest.builder().title("수정").build();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> productService.updateProduct(productId, requestDto, userId));
        verify(productRepository, times(1)).findById(productId);
        verify(categoryRepository, never()).findById(anyInt());
        verify(imageRepository, never()).deleteAll(anyList());
        verify(imageRepository, never()).save(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("상품 수정 실패 테스트 - 권한 없음")
    void updateProductNoPermissionFailTest() {
        // Given
        Long productId = 10L;
        Long userId = 1L;
        Long otherUserId = 2L;
        ProductDTO.UpdateRequest requestDto = ProductDTO.UpdateRequest.builder().title("수정").build();
        User user = User.builder().userId(otherUserId).build();
        Product product = Product.builder().productId(productId).user(user).build();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // When & Then
        assertThrows(SecurityException.class, () -> productService.updateProduct(productId, requestDto, userId));
        verify(productRepository, times(1)).findById(productId);
        verify(categoryRepository, never()).findById(anyInt());
        verify(imageRepository, never()).deleteAll(anyList());
        verify(imageRepository, never()).save(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("상품 삭제 성공 테스트")
    void deleteProductSuccessTest() {
        // Given
        Long productId = 10L;
        Long userId = 1L;
        User user = User.builder().userId(userId).build();
        Product product = Product.builder().productId(productId).user(user).build();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        doNothing().when(imageRepository).deleteAll(anyList());
        doNothing().when(dibsRepository).deleteAll(anyList());
        doNothing().when(bumpRepository).deleteAll(anyList());
        doNothing().when(productRepository).delete(product);

        // When
        productService.deleteProduct(productId, userId);

        // Then
        verify(productRepository, times(1)).findById(productId);
        verify(imageRepository, times(1)).deleteAll(anyList());
        verify(dibsRepository, times(1)).deleteAll(anyList());
        verify(bumpRepository, times(1)).deleteAll(anyList());
        verify(productRepository, times(1)).delete(product);
    }

    @Test
    @DisplayName("상품 삭제 실패 테스트 - 상품 없음")
    void deleteProductNotFoundFailTest() {
        // Given
        Long productId = 10L;
        Long userId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> productService.deleteProduct(productId, userId));
        verify(productRepository, times(1)).findById(productId);
        verify(imageRepository, never()).deleteAll(anyList());
        verify(dibsRepository, never()).deleteAll(anyList());
        verify(bumpRepository, never()).deleteAll(anyList());
        verify(productRepository, never()).delete(any());
    }

    @Test
    @DisplayName("상품 삭제 실패 테스트 - 권한 없음")
    void deleteProductNoPermissionFailTest() {
        // Given
        Long productId = 10L;
        Long userId = 1L;
        Long otherUserId = 2L;
        User user = User.builder().userId(otherUserId).build();
        Product product = Product.builder().productId(productId).user(user).build();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // When & Then
        assertThrows(SecurityException.class, () -> productService.deleteProduct(productId, userId));
        verify(productRepository, times(1)).findById(productId);
        verify(imageRepository, never()).deleteAll(anyList());
        verify(dibsRepository, never()).deleteAll(anyList());
        verify(bumpRepository, never()).deleteAll(anyList());
        verify(productRepository, never()).delete(any());
    }

    @Test
    @DisplayName("모든 상품 조회 (페이징) 성공 테스트")
    void getAllProductsSuccessTest() {
        // Given
        Long currentUserId = 1L;
        User seller = User.builder().userId(2L).userName("판매자").build();
        Category category = Category.builder().categoryId(1).categoryName("전자기기").build();
        Product product1 = Product.builder().productId(10L).user(seller).category(category).title("상품1").price(1000).createdAt(LocalDateTime.now()).build();
        Product product2 = Product.builder().productId(11L).user(seller).category(category).title("상품2").price(2000).createdAt(LocalDateTime.now().minusHours(1)).build();
        List<Product> products = Arrays.asList(product1, product2);
        Page<Product> productPage = new PageImpl<>(products, Pageable.ofSize(2), products.size());
        Image image1 = Image.builder().imageId(100L).product(product1).imageUrl("url1").build();
        Image image2 = Image.builder().imageId(100L).product(product2).imageUrl("url2").build();

        when(productRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(productPage);
        when(imageRepository.findByProduct_ProductId(10L)).thenReturn(List.of(image1));
        when(imageRepository.findByProduct_ProductId(11L)).thenReturn(List.of(image2));
        when(dibsRepository.findByProduct_ProductId(10L)).thenReturn(Collections.emptyList());
        when(dibsRepository.findByProduct_ProductId(11L)).thenReturn(Collections.emptyList());
        when(dibsRepository.existsByUser_UserIdAndProduct_ProductId(currentUserId, 10L)).thenReturn(false);
        when(dibsRepository.existsByUser_UserIdAndProduct_ProductId(currentUserId, 11L)).thenReturn(false);

        // When
        ProductDTO.ProductListData result = productService.getAllProducts(Pageable.ofSize(2), currentUserId);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getPagination().getTotalElements()).isEqualTo(2);
        verify(productRepository, times(1)).findAllByOrderByCreatedAtDesc(any(Pageable.class));
        verify(imageRepository, times(1)).findByProduct_ProductId(10L);
        verify(imageRepository, times(1)).findByProduct_ProductId(11L);
        verify(dibsRepository, times(1)).findByProduct_ProductId(10L);
        verify(dibsRepository, times(1)).findByProduct_ProductId(11L);
        verify(dibsRepository, times(1)).existsByUser_UserIdAndProduct_ProductId(currentUserId, 10L);
        verify(dibsRepository, times(1)).existsByUser_UserIdAndProduct_ProductId(currentUserId, 11L);
    }

    @Test
    @DisplayName("카테고리별 상품 조회 (페이징) 성공 테스트")
    void getProductsByCategorySuccessTest() {
        // Given
        int categoryId = 1;
        Long currentUserId = 1L;
        Category category = Category.builder().categoryId(categoryId).categoryName("전자기기").build();
        User seller = User.builder().userId(2L).userName("판매자").build();
        Product product1 = Product.builder().productId(10L).user(seller).category(category).title("상품1").price(1000).createdAt(LocalDateTime.now()).build();
        Product product2 = Product.builder().productId(11L).user(seller).category(category).title("상품2").price(2000).createdAt(LocalDateTime.now().minusHours(1)).build();
        List<Product> products = Arrays.asList(product1, product2);
        Page<Product> productPage = new PageImpl<>(products, Pageable.ofSize(2), products.size());
        Image image1 = Image.builder().imageId(100L).product(product1).imageUrl("url1").build();
        Image image2 = Image.builder().imageId(101L).product(product2).imageUrl("url2").build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.findByCategory(eq(category), any(Pageable.class))).thenReturn(productPage);
        when(imageRepository.findByProduct_ProductId(10L)).thenReturn(List.of(image1));
        when(imageRepository.findByProduct_ProductId(11L)).thenReturn(List.of(image2));
        when(dibsRepository.findByProduct_ProductId(10L)).thenReturn(Collections.emptyList());
        when(dibsRepository.findByProduct_ProductId(11L)).thenReturn(Collections.emptyList());
        when(dibsRepository.existsByUser_UserIdAndProduct_ProductId(currentUserId, 10L)).thenReturn(false);
        when(dibsRepository.existsByUser_UserIdAndProduct_ProductId(currentUserId, 11L)).thenReturn(false);

        // When
        ProductDTO.ProductListData result = productService.getProductsByCategory(categoryId, Pageable.ofSize(2), currentUserId);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getPagination().getTotalElements()).isEqualTo(2);
        verify(categoryRepository, times(1)).findById(categoryId);
        verify(productRepository, times(1)).findByCategory(eq(category), any(Pageable.class));
        verify(imageRepository, times(1)).findByProduct_ProductId(10L);
        verify(imageRepository, times(1)).findByProduct_ProductId(11L);
        verify(dibsRepository, times(1)).findByProduct_ProductId(10L);
        verify(dibsRepository, times(1)).findByProduct_ProductId(11L);
        verify(dibsRepository, times(1)).existsByUser_UserIdAndProduct_ProductId(currentUserId, 10L);
        verify(dibsRepository, times(1)).existsByUser_UserIdAndProduct_ProductId(currentUserId, 11L);
    }

    @Test
    @DisplayName("카테고리별 상품 조회 실패 테스트 - 카테고리 없음")
    void getProductsByCategoryNotFoundFailTest() {
        // Given
        int categoryId = 1;
        Long currentUserId = 1L;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> productService.getProductsByCategory(categoryId, Pageable.ofSize(2), currentUserId));
        verify(categoryRepository, times(1)).findById(categoryId);
        verify(productRepository, never()).findByCategory(any(), any());
        verify(imageRepository, never()).findByProduct_ProductId(anyLong());
        verify(dibsRepository, never()).findByProduct_ProductId(anyLong());
        verify(dibsRepository, never()).existsByUser_UserIdAndProduct_ProductId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("사용자별 상품 조회 (페이징) 성공 테스트")
    void getProductsByUserSuccessTest() {
        // Given
        Long targetUserId = 2L;
        Long currentUserId = 1L;
        User seller = User.builder().userId(targetUserId).userName("판매자").build();
        Category category = Category.builder().categoryId(1).categoryName("전자기기").build();
        Product product1 = Product.builder().productId(10L).user(seller).category(category).title("상품1").price(1000).createdAt(LocalDateTime.now()).build();
        Product product2 = Product.builder().productId(11L).user(seller).category(category).title("상품2").price(2000).createdAt(LocalDateTime.now().minusHours(1)).build();
        List<Product> products = Arrays.asList(product1, product2);
        Page<Product> productPage = new PageImpl<>(products, Pageable.ofSize(2), products.size());
        Image image1 = Image.builder().imageId(100L).product(product1).imageUrl("url1").build();
        Image image2 = Image.builder().imageId(101L).product(product2).imageUrl("url2").build();

        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(seller));
        when(productRepository.findByUser(eq(seller), any(Pageable.class))).thenReturn(productPage);
        when(imageRepository.findByProduct_ProductId(10L)).thenReturn(List.of(image1));
        when(imageRepository.findByProduct_ProductId(11L)).thenReturn(List.of(image2));
        when(dibsRepository.findByProduct_ProductId(10L)).thenReturn(Collections.emptyList());
        when(dibsRepository.findByProduct_ProductId(11L)).thenReturn(Collections.emptyList());
        when(dibsRepository.existsByUser_UserIdAndProduct_ProductId(currentUserId, 10L)).thenReturn(false);
        when(dibsRepository.existsByUser_UserIdAndProduct_ProductId(currentUserId, 11L)).thenReturn(false);

        // When
        ProductDTO.ProductListData result = productService.getProductsByUser(targetUserId, Pageable.ofSize(2), currentUserId);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getPagination().getTotalElements()).isEqualTo(2);
        verify(userRepository, times(1)).findById(targetUserId);
        verify(productRepository, times(1)).findByUser(eq(seller), any(Pageable.class));
        verify(imageRepository, times(1)).findByProduct_ProductId(10L);
        verify(imageRepository, times(1)).findByProduct_ProductId(11L);
        verify(dibsRepository, times(1)).findByProduct_ProductId(10L);
        verify(dibsRepository, times(1)).findByProduct_ProductId(11L);
        verify(dibsRepository, times(1)).existsByUser_UserIdAndProduct_ProductId(currentUserId, 10L);
        verify(dibsRepository, times(1)).existsByUser_UserIdAndProduct_ProductId(currentUserId, 11L);
    }

    @Test
    @DisplayName("사용자별 상품 조회 실패 테스트 - 사용자 없음")
    void getProductsByUserNotFoundFailTest() {
        // Given
        Long targetUserId = 2L;
        Long currentUserId = 1L;
        when(userRepository.findById(targetUserId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> productService.getProductsByUser(targetUserId, Pageable.ofSize(2), currentUserId));
        verify(userRepository, times(1)).findById(targetUserId);
        verify(productRepository, never()).findByUser(any(), any());
        verify(imageRepository, never()).findByProduct_ProductId(anyLong());
        verify(dibsRepository, never()).findByProduct_ProductId(anyLong());
        verify(dibsRepository, never()).existsByUser_UserIdAndProduct_ProductId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("제목으로 상품 검색 (페이징) 성공 테스트")
    void searchProductsByTitleSuccessTest() {
        // Given
        String title = "테스트";
        Long currentUserId = 1L;
        User seller = User.builder().userId(2L).userName("판매자").build();
        Category category = Category.builder().categoryId(1).categoryName("전자기기").build();
        Product product1 = Product.builder().productId(10L).user(seller).category(category).title("테스트 상품1").price(1000).createdAt(LocalDateTime.now()).build();
        Product product2 = Product.builder().productId(11L).user(seller).category(category).title("또 다른 테스트").price(2000).createdAt(LocalDateTime.now().minusHours(1)).build();
        List<Product> products = Arrays.asList(product1, product2);
        Page<Product> productPage = new PageImpl<>(products, Pageable.ofSize(2), products.size());
        Image image1 = Image.builder().imageId(100L).product(product1).imageUrl("url1").build();
        Image image2 = Image.builder().imageId(101L).product(product2).imageUrl("url2").build();

        when(productRepository.findByTitleContainsIgnoreCase(title)).thenReturn(products);
        when(imageRepository.findByProduct_ProductId(10L)).thenReturn(List.of(image1));
        when(imageRepository.findByProduct_ProductId(11L)).thenReturn(List.of(image2));
        when(dibsRepository.findByProduct_ProductId(10L)).thenReturn(Collections.emptyList());
        when(dibsRepository.findByProduct_ProductId(11L)).thenReturn(Collections.emptyList());
        when(dibsRepository.existsByUser_UserIdAndProduct_ProductId(currentUserId, 10L)).thenReturn(false);
        when(dibsRepository.existsByUser_UserIdAndProduct_ProductId(currentUserId, 11L)).thenReturn(false);

        // When
        ProductDTO.ProductListData result = productService.searchProductsByTitle(title, Pageable.ofSize(2), currentUserId);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getPagination().getTotalElements()).isEqualTo(2);
        verify(productRepository, times(1)).findByTitleContainsIgnoreCase(title);
        verify(imageRepository, times(1)).findByProduct_ProductId(10L);
        verify(imageRepository, times(1)).findByProduct_ProductId(11L);
        verify(dibsRepository, times(1)).findByProduct_ProductId(10L);
        verify(dibsRepository, times(1)).findByProduct_ProductId(11L);
        verify(dibsRepository, times(1)).existsByUser_UserIdAndProduct_ProductId(currentUserId, 10L);
        verify(dibsRepository, times(1)).existsByUser_UserIdAndProduct_ProductId(currentUserId, 11L);
    }

    @Test
    @DisplayName("키워드로 상품 검색 (페이징) 성공 테스트")
    void searchProductsByKeywordSuccessTest() {
        // Given
        String keyword = "테스트";
        Long currentUserId = 1L;
        User seller = User.builder().userId(2L).userName("판매자").build();
        Category category = Category.builder().categoryId(1).categoryName("전자기기").build();
        Product product1 = Product.builder().productId(10L).user(seller).category(category).title("테스트 상품1").content("이것은 테스트입니다.").price(1000).createdAt(LocalDateTime.now()).build();
        Product product2 = Product.builder().productId(11L).user(seller).category(category).title("다른 상품").content("테스트 내용 포함").price(2000).createdAt(LocalDateTime.now().minusHours(1)).build();
        List<Product> products = Arrays.asList(product1, product2);
        Page<Product> productPage = new PageImpl<>(products, Pageable.ofSize(2), products.size());
        Image image1 = Image.builder().imageId(100L).product(product1).imageUrl("url1").build();
        Image image2 = Image.builder().imageId(101L).product(product2).imageUrl("url2").build();

        when(productRepository.findByTitleContainsIgnoreCaseOrContentContainsIgnoreCase(keyword, keyword)).thenReturn(products);
        when(imageRepository.findByProduct_ProductId(10L)).thenReturn(List.of(image1));
        when(imageRepository.findByProduct_ProductId(11L)).thenReturn(List.of(image2));
        when(dibsRepository.findByProduct_ProductId(10L)).thenReturn(Collections.emptyList());
        when(dibsRepository.findByProduct_ProductId(11L)).thenReturn(Collections.emptyList());
        when(dibsRepository.existsByUser_UserIdAndProduct_ProductId(currentUserId, 10L)).thenReturn(false);
        when(dibsRepository.existsByUser_UserIdAndProduct_ProductId(currentUserId, 11L)).thenReturn(false);

        // When
        ProductDTO.ProductListData result = productService.searchProductsByKeyword(keyword, Pageable.ofSize(2), currentUserId);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getPagination().getTotalElements()).isEqualTo(2);
        verify(productRepository, times(1)).findByTitleContainsIgnoreCaseOrContentContainsIgnoreCase(keyword, keyword);
        verify(imageRepository, times(1)).findByProduct_ProductId(10L);
        verify(imageRepository, times(1)).findByProduct_ProductId(11L);
        verify(dibsRepository, times(1)).findByProduct_ProductId(10L);
        verify(dibsRepository, times(1)).findByProduct_ProductId(11L);
        verify(dibsRepository, times(1)).existsByUser_UserIdAndProduct_ProductId(currentUserId, 10L);
        verify(dibsRepository, times(1)).existsByUser_UserIdAndProduct_ProductId(currentUserId, 11L);
    }

    @Test
    @DisplayName("상품 상태 업데이트 성공 테스트 - 예약")
    void updateProductStatusReserveSuccessTest() {
        // Given
        Long productId = 10L;
        Long userId = 1L;
        User seller = User.builder().userId(userId).build();
        Product product = Product.builder().productId(productId).user(seller).isReserved(false).isCompleted(false).build();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        productService.updateProductStatus(productId, true, null, userId);

        // Then
        assertThat(product.getIsReserved()).isTrue();
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).save(product);
    }

    @Test
    @DisplayName("상품 상태 업데이트 성공 테스트 - 완료")
    void updateProductStatusCompleteSuccessTest() {
        // Given
        Long productId = 10L;
        Long userId = 1L;
        User seller = User.builder().userId(userId).build();
        Product product = Product.builder().productId(productId).user(seller).isReserved(false).isCompleted(false).build();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        productService.updateProductStatus(productId, null, true, userId);

        // Then
        assertThat(product.getIsCompleted()).isTrue();
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).save(product);
    }
    @Test
    @DisplayName("상품 상태 업데이트 실패 테스트 - 상품 없음")
    void updateProductStatusNotFoundFailTest() {
        // Given
        Long productId = 10L;
        Long userId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> productService.updateProductStatus(productId, true, null, userId));
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("상품 상태 업데이트 실패 테스트 - 권한 없음")
    void updateProductStatusNoPermissionFailTest() {
        // Given
        Long productId = 10L;
        Long userId = 1L;
        Long otherUserId = 2L;
        User seller = User.builder().userId(otherUserId).build();
        Product product = Product.builder().productId(productId).user(seller).build();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // When & Then
        assertThrows(SecurityException.class, () -> productService.updateProductStatus(productId, true, null, userId));
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, never()).save(any());
    }
}


