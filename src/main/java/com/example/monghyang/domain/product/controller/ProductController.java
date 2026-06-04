package com.example.monghyang.domain.product.controller;

import com.example.monghyang.domain.global.advice.ApplicationErrorDto;
import com.example.monghyang.domain.global.response.ResponseDataDto;
import com.example.monghyang.domain.product.dto.ResProductDto;
import com.example.monghyang.domain.product.dto.ResProductListDto;
import com.example.monghyang.domain.product.service.ProductService;
import com.example.monghyang.domain.product.tag.ProductTagService;
import com.example.monghyang.domain.tag.dto.ResTagListDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
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
@RequestMapping("/api/product")
@Tag(name = "상품 API")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final ProductTagService productTagService;

    // 상품 필터링 조회(태그, 도수, 가격)
    @GetMapping("/search/{startOffset}")
    @Operation(summary = "필터링 검색", description = "도수, 태그(주종, 뱃지 등), 가격대, 키워드")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 필터링 검색 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "가격, 도수, 태그 또는 페이지 요청값이 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Page<ResProductListDto>>> filteringProductList(@PathVariable Integer startOffset,
         @RequestParam(required = false) String keyword, @RequestParam(required = false) Integer min_price, @RequestParam(required = false) Integer max_price,
         @RequestParam(required = false) List<Integer> tag_id_list, @RequestParam(required = false) Double min_alcohol, @RequestParam(required = false) Double max_alcohol) {

        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(productService.dynamicSearch(startOffset, keyword, min_price, max_price, min_alcohol, max_alcohol, tag_id_list)));
    }

    // 상품 최신순 조회
    @GetMapping("/latest/{startOffset}")
    @Operation(summary = "상품 최신순 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 최신순 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "페이지 요청값이 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Page<ResProductListDto>>> getProductLatest(@PathVariable Integer startOffset) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(productService.getProductLatest(startOffset)));
    }

    // 특정 판매자(혹은 양조장)의 모든 상품 조회
    @GetMapping("/by-user/{userId}/{startOffset}")
    @Operation(summary = "특정 회원이 업로드한 모든 상품 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "특정 회원 상품 목록 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "페이지 요청값이 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "회원 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Page<ResProductListDto>>> getProductByUserId(@PathVariable Long userId, @PathVariable Integer startOffset) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(productService.getProductByUserId(userId, startOffset)));
    }

    // 특정 상품 세부조회(식별자 기준)
    @GetMapping("/{productId}")
    @Operation(summary = "상품 식별자 기준 세부 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 상세 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "404", description = "상품 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<ResProductDto>> getProductById(@PathVariable Long productId) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(productService.getProductById(productId)));
    }

    @GetMapping("/tag-list/{productId}")
    @Operation(summary = "특정 상품이 가지는 태그 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 태그 목록 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "404", description = "상품 또는 태그 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<List<ResTagListDto>>> getProductTagList(@PathVariable Long productId) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(productTagService.getProductTagsById(productId)));
    }
}
