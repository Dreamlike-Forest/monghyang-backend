package com.example.monghyang.domain.brewery.main.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_brewery_image_seq",
                columnNames = {"BREWERY_ID", "SEQ"}
        )
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BreweryImage {
    @Id @GeneratedValue
    private Long id;

    @JoinColumn(name = "BREWERY_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Brewery brewery;
    @Column(nullable = false, unique = true)
    private UUID imageKey;
    @Column(nullable = false)
    private Integer seq;
    @Column(nullable = false)
    private Integer volume;

    private BreweryImage(Brewery brewery, UUID imageKey, Integer seq, Integer volume) {
        this.brewery = brewery;
        this.imageKey = imageKey;
        this.seq = seq;
        this.volume = volume;
    }

    public static BreweryImage breweryKeySeqVolume(Brewery brewery, UUID imageKey, Integer seq, Integer volume) {
        return new BreweryImage(brewery, imageKey, seq, volume);
    }

    public void updateSeq(Integer seq) {
        this.seq = seq;
    }
}
