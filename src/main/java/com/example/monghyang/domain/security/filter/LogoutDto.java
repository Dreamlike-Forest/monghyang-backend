package com.example.monghyang.domain.security.filter;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LogoutDto {
    private Integer status;
    private String content;

    private LogoutDto(Integer status, String content) {
        this.status = status;
        this.content = content;
    }
    public static LogoutDto successContentFrom() {
        return new LogoutDto(200, "로그아웃하였습니다.");
    }
}
