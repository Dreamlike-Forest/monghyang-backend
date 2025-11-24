package com.example.monghyang.domain.users.dto;

import com.example.monghyang.domain.users.entity.Users;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL) // null 필드는 json 직렬화하지 않게 설정
public class ResUsersPrivateInfoDto {
    private final Long users_id;
    private final String role_name;
    private final String users_email;
    private final String users_nickname;
    private final String users_name;
    private final String users_phone;
    private final LocalDate users_birth;
    private final String users_gender;
    private final String users_address;
    private final String users_address_detail;

    // 사용자의 권한 정보에 따라 아래 두 개의 필드가 채워질 수 있습니다.
    @Setter
    private ResBreweryPrivateInfoDto brewery;
    @Setter
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
