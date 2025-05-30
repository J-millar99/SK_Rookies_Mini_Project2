package com.miniproject.rookiejangter.service;

import com.miniproject.rookiejangter.controller.dto.CancelationReasonDTO;
import com.miniproject.rookiejangter.entity.CancelationReason;
import com.miniproject.rookiejangter.exception.BusinessException;
import com.miniproject.rookiejangter.exception.ErrorCode;
import com.miniproject.rookiejangter.repository.CancelationReasonRepository;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CancelationReasonServiceTest {

    @Mock
    private CancelationReasonRepository cancelationReasonRepository;

    @InjectMocks
    private CancelationReasonService cancelationReasonService;

    @Test
    @DisplayName("취소 사유 생성 테스트")
    void createCancelationReasonTest() {
        // Given
        CancelationReasonDTO.Request request = CancelationReasonDTO.Request.builder()
                .cancelationReasonType("단순 변심")
                .build();
        CancelationReason savedReason = CancelationReason.builder()
                .cancelationReasonId(1)
                .cancelationReasonType("단순 변심")
                .build();
        when(cancelationReasonRepository.save(any(CancelationReason.class))).thenReturn(savedReason);

        // When
        CancelationReasonDTO.Response response = cancelationReasonService.createCancelationReason(request);

        // Then
        assertThat(response.getCancelationReasonId()).isEqualTo(1);
        assertThat(response.getCancelationReasonType()).isEqualTo("단순 변심");
        verify(cancelationReasonRepository, times(1)).save(any(CancelationReason.class));
    }

    @Test
    @DisplayName("ID로 취소 사유 조회 성공 테스트")
    void getCancelationReasonByIdSuccessTest() {
        // Given
        Integer reasonId = 1;
        CancelationReason foundReason = CancelationReason.builder()
                .cancelationReasonId(reasonId)
                .cancelationReasonType("상품 불량")
                .build();
        when(cancelationReasonRepository.findById(reasonId)).thenReturn(Optional.of(foundReason));

        // When
        CancelationReasonDTO.Response response = cancelationReasonService.getCancelationReasonById(reasonId);

        // Then
        assertThat(response.getCancelationReasonId()).isEqualTo(reasonId);
        assertThat(response.getCancelationReasonType()).isEqualTo("상품 불량");
        verify(cancelationReasonRepository, times(1)).findById(reasonId);
    }

    @Test
    @DisplayName("ID로 취소 사유 조회 실패 테스트 (BusinessException)")
    void getCancelationReasonByIdNotFoundTest() {
        // Given
        Integer invalidReasonId = 999;
        when(cancelationReasonRepository.findById(invalidReasonId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BusinessException.class, () -> cancelationReasonService.getCancelationReasonById(invalidReasonId));
        verify(cancelationReasonRepository, times(1)).findById(invalidReasonId);
    }

    @Test
    @DisplayName("모든 취소 사유 목록 조회 테스트")
    void getAllCancelationReasonsTest() {
        // Given
        List<CancelationReason> reasons = Arrays.asList(
                CancelationReason.builder().cancelationReasonId(1).cancelationReasonType("배송 지연").build(),
                CancelationReason.builder().cancelationReasonId(2).cancelationReasonType("사이즈 미스").build()
        );
        when(cancelationReasonRepository.findAll()).thenReturn(reasons);

        // When
        List<CancelationReasonDTO.Response> responses = cancelationReasonService.getAllCancelationReasons();

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getCancelationReasonType()).isEqualTo("배송 지연");
        assertThat(responses.get(1).getCancelationReasonType()).isEqualTo("사이즈 미스");
        verify(cancelationReasonRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("취소 사유 수정 테스트")
    void updateCancelationReasonTest() {
        // Given
        Integer reasonId = 1;
        CancelationReasonDTO.Request updateRequest = CancelationReasonDTO.Request.builder()
                .cancelationReasonType("색상 변경")
                .build();
        CancelationReason existingReason = CancelationReason.builder()
                .cancelationReasonId(reasonId)
                .cancelationReasonType("단순 변심")
                .build();
        CancelationReason updatedReason = CancelationReason.builder()
                .cancelationReasonId(reasonId)
                .cancelationReasonType("색상 변경")
                .build();
        when(cancelationReasonRepository.findById(reasonId)).thenReturn(Optional.of(existingReason));
        when(cancelationReasonRepository.save(any(CancelationReason.class))).thenReturn(updatedReason);

        // When
        CancelationReasonDTO.Response response = cancelationReasonService.updateCancelationReason(reasonId, updateRequest);

        // Then
        assertThat(response.getCancelationReasonId()).isEqualTo(reasonId);
        assertThat(response.getCancelationReasonType()).isEqualTo("색상 변경");
        verify(cancelationReasonRepository, times(1)).findById(reasonId);
        verify(cancelationReasonRepository, times(1)).save(any(CancelationReason.class));
    }

    @Test
    @DisplayName("취소 사유 수정 실패 테스트 (BusinessException)")
    void updateCancelationReasonNotFoundTest() {
        // Given
        Integer invalidReasonId = 999;
        CancelationReasonDTO.Request updateRequest = CancelationReasonDTO.Request.builder()
                .cancelationReasonType("사이즈 변경")
                .build();
        when(cancelationReasonRepository.findById(invalidReasonId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BusinessException.class, () -> cancelationReasonService.updateCancelationReason(invalidReasonId, updateRequest));
        verify(cancelationReasonRepository, times(1)).findById(invalidReasonId);
        verify(cancelationReasonRepository, never()).save(any(CancelationReason.class));
    }

    @Test
    @DisplayName("취소 사유 삭제 성공 테스트")
    void deleteCancelationReasonSuccessTest() {
        // Given
        Integer reasonId = 1;
        CancelationReason reasonToDelete = CancelationReason.builder()
                .cancelationReasonId(reasonId)
                .cancelationReasonType("오배송")
                .build();
        when(cancelationReasonRepository.findById(reasonId)).thenReturn(Optional.of(reasonToDelete));
        doNothing().when(cancelationReasonRepository).delete(reasonToDelete);

        // When
        cancelationReasonService.deleteCancelationReason(reasonId);

        // Then
        verify(cancelationReasonRepository, times(1)).findById(reasonId);
        verify(cancelationReasonRepository, times(1)).delete(reasonToDelete);
    }

    @Test
    @DisplayName("취소 사유 삭제 실패 테스트 (BusinessException)")
    void deleteCancelationReasonNotFoundTest() {
        // Given
        Integer invalidReasonId = 999;
        when(cancelationReasonRepository.findById(invalidReasonId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BusinessException.class, () -> cancelationReasonService.deleteCancelationReason(invalidReasonId));
        verify(cancelationReasonRepository, times(1)).findById(invalidReasonId);
        verify(cancelationReasonRepository, never()).delete(any());
    }
}