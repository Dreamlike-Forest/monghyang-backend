package com.example.monghyang.domain.cart.controller;

import com.example.monghyang.domain.cart.dto.ReqCartDto;
import com.example.monghyang.domain.cart.dto.ResCartDto;
import com.example.monghyang.domain.cart.service.CartService;
import com.example.monghyang.domain.global.annotation.auth.LoginUserId;
import com.example.monghyang.domain.global.advice.ApplicationErrorDto;
import com.example.monghyang.domain.global.response.ResponseDataDto;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "장바구니 API", description = "장바구니 수량 유효 범위: 1 ~ 99")
@RequiredArgsConstructor
@SecurityRequirement(name = "SessionID")
public class CartController {
    private final CartService cartService;

    @PostMapping
    @Operation(summary = "장바구니 추가")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "장바구니 추가 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "수량 범위가 올바르지 않거나 주문할 수 없는 상품",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "회원 또는 상품이 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> addCart(@RequestBody @Valid ReqCartDto reqCartDto, @Parameter(hidden = true) @LoginUserId Long userId) {
        cartService.addCart(reqCartDto, userId);
        return ResponseEntity.ok().body(ResponseDataDto.success("장바구니에 추가되었습니다."));
    }

    @PostMapping("/plus/{cartId}")
    @Operation(summary = "장바구니 수량 1 증가")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "장바구니 수량 증가 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "장바구니 요소가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> plusQuantity(@Parameter(hidden = true) @LoginUserId Long userId, @PathVariable Long cartId) {
        cartService.plusQuantity(userId, cartId);
        return ResponseEntity.ok().body(ResponseDataDto.success("해당 장바구니 수량이 1 증가되었습니다."));
    }

    @PostMapping("/minus/{cartId}")
    @Operation(summary = "장바구니 수량 1 감소")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "장바구니 수량 감소 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "장바구니 요소가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> minusQuantity(@Parameter(hidden = true) @LoginUserId Long userId, @PathVariable Long cartId) {
        cartService.minusQuantity(userId, cartId);
        return ResponseEntity.ok().body(ResponseDataDto.success("해당 장바구니 수량이 1 감소되었습니다."));
    }

    @PostMapping("/specified/{cartId}/{quantity}")
    @Operation(summary = "장바구니 수량 사용자 지정 수량으로 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "장바구니 수량 지정 수정 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "지정 수량이 장바구니 수량 범위를 벗어남",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "장바구니 요소가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> specifiedQuantity(@Parameter(hidden = true) @LoginUserId Long userId, @PathVariable Long cartId, @PathVariable Integer quantity) {
        cartService.updateSpecifiedQuantity(userId, cartId, quantity);
        return ResponseEntity.ok().body(ResponseDataDto.success("해당 장바구니 수량이 수정되었습니다."));
    }

    @DeleteMapping("/{cartId}")
    @Operation(summary = "장바구니 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "장바구니 요소 삭제 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "장바구니 요소가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> deleteCart(@Parameter(hidden = true) @LoginUserId Long userId, @PathVariable Long cartId) {
        cartService.deleteCart(userId, cartId);
        return ResponseEntity.ok().body(ResponseDataDto.success("해당 장바구니 요소가 삭제되었습니다."));
    }

    @GetMapping("/my")
    @Operation(summary = "자신의 장바구니 요소 모두 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내 장바구니 요소 목록 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "장바구니가 비어 있음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<List<ResCartDto>>> getMyCartList(@Parameter(hidden = true) @LoginUserId Long userId) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(cartService.getMyCartList(userId)));
    }
}
