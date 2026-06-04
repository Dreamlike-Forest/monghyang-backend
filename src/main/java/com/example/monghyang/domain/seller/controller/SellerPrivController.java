package com.example.monghyang.domain.seller.controller;

import com.example.monghyang.domain.auth.dto.VerifyAuthDto;
import com.example.monghyang.domain.global.annotation.auth.LoginUserId;
import com.example.monghyang.domain.global.advice.ApplicationErrorDto;
import com.example.monghyang.domain.global.response.ResponseDataDto;
import com.example.monghyang.domain.orders.item.dto.ResOrderItemForSellerDto;
import com.example.monghyang.domain.orders.item.service.OrderItemService;
import com.example.monghyang.domain.product.dto.ReqProductDto;
import com.example.monghyang.domain.product.dto.ResMyProductDto;
import com.example.monghyang.domain.product.dto.UpdateProductDto;
import com.example.monghyang.domain.product.service.ProductService;
import com.example.monghyang.domain.product.tag.ProductTagService;
import com.example.monghyang.domain.seller.dto.ReqSellerDto;
import com.example.monghyang.domain.seller.service.SellerService;
import com.example.monghyang.domain.tag.dto.ReqTagDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seller-priv")
@Tag(name = "판매자 관리자용 API", description = "양조장 및 판매자 권한을 가진 회원만 접근할 수 있습니다.")
@RequiredArgsConstructor
@SecurityRequirement(name = "SessionID")
public class SellerPrivController {
    private final SellerService sellerService;
    private final ProductTagService productTagService;
    private final ProductService productService;
    private final OrderItemService orderItemService;

    // 판매자 권한 검증: (@LoginUserId로 회원식별자 추출 -> 해당되는 판매자 조회 -> 판매자 식별자 사용)

    // 판매자 테이블 수정
    @PostMapping("/update")
    @Operation(summary = "판매자 테이블에 대한 정보 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "판매자 정보 수정 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "이미지 개수, 이미지 순서 또는 입력값 형식이 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "판매자 또는 이미지 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> updateSeller(@Parameter(hidden = true) @LoginUserId Long userId, @Valid @ModelAttribute ReqSellerDto reqSellerDto) {
        sellerService.sellerUpdate(userId, reqSellerDto);
        return ResponseEntity.ok().body(ResponseDataDto.success("판매자 정보가 수정되었습니다."));
    }

    // 판매자 삭제 처리(해당 회원의 기존 비밀번호 입력받고 일치하는지 검사)
    @DeleteMapping
    @Operation(summary = "판매자 삭제 처리", description = "해당 회원의 기존 비밀번호를 입력받고, 일치하는지 검사합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "판매자 삭제 처리 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없거나 기존 비밀번호가 일치하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "판매자 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> deleteSeller(@Parameter(hidden = true) @LoginUserId Long userId, @Valid @ModelAttribute VerifyAuthDto quitRequestDto) {
        sellerService.sellerQuit(userId, quitRequestDto);
        return ResponseEntity.ok().body(ResponseDataDto.success("판매자 정보가 삭제되었습니다."));
    }

    // 판매자 복구 처리: /restore
    @PostMapping("/restore")
    @Operation(summary = "판매자 복구", description = "해당 회원의 기존 비밀번호를 입력받고, 일치하는지 검사합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "판매자 복구 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없거나 기존 비밀번호가 일치하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "판매자 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> restoreSeller(@Parameter(hidden = true) @LoginUserId Long userId, @Valid @ModelAttribute VerifyAuthDto restoreRequestDto) {
        sellerService.sellerRestore(userId, restoreRequestDto);
        return ResponseEntity.ok().body(ResponseDataDto.success("판매자 정보가 복구되었습니다."));
    }

    // 상품 추가
    @PostMapping("/product-add")
    @Operation(summary = "상품 추가")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 추가 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "상품 입력값, 이미지 형식, 이미지 개수 또는 이미지 순서가 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "판매자 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> createProduct(@Parameter(hidden = true) @LoginUserId Long userId, @Valid @ModelAttribute ReqProductDto reqProductDto) {
        productService.createProduct(userId, reqProductDto);
        return ResponseEntity.ok().body(ResponseDataDto.success("상품이 추가되었습니다."));
    }

    // 상품 수정 로직(이미지까지 추가/삭제 한번에 가능하도록) - 온라인 판매 여부까지 수정 가능: /product-update
    @PostMapping("/product-update")
    @Operation(summary = "상품 수정", description = "기본 상품 정보, 이미지 추가/삭제, 품절 여부, 온라인 판매 여부 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 수정 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "상품 입력값, 이미지 형식, 이미지 개수 또는 이미지 순서가 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "자신의 상품 또는 이미지가 아닌 대상 수정 요청",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "상품 또는 이미지 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> updateProduct(@Parameter(hidden = true) @LoginUserId Long userId, @Valid @ModelAttribute UpdateProductDto updateProductDto) {
        productService.updateProduct(userId, updateProductDto);
        return ResponseEntity.ok().body(ResponseDataDto.success("상품 정보가 수정되었습니다."));
    }

    @PostMapping("/product-inc-inven/{productId}/{quantity}")
    @Operation(summary = "재고 입고")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 재고 입고 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "자신의 상품이 아닌 상품 재고 변경 요청",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "상품 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> increseInventory(@Parameter(hidden = true) @LoginUserId Long userId, @PathVariable Long productId, @PathVariable Integer quantity) {
        productService.increaseInventory(productId, userId, quantity);
        return ResponseEntity.ok().body(ResponseDataDto.success("상품 재고가 입고되었습니다."));
    }

    @PostMapping("/product-dec-inven/{productId}/{quantity}")
    @Operation(summary = "재고 출고")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 재고 출고 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "출고 수량이 올바르지 않거나 재고가 부족함",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "상품 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> decreaseInventory(@PathVariable Long productId, @PathVariable Integer quantity) {
        productService.decreseInventory(productId, quantity);
        return ResponseEntity.ok().body(ResponseDataDto.success("상품 재고가 출고되었습니다."));
    }

    // 상품 삭제 처리
    @DeleteMapping("/product/{productId}")
    @Operation(summary = "자신의 상품 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 삭제 처리 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "자신의 상품이 아닌 상품 삭제 요청",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "상품 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> deleteProduct(@Parameter(hidden = true) @LoginUserId Long userId, @PathVariable Long productId) {
        productService.deleteProduct(userId, productId);
        return ResponseEntity.ok().body(ResponseDataDto.success("상품이 삭제되었습니다."));
    }

    @GetMapping("/product/my/{startOffset}")
    @Operation(summary = "자신의 상품 리스트 등록 최신순 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내 상품 목록 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "판매자 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Page<ResMyProductDto>>> getMyProductList(@Parameter(hidden = true) @LoginUserId Long userId, @PathVariable Integer startOffset) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(productService.getMyProductList(userId, startOffset)));
    }

    // 상품 복구 처리
    @PostMapping("/product-restore/{productId}")
    @Operation(summary = "자신의 상품 복구")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 복구 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "자신의 상품이 아닌 상품 복구 요청",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "상품 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> restoreProduct(@Parameter(hidden = true) @LoginUserId Long userId, @PathVariable Long productId) {
        productService.restoreProduct(userId, productId);
        return ResponseEntity.ok().body(ResponseDataDto.success("상품이 복구되었습니다."));
    }

    @PostMapping("/product-set-soldout/{productId}")
    @Operation(summary = "상품 품절 처리")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 품절 처리 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "자신의 상품이 아닌 상품 품절 처리 요청",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "상품 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> setSoldoutProduct(@Parameter(hidden = true) @LoginUserId Long userId, @PathVariable Long productId) {
        productService.setSoldout(userId, productId);
        return ResponseEntity.ok().body(ResponseDataDto.success("상품이 품절 처리 되었습니다."));
    }

    @PostMapping("/product-unset-soldout/{productId}")
    @Operation(summary = "상품 품절 상태 복구")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 품절 상태 복구 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "자신의 상품이 아닌 상품 품절 복구 요청",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "상품 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> unSetSoldoutProduct(@Parameter(hidden = true) @LoginUserId Long userId, @PathVariable Long productId) {
        productService.unSetSoldout(userId, productId);
        return ResponseEntity.ok().body(ResponseDataDto.success("상품이 품절 해제 처리 되었습니다."));
    }


    // 상품에 대한 태그 추가 및 삭제
    @PostMapping("/product-tag/{productId}")
    @Operation(summary = "상품에 태그를 추가하거나 기존의 태그를 삭제합니다.", description = "추가 대상 태그 식별자 리스트와 삭제 대상 태그 식별자 리스트를 json으로 보내주세요.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 태그 수정 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "태그 수정 요청값이 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "자신의 상품이 아닌 상품 태그 수정 요청",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "상품 또는 태그 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> updateTag(@Parameter(hidden = true) @LoginUserId Long userId, @PathVariable Long productId, @RequestBody ReqTagDto reqTagDto) {
        productTagService.updateTag(userId, productId, reqTagDto);
        return ResponseEntity.ok().body(ResponseDataDto.success("태그 수정사항이 반영되었습니다."));
    }

    @GetMapping("/product-order/history/{startOffset}")
    @Operation(summary = "자신이 게시판 상품에 대한 모든 주문 정보 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내 상품 주문 정보 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "페이지 시작 위치가 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "판매자 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Page<ResOrderItemForSellerDto>>> findMyProductOrder(@Parameter(hidden = true) @LoginUserId Long userId, @PathVariable Integer startOffset) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(orderItemService.getMyProductOrderList(userId, startOffset)));
    }
}
