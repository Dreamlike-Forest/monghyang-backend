package com.example.monghyang.domain.oauth2.service;

import com.example.monghyang.domain.oauth2.details.CustomOAuth2UserDetails;
import com.example.monghyang.domain.oauth2.details.GoogleUserDetails;
import com.example.monghyang.domain.oauth2.details.OAuth2UserInfo;
import com.example.monghyang.domain.users.entity.Role;
import com.example.monghyang.domain.users.entity.RoleType;
import com.example.monghyang.domain.users.entity.Users;
import com.example.monghyang.domain.users.repository.RoleRepository;
import com.example.monghyang.domain.users.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UsersRepository usersRepository;
    private final RoleRepository roleRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oAuth2UserInfo = null;

        if(provider.equals("google")) {
            oAuth2UserInfo = new GoogleUserDetails(oAuth2User.getAttributes());
        }
        if(oAuth2UserInfo == null) {
            throw new OAuth2AuthenticationException("Unsupported provider: " + provider);
        }

        // OAuth2 제공 기업명 + 식별자를 언더바 기준으로 합하여 고유한 'oAuth2Id' 생성
        String oAuth2Id = provider + "_" + oAuth2UserInfo.getProviderId();

        Optional<Users> findUser = usersRepository.findByoAuth2Id(oAuth2Id);
        Users users;
        if(findUser.isPresent()) {
            users = findUser.get();
        } else {
            Role role = roleRepository.findByName(RoleType.ROLE_USER).orElseThrow(() ->
                    new OAuth2AuthenticationException("Unsupported role: " + RoleType.ROLE_USER));
            // 소셜 로그인은 '일반 사용자'만 가능
            users = Users.oAuth2Builder()
                    .email(oAuth2UserInfo.getEmail())
                    .name(oAuth2UserInfo.getName())
                    .oAuth2Id(oAuth2Id)
                    .role(role)
                    .build();
            usersRepository.save(users);
        }
        return new CustomOAuth2UserDetails(users, oAuth2User.getAttributes());
    }
}
