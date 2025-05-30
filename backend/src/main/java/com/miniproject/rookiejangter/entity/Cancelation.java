package com.miniproject.rookiejangter.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cancelations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cancelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cancelation_id")
    private Long cancelationId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancelation_reason_id")
    private CancelationReason cancelationReason;

    @Column(name = "cancelation_detail", length = 255)
    private String cancelationDetail;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Override
    public String toString() {
        return "Cancelation{" +
                "cancelationId=" + cancelationId +
                ", reservationId=" + (reservation != null ? reservation.getReservationId() : null) +
                ", cancelationReasonId=" + (cancelationReason != null ? cancelationReason.getCancelationReasonId() : null) +
                ", cancelationDetail='" + cancelationDetail + '\'' +
                ", canceledAt=" + canceledAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cancelation cancelation = (Cancelation) o;
        return reservation != null && reservation.getReservationId().equals(cancelation.getReservation().getReservationId());
    }

    @Override
    public int hashCode() {
        return reservation != null ? reservation.getReservationId().hashCode() : 0;
    }
}