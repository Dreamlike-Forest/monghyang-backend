package com.example.monghyang.domain.brewery.main.controller;

import com.example.monghyang.domain.auth.dto.VerifyAuthDto;
import com.example.monghyang.domain.brewery.main.dto.ReqBreweryDto;
import com.example.monghyang.domain.global.annotation.LoginUserId;
import com.example.monghyang.domain.brewery.main.service.BreweryService;
import com.example.monghyang.domain.global.response.ResponseDataDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/brewery-priv") // 양조장용 api
@Tag(name = "양조장 관리자용 API", description = "양조장 권한을 가진 회원만 접근할 수 있습니다.")
public class BreweryPrivController {
    private final BreweryService breweryService;
    @Autowired
    public BreweryPrivController(BreweryService breweryService) {
        this.breweryService = breweryService;
    }

    // 양조장 관련 수정 권한 검증: (@LoginUserId로 회원식별자 추출 -> 해당되는 양조장 조회 -> 양조장 식별자 사용)

    // 양조장 수정 로직(이미지 추가/삭제 또한 한번에 가능하도록)
    @PostMapping("/update")
    @Operation(summary = "양조장 정보 수정")
    public ResponseEntity<ResponseDataDto<Void>> updateImageList(@LoginUserId Long userId, @ModelAttribute ReqBreweryDto reqBreweryDto) {
        breweryService.breweryUpdate(userId, reqBreweryDto);
        return ResponseEntity.ok().body(ResponseDataDto.success("양조장 정보를 업데이트했습니다."));
    }

    // 양조장 태그 수정(추가 + 삭제)

    // 양조장 삭제 처리
    @DeleteMapping
    @Operation(summary = "양조장 삭제 처리", description = "해당 회원의 기존 비밀번호를 입력받고, 일치하는지 검사합니다.")
    public ResponseEntity<ResponseDataDto<Void>> breweryQuit(@Valid @ModelAttribute VerifyAuthDto quitRequestDto, @LoginUserId Long userId) {
        breweryService.breweryQuit(userId, quitRequestDto);
        return ResponseEntity.ok().body(ResponseDataDto.success("양조장 정보가 삭제되었습니다."));
    }
}
