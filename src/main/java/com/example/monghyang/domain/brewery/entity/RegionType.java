package com.example.monghyang.domain.brewery.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegionType {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false, unique = true)
    private String name;

    public static RegionType nameFrom(String name) {
        RegionType regionType = new RegionType();
        regionType.name = name;
        return regionType;
    }
}
