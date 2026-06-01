package com.example.monghyang.domain.users.dto;

import com.example.monghyang.domain.users.entity.Users;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL) // null 필드는 json 직렬화하지 않게 설정
@Schema(description = "내 정보 조회 응답 정보")
public class ResUsersPrivateInfoDto {
    @Schema(description = "회원 식별자입니다.", example = "10")
    private final Long users_id;
    @Schema(description = "회원 역할명입니다.", example = "ROLE_BREWERY", allowableValues = {"ROLE_ADMIN", "ROLE_BREWERY", "ROLE_SELLER", "ROLE_USER"})
    private final String role_name;
    @Schema(description = "회원 이메일입니다.", example = "brewery@example.com")
    private final String users_email;
    @Schema(description = "회원 닉네임 또는 상호명입니다.", example = "몽향양조장")
    private final String users_nickname;
    @Schema(description = "회원 실명 또는 대표자명입니다.", example = "홍길동")
    private final String users_name;
    @Schema(description = "회원 연락처입니다.", example = "010-1234-5678")
    private final String users_phone;
    @Schema(description = "회원 생년월일입니다.", example = "1995-05-20", type = "string", format = "date")
    private final LocalDate users_birth;
    @Schema(description = "회원 성별입니다.", example = "man", allowableValues = {"man", "woman"})
    private final String users_gender;
    @Schema(description = "회원 기본 주소입니다.", example = "서울시 중구 세종대로 110")
    private final String users_address;
    @Schema(description = "회원 상세 주소입니다.", example = "101호")
    private final String users_address_detail;

    // 사용자의 권한 정보에 따라 아래 두 개의 필드가 채워질 수 있습니다.
    @Setter
    @Schema(description = "양조장 회원일 때 포함되는 양조장 상세 정보입니다. 다른 역할에서는 응답에서 제외됩니다.", nullable = true)
    private ResBreweryPrivateInfoDto brewery;
    @Setter
    @Schema(description = "판매자 회원일 때 포함되는 판매자 상세 정보입니다. 다른 역할에서는 응답에서 제외됩니다.", nullable = true)
    private ResSellerPrivateInfoDto seller;

    private ResUsersPrivateInfoDto(Users users) {
        this.users_id = users.getId();
        this.role_name = users.getRole().getName().getRoleName();
        this.users_email = users.getEmail();
        this.users_nickname = users.getNickname();
        this.users_name = users.getName();
        this.users_phone = users.getPhone();
        this.users_birth = users.getBirth();
        this.users_gender = (users.getGender() == Boolean.FALSE) ? "man" : "woman";
        this.users_address = users.getAddress();
        this.users_address_detail = users.getAddressDetail();
    }

    public static ResUsersPrivateInfoDto usersJoinedWithRoleToDto(Users users) {
        return new ResUsersPrivateInfoDto(users);
    }
}
