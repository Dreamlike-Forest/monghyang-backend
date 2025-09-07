package com.example.monghyang.domain.seller.service;

import com.example.monghyang.domain.auth.dto.VerifyAuthDto;
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.seller.dto.ReqSellerDto;
import com.example.monghyang.domain.seller.dto.ResSellerDto;
import com.example.monghyang.domain.seller.entity.Seller;
import com.example.monghyang.domain.seller.repository.SellerRepository;
import com.example.monghyang.domain.users.entity.Users;
import com.example.monghyang.domain.users.repository.UsersRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class SellerService {
    private final int SELLER_PAGE_SIZE = 12;
    private final SellerRepository sellerRepository;
    private final UsersRepository usersRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public SellerService(SellerRepository sellerRepository, UsersRepository usersRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.sellerRepository = sellerRepository;
        this.usersRepository = usersRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    // 판매자 정보 수정
    public void sellerUpdate(Long userId, ReqSellerDto reqSellerDto) {
        Seller seller = sellerRepository.findByUserId(userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.SELLER_NOT_FOUND));

        if(reqSellerDto.getSeller_name() != null) {
            seller.updateSellerName(reqSellerDto.getSeller_name());
        }
        if(reqSellerDto.getSeller_address() != null) {
            seller.updateSellerAddress(reqSellerDto.getSeller_address());
        }
        if(reqSellerDto.getSeller_address_detail() != null) {
            seller.updateSellerAddressDetail(reqSellerDto.getSeller_address_detail());
        }
        if(reqSellerDto.getBusiness_registration_number() != null) {
            seller.updateBusinessRegistrationNumber(reqSellerDto.getBusiness_registration_number());
        }
        if(reqSellerDto.getSeller_account_number() != null) {
            seller.updateSellerAccountNumber(reqSellerDto.getSeller_account_number());
        }
        if(reqSellerDto.getSeller_depositor() != null) {
            seller.updateSellerDepositor(reqSellerDto.getSeller_depositor());
        }
        if(reqSellerDto.getSeller_bank_name() != null) {
            seller.updateSellerBankName(reqSellerDto.getSeller_bank_name());
        }
        if(reqSellerDto.getIntroduction() != null) {
            seller.updateIntroduction(reqSellerDto.getIntroduction());
        }

        sellerRepository.save(seller);
    }

    // 판매자 키워드 조회
    public Page<ResSellerDto> findByNameKeyword(String keyword, Integer startOffset) {
        if(startOffset == null) {
            startOffset = 0;
        }
        Sort sort = Sort.by(Sort.Direction.DESC, "registeredAt");
        Pageable pageable = PageRequest.of(startOffset, SELLER_PAGE_SIZE, sort);

        Page<Seller> result = sellerRepository.findActiveByNameKeyword(pageable, keyword);
        if(result.isEmpty()) {
            throw new ApplicationException(ApplicationError.SELLER_NOT_FOUND);
        }
        return result.map(ResSellerDto::sellerFrom);
    }

    // 판매자 최신순 조회
    public Page<ResSellerDto> getSellerLatest(Integer startOffset) {
        if(startOffset == null) {
            startOffset = 0;
        }
        Sort sort = Sort.by(Sort.Direction.DESC, "registeredAt");
        Pageable pageable = PageRequest.of(startOffset, SELLER_PAGE_SIZE, sort);
        Page<Seller> result = sellerRepository.findActiveLatest(pageable);
        return result.map(ResSellerDto::sellerFrom);
    }

    // 판매자 필터링 조회 - 추후 구현

    // 판매자 삭제 처리(양조장 불가)
    @Transactional
    public void sellerQuit(Long userId, VerifyAuthDto quitRequestDto) {
        Users users = usersRepository.findById(userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.USER_NOT_FOUND));
        if(!bCryptPasswordEncoder.matches(quitRequestDto.getPassword(), users.getPassword())) {
            throw new ApplicationException(ApplicationError.NOT_MATCH_CUR_PASSWORD);
        }
        Seller seller = sellerRepository.findByUserId(userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.SELLER_NOT_FOUND));
        seller.setDeleted();
    }

    // 판매자 복구 처리(양조장 불가)
    @Transactional
    public void sellerRestore(Long userId, VerifyAuthDto restoreRequestDto) {
        Users users = usersRepository.findById(userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.USER_NOT_FOUND));
        if(!bCryptPasswordEncoder.matches(restoreRequestDto.getPassword(), users.getPassword())) {
            throw new ApplicationException(ApplicationError.NOT_MATCH_CUR_PASSWORD);
        }
        Seller seller = sellerRepository.findByUserId(userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.SELLER_NOT_FOUND));
        seller.unSetDeleted();
    }
}
