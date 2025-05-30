package com.miniproject.rookiejangter.service;

import com.miniproject.rookiejangter.controller.dto.ReportDTO;
import com.miniproject.rookiejangter.entity.Report;
import com.miniproject.rookiejangter.entity.ReportReason;
import com.miniproject.rookiejangter.entity.User;
import com.miniproject.rookiejangter.exception.BusinessException;
import com.miniproject.rookiejangter.exception.ErrorCode;
import com.miniproject.rookiejangter.repository.ReportReasonRepository;
import com.miniproject.rookiejangter.repository.ReportRepository;
import com.miniproject.rookiejangter.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private ReportReasonRepository reportReasonRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    @DisplayName("신고 생성 성공 테스트")
    void createReportSuccessTest() {
        // Given
        Long userId = 1L;
        ReportDTO.Request request = ReportDTO.Request.builder()
                .reportReasonId(10)
                .targetId(2L)
                .targetType("USER")
                .reportDetail("불량 행위")
                .build();
        ReportReason reportReason = ReportReason.builder().reportReasonId(10).reportReasonType("광고").build();
        User reporter = User.builder().userId(userId).build();
        Report savedReport = Report.builder()
                .reportId(100L)
                .reportReason(reportReason)
                .user(reporter)
                .targetId(2L)
                .targetType("USER")
                .reportDetail("불량 행위")
                .isProcessed(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(reportReasonRepository.findByReportReasonId(10)).thenReturn(Optional.of(reportReason));
        when(userRepository.findById(userId)).thenReturn(Optional.of(reporter));
        when(reportRepository.save(any(Report.class))).thenReturn(savedReport);

        // When
        ReportDTO.Response response = reportService.createReport(request, userId);

        // Then
        assertThat(response.getReportId()).isEqualTo(100L);
        assertThat(response.getReportReasonId()).isEqualTo(10);
        assertThat(response.getReporterId()).isEqualTo(userId);
        assertThat(response.getTargetId()).isEqualTo(2L);
        assertThat(response.getTargetType()).isEqualTo("USER");
        assertThat(response.getReportDetail()).isEqualTo("불량 행위");
        assertThat(response.getIsProcessed()).isFalse();
        verify(reportReasonRepository, times(1)).findByReportReasonId(10);
        verify(userRepository, times(1)).findById(userId);
        verify(reportRepository, times(1)).save(any(Report.class));
    }

    @Test
    @DisplayName("신고 생성 실패 테스트 - 신고 사유 없음")
    void createReportReportReasonNotFoundFailTest() {
        // Given
        Long userId = 1L;
        ReportDTO.Request request = ReportDTO.Request.builder()
                .reportReasonId(10)
                .targetId(2L)
                .targetType("USER")
                .reportDetail("불량 행위")
                .build();
        when(reportReasonRepository.findByReportReasonId(10)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BusinessException.class, () -> reportService.createReport(request, userId));
        verify(reportReasonRepository, times(1)).findByReportReasonId(10);
        verify(userRepository, never()).findById(anyLong());
        verify(reportRepository, never()).save(any());
    }

    @Test
    @DisplayName("신고 생성 실패 테스트 - 유저 없음")
    void createReportUserNotFoundFailTest() {
        // Given
        Long userId = 1L;
        ReportDTO.Request request = ReportDTO.Request.builder()
                .reportReasonId(10)
                .targetId(2L)
                .targetType("USER")
                .reportDetail("불량 행위")
                .build();
        ReportReason reportReason = ReportReason.builder().reportReasonId(10).reportReasonType("광고").build();
        when(reportReasonRepository.findByReportReasonId(10)).thenReturn(Optional.of(reportReason));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BusinessException.class, () -> reportService.createReport(request, userId));
        verify(reportReasonRepository, times(1)).findByReportReasonId(10);
        verify(userRepository, times(1)).findById(userId);
        verify(reportRepository, never()).save(any());
    }

    @Test
    @DisplayName("신고 ID로 조회 성공 테스트")
    void getReportByIdSuccessTest() {
        // Given
        Long reportId = 100L;
        ReportReason reportReason = ReportReason.builder().reportReasonId(10).reportReasonType("광고").build();
        User reporter = User.builder().userId(1L).build();
        Report report = Report.builder()
                .reportId(reportId)
                .reportReason(reportReason)
                .user(reporter)
                .targetId(2L)
                .targetType("USER")
                .reportDetail("불량 행위")
                .isProcessed(false)
                .createdAt(LocalDateTime.now())
                .build();
        when(reportRepository.findByReportId(reportId)).thenReturn(Optional.of(report));

        // When
        ReportDTO.Response response = reportService.getReportById(reportId);

        // Then
        assertThat(response.getReportId()).isEqualTo(reportId);
        assertThat(response.getReportReasonId()).isEqualTo(10);
        assertThat(response.getReporterId()).isEqualTo(1L);
        verify(reportRepository, times(1)).findByReportId(reportId);
    }

    @Test
    @DisplayName("신고 ID로 조회 실패 테스트 - 신고 없음")
    void getReportByIdNotFoundFailTest() {
        // Given
        Long reportId = 100L;
        when(reportRepository.findByReportId(reportId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BusinessException.class, () -> reportService.getReportById(reportId));
        verify(reportRepository, times(1)).findByReportId(reportId);
    }

    @Test
    @DisplayName("유저 ID로 신고 목록 조회 성공 테스트")
    void getReportsByUserIdSuccessTest() {
        // Given
        Long userId = 1L;
        User reporter = User.builder().userId(userId).build();
        ReportReason reason1 = ReportReason.builder().reportReasonId(10).reportReasonType("광고").build();
        ReportReason reason2 = ReportReason.builder().reportReasonId(11).reportReasonType("욕설").build();
        List<Report> reports = Arrays.asList(
                Report.builder().reportId(100L).reportReason(reason1).user(reporter).targetId(2L).targetType("USER").reportDetail("광고성 게시물").isProcessed(false).createdAt(LocalDateTime.now()).build(),
                Report.builder().reportId(101L).reportReason(reason2).user(reporter).targetId(3L).targetType("PRODUCT").reportDetail("부적절한 내용").isProcessed(true).createdAt(LocalDateTime.now().minusHours(1)).build()
        );
        when(reportRepository.findByUser_UserId(userId)).thenReturn(reports);

        // When
        List<ReportDTO.Response> responses = reportService.getReportsByUserId(userId);

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getReporterId()).isEqualTo(userId);
        assertThat(responses.get(1).getReporterId()).isEqualTo(userId);
        verify(reportRepository, times(1)).findByUser_UserId(userId);
    }

    @Test
    @DisplayName("유저 ID로 신고 목록 조회 성공 테스트 - 신고 없음")
    void getReportsByUserIdNoReportsTest() {
        // Given
        Long userId = 1L;
        when(reportRepository.findByUser_UserId(userId)).thenReturn(List.of());

        // When
        List<ReportDTO.Response> responses = reportService.getReportsByUserId(userId);

        // Then
        assertThat(responses).isEmpty();
        verify(reportRepository, times(1)).findByUser_UserId(userId);
    }

    @Test
    @DisplayName("처리되지 않은 신고 목록 조회 성공 테스트")
    void getUnprocessedReportsSuccessTest() {
        // Given
        ReportReason reason1 = ReportReason.builder().reportReasonId(10).reportReasonType("광고").build();
        User reporter1 = User.builder().userId(1L).build();
        User reporter2 = User.builder().userId(2L).build();
        List<Report> reports = Arrays.asList(
                Report.builder().reportId(100L).reportReason(reason1).user(reporter1).targetId(2L).targetType("USER").reportDetail("광고").isProcessed(false).createdAt(LocalDateTime.now()).build(),
                Report.builder().reportId(101L).reportReason(reason1).user(reporter2).targetId(3L).targetType("PRODUCT").reportDetail("사기 의심").isProcessed(false).createdAt(LocalDateTime.now().minusHours(1)).build(),
                Report.builder().reportId(102L).reportReason(reason1).user(reporter1).targetId(4L).targetType("USER").reportDetail("욕설").isProcessed(true).createdAt(LocalDateTime.now().minusDays(1)).build()
        );
        when(reportRepository.findByIsProcessedFalse()).thenReturn(reports.stream().filter(report -> !report.getIsProcessed()).collect(Collectors.toList()));

        // When
        List<ReportDTO.Response> responses = reportService.getUnprocessedReports();

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getIsProcessed()).isFalse();
        assertThat(responses.get(1).getIsProcessed()).isFalse();
        verify(reportRepository, times(1)).findByIsProcessedFalse();
    }

    @Test
    @DisplayName("처리되지 않은 신고 목록 조회 성공 테스트 - 없음")
    void getUnprocessedReportsNoReportsTest() {
        // Given
        when(reportRepository.findByIsProcessedFalse()).thenReturn(List.of());

        // When
        List<ReportDTO.Response> responses = reportService.getUnprocessedReports();

        // Then
        assertThat(responses).isEmpty();
        verify(reportRepository, times(1)).findByIsProcessedFalse();
    }

    @Test
    @DisplayName("신고 수정 성공 테스트")
    void updateReportSuccessTest() {
        // Given
        Long reportIdToUpdate = 100L;
        ReportDTO.Request updateRequest = ReportDTO.Request.builder()
                .reportReasonId(11)
                .targetId(3L)
                .targetType("PRODUCT")
                .reportDetail("수정된 신고 내용")
                .build();
        ReportReason originalReason = ReportReason.builder().reportReasonId(10).reportReasonType("광고").build();
        ReportReason updatedReason = ReportReason.builder().reportReasonId(11).reportReasonType("욕설").build();
        User reporter = User.builder().userId(1L).build();
        Report originalReport = Report.builder()
                .reportId(reportIdToUpdate)
                .reportReason(originalReason)
                .user(reporter)
                .targetId(2L)
                .targetType("USER")
                .reportDetail("원래 신고 내용")
                .isProcessed(false)
                .createdAt(LocalDateTime.now())
                .build();
        Report updatedReport = Report.builder()
                .reportId(reportIdToUpdate)
                .reportReason(updatedReason)
                .user(reporter)
                .targetId(3L)
                .targetType("PRODUCT")
                .reportDetail("수정된 신고 내용")
                .isProcessed(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(reportRepository.findByReportId(reportIdToUpdate)).thenReturn(Optional.of(originalReport));
        when(reportReasonRepository.findByReportReasonId(11)).thenReturn(Optional.of(updatedReason));
        when(reportRepository.save(any(Report.class))).thenReturn(updatedReport);

        // When
        ReportDTO.Response response = reportService.updateReport(reportIdToUpdate, updateRequest);

        // Then
        assertThat(response.getReportId()).isEqualTo(reportIdToUpdate);
        assertThat(response.getReportReasonId()).isEqualTo(11);
        assertThat(response.getTargetId()).isEqualTo(3L);
        assertThat(response.getTargetType()).isEqualTo("PRODUCT");
        assertThat(response.getReportDetail()).isEqualTo("수정된 신고 내용");
        verify(reportRepository, times(1)).findByReportId(reportIdToUpdate);
        verify(reportReasonRepository, times(1)).findByReportReasonId(11);
        verify(reportRepository, times(1)).save(any(Report.class));
    }

    @Test
    @DisplayName("신고 수정 실패 테스트 - 신고 없음")
    void updateReportNotFoundFailTest() {
        // Given
        Long reportIdToUpdate = 100L;
        ReportDTO.Request updateRequest = ReportDTO.Request.builder()
                .reportReasonId(11)
                .targetId(3L)
                .targetType("PRODUCT")
                .reportDetail("수정된 신고 내용")
                .build();
        when(reportRepository.findByReportId(reportIdToUpdate)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BusinessException.class, () -> reportService.updateReport(reportIdToUpdate, updateRequest));
        verify(reportRepository, times(1)).findByReportId(reportIdToUpdate);
        verify(reportReasonRepository, never()).findByReportReasonId(anyInt());
        verify(reportRepository, never()).save(any());
    }

    @Test
    @DisplayName("신고 수정 실패 테스트 - 변경할 신고 사유 없음")
    void updateReportReasonNotFoundFailTest() {
        // Given
        Long reportIdToUpdate = 100L;
        ReportDTO.Request updateRequest = ReportDTO.Request.builder()
                .reportReasonId(11)
                .targetId(3L)
                .targetType("PRODUCT")
                .reportDetail("수정된 신고 내용")
                .build();
        ReportReason originalReason = ReportReason.builder().reportReasonId(10).reportReasonType("광고").build();
        User reporter = User.builder().userId(1L).build();
        Report originalReport = Report.builder()
                .reportId(reportIdToUpdate)
                .reportReason(originalReason)
                .user(reporter)
                .targetId(2L)
                .targetType("USER")
                .reportDetail("원래 신고 내용")
                .isProcessed(false)
                .createdAt(LocalDateTime.now())
                .build();
        when(reportRepository.findByReportId(reportIdToUpdate)).thenReturn(Optional.of(originalReport));
        when(reportReasonRepository.findByReportReasonId(11)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BusinessException.class, () -> reportService.updateReport(reportIdToUpdate, updateRequest));
        verify(reportRepository, times(1)).findByReportId(reportIdToUpdate);
        verify(reportReasonRepository, times(1)).findByReportReasonId(11);
        verify(reportRepository, never()).save(any());
    }

    @Test
    @DisplayName("신고 처리 완료 성공 테스트")
    void markReportAsProcessedSuccessTest() {
        // Given
        Long reportIdToProcess = 100L;
        ReportReason reason = ReportReason.builder().reportReasonId(10).reportReasonType("광고").build();
        User reporter = User.builder().userId(1L).build();
        Report report = Report.builder()
                .reportId(reportIdToProcess)
                .reportReason(reason)
                .user(reporter)
                .targetId(2L)
                .targetType("USER")
                .reportDetail("광고")
                .isProcessed(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(reportRepository.findByReportId(reportIdToProcess)).thenReturn(Optional.of(report));
        when(reportRepository.save(any(Report.class))).thenReturn(Report.builder()
                .reportId(reportIdToProcess)
                .reportReason(reason)
                .user(reporter)
                .targetId(2L)
                .targetType("USER")
                .reportDetail("광고")
                .isProcessed(true) // Processed 상태로 변경
                .createdAt(LocalDateTime.now())
                .build());

        // When
        reportService.markReportAsProcessed(reportIdToProcess);

        // Then
        ArgumentCaptor<Report> reportCaptor = ArgumentCaptor.forClass(Report.class);
        verify(reportRepository, times(1)).findByReportId(reportIdToProcess);
        verify(reportRepository, times(1)).save(reportCaptor.capture());
        assertThat(reportCaptor.getValue().getIsProcessed()).isTrue();
    }

    @Test
    @DisplayName("신고 처리 완료 실패 테스트 - 신고 없음")
    void markReportAsProcessedNotFoundFailTest() {
        // Given
        Long reportIdToProcess = 100L;
        when(reportRepository.findByReportId(reportIdToProcess)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BusinessException.class, () -> reportService.markReportAsProcessed(reportIdToProcess));
        verify(reportRepository, times(1)).findByReportId(reportIdToProcess);
        verify(reportRepository, never()).save(any());
    }
}