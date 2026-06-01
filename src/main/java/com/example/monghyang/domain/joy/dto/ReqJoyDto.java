package com.example.monghyang.domain.joy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Schema(description = "체험 추가 요청 정보")
public class ReqJoyDto {
    @Schema(description = "체험 이름입니다.", example = "전통주 빚기 체험", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "체험의 이름 정보를 입력해주세요.")
    private String name;
    @Schema(description = "체험 장소입니다.", example = "몽향양조장 체험실", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "체험의 장소 정보를 입력해주세요.")
    private String place;
    @Schema(description = "체험 상세 설명입니다.", example = "막걸리 빚기와 시음을 함께 진행합니다.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "체험의 설명 등 상세 정보를 입력해주세요.")
    private String detail;
    @Schema(description = "체험 진행 시간입니다. 분 단위로 입력하며, 예약 가능 시작 시간 검증에 사용됩니다.", example = "60", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "체험의 시간 단위를 분 단위로 입력해주세요.")
    @Min(value = 0, message = "체험 시간 단위는 음수가 될 수 없습니다.")
    private Integer time_unit;
    @Schema(description = "1인당 정가입니다. 소수점 없는 1억 미만 정수만 허용됩니다.", example = "30000", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "체험의 1인 당 정가 정보를 입력해주세요.")
    @Digits(integer = 8, fraction = 0, message = "가격 정보를 1억 미만의 정수로 입력해주세요.")
    @Min(value = 0, message = "정가는 음수가 될 수 없습니다.")
    private BigDecimal origin_price;
    @Schema(description = "동일 시간대 최대 예약 가능 인원입니다.", example = "12", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "해당 체험의 동시간 최대 수용 가능 인원 수를 입력해주세요.")
    @Min(value = 1, message = "최대 수용 가능 인원 수는 1 이상이어야 합니다.")
    private Integer max_count;
    /** 체험 생성 시 최초로 저장할 요일별 시작 시간 스냅샷입니다. */
    @Schema(description = "체험 생성 시 최초로 저장할 요일별 시작 시간 스냅샷입니다. 목록에 없는 요일은 체험 미운영으로 해석합니다.", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotEmpty(message = "체험 일정 목록을 입력해주세요.")
    private List<JoyScheduleDto> schedules;
    @Schema(description = "체험 대표 이미지 파일입니다. 전달하지 않으면 이미지 없이 체험이 생성됩니다.", type = "string", format = "binary", nullable = true)
    private MultipartFile image;
}
