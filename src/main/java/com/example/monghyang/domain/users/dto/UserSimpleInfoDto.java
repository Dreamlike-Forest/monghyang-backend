package com.example.monghyang.domain.users.dto;

import com.example.monghyang.domain.users.entity.RoleType;

public record UserSimpleInfoDto(String nickname, RoleType roleType) { }
