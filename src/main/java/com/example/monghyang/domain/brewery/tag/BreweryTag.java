package com.example.monghyang.domain.brewery.tag;

import com.example.monghyang.domain.brewery.entity.Brewery;
import com.example.monghyang.domain.tag.entity.Tags;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_brewery_tag",
                columnNames = {"BREWERY_ID", "TAG_ID"} // 양조장 식별자와 태그 식별자를 묶어서 유니크하게 설정
        )
})
public class BreweryTag {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "BREWERY_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Brewery brewery;

    @JoinColumn(name = "TAG_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Tags tags;

    private BreweryTag(Brewery brewery, Tags tags) {
        this.brewery = brewery;
        this.tags = tags;
    }

    public static BreweryTag breweryTagsOf(@NonNull Brewery brewery, @NonNull Tags tags) {
        return new BreweryTag(brewery, tags);
    }
}
