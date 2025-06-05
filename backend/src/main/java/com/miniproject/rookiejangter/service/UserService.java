package com.miniproject.rookiejangter.service;

import com.miniproject.rookiejangter.controller.dto.UserDTO;
import com.miniproject.rookiejangter.entity.Area;
import com.miniproject.rookiejangter.entity.Ban;
import com.miniproject.rookiejangter.entity.User;
import com.miniproject.rookiejangter.exception.BusinessException;
import com.miniproject.rookiejangter.exception.ErrorCode;
import com.miniproject.rookiejangter.repository.AreaRepository;
import com.miniproject.rookiejangter.repository.BanRepository;
import com.miniproject.rookiejangter.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AreaRepository areaRepository;
    private final BanRepository banRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserDTO.Response createUser(UserDTO.SignUpRequest requestDto) {

        if (userRepository.existsByLoginId(requestDto.getLoginId())) {
            throw new BusinessException(ErrorCode.LOGIN_ID_ALREADY_EXISTS, requestDto.getLoginId());
        }

        if (userRepository.existsByPhone(requestDto.getPhone())) {
            throw new BusinessException(ErrorCode.PHONE_ALREADY_EXISTS, requestDto.getPhone());
        }

        Area area = areaRepository.findById(requestDto.getAreaId().intValue())
                .orElseThrow(() -> new EntityNotFoundException("해당 지역을 찾을 수 없습니다: " + requestDto.getAreaId()));

        User user = User.builder()
                .loginId(requestDto.getLoginId())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .userName(requestDto.getUserName())
                .phone(requestDto.getPhone())
                .area(area)
                .isBanned(false)
                .isAdmin(false)
                .build();

        User savedUser = userRepository.save(user);
        return UserDTO.Response.fromEntity(savedUser);
    }

    @Transactional(readOnly = true)
    public UserDTO.Response getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, String.valueOf(userId)));
        return UserDTO.Response.fromEntity(user);
    }

    @Transactional(readOnly = true)
    public UserDTO.Response getUserByUserName(String userName) {
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, userName));
        return UserDTO.Response.fromEntity(user);
    }

    @Transactional(readOnly = true)
    public UserDTO.Response getUserByLoginId(String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, loginId));
        return UserDTO.Response.fromEntity(user);
    }

    // userId 기반 업데이트 메서드 (JWT 토큰용)
    @Transactional
    public UserDTO.Response updateUser(Long userId, UserDTO.UpdateRequest requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, String.valueOf(userId)));

        if (requestDto.getUserName() != null && !requestDto.getUserName().isEmpty()) {
            user.setUserName(requestDto.getUserName());
        }

        if (requestDto.getPhone() != null && !requestDto.getPhone().isEmpty()) {
            if (!user.getPhone().equals(requestDto.getPhone()) && userRepository.existsByPhone(requestDto.getPhone())) {
                throw new BusinessException(ErrorCode.PHONE_ALREADY_EXISTS, requestDto.getPhone());
            }
            user.setPhone(requestDto.getPhone());
        }

        if (requestDto.getAreaId() != null) {
            Area area = areaRepository.findById(requestDto.getAreaId().intValue())
                    .orElseThrow(() -> new EntityNotFoundException("해당 지역을 찾을 수 없습니다: " + requestDto.getAreaId()));
            user.setArea(area);
        }

        User updatedUser = userRepository.save(user);
        return UserDTO.Response.fromEntity(updatedUser);
    }

    // username 기반 업데이트 메서드 (기존 호환성을 위해 유지)
    @Transactional
    public UserDTO.Response updateUser(String username, UserDTO.UpdateRequest requestDto) {
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, username));

        if (requestDto.getUserName() != null && !requestDto.getUserName().isEmpty()) {
            user.setUserName(requestDto.getUserName());
        }

        if (requestDto.getPhone() != null && !requestDto.getPhone().isEmpty()) {
            if (!user.getPhone().equals(requestDto.getPhone()) && userRepository.existsByPhone(requestDto.getPhone())) {
                throw new BusinessException(ErrorCode.PHONE_ALREADY_EXISTS, requestDto.getPhone());
            }
            user.setPhone(requestDto.getPhone());
        }

        if (requestDto.getAreaId() != null) {
            Area area = areaRepository.findById(requestDto.getAreaId().intValue())
                    .orElseThrow(() -> new EntityNotFoundException("해당 지역을 찾을 수 없습니다: " + requestDto.getAreaId()));
            user.setArea(area);
        }

        User updatedUser = userRepository.save(user);
        return UserDTO.Response.fromEntity(updatedUser);
    }

    @Transactional
    public UserDTO.Response updateUserStatus(Long userId, UserDTO.StatusUpdateRequest requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, String.valueOf(userId)));

        user.setIsBanned(requestDto.getIsBanned());

        if (requestDto.getIsBanned()) {
            Ban ban = Ban.builder()
                    .user(user)
                    .banReason(requestDto.getBanReason())
                    .build();
            banRepository.save(ban);
        }

        User updatedUser = userRepository.save(user);
        User finalUser = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + userId));
        return UserDTO.Response.fromEntityStatus(finalUser);
    }

    // userId 기반 삭제 메서드 (JWT 토큰용)
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, String.valueOf(userId)));
        userRepository.delete(user);
    }

    @Transactional
    public void deleteUserWithPassword(Long userId, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, String.valueOf(userId)));

        // 비밀번호 확인
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH, "비밀번호가 일치하지 않습니다.");
        }

        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public UserDTO.UserListData getAllUsers(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);
        List<UserDTO.Response> userResponses = userPage.getContent().stream()
                .map(UserDTO.Response::fromEntity)
                .collect(Collectors.toList());

        UserDTO.UserListPagination pagination = UserDTO.UserListPagination.builder()
                .page(userPage.getNumber())
                .size(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .first(userPage.isFirst())
                .last(userPage.isLast())
                .build();

        return UserDTO.UserListData.builder()
                .content(userResponses)
                .pagination(pagination)
                .build();
    }

    @Transactional(readOnly = true)
    public List<UserDTO.Response> getBannedUsers() {
        List<User> bannedUsers = userRepository.findByIsBannedTrue();
        return bannedUsers.stream()
                .map(UserDTO.Response::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserDTO.Response> getAdminUsers() {
        List<User> adminUsers = userRepository.findByIsAdminTrue();
        return adminUsers.stream()
                .map(UserDTO.Response::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean isLoginIdAvailable(String loginId) {
        return !userRepository.existsByLoginId(loginId);
    }

    @Transactional(readOnly = true)
    public boolean isPhoneAvailable(String phone) {
        return !userRepository.existsByPhone(phone);
    }
}