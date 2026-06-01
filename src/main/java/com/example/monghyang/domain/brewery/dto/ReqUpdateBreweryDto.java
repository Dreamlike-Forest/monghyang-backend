package com.example.monghyang.domain.brewery.dto;

import com.example.monghyang.domain.global.annotation.validation.AllowNullNotBlankString;
import com.example.monghyang.domain.image.dto.AddImageDto;
import com.example.monghyang.domain.image.dto.ModifySeqImageDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Schema(description = "양조장 정보 부분 수정 요청 정보")
public class ReqUpdateBreweryDto {
    @Schema(description = "변경할 양조장 상호명입니다. null이면 변경하지 않습니다.", example = "몽향양조장", nullable = true)
    @AllowNullNotBlankString
    private String brewery_name; // 회원 엔티티 중복 값
    @Schema(description = "변경할 양조장 주소입니다. null이면 변경하지 않습니다.", example = "서울시 중구 세종대로 110", nullable = true)
    @AllowNullNotBlankString
    private String brewery_address; // 회원 엔티티 중복 값
    @Schema(description = "변경할 양조장 상세 주소입니다. null이면 변경하지 않습니다.", example = "101호", nullable = true)
    @AllowNullNotBlankString
    private String brewery_address_detail; // 회원 엔티티 중복 값
    @Schema(description = "변경할 사업자등록번호입니다. null이면 변경하지 않습니다.", example = "123-45-67890", nullable = true)
    @AllowNullNotBlankString
    private String business_registration_number;
    @Schema(description = "변경할 정산 계좌 예금주입니다. null이면 변경하지 않습니다.", example = "홍길동", nullable = true)
    @AllowNullNotBlankString
    private String brewery_depositor;
    @Schema(description = "변경할 정산 계좌번호입니다. null이면 변경하지 않습니다.", example = "110123456789", nullable = true)
    @AllowNullNotBlankString
    private String brewery_account_number;
    @Schema(description = "변경할 정산 은행명입니다. null이면 변경하지 않습니다.", example = "신한은행", nullable = true)
    @AllowNullNotBlankString
    private String brewery_bank_name;
    @Schema(description = "변경할 양조장 소개글입니다. null이면 변경하지 않습니다.", example = "전통주 체험을 운영하는 양조장입니다.", nullable = true)
    @AllowNullNotBlankString
    private String introduction;
    @Schema(description = "변경할 양조장 웹사이트 주소입니다. null이면 변경하지 않습니다.", example = "https://example.com", nullable = true)
    @AllowNullNotBlankString
    private String brewery_website;
    @Schema(description = "정기 방문 가능 여부입니다. null이면 변경하지 않습니다.", example = "true", nullable = true)
    private Boolean is_regular_visit;

    @Schema(description = "새로 추가할 이미지 파일과 순서 목록입니다. 추가 후 전체 이미지 개수는 5개 이하여야 합니다.")
    private List<AddImageDto> add_images = new ArrayList<>(); // 새로 추가할 이미지 리스트
    @Schema(description = "기존 이미지의 순서 변경 목록입니다. image_id는 자신의 양조장 이미지여야 하고 seq는 1~5 범위여야 합니다.")
    private List<ModifySeqImageDto> modify_images = new ArrayList<>(); // 위치 변경할 이미지 리스트
    @Schema(description = "삭제할 기존 이미지 식별자 목록입니다. 자신의 양조장 이미지 식별자만 삭제할 수 있습니다.", example = "[1, 2]")
    private List<Long> remove_images = new ArrayList<>(); // 삭제할 이미지의 식별자 값 리스트
}
