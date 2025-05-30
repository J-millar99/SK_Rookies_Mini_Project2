package com.miniproject.rookiejangter.service;

import com.miniproject.rookiejangter.controller.dto.ReportReasonDTO;
import com.miniproject.rookiejangter.entity.ReportReason;
import com.miniproject.rookiejangter.exception.BusinessException;
import com.miniproject.rookiejangter.exception.ErrorCode;
import com.miniproject.rookiejangter.repository.ReportReasonRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportReasonServiceTest {

    @Mock
    private ReportReasonRepository reportReasonRepository;

    @InjectMocks
    private ReportReasonService reportReasonService;

    @Test
    @DisplayName("신고 사유 생성 성공 테스트")
    void createReportReasonSuccessTest() {
        // Given
        ReportReasonDTO.Request request = ReportReasonDTO.Request.builder()
                .reportReasonType("광고")
                .build();
        ReportReason savedReportReason = ReportReason.builder()
                .reportReasonId(10)
                .reportReasonType("광고")
                .build();

        when(reportReasonRepository.save(any(ReportReason.class))).thenReturn(savedReportReason);

        // When
        ReportReasonDTO.Response response = reportReasonService.createReportReason(request);

        // Then
        assertThat(response.getReportReasonId()).isEqualTo(10);
        assertThat(response.getReportReasonType()).isEqualTo("광고");
        verify(reportReasonRepository, times(1)).save(any(ReportReason.class));
    }

    @Test
    @DisplayName("신고 사유 ID로 조회 성공 테스트")
    void getReportReasonByIdSuccessTest() {
        // Given
        Integer reportReasonId = 10;
        ReportReason foundReportReason = ReportReason.builder()
                .reportReasonId(reportReasonId)
                .reportReasonType("광고")
                .build();

        when(reportReasonRepository.findByReportReasonId(reportReasonId)).thenReturn(Optional.of(foundReportReason));

        // When
        ReportReasonDTO.Response response = reportReasonService.getReportReasonById(reportReasonId);

        // Then
        assertThat(response.getReportReasonId()).isEqualTo(reportReasonId);
        assertThat(response.getReportReasonType()).isEqualTo("광고");
        verify(reportReasonRepository, times(1)).findByReportReasonId(reportReasonId);
    }

    @Test
    @DisplayName("신고 사유 ID로 조회 실패 테스트 - 신고 사유 없음")
    void getReportReasonByIdNotFoundFailTest() {
        // Given
        Integer reportReasonId = 10;
        when(reportReasonRepository.findByReportReasonId(reportReasonId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BusinessException.class, () -> reportReasonService.getReportReasonById(reportReasonId));
        verify(reportReasonRepository, times(1)).findByReportReasonId(reportReasonId);
    }

    @Test
    @DisplayName("모든 신고 사유 목록 조회 성공 테스트")
    void getAllReportReasonsSuccessTest() {
        // Given
        List<ReportReason> reportReasons = Arrays.asList(
                ReportReason.builder().reportReasonId(10).reportReasonType("광고").build(),
                ReportReason.builder().reportReasonId(11).reportReasonType("욕설").build()
        );
        when(reportReasonRepository.findAll()).thenReturn(reportReasons);

        // When
        List<ReportReasonDTO.Response> responses = reportReasonService.getAllReportReasons();

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getReportReasonType()).isEqualTo("광고");
        assertThat(responses.get(1).getReportReasonType()).isEqualTo("욕설");
        verify(reportReasonRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("모든 신고 사유 목록 조회 성공 테스트 - 목록 없음")
    void getAllReportReasonsNoReasonsTest() {
        // Given
        when(reportReasonRepository.findAll()).thenReturn(List.of());

        // When
        List<ReportReasonDTO.Response> responses = reportReasonService.getAllReportReasons();

        // Then
        assertThat(responses).isEmpty();
        verify(reportReasonRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("신고 사유 수정 성공 테스트")
    void updateReportReasonSuccessTest() {
        // Given
        Integer reportReasonIdToUpdate = 10;
        ReportReasonDTO.Request updateRequest = ReportReasonDTO.Request.builder()
                .reportReasonType("스팸")
                .build();
        ReportReason originalReportReason = ReportReason.builder()
                .reportReasonId(reportReasonIdToUpdate)
                .reportReasonType("광고")
                .build();
        ReportReason updatedReportReason = ReportReason.builder()
                .reportReasonId(reportReasonIdToUpdate)
                .reportReasonType("스팸")
                .build();

        when(reportReasonRepository.findByReportReasonId(reportReasonIdToUpdate)).thenReturn(Optional.of(originalReportReason));
        when(reportReasonRepository.save(any(ReportReason.class))).thenReturn(updatedReportReason);

        // When
        ReportReasonDTO.Response response = reportReasonService.updateReportReason(reportReasonIdToUpdate, updateRequest);

        // Then
        assertThat(response.getReportReasonId()).isEqualTo(reportReasonIdToUpdate);
        assertThat(response.getReportReasonType()).isEqualTo("스팸");
        verify(reportReasonRepository, times(1)).findByReportReasonId(reportReasonIdToUpdate);
        verify(reportReasonRepository, times(1)).save(any(ReportReason.class));
    }

    @Test
    @DisplayName("신고 사유 수정 실패 테스트 - 신고 사유 없음")
    void updateReportReasonNotFoundFailTest() {
        // Given
        Integer reportReasonIdToUpdate = 10;
        ReportReasonDTO.Request updateRequest = ReportReasonDTO.Request.builder()
                .reportReasonType("스팸")
                .build();
        when(reportReasonRepository.findByReportReasonId(reportReasonIdToUpdate)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BusinessException.class, () -> reportReasonService.updateReportReason(reportReasonIdToUpdate, updateRequest));
        verify(reportReasonRepository, times(1)).findByReportReasonId(reportReasonIdToUpdate);
        verify(reportReasonRepository, never()).save(any());
    }

    @Test
    @DisplayName("신고 사유 삭제 성공 테스트")
    void deleteReportReasonSuccessTest() {
        // Given
        Integer reportReasonIdToDelete = 10;
        ReportReason reportReasonToDelete = ReportReason.builder()
                .reportReasonId(reportReasonIdToDelete)
                .reportReasonType("광고")
                .build();

        when(reportReasonRepository.findByReportReasonId(reportReasonIdToDelete)).thenReturn(Optional.of(reportReasonToDelete));
        doNothing().when(reportReasonRepository).deleteById(reportReasonIdToDelete);

        // When
        reportReasonService.deleteReportReason(reportReasonIdToDelete);

        // Then
        verify(reportReasonRepository, times(1)).findByReportReasonId(reportReasonIdToDelete);
        verify(reportReasonRepository, times(1)).deleteById(reportReasonIdToDelete);
    }

    @Test
    @DisplayName("신고 사유 삭제 실패 테스트 - 신고 사유 없음")
    void deleteReportReasonNotFoundFailTest() {
        // Given
        Integer reportReasonIdToDelete = 10;
        when(reportReasonRepository.findByReportReasonId(reportReasonIdToDelete)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BusinessException.class, () -> reportReasonService.deleteReportReason(reportReasonIdToDelete));
        verify(reportReasonRepository, times(1)).findByReportReasonId(reportReasonIdToDelete);
        verify(reportReasonRepository, never()).deleteById(anyInt());
    }
}