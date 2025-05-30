package com.miniproject.rookiejangter.service;

import com.miniproject.rookiejangter.controller.dto.UserDTO;
import com.miniproject.rookiejangter.entity.Area;
import com.miniproject.rookiejangter.entity.Ban;
import com.miniproject.rookiejangter.entity.User;
import com.miniproject.rookiejangter.repository.AreaRepository;
import com.miniproject.rookiejangter.repository.BanRepository;
import com.miniproject.rookiejangter.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AreaRepository areaRepository;
    @Mock
    private BanRepository banRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("사용자 생성 성공 테스트")
    void createUserSuccessTest() {
        // Given
        UserDTO.Request requestDto = UserDTO.Request.builder()
                .loginId("testId")
                .password("testPassword!1")
                .userName("테스트유저")
                .phone("010-1234-5678")
                .areaId(1L)
                .build();
        Area area = Area.builder().areaId(1).areaName("서울").build();
        User savedUser = User.builder()
                .userId(1L)
                .loginId("testId")
                .password("testPassword!1")
                .userName("테스트유저")
                .phone("010-1234-5678")
                .area(area)
                .isBanned(false)
                .isAdmin(false)
                .createdAt(LocalDateTime.now())
                .bans(Collections.emptyList())
                .build();

        when(userRepository.existsByLoginId("testId")).thenReturn(false);
        when(userRepository.existsByPhone("010-1234-5678")).thenReturn(false);
        when(areaRepository.findById(1)).thenReturn(Optional.of(area));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        UserDTO.Response response = userService.createUser(requestDto);

        // Then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getLoginId()).isEqualTo("testId");
        assertThat(response.getUserName()).isEqualTo("테스트유저");
        assertThat(response.getArea().getAreaName()).isEqualTo("서울");
        verify(userRepository, times(1)).existsByLoginId("testId");
        verify(userRepository, times(1)).existsByPhone("010-1234-5678");
        verify(areaRepository, times(1)).findById(1);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("사용자 생성 실패 테스트 - 중복된 로그인 ID")
    void createUserFailTest_duplicateLoginId() {
        // Given
        UserDTO.Request requestDto = UserDTO.Request.builder()
                .loginId("testId")
                .password("testPassword!1")
                .userName("테스트유저")
                .phone("010-1234-5678")
                .areaId(1L)
                .build();

        when(userRepository.existsByLoginId("testId")).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(requestDto));
        verify(userRepository, times(1)).existsByLoginId("testId");
        verify(userRepository, never()).existsByPhone(anyString());
        verify(areaRepository, never()).findById(anyInt());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("사용자 생성 실패 테스트 - 중복된 전화번호")
    void createUserFailTest_duplicatePhone() {
        // Given
        UserDTO.Request requestDto = UserDTO.Request.builder()
                .loginId("testId")
                .password("testPassword!1")
                .userName("테스트유저")
                .phone("010-1234-5678")
                .areaId(1L)
                .build();

        when(userRepository.existsByLoginId("testId")).thenReturn(false);
        when(userRepository.existsByPhone("010-1234-5678")).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(requestDto));
        verify(userRepository, times(1)).existsByLoginId("testId");
        verify(userRepository, times(1)).existsByPhone("010-1234-5678");
        verify(areaRepository, never()).findById(anyInt());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("사용자 생성 실패 테스트 - 지역 없음")
    void createUserFailTest_areaNotFound() {
        // Given
        UserDTO.Request requestDto = UserDTO.Request.builder()
                .loginId("testId")
                .password("testPassword!1")
                .userName("테스트유저")
                .phone("010-1234-5678")
                .areaId(1L)
                .build();

        when(userRepository.existsByLoginId("testId")).thenReturn(false);
        when(userRepository.existsByPhone("010-1234-5678")).thenReturn(false);
        when(areaRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> userService.createUser(requestDto));
        verify(userRepository, times(1)).existsByLoginId("testId");
        verify(userRepository, times(1)).existsByPhone("010-1234-5678");
        verify(areaRepository, times(1)).findById(1);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("사용자 ID로 조회 성공 테스트")
    void getUserByIdSuccessTest() {
        // Given
        Long userId = 1L;
        Area area = Area.builder().areaId(1).areaName("서울").build();
        User user = User.builder()
                .userId(userId)
                .loginId("testId")
                .userName("테스트유저")
                .phone("010-1234-5678")
                .area(area)
                .createdAt(LocalDateTime.now())
                .bans(Collections.emptyList())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        UserDTO.Response response = userService.getUserById(userId);

        // Then
        assertThat(response.getId()).isEqualTo(userId);
        assertThat(response.getLoginId()).isEqualTo("testId");
        assertThat(response.getUserName()).isEqualTo("테스트유저");
        assertThat(response.getArea().getAreaName()).isEqualTo("서울");
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("사용자 ID로 조회 실패 테스트 - 사용자 없음")
    void getUserByIdFailTest_userNotFound() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> userService.getUserById(userId));
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("로그인 ID로 조회 성공 테스트")
    void getUserByLoginIdSuccessTest() {
        // Given
        String loginId = "testId";
        Area area = Area.builder().areaId(1).areaName("서울").build();
        User user = User.builder()
                .userId(1L)
                .loginId(loginId)
                .userName("테스트유저")
                .phone("010-1234-5678")
                .area(area)
                .createdAt(LocalDateTime.now())
                .bans(Collections.emptyList())
                .build();

        when(userRepository.findByLoginId(loginId)).thenReturn(Optional.of(user));

        // When
        UserDTO.Response response = userService.getUserByLoginId(loginId);

        // Then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getLoginId()).isEqualTo(loginId);
        assertThat(response.getUserName()).isEqualTo("테스트유저");
        assertThat(response.getArea().getAreaName()).isEqualTo("서울");
        verify(userRepository, times(1)).findByLoginId(loginId);
    }

    @Test
    @DisplayName("로그인 ID로 조회 실패 테스트 - 사용자 없음")
    void getUserByLoginIdFailTest_userNotFound() {
        // Given
        String loginId = "testId";
        when(userRepository.findByLoginId(loginId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> userService.getUserByLoginId(loginId));
        verify(userRepository, times(1)).findByLoginId(loginId);
    }

    @Test
    @DisplayName("사용자 정보 업데이트 성공 테스트")
    void updateUserSuccessTest() {
        // Given
        Long userId = 1L;
        UserDTO.UpdateRequest requestDto = UserDTO.UpdateRequest.builder()
                .userName("수정된유저")
                .phone("010-9876-5432")
                .areaId(2L)
                .build();
        Area originalArea = Area.builder().areaId(1).areaName("서울").build();
        Area updatedArea = Area.builder().areaId(2).areaName("부산").build();
        User originalUser = User.builder()
                .userId(userId)
                .loginId("testId")
                .userName("테스트유저")
                .phone("010-1234-5678")
                .area(originalArea)
                .createdAt(LocalDateTime.now())
                .bans(Collections.emptyList())
                .build();
        User updatedUser = User.builder()
                .userId(userId)
                .loginId("testId")
                .userName("수정된유저")
                .phone("010-9876-5432")
                .area(updatedArea)
                .createdAt(LocalDateTime.now())
                .bans(Collections.emptyList())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(originalUser));
        when(areaRepository.findById(2)).thenReturn(Optional.of(updatedArea));
        when(userRepository.existsByPhone("010-9876-5432")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // When
        UserDTO.Response response = userService.updateUser(userId, requestDto);

        // Then
        assertThat(response.getId()).isEqualTo(userId);
        assertThat(response.getUserName()).isEqualTo("수정된유저");
        assertThat(response.getPhone()).isEqualTo("010-9876-5432");
        assertThat(response.getArea().getAreaName()).isEqualTo("부산");
        assertThat(response.getCreatedAt()).isNotNull(); // 추가된 검증
        verify(userRepository, times(1)).findById(userId);
        verify(areaRepository, times(1)).findById(2);
        verify(userRepository, times(1)).existsByPhone("010-9876-5432");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("사용자 정보 업데이트 실패 테스트 - 사용자 없음")
    void updateUserFailTest_userNotFound() {
        // Given
        Long userId = 1L;
        UserDTO.UpdateRequest requestDto = UserDTO.UpdateRequest.builder()
                .userName("수정된유저")
                .phone("010-9876-5432")
                .areaId(2L)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> userService.updateUser(userId, requestDto));
        verify(userRepository, times(1)).findById(userId);
        verify(areaRepository, never()).findById(anyInt());
        verify(userRepository, never()).existsByPhone(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("사용자 정보 업데이트 실패 테스트 - 중복된 전화번호")
    void updateUserFailTest_duplicatePhone() {
        // Given
        Long userId = 1L;
        UserDTO.UpdateRequest requestDto = UserDTO.UpdateRequest.builder()
                .phone("010-9876-5432")
                .build();
        Area originalArea = Area.builder().areaId(1).areaName("서울").build();
        User originalUser = User.builder()
                .userId(userId)
                .loginId("testId")
                .userName("테스트유저")
                .phone("010-1234-5678")
                .area(originalArea)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(originalUser));
        when(userRepository.existsByPhone("010-9876-5432")).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userService.updateUser(userId, requestDto));
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).existsByPhone("010-9876-5432");
        verify(areaRepository, never()).findById(anyInt());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("사용자 상태 업데이트 성공 테스트 - 밴")
    void updateUserStatusBanSuccessTest() {
        // Given
        Long userId = 1L;
        UserDTO.StatusUpdateRequest requestDto = UserDTO.StatusUpdateRequest.builder()
                .isBanned(true)
                .banReason("테스트 사유")
                .build();
        User originalUser = User.builder()
                .userId(userId)
                .loginId("testId")
                .userName("테스트유저")
                .phone("010-1234-5678")
                .isBanned(false)
                .bans(Collections.emptyList())
                .createdAt(LocalDateTime.now())
                .build();
        User updatedUser = User.builder()
                .userId(userId)
                .loginId("testId")
                .userName("테스트유저")
                .phone("010-1234-5678")
                .isBanned(true)
                .bans(Collections.singletonList(Ban.builder() // Ban 객체 직접 생성 및 연결
                        .banId(1L)
                        .banReason("테스트 사유")
                        .createdAt(LocalDateTime.now())
                        .build()))
                .createdAt(LocalDateTime.now())
                .build();
        Ban ban = Ban.builder()
                .banId(1L)
                .user(updatedUser)
                .banReason("테스트 사유")
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(originalUser)).thenReturn(Optional.of(updatedUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(banRepository.save(any(Ban.class))).thenReturn(ban);

        // When
        UserDTO.Response response = userService.updateUserStatus(userId, requestDto);

        // Then
        assertThat(response.getId()).isEqualTo(userId);
        assertThat(response.getIsBanned()).isTrue();
        assertThat(response.getBanReason()).isEqualTo("테스트 사유");
        assertThat(response.getBannedAt()).isNotNull();
        verify(userRepository, times(2)).findById(userId);
        verify(userRepository, times(1)).save(any(User.class));
        verify(banRepository, times(1)).save(any(Ban.class));
    }

    @Test
    @DisplayName("사용자 상태 업데이트 성공 테스트 - 밴 해제")
    void updateUserStatusUnbanSuccessTest() {
        // Given
        Long userId = 1L;
        UserDTO.StatusUpdateRequest requestDto = UserDTO.StatusUpdateRequest.builder()
                .isBanned(false)
                .banReason(null)
                .build();
        User originalUser = User.builder()
                .userId(userId)
                .loginId("testId")
                .userName("테스트유저")
                .phone("010-1234-5678")
                .isBanned(true)
                .bans(Collections.emptyList())
                .build();
        User updatedUser = User.builder()
                .userId(userId)
                .loginId("testId")
                .userName("테스트유저")
                .phone("010-1234-5678")
                .isBanned(false)
                .bans(Collections.emptyList())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(originalUser)).thenReturn(Optional.of(updatedUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        // 밴 해제 시 banRepository.save()는 호출되지 않음

        // When
        UserDTO.Response response = userService.updateUserStatus(userId, requestDto);

        // Then
        assertThat(response.getId()).isEqualTo(userId);
        assertThat(response.getIsBanned()).isFalse();
        assertThat(response.getBanReason()).isNull();
        assertThat(response.getBannedAt()).isNull();
        verify(userRepository, times(2)).findById(userId);
        verify(userRepository, times(1)).save(any(User.class));
        verify(banRepository, never()).save(any(Ban.class));
    }

    @Test
    @DisplayName("사용자 상태 업데이트 실패 테스트 - 사용자 없음")
    void updateUserStatusFailTest_userNotFound() {
        // Given
        Long userId = 1L;
        UserDTO.StatusUpdateRequest requestDto = UserDTO.StatusUpdateRequest.builder()
                .isBanned(true)
                .banReason("테스트 사유")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> userService.updateUserStatus(userId, requestDto));
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any(User.class));
        verify(banRepository, never()).save(any(Ban.class));
    }

    @Test
    @DisplayName("사용자 삭제 성공 테스트")
    void deleteUserSuccessTest() {
        // Given
        Long userId = 1L;
        User user = User.builder().userId(userId).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);

        // When
        userService.deleteUser(userId);

        // Then
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    @DisplayName("사용자 삭제 실패 테스트 - 사용자 없음")
    void deleteUserFailTest_userNotFound() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> userService.deleteUser(userId));
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).delete(any());
    }

    @Test
    @DisplayName("모든 사용자 조회 (페이징) 성공 테스트")
    void getAllUsersSuccessTest() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Area area = Area.builder().areaId(1).areaName("서울").build();
        List<User> users = List.of(
                User.builder().userId(1L).loginId("user1").userName("유저1").phone("010-1111-1111").area(area).bans(Collections.emptyList()).createdAt(LocalDateTime.now()).build(),
                User.builder().userId(2L).loginId("user2").userName("유저2").phone("010-2222-2222").area(area).bans(Collections.emptyList()).createdAt(LocalDateTime.now()).build()
        );
        Page<User> userPage = new PageImpl<>(users, pageable, users.size());
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        // When
        UserDTO.UserListData result = userService.getAllUsers(pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getPagination().getTotalElements()).isEqualTo(2);
        assertThat(result.getPagination().getTotalPages()).isEqualTo(1);
        assertThat(result.getContent().get(0).getLoginId()).isEqualTo("user1");
        verify(userRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("밴된 사용자 목록 조회 성공 테스트")
    void getBannedUsersSuccessTest() {
        // Given
        Area area = Area.builder().areaId(1).areaName("서울").build();
        List<User> bannedUsers = List.of(
                User.builder().userId(1L).loginId("banned1").userName("밴유저1").phone("010-1111-1111").area(area).isBanned(true).bans(Collections.emptyList()).createdAt(LocalDateTime.now()).build(),
                User.builder().userId(2L).loginId("banned2").userName("밴유저2").phone("010-2222-2222").area(area).isBanned(true).bans(Collections.emptyList()).createdAt(LocalDateTime.now()).build()
        );
        when(userRepository.findByIsBannedTrue()).thenReturn(bannedUsers);

        // When
        List<UserDTO.Response> result = userService.getBannedUsers();

        // Then
        assertThat(result).hasSize(2);
        assertTrue(result.get(0).getIsBanned());
        assertTrue(result.get(1).getIsBanned());
        verify(userRepository, times(1)).findByIsBannedTrue();
    }

    @Test
    @DisplayName("관리자 사용자 목록 조회 성공 테스트")
    void getAdminUsersSuccessTest() {
        // Given
        Area area = Area.builder().areaId(1).areaName("서울").build();
        List<User> adminUsers = List.of(
                User.builder().userId(1L).loginId("admin1").userName("관리자1").phone("010-1111-1111").area(area).isAdmin(true).bans(Collections.emptyList()).createdAt(LocalDateTime.now()).build(),
                User.builder().userId(2L).loginId("admin2").userName("관리자2").phone("010-2222-2222").area(area).isAdmin(true).bans(Collections.emptyList()).createdAt(LocalDateTime.now()).build()
        );
        when(userRepository.findByIsAdminTrue()).thenReturn(adminUsers);

        // When
        List<UserDTO.Response> result = userService.getAdminUsers();

        // Then
        assertThat(result).hasSize(2);
        assertTrue(result.get(0).getIsAdmin());
        assertTrue(result.get(1).getIsAdmin());
        verify(userRepository, times(1)).findByIsAdminTrue();
    }

    @Test
    @DisplayName("로그인 ID 사용 가능 여부 확인 테스트 - 사용 가능")
    void isLoginIdAvailableSuccessTest_available() {
        // Given
        String loginId = "availableId";
        when(userRepository.existsByLoginId(loginId)).thenReturn(false);

        // When
        boolean result = userService.isLoginIdAvailable(loginId);

        // Then
        assertTrue(result);
        verify(userRepository, times(1)).existsByLoginId(loginId);
    }

    @Test
    @DisplayName("로그인 ID 사용 가능 여부 확인 테스트 - 사용 불가능")
    void isLoginIdAvailableSuccessTest_unavailable() {
        // Given
        String loginId = "unavailableId";
        when(userRepository.existsByLoginId(loginId)).thenReturn(true);

        // When
        boolean result = userService.isLoginIdAvailable(loginId);

        // Then
        assertThat(result).isFalse();
        verify(userRepository, times(1)).existsByLoginId(loginId);
    }

    @Test
    @DisplayName("전화번호 사용 가능 여부 확인 테스트 - 사용 가능")
    void isPhoneAvailableSuccessTest_available() {
        // Given
        String phone = "010-3333-3333";
        when(userRepository.existsByPhone(phone)).thenReturn(false);

        // When
        boolean result = userService.isPhoneAvailable(phone);

        // Then
        assertTrue(result);
        verify(userRepository, times(1)).existsByPhone(phone);
    }

    @Test
    @DisplayName("전화번호 사용 가능 여부 확인 테스트 - 사용 불가능")
    void isPhoneAvailableSuccessTest_unavailable() {
        // Given
        String phone = "010-3333-3333";
        when(userRepository.existsByPhone(phone)).thenReturn(true);

        // When
        boolean result = userService.isPhoneAvailable(phone);

        // Then
        assertThat(result).isFalse();
        verify(userRepository, times(1)).existsByPhone(phone);
    }
}

