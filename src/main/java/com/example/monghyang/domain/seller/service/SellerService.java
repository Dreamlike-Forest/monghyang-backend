package com.example.monghyang.domain.seller.service;

import com.example.monghyang.domain.auth.dto.VerifyAuthDto;
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.image.dto.AddImageDto;
import com.example.monghyang.domain.image.dto.ModifySeqImageDto;
import com.example.monghyang.domain.image.service.ImageType;
import com.example.monghyang.domain.image.service.StorageService;
import com.example.monghyang.domain.seller.dto.ReqSellerDto;
import com.example.monghyang.domain.seller.dto.ResSellerDto;
import com.example.monghyang.domain.seller.entity.Seller;
import com.example.monghyang.domain.seller.entity.SellerImage;
import com.example.monghyang.domain.seller.repository.SellerImageRepository;
import com.example.monghyang.domain.seller.repository.SellerRepository;
import com.example.monghyang.domain.users.entity.Users;
import com.example.monghyang.domain.users.repository.UsersRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SellerService {
    @Getter
    private final int SELLER_PAGE_SIZE = 12;
    private final SellerRepository sellerRepository;
    private final UsersRepository usersRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final SellerImageRepository sellerImageRepository;
    private final StorageService storageService;

    @Autowired
    public SellerService(SellerRepository sellerRepository, UsersRepository usersRepository, BCryptPasswordEncoder bCryptPasswordEncoder, SellerImageRepository sellerImageRepository, StorageService storageService) {
        this.sellerRepository = sellerRepository;
        this.usersRepository = usersRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.sellerImageRepository = sellerImageRepository;
        this.storageService = storageService;
    }

    // 판매자 정보 수정
    @Transactional
    public void sellerUpdate(Long userId, ReqSellerDto reqSellerDto) {
        Seller seller = sellerRepository.findByUserId(userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.SELLER_NOT_FOUND));

        if(!reqSellerDto.getAdd_images().isEmpty() || !reqSellerDto.getRemove_images().isEmpty() || !reqSellerDto.getModify_images().isEmpty()) {
            List<SellerImage> imageList = sellerImageRepository.findBySeller(seller);

            Set<Long> imageListIds = imageList.stream().map(SellerImage::getId).collect(Collectors.toSet());

            // 이미지 삭제/추가 가능한지 검증(5개를 넘지 않는지 검사)
            if(imageList.size() - reqSellerDto.getRemove_images().size() + reqSellerDto.getAdd_images().size() > 5) {
                throw new ApplicationException(ApplicationError.IMAGE_COUNT_INVALID);
            }

            // 이미지 삭제
            for(Long removeImageId : reqSellerDto.getRemove_images()) {
                if(!imageListIds.contains(removeImageId)) {
                    // 자신의 이미지가 아니라면 삭제 불가
                    throw new ApplicationException(ApplicationError.REQUEST_FORBIDDEN);
                }
                SellerImage sellerImage = sellerImageRepository.findById(removeImageId).orElseThrow(() ->
                        new ApplicationException(ApplicationError.IMAGE_NOT_FOUND));
                storageService.remove(sellerImage.getImageKey());
                sellerImageRepository.delete(sellerImage);
                imageListIds.remove(removeImageId);
            }

            // 이미지 순서 정보 수정
            for(ModifySeqImageDto cur : reqSellerDto.getModify_images()) {
                if(!imageListIds.contains(cur.getImage_id())) {
                    throw new ApplicationException(ApplicationError.REQUEST_FORBIDDEN);
                }
                if(cur.getSeq() > 5 || cur.getSeq() < 1) {
                    throw new ApplicationException(ApplicationError.IMAGE_SEQ_INVALID);
                }
                SellerImage sellerImage = sellerImageRepository.findById(cur.getImage_id()).orElseThrow(() ->
                        new ApplicationException(ApplicationError.IMAGE_NOT_FOUND));
                sellerImage.updateSeq(cur.getSeq());
                // 수정 후 db 즉시 반영
                try {
                    sellerImageRepository.save(sellerImage);
                } catch (DataIntegrityViolationException e) {
                    // 중복된 seq 이미지가 존재할 경우 uk 제약 조건 위배
                    throw new ApplicationException(ApplicationError.IMAGE_SEQ_INVALID);
                }
            }

            // 이미지 추가
            for(AddImageDto cur : reqSellerDto.getAdd_images()) {
                if(cur.getSeq() > 5 || cur.getSeq() < 1) {
                    throw new ApplicationException(ApplicationError.IMAGE_SEQ_INVALID);
                }
                String imageKey = storageService.upload(cur.getImage(), ImageType.SELLER_IMAGE);
                try {
                    sellerImageRepository.save(SellerImage.sellerKeySeqVolume(seller, imageKey, cur.getSeq(), cur.getImage().getSize()));
                } catch (DataIntegrityViolationException e) {
                    throw new ApplicationException(ApplicationError.IMAGE_SEQ_INVALID);
                }
            }
        }


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
