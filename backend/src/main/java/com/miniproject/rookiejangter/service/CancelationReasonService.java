package com.miniproject.rookiejangter.service;

import com.miniproject.rookiejangter.controller.dto.CancelationReasonDTO;
import com.miniproject.rookiejangter.entity.CancelationReason;
import com.miniproject.rookiejangter.exception.BusinessException;
import com.miniproject.rookiejangter.exception.ErrorCode;
import com.miniproject.rookiejangter.repository.CancelationReasonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CancelationReasonService {

    private final CancelationReasonRepository cancelationReasonRepository;

    /**
     * 취소 사유 생성
     * @param request
     * @return
     */
    public CancelationReasonDTO.Response createCancelationReason(CancelationReasonDTO.Request request) {
        CancelationReason cancelationReason = CancelationReason.builder()
                .cancelationReasonType(request.getCancelationReasonType())
                .build();

        CancelationReason savedCancelationReason = cancelationReasonRepository.save(cancelationReason);
        return CancelationReasonDTO.Response.fromEntity(savedCancelationReason);
    }

    /**
     * 취소 사유 ID로 조회
     * @param cancelationReasonId
     * @return
     */
    public CancelationReasonDTO.Response getCancelationReasonById(Integer cancelationReasonId) {
        CancelationReason cancelationReason = cancelationReasonRepository.findById(cancelationReasonId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "CancelationReason", cancelationReasonId));

        return CancelationReasonDTO.Response.fromEntity(cancelationReason);
    }

    /**
     * 모든 취소 사유 목록 조회
     * @return
     */
    public List<CancelationReasonDTO.Response> getAllCancelationReasons() {
        List<CancelationReason> cancelationReasons = cancelationReasonRepository.findAll();

        return cancelationReasons.stream()
                .map(CancelationReasonDTO.Response::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 취소 사유 수정
     * @param cancelationReasonId
     * @param request
     * @return
     */
    public CancelationReasonDTO.Response updateCancelationReason(Integer cancelationReasonId, CancelationReasonDTO.Request request) {
        CancelationReason cancelationReason = cancelationReasonRepository.findById(cancelationReasonId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "CancelationReason", cancelationReasonId));

        cancelationReason.setCancelationReasonType(request.getCancelationReasonType());

        CancelationReason updatedCancelationReason = cancelationReasonRepository.save(cancelationReason);
        return CancelationReasonDTO.Response.fromEntity(updatedCancelationReason);
    }

    /**
     * 취소 사유 삭제
     * @param cancelationReasonId
     */
    public void deleteCancelationReason(Integer cancelationReasonId) {
        CancelationReason cancelationReason = cancelationReasonRepository.findById(cancelationReasonId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "CancelationReason", cancelationReasonId));

        cancelationReasonRepository.delete(cancelationReason);
    }
}