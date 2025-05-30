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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CancelationService {

    private final CancelationRepository cancelationRepository;
    private final CancelationReasonRepository cancelationReasonRepository;
    private final ReservationRepository reservationRepository;

    /**
     * 취소 정보 생성
     * @param reservationId
     * @param request
     * @return
     */
    public CancelationDTO.Response createCancelation(Long reservationId, CancelationDTO.Request request) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRADE_NOT_FOUND, reservationId));

        CancelationReason cancelationReason = cancelationReasonRepository.findById(request.getCancelationReasonId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "CancelationReason", "ID", request.getCancelationReasonId()));

        Cancelation cancelation = Cancelation.builder()
                .reservation(reservation)
                .cancelationReason(cancelationReason)
                .cancelationDetail(request.getCancelationDetail())
                .canceledAt(LocalDateTime.now())
                .build();

        Cancelation savedCancelation = cancelationRepository.save(cancelation);
        return CancelationDTO.Response.fromEntity(savedCancelation);
    }

    /**
     * 예약 ID로 취소 정보 조회
     * @param reservationId
     * @return
     */
    public CancelationDTO.Response getCancelationByReservationId(Long reservationId) {
        Cancelation cancelation = cancelationRepository.findByReservation_ReservationId(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRADE_NOT_FOUND, reservationId));

        return CancelationDTO.Response.fromEntity(cancelation);
    }

    /**
     * 취소 사유 ID로 취소 정보 목록 조회
     * @param cancelationReasonId
     * @return
     */
    public List<CancelationDTO.Response> getCancelationsByCancelationReasonId(Integer cancelationReasonId) {
        List<Cancelation> cancelations = cancelationRepository.findByCancelationReason_CancelationReasonId(cancelationReasonId);

        return cancelations.stream()
                .map(CancelationDTO.Response::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 취소 정보 수정
     * @param reservationId
     * @param request
     * @return
     */
    public CancelationDTO.Response updateCancelation(Long reservationId, CancelationDTO.Request request) {
        Cancelation cancelation = cancelationRepository.findByReservation_ReservationId(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRADE_NOT_FOUND, reservationId));

        CancelationReason cancelationReason = cancelationReasonRepository.findById(request.getCancelationReasonId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "CancelationReason", "ID", request.getCancelationReasonId()));

        cancelation.setCancelationReason(cancelationReason);
        cancelation.setCancelationDetail(request.getCancelationDetail());

        Cancelation updatedCancelation = cancelationRepository.save(cancelation);
        return CancelationDTO.Response.fromEntity(updatedCancelation);
    }

    /**
     * 취소 정보 삭제
     * @param reservationId
     */
    public void deleteCancelation(Long reservationId) {
        Cancelation cancelation = cancelationRepository.findByReservation_ReservationId(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRADE_NOT_FOUND, reservationId));

        cancelationRepository.delete(cancelation);
    }
}