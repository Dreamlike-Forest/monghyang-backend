package com.example.monghyang.domain.joy.dto;

import com.example.monghyang.domain.joy.entity.Joy;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ResJoyDto {
    private final Long joy_id;
    private final String joy_name;
    private final String joy_place;
    private final String joy_detail;
    private final BigDecimal joy_origin_price;
    private final BigDecimal joy_discount_rate;
    private final BigDecimal joy_final_price;
    private final Integer joy_sales_volume;
    private final String joy_image_key;
    private final Boolean joy_is_soldout;
    private final Integer joy_time_unit;
    private final Integer joy_max_count;
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
