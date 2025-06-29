package com.miniproject.rookiejangter.service;

import com.miniproject.rookiejangter.dto.ImageDTO;
import com.miniproject.rookiejangter.entity.Image;
import com.miniproject.rookiejangter.entity.Product;
import com.miniproject.rookiejangter.exception.BusinessException;
import com.miniproject.rookiejangter.exception.ErrorCode;
import com.miniproject.rookiejangter.repository.ImageRepository;
import com.miniproject.rookiejangter.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final ProductRepository productRepository;

    /**
     * 상품에 이미지를 추가합니다.
     *
     * @param productId 상품 ID
     * @param imageUrl 이미지 URL
     * @return 생성된 이미지 정보
     */
    @Transactional
    public ImageDTO.Response createImage(Long productId, String imageUrl) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, productId));

        Image image = Image.builder()
                .product(product)
                .imageUrl(imageUrl)
                .build();
        Image savedImage = imageRepository.save(image);
        return ImageDTO.Response.fromEntity(savedImage);
    }

    /**
     * 상품의 이미지를 ID로 조회합니다.
     *
     * @param imageId 이미지 ID
     * @return 이미지 정보
     */
    @Transactional(readOnly = true)
    public ImageDTO.Response getImageByImageId(Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.IMAGE_NOT_FOUND, imageId));
        return ImageDTO.Response.fromEntity(image);
    }

    /**
     * 특정 상품의 모든 이미지를 조회합니다.
     *
     * @param productId 상품 ID
     * @return 이미지 리스트
     */
    @Transactional(readOnly = true)
    public List<ImageDTO.Response> getImagesByProductId(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, productId);
        }
        return imageRepository.findByProduct_ProductId(productId).stream()
                .map(ImageDTO.Response::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 특정 이미지의 URL을 수정합니다.
     *
     * @param imageId 이미지 ID
     * @param newImageUrl 새로운 이미지 URL
     * @return 수정된 이미지 정보
     */
    @Transactional
    public void deleteImage(Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.IMAGE_NOT_FOUND, imageId));
        imageRepository.delete(image);
    }

    /**
     * 특정 상품의 모든 이미지를 삭제합니다.
     *
     * @param productId 상품 ID
     */
    @Transactional
    public void deleteImagesByProductId(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, productId);
        }
        List<Image> images = imageRepository.findByProduct_ProductId(productId);
        imageRepository.deleteAll(images);
    }
}