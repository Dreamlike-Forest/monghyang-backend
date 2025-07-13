package com.example.monghyang.domain.oauth2.details;

import com.example.monghyang.domain.users.entity.Users;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class CustomOAuth2UserDetails implements OAuth2User {
    private final Users users;
    private Map<String, Object> attributes;
    public CustomOAuth2UserDetails(Users users, Map<String, Object> attributes) {
        this.users = users;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return users.getRole().getName().toString();
            }
        });

        return collection;
    }

    @Override
    public String getName() {
        return users.getName();
    }

    public String getOAuth2Id() {
        return users.getOAuth2Id();
    }

    public Users getUser() {
        return users;
    }
}
