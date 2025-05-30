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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final ReportReasonRepository reportReasonRepository;
    private final UserRepository userRepository;

    /**
     * 신고 생성
     * @param request
     * @param userId
     * @return
     */
    public ReportDTO.Response createReport(ReportDTO.Request request, Long userId) {
        ReportReason reportReason = reportReasonRepository.findByReportReasonId(request.getReportReasonId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "ReportReason", request.getReportReasonId(), ""));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, userId));

        Report report = Report.builder()
                .reportReason(reportReason)
                .user(user)
                .targetId(request.getTargetId())
                .targetType(request.getTargetType())
                .reportDetail(request.getReportDetail())
                .isProcessed(false)
                .build();

        Report savedReport = reportRepository.save(report);
        return ReportDTO.Response.fromEntity(savedReport);
    }

    /**
     * 신고 ID로 조회
     * @param reportId
     * @return
     */
    public ReportDTO.Response getReportById(Long reportId) {
        Report report = reportRepository.findByReportId(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Report", reportId, ""));

        return ReportDTO.Response.fromEntity(report);
    }

    /**
     * 유저 ID로 신고 목록 조회
     * @param userId
     * @return
     */
    public List<ReportDTO.Response> getReportsByUserId(Long userId) {
        List<Report> reports = reportRepository.findByUser_UserId(userId);

        return reports.stream()
                .map(ReportDTO.Response::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 처리되지 않은 신고 목록 조회
     * @return
     */
    public List<ReportDTO.Response> getUnprocessedReports() {
        List<Report> reports = reportRepository.findByIsProcessedFalse();

        return reports.stream()
                .map(ReportDTO.Response::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 신고 수정
     * @param reportId
     * @param request
     * @return
     */
    public ReportDTO.Response updateReport(Long reportId, ReportDTO.Request request) {
        Report report = reportRepository.findByReportId(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Report", reportId, ""));

        ReportReason reportReason = reportReasonRepository.findByReportReasonId(request.getReportReasonId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "ReportReason", request.getReportReasonId(), ""));

        report.setReportReason(reportReason);
        report.setTargetId(request.getTargetId());
        report.setTargetType(request.getTargetType());
        report.setReportDetail(request.getReportDetail());

        Report updatedReport = reportRepository.save(report);
        return ReportDTO.Response.fromEntity(updatedReport);
    }

    /**
     * 신고 처리 완료
     * @param reportId
     */
    public void markReportAsProcessed(Long reportId) {
        Report report = reportRepository.findByReportId(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Report", reportId, ""));

        report.setIsProcessed(true);
        reportRepository.save(report);
    }
}