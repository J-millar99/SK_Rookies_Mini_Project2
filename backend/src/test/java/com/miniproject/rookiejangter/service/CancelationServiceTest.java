package com.miniproject.rookiejangter.service;

import com.miniproject.rookiejangter.controller.dto.CancelationDTO;
import com.miniproject.rookiejangter.entity.Cancelation;
import com.miniproject.rookiejangter.entity.CancelationReason;
import com.miniproject.rookiejangter.entity.Reservation;
import com.miniproject.rookiejangter.exception.BusinessException;
import com.miniproject.rookiejangter.exception.ErrorCode;
import com.miniproject.rookiejangter.repository.CancelationReasonRepository;
import com.miniproject.rookiejangter.repository.CancelationRepository;
import com.miniproject.rookiejangter.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CancelationServiceTest {

    @Mock
    private CancelationRepository cancelationRepository;

    @Mock
    private CancelationReasonRepository cancelationReasonRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private CancelationService cancelationService;

    private Reservation reservation;
    private CancelationReason cancelationReason;
    private Cancelation cancelation;

    @BeforeEach
    void setUp() {
        reservation = Reservation.builder().reservationId(1L).build();
        cancelationReason = CancelationReason.builder().cancelationReasonId(10).cancelationReasonType("테스트 사유").build();
        cancelation = Cancelation.builder()
                .cancelationId(1L)
                .reservation(reservation)
                .cancelationReason(cancelationReason)
                .cancelationDetail("Test Cancelation")
                .canceledAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("예약 취소 생성 성공")
    void createCancelation_성공() {
        // Given
        CancelationDTO.Request request = CancelationDTO.Request.builder()
                .cancelationReasonId(cancelationReason.getCancelationReasonId())
                .cancelationDetail("Test Cancelation")
                .build();
        Cancelation savedCancelation = Cancelation.builder()
                .cancelationId(1L)
                .reservation(reservation)
                .cancelationReason(cancelationReason)
                .cancelationDetail("Test Cancelation")
                .canceledAt(LocalDateTime.now())
                .build();

        when(reservationRepository.findById(reservation.getReservationId())).thenReturn(Optional.of(reservation));
        when(cancelationReasonRepository.findById(request.getCancelationReasonId())).thenReturn(Optional.of(cancelationReason));
        when(cancelationRepository.save(any(Cancelation.class))).thenReturn(savedCancelation);

        // When
        CancelationDTO.Response response = cancelationService.createCancelation(reservation.getReservationId(), request);

        // Then
        assertNotNull(response);
        assertEquals(reservation.getReservationId(), response.getReservationId());
        assertEquals(cancelationReason.getCancelationReasonId(), response.getCancelationReasonId());
        verify(reservationRepository, times(1)).findById(reservation.getReservationId());
        verify(cancelationReasonRepository, times(1)).findById(request.getCancelationReasonId());
        verify(cancelationRepository, times(1)).save(any(Cancelation.class));
    }

    @Test
    @DisplayName("예약 취소 생성 실패 - 예약 정보 없음")
    void createCancelation_실패_RESERVATION_NOT_FOUND() {
        // Given
        CancelationDTO.Request request = CancelationDTO.Request.builder()
                .cancelationReasonId(cancelationReason.getCancelationReasonId())
                .cancelationDetail("Test Cancelation")
                .build();

        when(reservationRepository.findById(reservation.getReservationId())).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> cancelationService.createCancelation(reservation.getReservationId(), request));
        assertEquals(ErrorCode.TRADE_NOT_FOUND, exception.getErrorCode());
        verify(reservationRepository, times(1)).findById(reservation.getReservationId());
        verify(cancelationReasonRepository, never()).findById(anyInt());
        verify(cancelationRepository, never()).save(any());
    }

    @Test
    @DisplayName("예약 취소 생성 실패 - 취소 사유 없음")
    void createCancelation_실패_CANCELATION_REASON_NOT_FOUND() {
        // Given
        CancelationDTO.Request request = CancelationDTO.Request.builder()
                .cancelationReasonId(cancelationReason.getCancelationReasonId())
                .cancelationDetail("Test Cancelation")
                .build();

        when(reservationRepository.findById(reservation.getReservationId())).thenReturn(Optional.of(reservation));
        when(cancelationReasonRepository.findById(request.getCancelationReasonId())).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> cancelationService.createCancelation(reservation.getReservationId(), request));
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.getErrorCode());
        assertEquals("CancelationReason을(를) 찾을 수 없습니다. ID: 10", exception.getMessage());
        verify(reservationRepository, times(1)).findById(reservation.getReservationId());
        verify(cancelationReasonRepository, times(1)).findById(request.getCancelationReasonId());
        verify(cancelationRepository, never()).save(any());
    }

    @Test
    @DisplayName("예약 ID로 취소 정보 조회 성공")
    void getCancelationByReservationId_성공() {
        // Given
        when(cancelationRepository.findByReservation_ReservationId(reservation.getReservationId())).thenReturn(Optional.of(cancelation));

        // When
        CancelationDTO.Response response = cancelationService.getCancelationByReservationId(reservation.getReservationId());

        // Then
        assertNotNull(response);
        assertEquals(reservation.getReservationId(), response.getReservationId());
        assertEquals(cancelationReason.getCancelationReasonId(), response.getCancelationReasonId());
        verify(cancelationRepository, times(1)).findByReservation_ReservationId(reservation.getReservationId());
    }

    @Test
    @DisplayName("예약 ID로 취소 정보 조회 실패 - 취소 정보 없음")
    void getCancelationByReservationId_실패_CANCELATION_NOT_FOUND() {
        // Given
        when(cancelationRepository.findByReservation_ReservationId(reservation.getReservationId())).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> cancelationService.getCancelationByReservationId(reservation.getReservationId()));
        assertEquals(ErrorCode.TRADE_NOT_FOUND, exception.getErrorCode());
        assertEquals("거래 정보를 찾을 수 없습니다. ID: 1", exception.getMessage());
        verify(cancelationRepository, times(1)).findByReservation_ReservationId(reservation.getReservationId());
    }

    @Test
    @DisplayName("취소 사유 ID로 취소 목록 조회 성공")
    void getCancelationsByCancelationReasonId_성공() {
        // Given
        List<Cancelation> cancelationList = Arrays.asList(
                Cancelation.builder().cancelationId(1L).reservation(Reservation.builder().reservationId(1L).build()).cancelationReason(cancelationReason).cancelationDetail("Detail 1").canceledAt(LocalDateTime.now()).build(),
                Cancelation.builder().cancelationId(2L).reservation(Reservation.builder().reservationId(2L).build()).cancelationReason(cancelationReason).cancelationDetail("Detail 2").canceledAt(LocalDateTime.now()).build()
        );
        when(cancelationRepository.findByCancelationReason_CancelationReasonId(cancelationReason.getCancelationReasonId())).thenReturn(cancelationList);

        // When
        List<CancelationDTO.Response> responses = cancelationService.getCancelationsByCancelationReasonId(cancelationReason.getCancelationReasonId());

        // Then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(cancelationReason.getCancelationReasonId(), responses.get(0).getCancelationReasonId());
        assertEquals(cancelationReason.getCancelationReasonId(), responses.get(1).getCancelationReasonId());
        verify(cancelationRepository, times(1)).findByCancelationReason_CancelationReasonId(cancelationReason.getCancelationReasonId());
    }

    @Test
    @DisplayName("취소 사유 ID로 취소 목록 조회 성공 - 목록 없음")
    void getCancelationsByCancelationReasonId_성공_EMPTY_LIST() {
        // Given
        when(cancelationRepository.findByCancelationReason_CancelationReasonId(cancelationReason.getCancelationReasonId())).thenReturn(List.of());

        // When
        List<CancelationDTO.Response> responses = cancelationService.getCancelationsByCancelationReasonId(cancelationReason.getCancelationReasonId());

        // Then
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(cancelationRepository, times(1)).findByCancelationReason_CancelationReasonId(cancelationReason.getCancelationReasonId());
    }

    @Test
    @DisplayName("취소 정보 업데이트 성공")
    void updateCancelation_성공() {
        // Given
        Long reservationIdToUpdate = reservation.getReservationId(); // Reservation ID로 업데이트 로직을 사용하므로
        CancelationDTO.Request updateRequest = CancelationDTO.Request.builder()
                .cancelationReasonId(20)
                .cancelationDetail("Updated Detail")
                .build();
        CancelationReason updatedReason = CancelationReason.builder().cancelationReasonId(20).cancelationReasonType("업데이트된 사유").build();
        Cancelation existingCancelation = Cancelation.builder()
                .cancelationId(1L)
                .reservation(reservation)
                .cancelationReason(cancelationReason)
                .cancelationDetail("Original Detail")
                .canceledAt(LocalDateTime.now())
                .build();
        Cancelation updatedCancelation = Cancelation.builder()
                .cancelationId(1L)
                .reservation(reservation)
                .cancelationReason(updatedReason)
                .cancelationDetail("Updated Detail")
                .canceledAt(LocalDateTime.now())
                .build();

        when(cancelationRepository.findByReservation_ReservationId(reservationIdToUpdate)).thenReturn(Optional.of(existingCancelation));
        when(cancelationReasonRepository.findById(updateRequest.getCancelationReasonId())).thenReturn(Optional.of(updatedReason));
        when(cancelationRepository.save(any(Cancelation.class))).thenReturn(updatedCancelation);

        // When
        CancelationDTO.Response response = cancelationService.updateCancelation(reservationIdToUpdate, updateRequest);

        // Then
        assertNotNull(response);
        assertEquals(reservation.getReservationId(), response.getReservationId());
        assertEquals(updatedReason.getCancelationReasonId(), response.getCancelationReasonId());
        assertEquals("Updated Detail", response.getCancelationDetail());
        verify(cancelationRepository, times(1)).findByReservation_ReservationId(reservationIdToUpdate);
        verify(cancelationReasonRepository, times(1)).findById(updateRequest.getCancelationReasonId());
        verify(cancelationRepository, times(1)).save(any(Cancelation.class));
    }

    @Test
    @DisplayName("취소 정보 업데이트 실패 - 취소 정보 없음")
    void updateCancelation_실패_CANCELATION_NOT_FOUND() {
        // Given
        Long reservationIdToUpdate = reservation.getReservationId();
        CancelationDTO.Request updateRequest = CancelationDTO.Request.builder()
                .cancelationReasonId(20)
                .cancelationDetail("Updated Detail")
                .build();

        when(cancelationRepository.findByReservation_ReservationId(reservationIdToUpdate)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> cancelationService.updateCancelation(reservationIdToUpdate, updateRequest));
        assertEquals(ErrorCode.TRADE_NOT_FOUND, exception.getErrorCode());
        assertEquals("거래 정보를 찾을 수 없습니다. ID: 1", exception.getMessage());
        verify(cancelationRepository, times(1)).findByReservation_ReservationId(reservationIdToUpdate);
        verify(cancelationReasonRepository, never()).findById(anyInt());
        verify(cancelationRepository, never()).save(any());
    }

    @Test
    @DisplayName("취소 정보 업데이트 실패 - 변경할 취소 사유 없음")
    void updateCancelation_실패_CANCELATION_REASON_NOT_FOUND() {
        // Given
        Long reservationIdToUpdate = reservation.getReservationId();
        CancelationDTO.Request updateRequest = CancelationDTO.Request.builder()
                .cancelationReasonId(20)
                .cancelationDetail("Updated Detail")
                .build();

        when(cancelationRepository.findByReservation_ReservationId(reservationIdToUpdate)).thenReturn(Optional.of(cancelation));
        when(cancelationReasonRepository.findById(updateRequest.getCancelationReasonId())).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> cancelationService.updateCancelation(reservationIdToUpdate, updateRequest));
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.getErrorCode());
        assertEquals("CancelationReason을(를) 찾을 수 없습니다. ID: 20", exception.getMessage());
        verify(cancelationRepository, times(1)).findByReservation_ReservationId(reservationIdToUpdate);
        verify(cancelationReasonRepository, times(1)).findById(updateRequest.getCancelationReasonId());
        verify(cancelationRepository, never()).save(any());
    }

    @Test
    @DisplayName("취소 정보 삭제 성공")
    void deleteCancelation_성공() {
        // Given
        Long reservationIdToDelete = reservation.getReservationId(); // Reservation ID로 삭제 로직을 사용하므로
        when(cancelationRepository.findByReservation_ReservationId(reservationIdToDelete)).thenReturn(Optional.of(cancelation));
        doNothing().when(cancelationRepository).delete(any(Cancelation.class));

        // When
        cancelationService.deleteCancelation(reservationIdToDelete);

        // Then
        verify(cancelationRepository, times(1)).findByReservation_ReservationId(reservationIdToDelete);
        verify(cancelationRepository, times(1)).delete(any(Cancelation.class));
    }

    @Test
    @DisplayName("취소 정보 삭제 실패 - 취소 정보 없음")
    void deleteCancelation_실패_CANCELATION_NOT_FOUND() {
        // Given
        Long reservationIdToDelete = reservation.getReservationId();
        when(cancelationRepository.findByReservation_ReservationId(reservationIdToDelete)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> cancelationService.deleteCancelation(reservationIdToDelete));
        assertEquals(ErrorCode.TRADE_NOT_FOUND, exception.getErrorCode());
        assertEquals("거래 정보를 찾을 수 없습니다. ID: 1", exception.getMessage());
        verify(cancelationRepository, times(1)).findByReservation_ReservationId(reservationIdToDelete);
        verify(cancelationRepository, never()).delete(any());
    }
}