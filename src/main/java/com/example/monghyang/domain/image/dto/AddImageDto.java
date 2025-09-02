package com.example.monghyang.domain.image.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class AddImageDto { // 이미지 새로 추가할 때 하나의 이미지 파일과 순서 정보를 묶어서 받기 위한 dto
    private MultipartFile image; // 새로 추가할 이미지 파일
    private Integer seq;
}
