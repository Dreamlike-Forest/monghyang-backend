package com.example.monghyang.domain.tag.dto;

// DB 조회 결과를 받아오기 위한 record
// ownerId: 양조장, 판매자 등 해당 태그를 보유한 엔티티의 식별자
public record TagNameDto(Long ownerId, String tagName) {}
