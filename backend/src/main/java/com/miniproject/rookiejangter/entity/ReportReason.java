package com.miniproject.rookiejangter.entity;

import com.miniproject.rookiejangter.exception.BusinessException;
import com.miniproject.rookiejangter.exception.ErrorCode;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "report_reasons")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString
public class ReportReason extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_reason_id")
    private Integer reportReasonId;

    @Column(name = "report_reason_type", length = 50)
    private String reportReasonType;

    @OneToMany(mappedBy = "reportReason", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Report> reports = new ArrayList<>();

    /**
     * 신고 사유 내용을 변경합니다.
     *
     * @param newReasonType 신고 사유 타입
     */
    public void changeReasonType(String newReasonType) {
        if (newReasonType == null || newReasonType.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.REPORT_REASON_EMPTY);
        }
        if (newReasonType.length() > 50) {
            throw new BusinessException(ErrorCode.REPORT_REASON_TOO_LONG);
        }
        this.reportReasonType = newReasonType;
    }
}