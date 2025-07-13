package com.example.monghyang.domain.oauth2.details;

public interface OAuth2UserInfo {
    String getProvider();
    String getProviderId();
    String getName();
    String getEmail();
}
