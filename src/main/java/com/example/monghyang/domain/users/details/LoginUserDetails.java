package com.example.monghyang.domain.users.details;

import com.example.monghyang.domain.users.dto.AuthDto;

public class LoginUserDetails extends JwtUserDetails {
    private final String nickname;
    private final String password;
    public LoginUserDetails(AuthDto authDto, String nickname, String password) {
        super(authDto);
        this.nickname = nickname;
        this.password = password;
    }

    public String getNickname() {
        return this.nickname;
    }

    public String getPassword() {
        return this.password;
    }
}
