package com.example.monghyang.domain.joy.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_joy_id_date_time",
                columnNames = {"joy_id", "reservation_date", "reservation_time"}
        )
})
public class JoySlot {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JoinColumn(name = "JOY_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Joy joy;
    @Column(nullable = false)
    private LocalDate reservationDate;
    @Column(nullable = false)
    private LocalTime reservationTime;
    @Min(0)
    @Column(nullable = false)
    private Integer count;

    private JoySlot(Joy joy, LocalDate reservationDate, LocalTime reservationTime, Integer count) {
        this.joy = joy;
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime.withSecond(0);
        this.count = count;
    }

    public static JoySlot joyReservationOf(Joy joy, LocalDate reservationDate, LocalTime reservationTime, Integer count) {
        return new JoySlot(joy, reservationDate, reservationTime, count);
    }

    public void updateReservation(LocalDate reservationDate, LocalTime reservationTime) {
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime.withSecond(0);
    }
}
