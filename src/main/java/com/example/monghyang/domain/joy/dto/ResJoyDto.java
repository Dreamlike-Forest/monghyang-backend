package com.example.monghyang.domain.joy.dto;

import com.example.monghyang.domain.joy.entity.Joy;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Schema(description = "체험 응답 정보")
public class ResJoyDto {
    @Schema(description = "체험 식별자입니다.", example = "1")
    private final Long joy_id;
    @Schema(description = "체험 이름입니다.", example = "전통주 빚기 체험")
    private final String joy_name;
    @Schema(description = "체험 장소입니다.", example = "몽향양조장 체험실")
    private final String joy_place;
    @Schema(description = "체험 상세 설명입니다.", example = "막걸리 빚기와 시음을 함께 진행합니다.")
    private final String joy_detail;
    @Schema(description = "1인당 정가입니다.", example = "30000")
    private final BigDecimal joy_origin_price;
    @Schema(description = "할인율입니다.", example = "0")
    private final BigDecimal joy_discount_rate;
    @Schema(description = "할인 적용 후 최종 가격입니다.", example = "30000")
    private final BigDecimal joy_final_price;
    @Schema(description = "체험 판매 수량입니다.", example = "25")
    private final Integer joy_sales_volume;
    @Schema(description = "체험 이미지 key입니다.", example = "joy/1/main.jpg", nullable = true)
    private final String joy_image_key;
    @Schema(description = "체험 품절 여부입니다.", example = "false")
    private final Boolean joy_is_soldout;
    @Schema(description = "체험 진행 시간입니다. 분 단위입니다.", example = "60")
    private final Integer joy_time_unit;
    @Schema(description = "동일 시간대 최대 예약 가능 인원입니다.", example = "12")
    private final Integer joy_max_count;
    @Schema(description = "체험 삭제 처리 여부입니다.", example = "false")
    private final Boolean joy_is_deleted;

    private ResJoyDto(Joy joy) {
        this.joy_id = joy.getId();
        this.joy_name = joy.getName();
        this.joy_place = joy.getPlace();
        this.joy_detail = joy.getDetail();
        this.joy_origin_price = joy.getOriginPrice();
        this.joy_discount_rate = joy.getDiscountRate();
        this.joy_final_price = joy.getFinalPrice();
        this.joy_sales_volume = joy.getSalesVolume();
        this.joy_is_soldout = joy.getIsSoldout();
        this.joy_image_key = joy.getImageKey();
        this.joy_time_unit = joy.getTimeUnit();
        this.joy_max_count = joy.getMaxCount();
        this.joy_is_deleted = (joy.getIsDeleted() == null) ? false : joy.getIsDeleted();
    }

    public static ResJoyDto joyFrom(Joy joy) {
        return new ResJoyDto(joy);
    }
}
