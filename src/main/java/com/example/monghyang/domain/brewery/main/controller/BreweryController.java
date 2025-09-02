package com.example.monghyang.domain.brewery.main.controller;

import com.example.monghyang.domain.brewery.joy.dto.ResJoyDto;
import com.example.monghyang.domain.brewery.joy.service.JoyService;
import com.example.monghyang.domain.brewery.main.dto.ResBreweryListDto;
import com.example.monghyang.domain.brewery.main.service.BreweryService;
import com.example.monghyang.domain.brewery.tag.BreweryTagService;
import com.example.monghyang.domain.brewery.tag.ResBreweryTagDto;
import com.example.monghyang.domain.global.response.ResponseDataDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/brewery") // 모두가 접근할 수 있는 양조장 관련 api(조회 등)
@Tag(name = "양조장 API")
public class BreweryController {
    private final BreweryService breweryService;
    private final BreweryTagService breweryTagService;
    private final JoyService joyService;

    @Autowired
    public BreweryController(BreweryService breweryService, BreweryTagService breweryTagService, JoyService joyService) {
        this.breweryService = breweryService;
        this.breweryTagService = breweryTagService;
        this.joyService = joyService;
    }

    @GetMapping("/tag-list/{breweryId}")
    @Operation(summary = "특정 양조장이 가지고 있는 태그 조회")
    public ResponseEntity<ResponseDataDto<List<ResBreweryTagDto>>> getBreweryTagList(@PathVariable Long breweryId) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(breweryTagService.getBreweryTags(breweryId)));
    }

    @GetMapping("/joy-list/{breweryId}")
    @Operation(summary = "특정 양조장의 체험 리스트 조회")
    public ResponseEntity<ResponseDataDto<List<ResJoyDto>>> getBreweryJoyList(@PathVariable Long breweryId) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(joyService.getJoyListByBreweryId(breweryId)));
    }

    @GetMapping("/search/{startOffset}")
    @Operation(summary = "필터링 검색", description = "keyword: 양조장 이름 키워드, min_price: 체험 최소가격, max_price: 체험 최대가격, tag_id_list: 태그(주종, 배지 등) 식별자, region_id_list: 지역 식별자")
    public ResponseEntity<ResponseDataDto<List<ResBreweryListDto>>> filteringBreweryList(@PathVariable Integer startOffset,
            @RequestParam(required = false) String keyword, @RequestParam(required = false) Integer min_price, @RequestParam(required = false) Integer max_price,
            @RequestParam(required = false) List<Integer> tag_id_list, @RequestParam(required = false) List<Integer> region_id_list) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(breweryService.getFilteringSearch(
                startOffset, keyword, min_price, max_price, tag_id_list, region_id_list
        )));
    }

    // 양조장 개별 상세 조회: 양조장 모든 이미지, 양조장 소개글, 주소, 주종 태그, 연락처(회원 entity), 이메일(회원 entity), 홈페이지 링크
    // 체험 프로그램 리스트 반환 api: 체험 이름, 체험 장소, 체험 내용, 체험 인당 비용(단위: 원), 매진 여부
    // 삭제처리된 체험 프로그램은 취급하지 않음.
    // 판매 상품 조회 api: 상품 이미지, 상품 이름, 상품 평점, 상품 리뷰 수, 상품 도수(double), 상품 용량, 상품 가격(단위: 원)
    // 체험 리뷰 조회 api: 체험 평점, 체험 총 리뷰 수, 해당 체험의 리뷰의 총 좋아요 수, 해당 체험 리뷰의 총 조회수
    // 리뷰 대표 이미지, 리뷰 제목, 리뷰 평가 점수, 리뷰 본문, 리뷰 작성자 유저네임, 리뷰 작성일자, 체험 장소, 해당 리뷰 조회수, 해당 리뷰 좋아요 수, 해당 리뷰 댓글 수
    // 해당 리뷰 태그 종, 해당 리뷰 이미지 수

}
