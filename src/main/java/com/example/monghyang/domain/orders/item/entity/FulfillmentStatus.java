package com.example.monghyang.domain.orders.item.entity;

public enum FulfillmentStatus {
    CREATED, ALLOCATED, PACKED, IN_TRANSIT, DELIVERED, CANCELED, FAILED
    // 생성됨, 배송 물품 할당됨, 배송 준비, 배송 중, 배송 완료, 취소됨, 실패함
}
