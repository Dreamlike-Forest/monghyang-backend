package com.example.monghyang.domain.product.controller;

import com.example.monghyang.domain.global.response.ResponseDataDto;
import com.example.monghyang.domain.product.dto.ResProductDto;
import com.example.monghyang.domain.product.dto.ResProductListDto;
import com.example.monghyang.domain.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@Tag(name = "상품 API")
public class ProductController {
    private final ProductService productService;
    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // 상품 필터링 조회(태그, 도수, 가격)
    @GetMapping("/search/{startOffset}")
    @Operation(summary = "필터링 검색", description = "도수, 태그(주종, 뱃지 등), 가격대, 키워드")
    public ResponseEntity<ResponseDataDto<Page<ResProductListDto>>> filteringProductList(@PathVariable Integer startOffset,
         @RequestParam(required = false) String keyword, @RequestParam(required = false) Integer min_price, @RequestParam(required = false) Integer max_price,
         @RequestParam(required = false) List<Integer> tag_id_list, @RequestParam(required = false) Double min_alcohol, @RequestParam(required = false) Double max_alcohol) {

        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(productService.dynamicSearch(startOffset, keyword, min_price, max_price, min_alcohol, max_alcohol, tag_id_list)));
    }

    // 상품 최신순 조회
    @GetMapping("/latest/{startOffset}")
    @Operation(summary = "상품 최신순 조회")
    public ResponseEntity<ResponseDataDto<Page<ResProductListDto>>> getProductLatest(@PathVariable Integer startOffset) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(productService.getProductLatest(startOffset)));
    }

    // 특정 판매자(혹은 양조장)의 모든 상품 조회
    @GetMapping("/by-user/{userId}/{startOffset}")
    @Operation(summary = "특정 회원이 업로드한 모든 상품 조회")
    public ResponseEntity<ResponseDataDto<Page<ResProductListDto>>> getProductByUserId(@PathVariable Long userId, @PathVariable Integer startOffset) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(productService.getProductByUserId(userId, startOffset)));
    }

    // 특정 상품 세부조회(식별자 기준)
    @GetMapping("/{productId}")
    @Operation(summary = "상품 식별자 기준 세부 조회")
    public ResponseEntity<ResponseDataDto<ResProductDto>> getProductById(@PathVariable Long productId) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(productService.getProductById(productId)));
    }
}
