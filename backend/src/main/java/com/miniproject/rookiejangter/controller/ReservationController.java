package com.miniproject.rookiejangter.controller;

import com.miniproject.rookiejangter.controller.dto.ProductDTO;
import com.miniproject.rookiejangter.controller.dto.ReservationDTO;
import com.miniproject.rookiejangter.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS}) // <--- 이 라인 수정
public class ReservationController {

    private final ReservationService reservationService;

    // 현재 로그인 중인 사용자가 buyerId로 설정된 모든 reservations를 목록으로 보여줌
    @GetMapping("/buyer")
    public ResponseEntity<ProductDTO.ApiResponseWrapper<List<ReservationDTO.Response>>> getReservationsByBuyer(
            Authentication authentication) {
        Long currentUserId = Long.parseLong(authentication.getName()); // JWT에서 userId 추출
        List<ReservationDTO.Response> reservations = reservationService.getReservationsByBuyer(currentUserId);
        return ResponseEntity.ok(ProductDTO.ApiResponseWrapper.<List<ReservationDTO.Response>>builder()
                .success(true)
                .data(reservations)
                .message("구매자로서 참여한 예약 목록이 성공적으로 조회되었습니다.")
                .build());
    }

    // 현재 로그인 중인 사용자가 sellerId로 설정된 모든 reservations를 목록으로 보여줌
    @GetMapping("/seller")
    public ResponseEntity<ProductDTO.ApiResponseWrapper<List<ReservationDTO.Response>>> getReservationsBySeller(
            Authentication authentication) { // @RequestHeader 대신 Authentication 사용
        Long currentUserId = Long.parseLong(authentication.getName()); // JWT에서 userId 추출
        List<ReservationDTO.Response> reservations = reservationService.getReservationsBySeller(currentUserId);
        return ResponseEntity.ok(ProductDTO.ApiResponseWrapper.<List<ReservationDTO.Response>>builder()
                .success(true)
                .data(reservations)
                .message("판매자로서 참여한 예약 목록이 성공적으로 조회되었습니다.")
                .build());
    }

    // 특정 Product(상품 게시물)에 대한 예약 요청을 보냄
    @PostMapping
    public ResponseEntity<ProductDTO.ApiResponseWrapper<ReservationDTO.Response>> createReservation(
            @RequestBody ReservationDTO.TradeRequest request,
            Authentication authentication) { // @RequestHeader 대신 Authentication 사용
        Long currentUserId = Long.parseLong(authentication.getName()); // JWT에서 userId 추출
        ReservationDTO.Response reservation = reservationService.createReservation(currentUserId, request.getProductId());
        return ResponseEntity.ok(ProductDTO.ApiResponseWrapper.<ReservationDTO.Response>builder()
                .success(true)
                .data(reservation)
                .message("상품 예약 요청이 성공적으로 완료되었습니다.")
                .build());
    }

    // 예약 상태 업데이트 (판매자가 수락/거절/완료, 구매자/판매자가 취소)
    @PatchMapping("/{reservation_id}/status")
    public ResponseEntity<ProductDTO.ApiResponseWrapper<ReservationDTO.Response>> updateReservationStatus(
            @PathVariable("reservation_id") Long reservationId,
            @RequestBody ReservationDTO.StatusUpdateRequest request,
            Authentication authentication) { // @RequestHeader 대신 Authentication 사용
        Long currentUserId = Long.parseLong(authentication.getName()); // JWT에서 userId 추출
        ReservationDTO.Response updatedReservation = reservationService.updateReservationStatus(reservationId, request.getStatus(), currentUserId);
        return ResponseEntity.ok(ProductDTO.ApiResponseWrapper.<ReservationDTO.Response>builder()
                .success(true)
                .data(updatedReservation)
                .message("예약 상태가 성공적으로 업데이트되었습니다.")
                .build());
    }

    // 예약 상세 조회 (필요시)
    @GetMapping("/{reservation_id}")
    public ResponseEntity<ProductDTO.ApiResponseWrapper<ReservationDTO.Response>> getReservationById(
            @PathVariable("reservation_id") Long reservationId) {
        ReservationDTO.Response reservation = reservationService.getReservationById(reservationId);
        return ResponseEntity.ok(ProductDTO.ApiResponseWrapper.<ReservationDTO.Response>builder()
                .success(true)
                .data(reservation)
                .message("예약 상세 정보가 성공적으로 조회되었습니다.")
                .build());
    }

    // 예약 삭제 (구매자만 가능, 특정 상태에서만)
    @DeleteMapping("/{reservation_id}")
    public ResponseEntity<ProductDTO.ApiResponseWrapper<Void>> deleteReservation(
            @PathVariable("reservation_id") Long reservationId,
            Authentication authentication) { // @RequestHeader 대신 Authentication 사용
        Long currentUserId = Long.parseLong(authentication.getName()); // JWT에서 userId 추출
        reservationService.deleteReservation(reservationId, currentUserId);
        return ResponseEntity.ok(ProductDTO.ApiResponseWrapper.<Void>builder()
                .success(true)
                .message("예약이 성공적으로 삭제되었습니다.")
                .build());
    }
}