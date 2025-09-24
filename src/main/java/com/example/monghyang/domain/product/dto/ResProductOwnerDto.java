package com.example.monghyang.domain.product.dto;

import com.example.monghyang.domain.users.entity.RoleType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL) // null 필드는 json 직렬화하지 않게 설정
public class ResProductOwnerDto {
    private Long owner_id; // 필수
    private RoleType owner_role; // 필수
    private String owner_region;
    private String image_key;
    private List<String> tags_name;

    public ResProductOwnerDto(Long owner_id) {
        // 판매자 정보 조회 시 사용하는 생성자
        this.owner_id = owner_id;
        this.owner_role = RoleType.ROLE_SELLER;
    }

    public ResProductOwnerDto(Long owner_id, String owner_region, String image_key) {
        // 양조장 정보 조회 시 사용하는 생성자
        this.owner_id = owner_id;
        this.owner_role = RoleType.ROLE_BREWERY;
        this.owner_region = owner_region;
        this.image_key = image_key;
    }

    public void setTags_name(List<String> tags_name) {
        this.tags_name = tags_name;
    }
}
