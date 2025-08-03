package com.example.monghyang.domain.util.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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