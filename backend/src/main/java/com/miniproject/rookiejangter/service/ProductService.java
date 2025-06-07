package com.miniproject.rookiejangter.service;

import com.miniproject.rookiejangter.controller.dto.ProductDTO;
import com.miniproject.rookiejangter.entity.*;
import com.miniproject.rookiejangter.exception.BusinessException;
import com.miniproject.rookiejangter.exception.ErrorCode;
import com.miniproject.rookiejangter.repository.*;
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
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, userId));
        Category category = categoryRepository.findByCategoryId(requestDto.getCategoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND, requestDto.getCategoryId()));

        Product product = Product.builder()
                .category(category)
                .user(user)
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .price(requestDto.getPrice())
                .viewCount(0)
                .isBumped(false)
                .isReserved(false)
                .isCompleted(false)
                .build();
        Product savedProduct = productRepository.save(product);

        return mapToProductDTOResponse(savedProduct, userId);
    }

    @Transactional
    public ProductDTO.Response getProductById(Long productId, Long currentUserId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, productId));

        product.setViewCount(product.getViewCount() + 1);
        productRepository.save(product);

        List<Image> images = imageRepository.findByProduct_ProductId(productId);
        return mapToProductDTOResponse(product, currentUserId);
    }

    @Transactional
    public ProductDTO.Response updateProduct(Long productId, ProductDTO.UpdateRequest requestDto, Long userId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, productId));

        if (!product.getUser().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.PRODUCT_OPERATION_FORBIDDEN, "수정");
        }

        if (requestDto.getTitle() != null) product.setTitle(requestDto.getTitle());
        if (requestDto.getContent() != null) product.setContent(requestDto.getContent());
        if (requestDto.getPrice() != null) product.setPrice(requestDto.getPrice());

        if (requestDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(requestDto.getCategoryId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND, requestDto.getCategoryId()));
            product.setCategory(category);
        }

        Product updatedProduct = productRepository.save(product);
        return mapToProductDTOResponse(updatedProduct, userId);
    }

    @Transactional
    public void deleteProduct(Long productId, Long userId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, productId));

        if (!product.getUser().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.PRODUCT_OPERATION_FORBIDDEN, "삭제");
        }

        imageRepository.deleteAll(imageRepository.findByProduct_ProductId(productId));
        dibsRepository.deleteAll(dibsRepository.findByProduct_ProductId(productId));
        bumpRepository.deleteAll(bumpRepository.findByProduct_ProductId(productId));

        productRepository.delete(product);
    }

    @Transactional(readOnly = true)
    public ProductDTO.ProductListData getProductsByUser(Long targetUserId, Pageable pageable, Long currentUserId) {
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, targetUserId));
        Page<Product> productPage = productRepository.findByUser(user, pageable);
        return convertToProductListData(productPage, currentUserId);
    }

    @Transactional
    public ProductDTO.Response getUserProductById(Long productId, Long userId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, productId));

        // 해당 유저의 상품인지 확인
        if (!product.getUser().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.PRODUCT_OPERATION_FORBIDDEN, "조회");
        }

        List<Image> images = imageRepository.findByProduct_ProductId(productId);
        return mapToProductDTOResponse(product, userId);
    }

    @Transactional(readOnly = true)
    public ProductDTO.ProductListData getAllProducts(Pageable pageable, Long currentUserId) {
        Page<Product> productPage = productRepository.findAllByOrderByCreatedAtDesc(pageable);
        return convertToProductListData(productPage, currentUserId);
    }

    @Transactional(readOnly = true)
    public ProductDTO.ProductListData getProductsByCategory(Integer categoryId, Pageable pageable, Long currentUserId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND, categoryId));
        Page<Product> productPage = productRepository.findByCategory(category, pageable);
        return convertToProductListData(productPage, currentUserId);
    }

    @Transactional(readOnly = true)
    public ProductDTO.ProductListData searchProductsByTitle(String title, Pageable pageable, Long currentUserId) {
        List<Product> productList = productRepository.findByTitleContainsIgnoreCase(title);
        Page<Product> productPage = paginateList(productList, pageable);
        return convertToProductListData(productPage, currentUserId);
    }

    @Transactional(readOnly = true)
    public ProductDTO.ProductListData searchProductsByKeyword(String keyword, Pageable pageable, Long currentUserId) {
        List<Product> productList = productRepository.findByTitleContainsIgnoreCaseOrContentContainsIgnoreCase(keyword, keyword);
        Page<Product> productPage = paginateList(productList, pageable);
        return convertToProductListData(productPage, currentUserId);
    }

    @Transactional
    public void updateProductStatus(Long productId, Boolean isReserved, Boolean isCompleted, Long userId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, productId));

        if (!product.getUser().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.PRODUCT_OPERATION_FORBIDDEN, "상태 변경");
        }
            product.setIsReserved(isReserved);
            product.setIsCompleted(isCompleted);
        productRepository.save(product);
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
                    return mapToProductDTOResponse(product, currentUserId);
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

    private ProductDTO.Response mapToProductDTOResponse(Product product, Long currentUserId) {

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
                .seller(ProductDTO.SellerInfo.fromEntity(product.getUser()))
                .createdAt(product.getCreatedAt().atOffset(java.time.ZoneOffset.UTC))
                .updatedAt(product.getUpdatedAt() != null ? product.getUpdatedAt().atOffset(java.time.ZoneOffset.UTC) : null)
                .viewCount(product.getViewCount())
                .isBumped(product.getIsBumped())
                .isReserved(product.getIsReserved())
                .isCompleted(product.getIsCompleted())
                .build();
    }
}