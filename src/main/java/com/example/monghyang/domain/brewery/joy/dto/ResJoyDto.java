package com.example.monghyang.domain.brewery.joy.dto;

import com.example.monghyang.domain.brewery.joy.entity.Joy;
import lombok.Getter;

@Getter
public class ResJoyDto {
    private final Long joy_id;
    private final String joy_name;
    private final String joy_place;
    private final String joy_detail;
    private final Integer joy_origin_price;
    private final Integer joy_discount_rate;
    private final Integer joy_final_price;
    private final Integer joy_sales_volume;
    private final Boolean joy_is_soldout;

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
    }

    public static ResJoyDto joyFrom(Joy joy) {
        return new ResJoyDto(joy);
    }
}
