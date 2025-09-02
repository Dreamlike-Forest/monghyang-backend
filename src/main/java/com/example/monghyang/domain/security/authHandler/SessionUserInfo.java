package com.example.monghyang.domain.security.authHandler;

import java.io.Serializable;

public record SessionUserInfo(Long userId, String role) implements Serializable {
}
