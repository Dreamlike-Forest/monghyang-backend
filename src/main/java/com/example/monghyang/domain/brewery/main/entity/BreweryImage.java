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
                columnNames = {"BREWERY_ID", "SEQ"} // 양조장 식별자와 '이미지 순서 정보'를 묶어서 유니크하게 설정
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
    private Long volume;

    private BreweryImage(Brewery brewery, UUID imageKey, Integer seq, Long volume) {
        this.brewery = brewery;
        this.imageKey = imageKey;
        this.seq = seq;
        this.volume = volume;
    }


    /**
     * BreweryImage 엔티티를 저장하고 저장된 엔티티를 반환합니다.
     * @param brewery 양조장 Entity
     * @param imageKey 이미지 업로드 후 반환된 UUID 값
     * @param seq 해당 이미지의 순서 정보
     * @param volume 해당 이미지의 용량 정보
     * @return 저장 후 생성된 이미지 UUID key
     */
    public static BreweryImage breweryKeySeqVolume(Brewery brewery, UUID imageKey, Integer seq, Long volume) {
        return new BreweryImage(brewery, imageKey, seq, volume);
    }

    public void updateSeq(Integer seq) {
        this.seq = seq;
    }
}
