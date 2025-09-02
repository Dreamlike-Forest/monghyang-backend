package com.example.monghyang.domain.image.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface StorageService {
    static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    static final int BREWERY_IMAGE_MAX_WIDTH = 1920;
    static final int BREWERY_IMAGE_MAX_HEIGHT = 1080;
    static final float BREWERY_IMAGE_QUALITY = 0.90f;

    /**
     * 파일을 저장하고, 저장된 파일의 고유한 이름을 반환합니다.
     * @param file 업로드할 이미지 파일 MultipartFile
     * @param imageType 업로드 할 이미지의 타입 enum
     * @return 저장 후 생성된 이미지 UUID + 확장자명 형태의 이미지 전체 이름
     */
    String upload(MultipartFile file, ImageType imageType);

    /**
     * 파일 이름으로 실제 파일 리소스를 반환합니다.
     * @param imageFullName 확장자명을 포함한 이미지 전체 이름
     * @return 실제 이미지 파일에 접근할 수 있는 Resource 객체
     */
    Resource load(String imageFullName);

    /**
     * 확장자까지 포함한 이미지 전체 이름을 통해 이미지 파일을 스토리지에서 제거합니다.
     * @param imageFullName 확장자명을 포함한 이미지 전체 이름
     */
    void remove(String imageFullName);
}
