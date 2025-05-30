package com.miniproject.rookiejangter.service;

import com.miniproject.rookiejangter.controller.dto.BanDTO;
import com.miniproject.rookiejangter.entity.Ban;
import com.miniproject.rookiejangter.entity.Report;
import com.miniproject.rookiejangter.entity.User;
import com.miniproject.rookiejangter.exception.BusinessException;
import com.miniproject.rookiejangter.exception.ErrorCode;
import com.miniproject.rookiejangter.repository.BanRepository;
import com.miniproject.rookiejangter.repository.ReportRepository;
import com.miniproject.rookiejangter.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BanServiceTest {

    @Mock
    private BanRepository banRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private BanService banService;

    @Test
    @DisplayName("유저 제재 성공 테스트")
    void banUserSuccessTest() {
        // Given
        Long userId = 1L;
        Long reportId = 2L;
        String banReason = "불량 행위";
        User user = User.builder().userId(userId).isBanned(false).build();
        Report report = Report.builder().reportId(reportId).build();
        Ban savedBan = Ban.builder()
                .banId(3L)
                .user(user)
                .report(report)
                .banReason(banReason)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(reportRepository.findByReportId(reportId)).thenReturn(Optional.of(report));
        when(banRepository.save(any(Ban.class))).thenReturn(savedBan);

        // When
        BanDTO.Response response = banService.banUser(userId, reportId, banReason);

        // Then
        assertThat(response.getBanId()).isEqualTo(3L);
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getReportId()).isEqualTo(reportId);
        assertThat(response.getBanReason()).isEqualTo(banReason);
        assertThat(user.getIsBanned()).isTrue();
        verify(userRepository, times(1)).findById(userId);
        verify(reportRepository, times(1)).findByReportId(reportId);
        verify(banRepository, times(1)).save(any(Ban.class));
    }

    @Test
    @DisplayName("유저 제재 실패 테스트 - 유저 없음")
    void banUserUserNotFoundFailTest() {
        // Given
        Long userId = 1L;
        Long reportId = 2L;
        String banReason = "불량 행위";
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BusinessException.class, () -> banService.banUser(userId, reportId, banReason));
        verify(userRepository, times(1)).findById(userId);
        verify(reportRepository, never()).findByReportId(anyLong());
        verify(banRepository, never()).save(any());
    }

    @Test
    @DisplayName("유저 제재 실패 테스트 - 신고 없음")
    void banUserReportNotFoundFailTest() {
        // Given
        Long userId = 1L;
        Long reportId = 2L;
        String banReason = "불량 행위";
        User user = User.builder().userId(userId).isBanned(false).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(reportRepository.findByReportId(reportId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BusinessException.class, () -> banService.banUser(userId, reportId, banReason));
        verify(userRepository, times(1)).findById(userId);
        verify(reportRepository, times(1)).findByReportId(reportId);
        verify(banRepository, never()).save(any());
    }

    @Test
    @DisplayName("유저 제재 해제 성공 테스트")
    void unbanUserSuccessTest() {
        // Given
        Long userId = 1L;
        User user = User.builder().userId(userId).isBanned(true).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        banService.unbanUser(userId);

        // Then
        assertThat(user.getIsBanned()).isFalse();
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("유저 제재 해제 실패 테스트 - 유저 없음")
    void unbanUserUserNotFoundFailTest() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BusinessException.class, () -> banService.unbanUser(userId));
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("제재 ID로 조회 성공 테스트")
    void getBanByIdSuccessTest() {
        // Given
        Long banId = 3L;
        User user = User.builder().userId(1L).build();
        Report report = Report.builder().reportId(2L).build();
        Ban ban = Ban.builder()
                .banId(banId)
                .user(user)
                .report(report)
                .banReason("불량 행위")
                .createdAt(LocalDateTime.now())
                .build();
        when(banRepository.findByBanId(banId)).thenReturn(Optional.of(ban));

        // When
        BanDTO.Response response = banService.getBanById(banId);

        // Then
        assertThat(response.getBanId()).isEqualTo(banId);
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getReportId()).isEqualTo(2L);
        assertThat(response.getBanReason()).isEqualTo("불량 행위");
        verify(banRepository, times(1)).findByBanId(banId);
    }

    @Test
    @DisplayName("제재 ID로 조회 실패 테스트 - 제재 없음")
    void getBanByIdNotFoundFailTest() {
        // Given
        Long banId = 3L;
        when(banRepository.findByBanId(banId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BusinessException.class, () -> banService.getBanById(banId));
        verify(banRepository, times(1)).findByBanId(banId);
    }

    @Test
    @DisplayName("유저 ID로 제재 목록 조회 성공 테스트")
    void getBansByUserIdSuccessTest() {
        // Given
        Long userId = 1L;
        User user = User.builder().userId(userId).build();
        Report report1 = Report.builder().reportId(2L).build();
        Report report2 = Report.builder().reportId(3L).build();
        List<Ban> bans = Arrays.asList(
                Ban.builder().banId(4L).user(user).report(report1).banReason("도배").createdAt(LocalDateTime.now()).build(),
                Ban.builder().banId(5L).user(user).report(report2).banReason("욕설").createdAt(LocalDateTime.now().minusHours(1)).build()
        );
        when(banRepository.findByUser_UserId(userId)).thenReturn(bans);

        // When
        List<BanDTO.Response> responses = banService.getBansByUserId(userId);

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getUserId()).isEqualTo(userId);
        assertThat(responses.get(1).getUserId()).isEqualTo(userId);
        verify(banRepository, times(1)).findByUser_UserId(userId);
    }

    @Test
    @DisplayName("유저 ID로 제재 목록 조회 성공 테스트 - 제재 없음")
    void getBansByUserIdNoBansTest() {
        // Given
        Long userId = 1L;
        when(banRepository.findByUser_UserId(userId)).thenReturn(List.of());

        // When
        List<BanDTO.Response> responses = banService.getBansByUserId(userId);

        // Then
        assertThat(responses).isEmpty();
        verify(banRepository, times(1)).findByUser_UserId(userId);
    }

    @Test
    @DisplayName("신고 ID로 제재 조회 성공 테스트")
    void getBanByReportIdSuccessTest() {
        // Given
        Long reportId = 2L;
        User user = User.builder().userId(1L).build();
        Report report = Report.builder().reportId(reportId).build();
        Ban ban = Ban.builder()
                .banId(3L)
                .user(user)
                .report(report)
                .banReason("불량 행위")
                .createdAt(LocalDateTime.now())
                .build();
        when(banRepository.findByReport_ReportId(reportId)).thenReturn(Optional.of(ban));

        // When
        BanDTO.Response response = banService.getBanByReportId(reportId);

        // Then
        assertThat(response.getReportId()).isEqualTo(reportId);
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getBanReason()).isEqualTo("불량 행위");
        verify(banRepository, times(1)).findByReport_ReportId(reportId);
    }

    @Test
    @DisplayName("신고 ID로 제재 조회 실패 테스트 - 제재 없음")
    void getBanByReportIdNotFoundFailTest() {
        // Given
        Long reportId = 2L;
        when(banRepository.findByReport_ReportId(reportId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BusinessException.class, () -> banService.getBanByReportId(reportId));
        verify(banRepository, times(1)).findByReport_ReportId(reportId);
    }

    @Test
    @DisplayName("제재 사유로 제재 목록 조회 성공 테스트")
    void getBansByBanReasonSuccessTest() {
        // Given
        String banReason = "도배";
        User user1 = User.builder().userId(1L).build();
        User user2 = User.builder().userId(2L).build();
        Report report1 = Report.builder().reportId(3L).build();
        Report report2 = Report.builder().reportId(4L).build();
        List<Ban> bans = Arrays.asList(
                Ban.builder().banId(5L).user(user1).report(report1).banReason(banReason).createdAt(LocalDateTime.now()).build(),
                Ban.builder().banId(6L).user(user2).report(report2).banReason(banReason).createdAt(LocalDateTime.now().minusHours(1)).build()
        );
        when(banRepository.findByBanReason(banReason)).thenReturn(bans);

        // When
        List<BanDTO.Response> responses = banService.getBansByBanReason(banReason);

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getBanReason()).isEqualTo(banReason);
        assertThat(responses.get(1).getBanReason()).isEqualTo(banReason);
        verify(banRepository, times(1)).findByBanReason(banReason);
    }

    @Test
    @DisplayName("제재 사유로 제재 목록 조회 성공 테스트 - 제재 없음")
    void getBansByBanReasonNoBansTest() {
        // Given
        String banReason = "도배";
        when(banRepository.findByBanReason(banReason)).thenReturn(List.of());

        // When
        List<BanDTO.Response> responses = banService.getBansByBanReason(banReason);

        // Then
        assertThat(responses).isEmpty();
        verify(banRepository, times(1)).findByBanReason(banReason);
    }

    @Test
    @DisplayName("제재 삭제 성공 테스트")
    void deleteBanSuccessTest() {
        // Given
        Long banIdToDelete = 3L;
        User user = User.builder().userId(1L).build();
        Report report = Report.builder().reportId(2L).build();
        Ban banToDelete = Ban.builder()
                .banId(banIdToDelete)
                .user(user)
                .report(report)
                .banReason("불량 행위")
                .createdAt(LocalDateTime.now())
                .build();
        when(banRepository.findByBanId(banIdToDelete)).thenReturn(Optional.of(banToDelete));
        doNothing().when(banRepository).delete(banToDelete);

        // When
        banService.deleteBan(banIdToDelete);

        // Then
        verify(banRepository, times(1)).findByBanId(banIdToDelete);
        verify(banRepository, times(1)).delete(banToDelete);
    }

    @Test
    @DisplayName("제재 삭제 실패 테스트 - 제재 없음")
    void deleteBanNotFoundFailTest() {
        // Given
        Long banIdToDelete = 3L;
        when(banRepository.findByBanId(banIdToDelete)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BusinessException.class, () -> banService.deleteBan(banIdToDelete));
        verify(banRepository, times(1)).findByBanId(banIdToDelete);
        verify(banRepository, never()).delete(any());
    }
}