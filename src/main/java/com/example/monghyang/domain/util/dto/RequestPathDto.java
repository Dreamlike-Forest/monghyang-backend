package com.example.monghyang.domain.util.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RequestPathDto {
    private String httpMethod;
    private String path;

    private RequestPathDto(String httpMethod, String path) {
        this.httpMethod = httpMethod;
        this.path = path;
    }

    public static RequestPathDto httpMethodPathOf(String httpMethod, String path) {
        return new RequestPathDto(httpMethod, path);
    }
}
