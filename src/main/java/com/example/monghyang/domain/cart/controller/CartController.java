package com.example.monghyang.domain.cart.controller;

import com.example.monghyang.domain.cart.dto.ReqCartDto;
import com.example.monghyang.domain.cart.dto.ResCartDto;
import com.example.monghyang.domain.cart.service.CartService;
import com.example.monghyang.domain.global.annotation.auth.LoginUserId;
import com.example.monghyang.domain.global.response.ResponseDataDto;
import io.swagger.v3.oas.annotations.Operation;
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
public class CartController {
    private final CartService cartService;

    @PostMapping
    @Operation(summary = "장바구니 추가")
    public ResponseEntity<ResponseDataDto<Void>> addCart(@RequestBody @Valid ReqCartDto reqCartDto, @LoginUserId Long userId) {
        cartService.addCart(reqCartDto, userId);
        return ResponseEntity.ok().body(ResponseDataDto.success("장바구니에 추가되었습니다."));
    }

    @PostMapping("/plus/{cartId}")
    @Operation(summary = "장바구니 수량 1 증가")
    public ResponseEntity<ResponseDataDto<Void>> plusQuantity(@LoginUserId Long userId, @PathVariable Long cartId) {
        cartService.plusQuantity(userId, cartId);
        return ResponseEntity.ok().body(ResponseDataDto.success("해당 장바구니 수량이 1 증가되었습니다."));
    }

    @PostMapping("/minus/{cartId}")
    @Operation(summary = "장바구니 수량 1 감소")
    public ResponseEntity<ResponseDataDto<Void>> minusQuantity(@LoginUserId Long userId, @PathVariable Long cartId) {
        cartService.minusQuantity(userId, cartId);
        return ResponseEntity.ok().body(ResponseDataDto.success("해당 장바구니 수량이 1 감소되었습니다."));
    }

    @PostMapping("/specified/{cartId}/{quantity}")
    @Operation(summary = "장바구니 수량 사용자 지정 수량으로 수정")
    public ResponseEntity<ResponseDataDto<Void>> specifiedQuantity(@LoginUserId Long userId, @PathVariable Long cartId, @PathVariable Integer quantity) {
        cartService.updateSpecifiedQuantity(userId, cartId, quantity);
        return ResponseEntity.ok().body(ResponseDataDto.success("해당 장바구니 수량이 수정되었습니다."));
    }

    @DeleteMapping("/{cartId}")
    @Operation(summary = "장바구니 삭제")
    public ResponseEntity<ResponseDataDto<Void>> deleteCart(@LoginUserId Long userId, @PathVariable Long cartId) {
        cartService.deleteCart(userId, cartId);
        return ResponseEntity.ok().body(ResponseDataDto.success("해당 장바구니 요소가 삭제되었습니다."));
    }

    @GetMapping("/my")
    @Operation(summary = "자신의 장바구니 요소 모두 조회")
    public ResponseEntity<ResponseDataDto<List<ResCartDto>>> getMyCartList(@LoginUserId Long userId) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(cartService.getMyCartList(userId)));
    }
}
