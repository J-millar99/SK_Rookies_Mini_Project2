package com.miniproject.rookiejangter.controller;

import com.miniproject.rookiejangter.controller.dto.DibsDTO;
import com.miniproject.rookiejangter.controller.dto.ProductDTO;
import com.miniproject.rookiejangter.controller.dto.UserDTO;
import com.miniproject.rookiejangter.exception.BusinessException;
import com.miniproject.rookiejangter.exception.ErrorCode;
import com.miniproject.rookiejangter.service.DibsService;
import com.miniproject.rookiejangter.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 로깅 추가
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset; // UTC 명시를 위해 추가
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class DibsController {

    private final DibsService dibsService;
    private final UserService userService;

    /**
     * Spring Security의 Authentication 객체로부터 현재 인증된 사용자의 ID를 추출합니다.
     *
     * @param authentication 컨트롤러 메소드에 주입된 Authentication 객체
     * @return 추출된 사용자 ID (Long)
     * @throws BusinessException 인증 정보가 없거나, 사용자를 찾을 수 없는 경우 발생
     */
    private Long getAuthenticatedUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            log.warn("DibsController: 인증 정보가 없거나 유효하지 않습니다.");
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        String loginId = authentication.getName();
        log.debug("DibsController: 인증된 사용자 loginId: {}", loginId);

        UserDTO.Response userDto = userService.getUserByUserName(loginId);
        if (userDto == null) {
            log.warn("DibsController: loginId '{}'에 해당하는 사용자를 찾을 수 없습니다.", loginId);
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        log.debug("DibsController: 조회된 사용자 ID: {}", userDto.getId());
        return userDto.getId();
    }

    /**
     * 찜하기 추가/제거 (토글)
     * PUT /api/wishlist/{product_id}
     */
    @PutMapping("/{productId}")
    public ResponseEntity<ProductDTO.ApiResponseWrapper<DibsDTO.Response>> toggleWishlist(
            @PathVariable Long productId,
            Authentication authentication
    ) {
        Long userId = getAuthenticatedUserId(authentication);
        log.info("toggleWishlist 요청: userId={}, productId={}", userId, productId);

        DibsDTO.Response toggleResult = dibsService.getDibsStatus(userId, productId);

        String message = toggleResult.isLiked() ? "찜 목록에 추가되었습니다." : "찜 목록에서 제거되었습니다.";
        log.info("toggleWishlist 결과: isLiked={}", toggleResult.isLiked());

        ProductDTO.ApiResponseWrapper<DibsDTO.Response> response = ProductDTO.ApiResponseWrapper.<DibsDTO.Response>builder()
                .success(true)
                .data(toggleResult)
                .message(message)
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .requestId(UUID.randomUUID().toString())
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * 찜한 상품 목록 조회
     * GET /api/wishlist
     */
    @GetMapping
    public ResponseEntity<ProductDTO.ApiResponseWrapper<DibsDTO.DibsListResponse>> getWishlist(
            @PageableDefault(size = 20, page = 0) Pageable pageable,
            Authentication authentication
    ){
        Long userId = getAuthenticatedUserId(authentication);
        log.info("getWishlist 요청: userId={}, pageable={}", userId, pageable);

        DibsDTO.DibsListResponse dibsListResponse = dibsService.getUserDibsList(userId, pageable);
        log.info("getWishlist 결과: totalElements={}, totalPages={}",
                dibsListResponse.getPagination() != null ? dibsListResponse.getPagination().getTotalElements() : "N/A",
                dibsListResponse.getPagination() != null ? dibsListResponse.getPagination().getTotalPages() : "N/A");

        if (dibsListResponse.getPagination() != null && dibsListResponse.getPagination().getContent() != null) {
            log.debug("DibsListResponse.pagination.content 중복 필드 제거");
            dibsListResponse.getPagination().setContent(null);
        }


        ProductDTO.ApiResponseWrapper<DibsDTO.DibsListResponse> response = ProductDTO.ApiResponseWrapper.<DibsDTO.DibsListResponse>builder()
                .success(true)
                .data(dibsListResponse)
                .message("찜 목록 조회가 성공했습니다.")
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .requestId(UUID.randomUUID().toString())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{productId}/my-status")
    public ResponseEntity<ProductDTO.ApiResponseWrapper<DibsDTO.Response>> getMyDibsStatusForProduct(
            @PathVariable Long productId,
            Authentication authentication
    ) {
        Long userId = getAuthenticatedUserId(authentication);
        log.info("getMyDibsStatusForProduct 요청: userId={}, productId={}", userId, productId);

        DibsDTO.Response dibsStatusData = dibsService.getDibsStatus(userId, productId);
        log.info("getMyDibsStatusForProduct 결과: isLiked={}", dibsStatusData.isLiked());

        ProductDTO.ApiResponseWrapper<DibsDTO.Response> response = ProductDTO.ApiResponseWrapper.<DibsDTO.Response>builder()
                .success(true)
                .data(dibsStatusData)
                .message("사용자의 상품 찜 상태 조회 성공")
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .requestId(UUID.randomUUID().toString())
                .build();
        return ResponseEntity.ok(response);
    }
}




//package com.miniproject.rookiejangter.controller;
//
//import com.miniproject.rookiejangter.controller.dto.DibsDTO;
//import com.miniproject.rookiejangter.controller.dto.ProductDTO; // 공용 ApiResponseWrapper를 위해 (또는 DibsDTO에 정의된 것 사용)
//import com.miniproject.rookiejangter.exception.BusinessException;
//import com.miniproject.rookiejangter.exception.ErrorCode;
//import com.miniproject.rookiejangter.service.DibsService;
//import com.miniproject.rookiejangter.entity.User;
//import com.miniproject.rookiejangter.repository.UserRepository;
//
//import com.miniproject.rookiejangter.service.UserService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.web.PageableDefault;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.OffsetDateTime;
//import java.util.UUID;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/wishlist") // RESTAPI.md 명세에 따른 기본 경로
//@RequiredArgsConstructor
//public class DibsController {
//
//    private final DibsService dibsService;
//    private final UserService userService;
//
//    // 사용자 ID를 가져오는 헬퍼 메소드
//    //            try {
////                return Long.parseLong(username);
////            } catch (NumberFormatException e) {
////                User user = userRepository.findByLoginId(username)
////                        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND_BY_LOGIN_ID, username));
////                return user.getUserId();
////            }
////        }
////        throw new BusinessException(ErrorCode.INVALID_AUTHENTICATION_PRINCIPAL);
////    }
//
//    /**
//     * 찜하기 추가/제거 (토글)
//     * PUT /api/wishlist/{product_id}
//     */
//    @PutMapping("/{productId}")
//    public ResponseEntity<ProductDTO.ApiResponseWrapper<DibsDTO.Response>> toggleWishlist(
//            @PathVariable Long productId) {
//        Long userId = userService.getUserById(userId);
//        DibsDTO.Response toggleResult = dibsService.getDibsStatus(userId, productId); // 서비스 메소드명 및 반환 DTO 수정 가정
//
//        String message = toggleResult.isLiked() ? "찜 목록에 추가되었습니다." : "찜 목록에서 제거되었습니다.";
//
//        ProductDTO.ApiResponseWrapper<DibsDTO.Response> response = ProductDTO.ApiResponseWrapper.<DibsDTO.Response>builder()
//                .success(true)
//                .data(toggleResult)
//                .message(message)
//                .timestamp(OffsetDateTime.now())
//                .requestId(UUID.randomUUID().toString())
//                .build();
//        return ResponseEntity.ok(response);
//    }
//
//    /**
//     * 찜한 상품 목록 조회
//     * GET /api/wishlist
//     */
//    @GetMapping
//    public ResponseEntity<ProductDTO.ApiResponseWrapper<DibsDTO.DibsListResponse>> getWishlist(
//        @PageableDefault(size = 20, page = 0) Pageable pageable){
//        Long userId = userService.getUserById(userId);
//        DibsDTO.DibsListResponse dibsListResponse = dibsService.getUserDibsList(userId, pageable);
//
//        ProductDTO.ApiResponseWrapper<DibsDTO.DibsListResponse> response = ProductDTO.ApiResponseWrapper.<DibsDTO.DibsListResponse>builder()
//                .success(true)
//                .data(dibsListResponse)
//                .message("찜 목록 조회가 성공했습니다.")
//                .timestamp(OffsetDateTime.now())
//                .requestId(UUID.randomUUID().toString())
//                .build();
//        return ResponseEntity.ok(response);
//    }
//    @GetMapping("/{productId}/my-status")
//    public ResponseEntity<ProductDTO.ApiResponseWrapper<DibsDTO.Response>> getMyDibsStatusForProduct(@PathVariable Long productId) {
//        Long userId = userService.getUserById(userId);
//        DibsDTO.Response dibsStatusData = dibsService.getDibsStatus(userId, productId);
//
//        ProductDTO.ApiResponseWrapper<DibsDTO.Response> response = ProductDTO.ApiResponseWrapper.<DibsDTO.Response>builder()
//                .success(true)
//                .data(dibsStatusData)
//                .message("사용자의 상품 찜 상태 조회 성공")
//                .timestamp(OffsetDateTime.now())
//                .requestId(UUID.randomUUID().toString())
//                .build();
//        return ResponseEntity.ok(response);
//    }
//
////    @GetMapping("/{productId}/total-count")
////    public ResponseEntity<ProductDTO.ApiResponseWrapper<Long>> getDibsCountForProduct(@PathVariable Long productId) {
////        long count = dibsService.getDibsCountForProduct(productId);
////
////         ProductDTO.ApiResponseWrapper<Long> response = ProductDTO.ApiResponseWrapper.<Long>builder()
////                .success(true)
////                .data(count)
////                .message("상품 총 찜 수 조회 성공")
////                .timestamp(OffsetDateTime.now())
////                .requestId(UUID.randomUUID().toString())
////                .build();
////        return ResponseEntity.ok(response);
////    }
//}