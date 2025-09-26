package com.example.monghyang.domain.cart.service;

import com.example.monghyang.domain.cart.dto.ReqCartDto;
import com.example.monghyang.domain.cart.dto.ResCartDto;
import com.example.monghyang.domain.cart.entity.Cart;
import com.example.monghyang.domain.cart.repository.CartRepository;
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.product.entity.Product;
import com.example.monghyang.domain.product.repository.ProductRepository;
import com.example.monghyang.domain.users.entity.Users;
import com.example.monghyang.domain.users.repository.UsersRepository;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class CartService {
    private final CartRepository cartRepository;
    private final UsersRepository usersRepository;
    private final ProductRepository productRepository;

    @Autowired
    public CartService(CartRepository cartRepository, UsersRepository usersRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.usersRepository = usersRepository;
        this.productRepository = productRepository;
    }

    // 장바구니 추가
    public void addCart(ReqCartDto dto, Long userId) throws ConstraintViolationException {
        Users user = usersRepository.findById(userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.USER_NOT_FOUND));
        Product product = productRepository.findById(dto.getProduct_id()).orElseThrow(() ->
                new ApplicationException(ApplicationError.PRODUCT_NOT_FOUND));
        Optional<Cart> previous = cartRepository.findByProductIdAndUserId(product.getId(), user.getId());
        if(previous.isPresent()) {
            // 이미 해당 유저가 해당 상품을 장바구니에 추가했던 상태인 경우: 새로 레코드를 생성하지 않고, 기존 수량을 증가 후 로직 종료
            Integer previousQuantity = previous.get().getQuantity();
            previous.get().updateSpecifiedQuantity(previousQuantity + dto.getQuantity());
            cartRepository.save(previous.get());
            return;
        }

        Cart cart = Cart.userProductQuantityOf(user, product, dto.getQuantity());
        cartRepository.save(cart);
    }

    // 장바구니 수량 1 증가
    public void plusQuantity(Long userId, Long cartId) {
        Cart cart = cartRepository.findByIdAndUserId(cartId, userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.CART_ITEM_NOT_FOUND));
        cart.plusOneQuantity();
        cartRepository.save(cart);
    }

    // 장바구니 수량 1 감소
    public void minusQuantity(Long userId, Long cartId) {
        Cart cart = cartRepository.findByIdAndUserId(cartId, userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.CART_ITEM_NOT_FOUND));
        cart.minusOneQuantity();
        cartRepository.save(cart);
    }

    // 장바구니 사용자 지정 수량으로 수정
    public void updateSpecifiedQuantity(Long userId, Long cartId, Integer newQuantity) {
        Cart cart = cartRepository.findByIdAndUserId(cartId, userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.CART_ITEM_NOT_FOUND));
        cart.updateSpecifiedQuantity(newQuantity);
        cartRepository.save(cart);
    }

    // 장바구니 삭제 (hard-delete)
    public void deleteCart(Long userId, Long cartId) {
        Cart cart = cartRepository.findByIdAndUserId(cartId, userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.CART_ITEM_NOT_FOUND));
        cartRepository.delete(cart);
    }

    // 자신의 장바구니 조회
    public List<ResCartDto> getMyCartList(Long userId) {
        List<Cart> cartList = cartRepository.findByUserId(userId);
        if(cartList.isEmpty()) {
            throw new ApplicationException(ApplicationError.CART_NOT_FOUND);
        }
        return cartList.stream().map(ResCartDto::cartFrom).toList();
    }
}
