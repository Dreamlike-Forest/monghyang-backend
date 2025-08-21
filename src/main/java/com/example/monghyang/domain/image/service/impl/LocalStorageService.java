package com.example.monghyang.domain.image.service.impl;

import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.image.service.ImageType;
import com.example.monghyang.domain.image.service.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Profile("local") // 로컬 개발 환경에서 사용되는 스토리지 서비스
public class LocalStorageService implements StorageService {
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    public static final int BREWERY_IMAGE_MAX_WIDTH = 1920;
    public static final int BREWERY_IMAGE_MAX_HEIGHT = 1080;
    public static final float BREWERY_IMAGE_QUALITY = 0.90f;
    private final Path storagePath;

    public LocalStorageService(@Value("${app.storage-path}") String storagePath) {
        this.storagePath = Paths.get(storagePath).toAbsolutePath().normalize();

        try {
            // Bean 생성 시 스토리지로 활용할 디렉토리 생성
            Files.createDirectories(this.storagePath);
        } catch (Exception e) {
            throw new RuntimeException("LocalStorageService 생성 에러: 스토리지로 사용될 디렉토리를 생성하는 데 실패하였습니다.", e);
        }
    }

    @Override
    public UUID upload(MultipartFile file, ImageType imageType) {
        if(file.isEmpty()) {
            throw new ApplicationException(ApplicationError.IMAGE_REQUEST_NULL);
        }
        if(file.getOriginalFilename() == null || !file.getOriginalFilename().contains(".")) {
            // 이미지 이름이나 확장자 형식이 잘못되었으면 예외 발생
            throw new ApplicationException(ApplicationError.IMAGE_FORMAT_ERROR);
        }
        if(file.getSize() > MAX_FILE_SIZE) {
            // 이미지 용량이 10MB를 초과하거나 잘못된 용량일 때 예외 발생
            throw new ApplicationException(ApplicationError.IMAGE_SIZE_OVER);
        } else if(file.getSize() <= 0) {
            throw new ApplicationException(ApplicationError.IMAGE_SIZE_ERROR);
        }

        String ext = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")).toLowerCase();
        UUID key = UUID.randomUUID();

        try {
            Path uploadPath = storagePath.resolve(key + ext);
            Files.copy(file.getInputStream(), uploadPath); // 최종 경로 'uploadPath'에 이미지 복사(업로드)
            return key;
        } catch (IOException e) {
            throw new ApplicationException(ApplicationError.IMAGE_UPLOAD_ERROR);
        }
    }

    @Override
    public Resource load(String imageFullName) {
        try {
            Path filePath = storagePath.resolve(imageFullName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new ApplicationException(ApplicationError.IMAGE_NOT_FOUND);
            }
        } catch (MalformedURLException e) {
            throw new ApplicationException(ApplicationError.IMAGE_LOAD_ERROR);
        }
    }
}
