package com.example.monghyang.domain.image.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface StorageService {

    /**
     * 파일을 저장하고, 저장된 파일의 고유한 이름을 반환합니다.
     * @param file 업로드된 MultipartFile
     * @return 저장 후 생성된 이미지 UUID key
     */
    String upload(MultipartFile file);

    /**
     * 파일 이름으로 실제 파일 리소스를 반환합니다.
     * @param imageFullName 확장자명을 포함한 이미지 전체 이름
     * @return 실제 이미지 파일에 접근할 수 있는 Resource 객체
     */
    Resource load(String imageFullName);
}
