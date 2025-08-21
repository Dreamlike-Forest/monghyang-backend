package com.example.monghyang.domain.image.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ImageUpdateDtoList {
    private List<ImageUpdateDto> addImageList; // 새로 추가할 이미지 파일 + 순서 정보 쌍의 리스트
    private List<Long> removeImageList; // 제거할 이미지의 식별자 리스트
}
