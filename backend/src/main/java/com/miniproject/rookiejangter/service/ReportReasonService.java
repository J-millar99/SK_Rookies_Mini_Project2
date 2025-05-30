package com.miniproject.rookiejangter.service;

import com.miniproject.rookiejangter.controller.dto.ReportReasonDTO;
import com.miniproject.rookiejangter.entity.ReportReason;
import com.miniproject.rookiejangter.exception.BusinessException;
import com.miniproject.rookiejangter.exception.ErrorCode;
import com.miniproject.rookiejangter.repository.ReportReasonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ReportReasonService {

    private final ReportReasonRepository reportReasonRepository;

    /**
     * 신고 사유 생성
     * @param request
     * @return
     */
    public ReportReasonDTO.Response createReportReason(ReportReasonDTO.Request request) {
        ReportReason reportReason = ReportReason.builder()
                .reportReasonType(request.getReportReasonType())
                .build();

        ReportReason savedReportReason = reportReasonRepository.save(reportReason);
        return ReportReasonDTO.Response.fromEntity(savedReportReason);
    }

    /**
     * 신고 사유 ID로 조회
     * @param reportReasonId
     * @return
     */
    public ReportReasonDTO.Response getReportReasonById(Integer reportReasonId) {
        ReportReason reportReason = reportReasonRepository.findByReportReasonId(reportReasonId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "ReportReason", reportReasonId, ""));

        return ReportReasonDTO.Response.fromEntity(reportReason);
    }

    /**
     * 모든 신고 사유 목록 조회
     * @return
     */
    public List<ReportReasonDTO.Response> getAllReportReasons() {
        List<ReportReason> reportReasons = reportReasonRepository.findAll();

        return reportReasons.stream()
                .map(ReportReasonDTO.Response::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 신고 사유 수정
     * @param reportReasonId
     * @param request
     * @return
     */
    public ReportReasonDTO.Response updateReportReason(Integer reportReasonId, ReportReasonDTO.Request request) {
        ReportReason reportReason = reportReasonRepository.findByReportReasonId(reportReasonId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "ReportReason", reportReasonId, ""));

        reportReason.setReportReasonType(request.getReportReasonType());

        ReportReason updatedReportReason = reportReasonRepository.save(reportReason);
        return ReportReasonDTO.Response.fromEntity(updatedReportReason);
    }

    /**
     * 신고 사유 삭제
     * @param reportReasonId
     */
    public void deleteReportReason(Integer reportReasonId) {
        ReportReason reportReason = reportReasonRepository.findByReportReasonId(reportReasonId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "ReportReason", reportReasonId, ""));

        reportReasonRepository.deleteById(reportReasonId);
    }
}