package com.example.monghyang.domain.brewery.controller;

import com.example.monghyang.domain.joy.service.JoyService;
import com.example.monghyang.domain.brewery.dto.ResBreweryDto;
import com.example.monghyang.domain.brewery.dto.ResBreweryListDto;
import com.example.monghyang.domain.brewery.service.BreweryService;
import com.example.monghyang.domain.brewery.tag.BreweryTagService;
import com.example.monghyang.domain.tag.dto.ResTagListDto;
import com.example.monghyang.domain.global.response.ResponseDataDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
    public ResponseEntity<ResponseDataDto<List<ResTagListDto>>> getBreweryTagList(@PathVariable Long breweryId) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(breweryTagService.getBreweryTagsById(breweryId)));
    }

    @GetMapping("/search/{startOffset}")
    @Operation(summary = "필터링 검색", description = "keyword: 양조장 이름 키워드, min_price: 체험 최소가격, max_price: 체험 최대가격, tag_id_list: 태그(주종, 배지 등) 식별자, region_id_list: 지역 식별자")
    public ResponseEntity<ResponseDataDto<Page<ResBreweryListDto>>> filteringBreweryList(@PathVariable Integer startOffset,
            @RequestParam(required = false) String keyword, @RequestParam(required = false) Integer min_price, @RequestParam(required = false) Integer max_price,
            @RequestParam(required = false) List<Integer> tag_id_list, @RequestParam(required = false) List<Integer> region_id_list) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(breweryService.getFilteringSearch(
                startOffset, keyword, min_price, max_price, tag_id_list, region_id_list
        )));
    }

    @GetMapping("/latest/{startOffset}")
    @Operation(summary = "최신순 조회")
    public ResponseEntity<ResponseDataDto<Page<ResBreweryListDto>>> getLatestBreweryList(@PathVariable Integer startOffset) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(breweryService.getLatest(startOffset)));
    }

    @GetMapping("/{breweryId}")
    @Operation(summary = "양조장 식별자로 양조장 검색", description = "제공 정보: 양조장에 대한 정보, 태그, 이미지 key 및 순서, 체험 정보")
    public ResponseEntity<ResponseDataDto<ResBreweryDto>> getBreweryById(@PathVariable Long breweryId) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(breweryService.getBreweryById(breweryId)));
    }
}
