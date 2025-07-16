package com.example.monghyang.domain.global.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ResponseDto {
    // 모든 단순 응답에 사용되는 dto(추후 필드 확장 가능성 O)
    private final int status = 200;
}
