package com.example.monghyang.domain.brewery.controller;

import com.example.monghyang.domain.brewery.dto.ResRegionDto;
import com.example.monghyang.domain.joy.service.JoyService;
import com.example.monghyang.domain.brewery.dto.ResBreweryDto;
import com.example.monghyang.domain.brewery.dto.ResBreweryListDto;
import com.example.monghyang.domain.brewery.service.BreweryService;
import com.example.monghyang.domain.brewery.tag.BreweryTagService;
import com.example.monghyang.domain.global.advice.ApplicationErrorDto;
import com.example.monghyang.domain.tag.dto.ResTagListDto;
import com.example.monghyang.domain.global.response.ResponseDataDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/brewery") // 모두가 접근할 수 있는 양조장 관련 api(조회 등)
@Tag(name = "양조장 API")
@RequiredArgsConstructor
public class BreweryController {
    private final BreweryService breweryService;
    private final BreweryTagService breweryTagService;

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
    @Operation(
            summary = "양조장 상세 조회",
            description = "삭제 처리되지 않은 양조장 상세 정보를 조회합니다. 응답에는 양조장 기본 정보, 이미지 목록, 태그 이름 목록, 활성 체험 목록, 온라인 판매 상품 0페이지가 포함됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "양조장 상세 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "content": {
                                        "brewery_id": 1,
                                        "users_id": 10,
                                        "users_email": "brewery@example.com",
                                        "users_phone": "010-1234-5678",
                                        "region_type_name": "서울",
                                        "brewery_name": "몽향양조장",
                                        "brewery_address": "서울시 중구",
                                        "brewery_address_detail": "101호",
                                        "brewery_introduction": "전통주 체험을 운영하는 양조장입니다.",
                                        "brewery_website": "https://example.com",
                                        "brewery_registered_at": "2026-06-01",
                                        "brewery_is_regular_visit": true,
                                        "brewery_is_visiting_brewery": true,
                                        "brewery_image_image_key": [
                                          {
                                            "brewery_image_id": 1,
                                            "brewery_image_image_key": "brewery/1/main.jpg",
                                            "brewery_image_seq": 1
                                          }
                                        ],
                                        "tags_name": ["탁주"],
                                        "joy": [],
                                        "product_list": {
                                          "content": [],
                                          "number": 0,
                                          "size": 12
                                        }
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "양조장 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<ResBreweryDto>> getBreweryById(
            @Parameter(description = "조회할 양조장 식별자", example = "1", required = true)
            @PathVariable Long breweryId
    ) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(breweryService.getBreweryById(breweryId)));
    }

    @GetMapping("/regions")
    @Operation(summary = "양조장 지역 리스트 반환")
    public ResponseEntity<ResponseDataDto<List<ResRegionDto>>> getRegionList() {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(breweryService.getAllRegions()));
    }
}
