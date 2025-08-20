package com.example.monghyang.domain.image.service.impl;

import com.example.monghyang.domain.image.service.StorageService;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@Profile("prod") // 운영 환경에서 사용되는 스토리지 서비스
public class AwsStorageService implements StorageService {
    @Override
    public String upload(MultipartFile file) {
        return "";
    }

    @Override
    public Resource load(String imageFullName) {
        return null;
    }
}
