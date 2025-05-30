package com.miniproject.rookiejangter.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_reason_id")
    private ReportReason reportReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "target_id")
    private Long targetId;

    @Size(max = 10, message = "신고 대상 타입은 최대 20자까지 가능합니다.")
    @Column(name = "target_type", length = 20)
    private String targetType;

    @Size(max = 255, message = "신고 상세는 최대 255자까지 가능합니다.")
    @Column(name = "report_detail", length = 255)
    private String reportDetail;

    @Column(name = "is_processed")
    private Boolean isProcessed;

    @Override
    public String toString() {
        return "Report{" +
                "reportId=" + reportId +
                ", targetId=" + targetId +
                ", targetType='" + targetType + '\'' +
                ", isProcessed=" + isProcessed +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Report report = (Report) o;
        return reportId != null && reportId.equals(report.reportId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}