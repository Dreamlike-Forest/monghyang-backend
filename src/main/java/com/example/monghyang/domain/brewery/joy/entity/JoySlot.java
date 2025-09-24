package com.example.monghyang.domain.brewery.joy.entity;

import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "joy_reservation_uk",
                columnNames = {"joy_id", "reservation"}
        )
})
public class JoySlot {
    @Id @GeneratedValue
    private Long id;
    @JoinColumn(name = "JOY_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Joy joy;
    @Column(nullable = false)
    private LocalDateTime reservation;

    private JoySlot(Joy joy, LocalDateTime reservation) {
        this.joy = joy;
        this.reservation = reservation.withSecond(0).withNano(0);
    }

    public static JoySlot joyReservationOf(Joy joy, LocalDateTime reservation) {
        return new JoySlot(joy, reservation);
    }

    public void updateReservation(LocalDateTime reservation) {
        this.reservation = reservation.withSecond(0).withNano(0);
    }
}
