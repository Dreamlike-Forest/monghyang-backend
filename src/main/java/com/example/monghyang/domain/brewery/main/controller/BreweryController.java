package com.example.monghyang.domain.brewery.main.controller;

import com.example.monghyang.domain.brewery.main.service.BreweryService;
import com.example.monghyang.domain.brewery.tag.BreweryTagService;
import com.example.monghyang.domain.brewery.tag.ResBreweryTagDto;
import com.example.monghyang.domain.global.response.ResponseDataDto;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/brewery") // 모두가 접근할 수 있는 양조장 관련 api(조회 등)
public class BreweryController {
    private final BreweryService breweryService;
    private final BreweryTagService breweryTagService;

    @Autowired
    public BreweryController(BreweryService breweryService, BreweryTagService breweryTagService) {
        this.breweryService = breweryService;
        this.breweryTagService = breweryTagService;
    }

    // 특정 양조장이 가지고 있는 태그 리스트 조회
    @GetMapping("/tag-list/{breweryId}")
    @Operation(summary = "특정 양조장이 가지고 있는 태그 조회")
    public ResponseEntity<ResponseDataDto<List<ResBreweryTagDto>>> getBreweryTagList(@PathVariable Long breweryId) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(breweryTagService.getBreweryTags(breweryId)));
    }


}
