package com.example.monghyang.domain.users.details;

import com.example.monghyang.domain.users.dto.AuthDto;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

public class LoginUserDetails implements UserDetails {
    private final AuthDto authDto;
    private final String nickname;
    private final String password;
    private LoginUserDetails(AuthDto authDto, String nickname, String password) {
        this.authDto = authDto;
        this.nickname = nickname;
        this.password = password;
    }
    public static LoginUserDetails authDtoNicknamePasswordOf(AuthDto authDto, String nickname, String password) {
        return new LoginUserDetails(authDto, nickname, password);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return authDto.getRoleType();
            }
        });
        return authorities;
    }

    public Long getUserId() {
        return authDto.getUserId();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return this.nickname;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
