package com.example.monghyang.domain.joy.review.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqJoyReviewDto {
    private Long joy_id;
    private String content;
    private Double star;
    private Integer view;
    private Integer likes;
}
