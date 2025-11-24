package com.example.monghyang.domain.seller.controller;

import com.example.monghyang.domain.auth.dto.VerifyAuthDto;
import com.example.monghyang.domain.global.annotation.auth.LoginUserId;
import com.example.monghyang.domain.global.response.ResponseDataDto;
import com.example.monghyang.domain.product.dto.ReqProductDto;
import com.example.monghyang.domain.product.dto.ResMyProductDto;
import com.example.monghyang.domain.product.dto.UpdateProductDto;
import com.example.monghyang.domain.product.service.ProductService;
import com.example.monghyang.domain.product.tag.ProductTagService;
import com.example.monghyang.domain.seller.dto.ReqSellerDto;
import com.example.monghyang.domain.seller.service.SellerService;
import com.example.monghyang.domain.tag.dto.ReqTagDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seller-priv")
@Tag(name = "판매자 관리자용 API", description = "양조장 및 판매자 권한을 가진 회원만 접근할 수 있습니다.")
public class SellerPrivController {
    private final SellerService sellerService;
    private final ProductTagService productTagService;
    private final ProductService productService;

    @Autowired
    public SellerPrivController(SellerService sellerService, ProductTagService productTagService, ProductService productService) {
        this.sellerService = sellerService;
        this.productTagService = productTagService;
        this.productService = productService;
    }

    // 판매자 권한 검증: (@LoginUserId로 회원식별자 추출 -> 해당되는 판매자 조회 -> 판매자 식별자 사용)

    // 판매자 테이블 수정
    @PostMapping("/update")
    @Operation(summary = "판매자 테이블에 대한 정보 수정")
    public ResponseEntity<ResponseDataDto<Void>> updateSeller(@LoginUserId Long userId, @Valid @ModelAttribute ReqSellerDto reqSellerDto) {
        sellerService.sellerUpdate(userId, reqSellerDto);
        return ResponseEntity.ok().body(ResponseDataDto.success("판매자 정보가 수정되었습니다."));
    }

    // 판매자 삭제 처리(해당 회원의 기존 비밀번호 입력받고 일치하는지 검사)
    @DeleteMapping
    @Operation(summary = "판매자 삭제 처리", description = "해당 회원의 기존 비밀번호를 입력받고, 일치하는지 검사합니다.")
    public ResponseEntity<ResponseDataDto<Void>> deleteSeller(@LoginUserId Long userId, @Valid @ModelAttribute VerifyAuthDto quitRequestDto) {
        sellerService.sellerQuit(userId, quitRequestDto);
        return ResponseEntity.ok().body(ResponseDataDto.success("판매자 정보가 삭제되었습니다."));
    }

    // 판매자 복구 처리: /restore
    @PostMapping("/restore")
    @Operation(summary = "판매자 복구", description = "해당 회원의 기존 비밀번호를 입력받고, 일치하는지 검사합니다.")
    public ResponseEntity<ResponseDataDto<Void>> restoreSeller(@LoginUserId Long userId, @Valid @ModelAttribute VerifyAuthDto restoreRequestDto) {
        sellerService.sellerRestore(userId, restoreRequestDto);
        return ResponseEntity.ok().body(ResponseDataDto.success("판매자 정보가 복구되었습니다."));
    }

    // 상품 추가
    @PostMapping("/product-add")
    @Operation(summary = "상품 추가")
    public ResponseEntity<ResponseDataDto<Void>> createProduct(@LoginUserId Long userId, @Valid @ModelAttribute ReqProductDto reqProductDto) {
        productService.createProduct(userId, reqProductDto);
        return ResponseEntity.ok().body(ResponseDataDto.success("상품이 추가되었습니다."));
    }

    // 상품 수정 로직(이미지까지 추가/삭제 한번에 가능하도록) - 온라인 판매 여부까지 수정 가능: /product-update
    @PostMapping("/product-update")
    @Operation(summary = "상품 수정", description = "기본 상품 정보, 이미지 추가/삭제, 품절 여부, 온라인 판매 여부 수정")
    public ResponseEntity<ResponseDataDto<Void>> updateProduct(@LoginUserId Long userId, @Valid @ModelAttribute UpdateProductDto updateProductDto) {
        productService.updateProduct(userId, updateProductDto);
        return ResponseEntity.ok().body(ResponseDataDto.success("상품 정보가 수정되었습니다."));
    }

    @PostMapping("/product-inc-inven/{productId}/{quantity}")
    @Operation(summary = "재고 입고")
    public ResponseEntity<ResponseDataDto<Void>> increseInventory(@LoginUserId Long userId, @PathVariable Long productId, @PathVariable Integer quantity) {
        productService.increaseInventory(productId, userId, quantity);
        return ResponseEntity.ok().body(ResponseDataDto.success("상품 재고가 입고되었습니다."));
    }

    @PostMapping("/product-dec-inven/{productId}/{quantity}")
    @Operation(summary = "재고 출고")
    public ResponseEntity<ResponseDataDto<Void>> decreaseInventory(@PathVariable Long productId, @PathVariable Integer quantity) {
        productService.decreseInventory(productId, quantity);
        return ResponseEntity.ok().body(ResponseDataDto.success("상품 재고가 출고되었습니다."));
    }

    // 상품 삭제 처리
    @DeleteMapping("/product/{productId}")
    @Operation(summary = "자신의 상품 삭제")
    public ResponseEntity<ResponseDataDto<Void>> deleteProduct(@LoginUserId Long userId, @PathVariable Long productId) {
        productService.deleteProduct(userId, productId);
        return ResponseEntity.ok().body(ResponseDataDto.success("상품이 삭제되었습니다."));
    }

    @GetMapping("/product/my/{startOffset}")
    @Operation(summary = "자신의 상품 리스트 등록 최신순 조회")
    public ResponseEntity<ResponseDataDto<Page<ResMyProductDto>>> getMyProductList(@LoginUserId Long userId, @PathVariable Integer startOffset) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(productService.getMyProductList(userId, startOffset)));
    }

    // 상품 복구 처리
    @PostMapping("/product-restore/{productId}")
    @Operation(summary = "자신의 상품 복구")
    public ResponseEntity<ResponseDataDto<Void>> restoreProduct(@LoginUserId Long userId, @PathVariable Long productId) {
        productService.restoreProduct(userId, productId);
        return ResponseEntity.ok().body(ResponseDataDto.success("상품이 복구되었습니다."));
    }

    @PostMapping("/product-set-soldout/{productId}")
    @Operation(summary = "상품 삭제 처리")
    public ResponseEntity<ResponseDataDto<Void>> setSoldoutProduct(@LoginUserId Long userId, @PathVariable Long productId) {
        productService.setSoldout(userId, productId);
        return ResponseEntity.ok().body(ResponseDataDto.success("상품이 품절 처리 되었습니다."));
    }

    @PostMapping("/product-unset-soldout/{productId}")
    public ResponseEntity<ResponseDataDto<Void>> unSetSoldoutProduct(@LoginUserId Long userId, @PathVariable Long productId) {
        productService.unSetSoldout(userId, productId);
        return ResponseEntity.ok().body(ResponseDataDto.success("상품이 품절 해제 처리 되었습니다."));
    }


    // 상품에 대한 태그 추가 및 삭제
    @PostMapping("/product-tag/{productId}")
    @Operation(summary = "상품에 태그를 추가하거나 기존의 태그를 삭제합니다.", description = "추가 대상 태그 식별자 리스트와 삭제 대상 태그 식별자 리스트를 json으로 보내주세요.")
    public ResponseEntity<ResponseDataDto<Void>> updateTag(@LoginUserId Long userId, @PathVariable Long productId, @RequestBody ReqTagDto reqTagDto) {
        productTagService.updateTag(userId, productId, reqTagDto);
        return ResponseEntity.ok().body(ResponseDataDto.success("태그 수정사항이 반영되었습니다."));
    }
}

