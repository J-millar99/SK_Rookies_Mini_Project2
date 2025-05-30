package com.miniproject.rookiejangter.service;

import com.miniproject.rookiejangter.controller.dto.ImageDTO;
import com.miniproject.rookiejangter.entity.Image;
import com.miniproject.rookiejangter.entity.Product;
import com.miniproject.rookiejangter.repository.ImageRepository;
import com.miniproject.rookiejangter.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ImageServiceTest {

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ImageService imageService;

    @Test
    @DisplayName("이미지 생성 성공 테스트")
    void createImageSuccessTest() {
        // Given
        Long productId = 1L;
        String imageUrl = "http://example.com/image.jpg";
        Product product = Product.builder().productId(productId).build();
        Image savedImage = Image.builder()
                .imageId(1L)
                .product(product)
                .imageUrl(imageUrl)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(imageRepository.save(any(Image.class))).thenReturn(savedImage);

        // When
        ImageDTO.Response response = imageService.createImage(productId, imageUrl);

        // Then
        assertThat(response.getImageId()).isEqualTo(1L);
        assertThat(response.getImageUrl()).isEqualTo(imageUrl);
        assertThat(response.getProductId()).isEqualTo(productId);
        verify(productRepository, times(1)).findById(productId);
        verify(imageRepository, times(1)).save(any(Image.class));
    }

    @Test
    @DisplayName("이미지 생성 실패 테스트 (Product Not Found)")
    void createImageProductNotFoundTest() {
        // Given
        Long productId = 1L;
        String imageUrl = "http://example.com/image.png";
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> imageService.createImage(productId, imageUrl));
        verify(productRepository, times(1)).findById(productId);
        verify(imageRepository, never()).save(any(Image.class));
    }

    @Test
    @DisplayName("Product ID로 이미지 목록 조회 성공 테스트")
    void getImagesByProductIdSuccessTest() {
        // Given
        Long productId = 1L;
        Product product = Product.builder().productId(productId).build();
        List<Image> images = Arrays.asList(
                Image.builder().imageId(1L).product(product).imageUrl("url1").build(),
                Image.builder().imageId(2L).product(product).imageUrl("url2").build()
        );
        when(productRepository.existsById(productId)).thenReturn(true);
        when(imageRepository.findByProduct_ProductId(productId)).thenReturn(images);

        // When
        List<ImageDTO.Response> responses = imageService.getImagesByProductId(productId);

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getImageUrl()).isEqualTo("url1");
        assertThat(responses.get(0).getProductId()).isEqualTo(productId);
        assertThat(responses.get(1).getImageUrl()).isEqualTo("url2");
        assertThat(responses.get(1).getProductId()).isEqualTo(productId);
        verify(productRepository, times(1)).existsById(productId);
        verify(imageRepository, times(1)).findByProduct_ProductId(productId);
    }

    @Test
    @DisplayName("Product ID로 이미지 목록 조회 실패 테스트 (Product Not Found)")
    void getImagesByProductIdNotFoundTest() {
        // Given
        Long productId = 1L;
        when(productRepository.existsById(productId)).thenReturn(false);

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> imageService.getImagesByProductId(productId));
        verify(productRepository, times(1)).existsById(productId);
        verify(imageRepository, never()).findByProduct_ProductId(anyLong());
    }

    @Test
    @DisplayName("이미지 삭제 성공 테스트")
    void deleteImageSuccessTest() {
        // Given
        Long imageId = 1L;
        Image image = Image.builder().imageId(imageId).imageUrl("url").build();
        when(imageRepository.findById(imageId)).thenReturn(Optional.of(image));
        doNothing().when(imageRepository).delete(image);

        // When
        imageService.deleteImage(imageId);

        // Then
        verify(imageRepository, times(1)).findById(imageId);
        verify(imageRepository, times(1)).delete(image);
    }

    @Test
    @DisplayName("이미지 삭제 실패 테스트 (Image Not Found)")
    void deleteImageNotFoundTest() {
        // Given
        Long invalidImageId = 999L;
        when(imageRepository.findById(invalidImageId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> imageService.deleteImage(invalidImageId));
        verify(imageRepository, times(1)).findById(invalidImageId);
        verify(imageRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Product ID로 모든 이미지 삭제 성공 테스트")
    void deleteImagesByProductIdSuccessTest() {
        // Given
        Long productId = 1L;
        Product product = Product.builder().productId(productId).build();
        List<Image> imagesToDelete = Arrays.asList(
                Image.builder().imageId(1L).product(product).imageUrl("url1").build(),
                Image.builder().imageId(2L).product(product).imageUrl("url2").build()
        );
        when(productRepository.existsById(productId)).thenReturn(true);
        when(imageRepository.findByProduct_ProductId(productId)).thenReturn(imagesToDelete);
        doNothing().when(imageRepository).deleteAll(imagesToDelete);

        // When
        imageService.deleteImagesByProductId(productId);

        // Then
        verify(productRepository, times(1)).existsById(productId);
        verify(imageRepository, times(1)).findByProduct_ProductId(productId);
        verify(imageRepository, times(1)).deleteAll(imagesToDelete);
    }

    @Test
    @DisplayName("Product ID로 모든 이미지 삭제 실패 테스트 (Product Not Found)")
    void deleteImagesByProductIdNotFoundTest() {
        // Given
        Long productId = 1L;
        when(productRepository.existsById(productId)).thenReturn(false);

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> imageService.deleteImagesByProductId(productId));
        verify(productRepository, times(1)).existsById(productId);
        verify(imageRepository, never()).findByProduct_ProductId(anyLong());
        verify(imageRepository, never()).deleteAll(any());
    }
}