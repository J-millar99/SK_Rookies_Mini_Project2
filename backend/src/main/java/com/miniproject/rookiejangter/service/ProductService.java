package com.miniproject.rookiejangter.service;

import com.miniproject.rookiejangter.controller.dto.ProductDTO;
import com.miniproject.rookiejangter.entity.*;
import com.miniproject.rookiejangter.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ImageRepository imageRepository;
    private final DibsRepository dibsRepository;
    private final BumpRepository bumpRepository;

    @Transactional
    public ProductDTO.Response createProduct(ProductDTO.Request requestDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + userId));
        Category category = categoryRepository.findById(requestDto.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을 수 없습니다: " + requestDto.getCategoryId()));

        Product product = Product.builder()
                .user(user)
                .category(category)
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .price(requestDto.getPrice())
                .viewCount(0)
                .isBumped(false)
                .isReserved(false)
                .isCompleted(false)
                .build();
        Product savedProduct = productRepository.save(product);

        List<Image> savedImages = new ArrayList<>();
        if (requestDto.getImages() != null && !requestDto.getImages().isEmpty()) {
            for (String imageUrl : requestDto.getImages()) {
                Image image = Image.builder()
                        .product(savedProduct)
                        .imageUrl(imageUrl)
                        .build();
                savedImages.add(imageRepository.save(image));
            }
        }
        return mapToProductDTOResponse(savedProduct, savedImages, userId);
    }

    @Transactional
    public ProductDTO.Response getProductById(Long productId, Long currentUserId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다: " + productId));

        product.setViewCount(product.getViewCount() + 1);
        productRepository.save(product);

        List<Image> images = imageRepository.findByProduct_ProductId(productId);
        return mapToProductDTOResponse(product, images, currentUserId);
    }

    @Transactional
    public ProductDTO.Response updateProduct(Long productId, ProductDTO.UpdateRequest requestDto, Long userId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다: " + productId));

        if (!product.getUser().getUserId().equals(userId)) {
            throw new SecurityException("게시글 수정 권한이 없습니다.");
        }

        if (requestDto.getTitle() != null) product.setTitle(requestDto.getTitle());
        if (requestDto.getContent() != null) product.setContent(requestDto.getContent());
        if (requestDto.getPrice() != null) product.setPrice(requestDto.getPrice());


        if (requestDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(requestDto.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을 수 없습니다: " + requestDto.getCategoryId()));
            product.setCategory(category);
        }

        List<Image> updatedImages;
        if (requestDto.getImages() != null) {
            imageRepository.deleteAll(imageRepository.findByProduct_ProductId(productId));
            updatedImages = new ArrayList<>();
            for (String imageUrl : requestDto.getImages()) {
                Image image = Image.builder()
                        .product(product)
                        .imageUrl(imageUrl)
                        .build();
                updatedImages.add(imageRepository.save(image));
            }
        } else {
            updatedImages = imageRepository.findByProduct_ProductId(productId);
        }

        Product updatedProduct = productRepository.save(product);
        return mapToProductDTOResponse(updatedProduct, updatedImages, userId);
    }

    @Transactional
    public void deleteProduct(Long productId, Long userId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다: " + productId));

        if (!product.getUser().getUserId().equals(userId)) {
            throw new SecurityException("게시글 삭제 권한이 없습니다.");
        }

        imageRepository.deleteAll(imageRepository.findByProduct_ProductId(productId));
        dibsRepository.deleteAll(dibsRepository.findByProduct_ProductId(productId));
        bumpRepository.deleteAll(bumpRepository.findByProduct_ProductId(productId));


        productRepository.delete(product);
    }

    @Transactional(readOnly = true)
    public ProductDTO.ProductListData getAllProducts(Pageable pageable, Long currentUserId) {
        Page<Product> productPage = productRepository.findAllByOrderByCreatedAtDesc(pageable); //
        return convertToProductListData(productPage, currentUserId);
    }

    @Transactional(readOnly = true)
    public ProductDTO.ProductListData getProductsByCategory(Integer categoryId, Pageable pageable, Long currentUserId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을 수 없습니다: " + categoryId));
        Page<Product> productPage = productRepository.findByCategory(category, pageable); //
        return convertToProductListData(productPage, currentUserId);
    }

    @Transactional(readOnly = true)
    public ProductDTO.ProductListData getProductsByUser(Long targetUserId, Pageable pageable, Long currentUserId) {
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + targetUserId));
        Page<Product> productPage = productRepository.findByUser(user, pageable); //
        return convertToProductListData(productPage, currentUserId);
    }

    @Transactional(readOnly = true)
    public ProductDTO.ProductListData searchProductsByTitle(String title, Pageable pageable, Long currentUserId) {
        List<Product> productList = productRepository.findByTitleContainsIgnoreCase(title); //

        Page<Product> productPage = paginateList(productList, pageable);
        return convertToProductListData(productPage, currentUserId);
    }

    @Transactional(readOnly = true)
    public ProductDTO.ProductListData searchProductsByKeyword(String keyword, Pageable pageable, Long currentUserId) {
        List<Product> productList = productRepository.findByTitleContainsIgnoreCaseOrContentContainsIgnoreCase(keyword, keyword);

        Page<Product> productPage = paginateList(productList, pageable);
        return convertToProductListData(productPage, currentUserId);
    }

    private Page<Product> paginateList(List<Product> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        if (start > end) {
            return new PageImpl<>(Collections.emptyList(), pageable, list.size());
        }
        return new PageImpl<>(list.subList(start, end), pageable, list.size());
    }

    private ProductDTO.ProductListData convertToProductListData(Page<Product> productPage, Long currentUserId) {
        List<ProductDTO.Response> productResponses = productPage.getContent().stream()
                .map(product -> {
                    List<Image> images = imageRepository.findByProduct_ProductId(product.getProductId());
                    return mapToProductDTOResponse(product, images, currentUserId);
                })
                .collect(Collectors.toList());

        ProductDTO.ProductListPagination pagination = ProductDTO.ProductListPagination.builder()
                .page(productPage.getNumber())
                .size(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .first(productPage.isFirst())
                .last(productPage.isLast())
                .build();

        return ProductDTO.ProductListData.builder()
                .content(productResponses)
                .pagination(pagination)
                .build();
    }

    private ProductDTO.Response mapToProductDTOResponse(Product product, List<Image> images, Long currentUserId) {
        List<ProductDTO.ImageResponse> imageResponses = images.stream()
                .map(ProductDTO.ImageResponse::fromEntity)
                .collect(Collectors.toList());

        boolean isLiked = false;
        if (currentUserId != null) {
            isLiked = dibsRepository.existsByUser_UserIdAndProduct_ProductId(currentUserId, product.getProductId());
        }
        long likeCount = dibsRepository.findByProduct_ProductId(product.getProductId()).size();


        return ProductDTO.Response.builder()
                .id(product.getProductId())
                .title(product.getTitle())
                .content(product.getContent())
                .price(product.getPrice())
                .categoryName(product.getCategory() != null ? product.getCategory().getCategoryName() : null)
                .status(product.getIsCompleted() != null && product.getIsCompleted() ? "COMPLETED" :
                        product.getIsReserved() != null && product.getIsReserved() ? "RESERVED" : "SALE")
                .images(imageResponses)
                .seller(ProductDTO.SellerInfo.fromEntity(product.getUser()))
                .createdAt(product.getCreatedAt().atOffset(java.time.ZoneOffset.UTC))
                .updatedAt(product.getUpdatedAt() != null ? product.getUpdatedAt().atOffset(java.time.ZoneOffset.UTC) : null)
                .viewCount(product.getViewCount())
                .likeCount((int) likeCount)
                .isLiked(isLiked)
                .build();
    }

    @Transactional
    public void updateProductStatus(Long productId, Boolean isReserved, Boolean isCompleted, Long userId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다: " + productId));

        if (!product.getUser().getUserId().equals(userId)) {
            throw new SecurityException("게시글 상태 변경 권한이 없습니다.");
        }
        if (isReserved != null) {
            product.setIsReserved(isReserved);
        }
        if (isCompleted != null) {
            product.setIsCompleted(isCompleted);
        }
        productRepository.save(product);
    }
}