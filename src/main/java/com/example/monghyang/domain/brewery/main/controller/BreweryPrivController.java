package com.example.monghyang.domain.brewery.main.controller;

import com.example.monghyang.domain.image.dto.ImageUpdateDto;
import com.example.monghyang.domain.brewery.main.service.BreweryService;
import com.example.monghyang.domain.global.response.ResponseDataDto;
import com.example.monghyang.domain.image.dto.ImageUpdateDtoList;
import com.example.monghyang.domain.image.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/brewery-priv") // 양조장용 api
public class BreweryPrivController {
    private final StorageService storageService;
    private final BreweryService breweryService;
    @Autowired
    public BreweryPrivController(StorageService storageService, BreweryService breweryService) {
        this.storageService = storageService;
        this.breweryService = breweryService;
    }

    // 양조장 관련 수정 권한 검증: (@LoginUserId로 회원식별자 추출 -> 해당되는 양조장 조회 -> 양조장 식별자 사용)

    // 양조장 수정 로직(이미지 추가/삭제 또한 한번에 가능하도록)
    // 동일한 이미지 순서값을 여러 개 받을 경우(ex: 순서가 1인 이미지를 2개 이상 받는 경우) DB 레벨 예외 발생 -> 처리 로직 필요
    // 로직을 실제로 수행하기 전 애플리케이션 레벨에서 '삭제 리스트 삭제' -> '추가 리스트 추가'를 실행한 시나리오를 실행하여 이미지 개수가 5개 이하인지 검증
//    @PostMapping("/update-image-list")
//    public ResponseEntity<ResponseDataDto<Void>> updateImageList(@ModelAttribute ImageUpdateDtoList ) {
//        // 'brewery' 엔티티의 내용만 수정할 수 있다.
//        // 'users' 엔티티와 겹치는 컬럼(주소, 주소상세, 상호명
//    }

    // 양조장 태그 수정(추가 + 삭제)

    // 양조장 탈퇴(삭제 처리)
}
