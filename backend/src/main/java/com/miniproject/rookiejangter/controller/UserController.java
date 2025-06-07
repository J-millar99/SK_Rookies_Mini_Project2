package com.miniproject.rookiejangter.controller;

import com.miniproject.rookiejangter.controller.dto.NotificationDTO;
import com.miniproject.rookiejangter.controller.dto.ProductDTO;
import com.miniproject.rookiejangter.controller.dto.ReservationDTO;
import com.miniproject.rookiejangter.controller.dto.UserDTO;
import com.miniproject.rookiejangter.entity.User;
import com.miniproject.rookiejangter.service.NotificationService;
import com.miniproject.rookiejangter.service.ProductService;
import com.miniproject.rookiejangter.service.ReservationService;
import com.miniproject.rookiejangter.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final ReservationService reservationService;

    // 현재 사용자 프로필 조회
    @GetMapping("/profile")
    public ResponseEntity<UserDTO.Response> getCurrentUserProfile(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        UserDTO.Response user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    // 현재 사용자 프로필 수정
    @PutMapping("/profile")
    public ResponseEntity<UserDTO.Response> updateCurrentUserProfile(
            @Valid @RequestBody UserDTO.UpdateRequest request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        UserDTO.Response user = userService.updateUser(userId, request);
        return ResponseEntity.ok(user);
    }

    // 현재 사용자 계정 삭제
    @DeleteMapping("/profile")
    public ResponseEntity<Void> deleteCurrentUser(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reservations")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ReservationDTO.Response>> getAllReservations(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        List<ReservationDTO.Response> allReservations = reservationService.getAllReservations(userId);
        return ResponseEntity.ok(allReservations);
    }
}