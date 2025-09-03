package com.example.monghyang.domain.image.controller;

import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.image.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/image")
@Tag(name = "이미지 파일 요청 API")
@Slf4j
public class ImageController {
    private final StorageService storageService;
    @Autowired
    public ImageController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/{imageFullName}")
    @Operation(summary = "실제 이미지 파일 요청 API", description = "확장자명을 포함한 이미지 전체 이름을 파라메터에 넣어주세요.")
    public ResponseEntity<Resource> loadImage(@PathVariable String imageFullName, HttpServletRequest request) {
        try {
            Resource image = storageService.load(imageFullName);
            String contentType = request.getServletContext().getMimeType(image.getFile().getAbsolutePath());
            if(contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(image);
        } catch (IOException e) {
            log.error("이미지 로드 에러: 반환하려는 파일의 타입을 결정할 수 없습니다. {}", e.getMessage());
            throw new ApplicationException(ApplicationError.IMAGE_LOAD_ERROR);
        }
    }
}
