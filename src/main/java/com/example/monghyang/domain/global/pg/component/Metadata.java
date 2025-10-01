package com.example.monghyang.domain.global.pg.component;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class Metadata {
    // 최대 5개의 키-값 쌍(키: 40자 이내, 값: 2000자 이내)
    // 예시로 Map 사용
    private Map<String, String> data;   // 결제 관련 부가 정보
}
