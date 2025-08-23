package com.example.monghyang.domain.brewery.main.controller;

import com.example.monghyang.domain.auth.dto.QuitRequestDto;
import com.example.monghyang.domain.global.annotation.LoginUserId;
import com.example.monghyang.domain.image.dto.ImageUpdateDto;
import com.example.monghyang.domain.brewery.main.service.BreweryService;
import com.example.monghyang.domain.global.response.ResponseDataDto;
import com.example.monghyang.domain.image.dto.ImageUpdateDtoList;
import com.example.monghyang.domain.image.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/brewery-priv") // 양조장용 api
@Tag(name = "양조장 관리자용 API")
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
//    @PostMapping("/update-image-list")
//    public ResponseEntity<ResponseDataDto<Void>> updateImageList(@ModelAttribute ImageUpdateDtoList ) {
//
//    }

    // 양조장 태그 수정(추가 + 삭제)

    // 양조장 삭제 처리
    @DeleteMapping
    @Operation(summary = "양조장 삭제 처리")
    public ResponseEntity<ResponseDataDto<Void>> breweryQuit(@Valid @ModelAttribute QuitRequestDto quitRequestDto, @LoginUserId Long userId) {
        breweryService.breweryQuit(userId, quitRequestDto);
        return ResponseEntity.ok().body(ResponseDataDto.success("양조장 정보가 삭제되었습니다."));
    }
}
