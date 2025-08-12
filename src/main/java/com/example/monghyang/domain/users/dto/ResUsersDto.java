package com.example.monghyang.domain.users.dto;

import com.example.monghyang.domain.users.entity.Users;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ResUsersDto {
    private Long id;
    private String role;
    private String email;
    private String nickname;
    private String name;
    private String phone;
    private LocalDate birth;
    private String gender;
    private String address;
    private String address_detail;

    private ResUsersDto(Users users) {
        this.id = users.getId();
        this.role = users.getRole().getName().getRoleName();
        this.email = users.getEmail();
        this.nickname = users.getNickname();
        this.name = users.getName();
        this.phone = users.getPhone();
        this.birth = users.getBirth();
        this.gender = (users.getGender() == Boolean.FALSE) ? "man" : "woman";
        this.address = users.getAddress();
        this.address_detail = users.getAddressDetail();
    }

    public static ResUsersDto usersFetchJoinedRoleFrom(Users users) {
        return new ResUsersDto(users);
    }
}
