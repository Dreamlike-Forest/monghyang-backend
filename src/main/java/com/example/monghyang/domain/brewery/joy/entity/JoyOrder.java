package com.example.monghyang.domain.brewery.joy.entity;

import com.example.monghyang.domain.users.entity.Users;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JoyOrder {
    @Id @GeneratedValue
    private Long id;
    @JoinColumn(name = "USER_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Users users;
    @JoinColumn(name = "JOY_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Joy joy;
    private Integer count;

}
