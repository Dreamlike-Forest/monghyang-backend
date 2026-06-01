package com.example.monghyang.domain.auth.dto;

import com.example.monghyang.domain.global.annotation.validation.AllowNullNotBlankString;
import com.example.monghyang.domain.image.dto.AddImageDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "양조장 회원가입 요청 정보")
public class BreweryJoinDto extends JoinDto {
    @Schema(description = "양조장 사업자등록번호입니다.", example = "123-45-67890", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "business_registration_number 값이 공백일 수 없습니다.")
    private String business_registration_number;
    @Schema(description = "양조장 정산 계좌 예금주입니다.", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "brewery_depositor 값이 공백일 수 없습니다.")
    private String brewery_depositor;
    @Schema(description = "양조장 정산 계좌번호입니다.", example = "110123456789", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "brewery_account_number 값이 공백일 수 없습니다.")
    private String brewery_account_number;
    @Schema(description = "양조장 정산 은행명입니다.", example = "신한은행", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "brewery_bank_name 값이 공백일 수 없습니다.")
    private String brewery_bank_name;
    @Schema(description = "양조장 소개글입니다. null은 허용하지만 빈 문자열은 허용하지 않습니다.", example = "전통주 체험을 운영하는 양조장입니다.", nullable = true)
    @AllowNullNotBlankString
    private String introduction;
    @Schema(description = "양조장 웹사이트 주소입니다. null은 허용하지만 빈 문자열은 허용하지 않습니다.", example = "https://example.com", nullable = true)
    @AllowNullNotBlankString
    private String brewery_website;
    @Schema(description = "요일별 양조장 운영/휴게시간 목록입니다. 최소 1개 이상 필요하고, 같은 요일은 중복될 수 없습니다.", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotNull
    @Size(min = 1, message = "양조장 운영 시간대 정보는 최소 1개 이상 입력해야합니다.")
    private List<BreweryScheduleDto> schedules; // 요일 별 운영/휴게시간 정보. 최소 1개 이상 있어야 한다.

    @Schema(description = "양조장 이미지 목록입니다. 최대 5개까지 전달할 수 있고, seq=1인 이미지가 대표 이미지입니다.")
    @Size(max = 5, message = "이미지는 최대 5개까지 추가할 수 있습니다.")
    private List<AddImageDto> images; // 새로 추가할 이미지 파일 + 순서 정보 쌍의 리스트

    @Schema(description = "양조장 지역 식별자입니다. /api/brewery/regions 응답의 region_type_id 값을 사용합니다.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "region_type_id 값이 공백일 수 없습니다.")
    private Integer region_type_id;

    @Schema(description = "정기 방문 가능 여부입니다.", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "is_regular_visit 값이 공백일 수 없습니다.")
    private Boolean is_regular_visit;
    @Schema(description = "양조장 회원 약관 동의 여부입니다. 회원가입하려면 true여야 합니다.", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "is_agreed_brewery 값이 공백일 수 없습니다.")
    private Boolean is_agreed_brewery;
}
