package com.example.monghyang.domain.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL) // null 필드는 json 직렬화하지 않게 설정
@Schema(description = "공통 성공 응답 형식")
public class ResponseDataDto<T> {
    // 모든 데이터 조회 응답에 사용되는 dto

    @Schema(description = "성공 응답 상태 코드입니다. 정상 응답에서는 200으로 고정됩니다.", example = "200")
    private Integer status; // 정상적인 응답만 취급하므로, http status 200으로 고정
    @Schema(description = "성공 메시지입니다. 조회 응답처럼 content가 있는 경우 생략될 수 있습니다.", example = "요청이 성공했습니다.", nullable = true)
    private String message; // 성공 응답 메시지
    @Schema(description = "조회 또는 처리 결과 데이터입니다. 메시지형 성공 응답에서는 생략될 수 있습니다.", nullable = true)
    private T content; // 제네릭(T)은 객체 혹은 컬렉션 타입이 될 수 있습니다.

    private ResponseDataDto(T content) {
        this.status = 200;
        this.content = content;
    }

    private ResponseDataDto(String message) {
        this.status = 200;
        this.message = message;
    }

    public static <T> ResponseDataDto<T> success(String message) { // 요청 처리 성공 응답
        return new ResponseDataDto<>(message);
    }

    public static <T> ResponseDataDto<T> contentFrom(T content) { // 조회 데이터 포함한 성공 응답
        return new ResponseDataDto<>(content);
    }
}
