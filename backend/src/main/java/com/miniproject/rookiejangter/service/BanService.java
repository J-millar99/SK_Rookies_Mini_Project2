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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class BanService {

    private final BanRepository banRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;

    /**
     * 유저 제재
     * @param userId
     * @param reportId
     * @param banReason
     * @return
     */
    public BanDTO.Response banUser(Long userId, Long reportId, String banReason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, userId));

        Report report = reportRepository.findByReportId(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Report", reportId));

        Ban ban = Ban.builder()
                .user(user)
                .report(report)
                .banReason(banReason)
                .build();

        Ban savedBan = banRepository.save(ban);

        user.setIsBanned(true); // User 엔티티의 isBanned 필드를 true로 설정

        return BanDTO.Response.fromEntity(savedBan);
    }

    /**
     * 유저 제재 해제
     * @param userId
     */
    public void unbanUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, userId));

        user.setIsBanned(false); // User 엔티티의 isBanned 필드를 false로 설정
    }

    /**
     * 제재 ID로 조회
     * @param banId
     * @return
     */
    public BanDTO.Response getBanById(Long banId) {
        Ban ban = banRepository.findByBanId(banId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Ban", banId));

        return BanDTO.Response.fromEntity(ban);
    }

    /**
     * 유저 ID로 제재 목록 조회
     * @param userId
     * @return
     */
    public List<BanDTO.Response> getBansByUserId(Long userId) {
        List<Ban> bans = banRepository.findByUser_UserId(userId);

        return bans.stream()
                .map(BanDTO.Response::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 신고 ID로 제재 조회
     * @param reportId
     * @return
     */
    public BanDTO.Response getBanByReportId(Long reportId) {
        Ban ban = banRepository.findByReport_ReportId(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Report", reportId));

        return BanDTO.Response.fromEntity(ban);
    }

    /**
     * 제재 사유로 제재 목록 조회
     * @param banReason
     * @return
     */
    public List<BanDTO.Response> getBansByBanReason(String banReason) {
        List<Ban> bans = banRepository.findByBanReason(banReason);

        return bans.stream()
                .map(BanDTO.Response::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 제재 삭제
     * @param banId
     */
    public void deleteBan(Long banId) {
        Ban ban = banRepository.findByBanId(banId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Ban", banId));

        banRepository.delete(ban);
    }
}