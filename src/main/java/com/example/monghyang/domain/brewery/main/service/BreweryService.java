package com.example.monghyang.domain.brewery.main.service;

import com.example.monghyang.domain.auth.dto.VerifyAuthDto;
import com.example.monghyang.domain.brewery.main.dto.ReqBreweryDto;
import com.example.monghyang.domain.brewery.main.entity.Brewery;
import com.example.monghyang.domain.brewery.main.entity.BreweryImage;
import com.example.monghyang.domain.brewery.main.repository.BreweryImageRepository;
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.brewery.main.repository.BreweryRepository;
import com.example.monghyang.domain.image.dto.AddImageDto;
import com.example.monghyang.domain.image.dto.ModifySeqImageDto;
import com.example.monghyang.domain.image.service.ImageType;
import com.example.monghyang.domain.image.service.StorageService;
import com.example.monghyang.domain.users.entity.Users;
import com.example.monghyang.domain.users.repository.UsersRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BreweryService {
    private final BreweryRepository breweryRepository;
    private final UsersRepository usersRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final BreweryImageRepository breweryImageRepository;
    private final StorageService storageService;

    @Autowired
    public BreweryService(BreweryRepository breweryRepository, UsersRepository usersRepository, BCryptPasswordEncoder bCryptPasswordEncoder, BreweryImageRepository breweryImageRepository, StorageService storageService) {
        this.breweryRepository = breweryRepository;
        this.usersRepository = usersRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.breweryImageRepository = breweryImageRepository;
        this.storageService = storageService;
    }

    // 양조장 검색 (필터링 옵션 종류: 지역 타입, 가격 범위, 주종(태그), 배지(태그)): 이때 각 양조장의 이미지는 '대표 이미지'만 조회되도록
    // 삭제처리된 양조장은 취급하지 않음


    // 양조장 수정 api
        // 'brewery' 엔티티의 내용만 수정할 수 있다.
        // 'users' 엔티티와 겹치는 컬럼(주소, 주소상세, 상호명)이 수정될 경우 users 테이블도 수정
        // 동일한 이미지 순서값을 여러 개 받을 경우(ex: 순서가 1인 이미지를 2개 이상 받는 경우) DB 레벨 예외 발생 -> 처리 로직 필요
        // 로직을 실제로 수행하기 전 애플리케이션 레벨에서 '삭제 리스트 삭제' -> '추가 리스트 추가'를 실행한 시나리오를 실행하여 이미지 개수가 5개 이하인지 검증
    @Transactional
    public void breweryUpdate(Long userId, ReqBreweryDto reqBreweryDto) {
        Brewery brewery = breweryRepository.findByUserId(userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.BREWERY_NOT_FOUND));

        if(!reqBreweryDto.getAdd_images().isEmpty() || !reqBreweryDto.getRemove_images().isEmpty() || !reqBreweryDto.getModify_images().isEmpty()) {
            // 이미지 관련 수정 사항이 존재하는 경우 아래의 로직 실행
            List<BreweryImage> imageList = breweryImageRepository.findByBrewery(brewery);

            // 해당 양조장이 가지고 있는 이미지의 식별자 리스트 set
            // 아래부터는 set에 존재하는 식별자로만 이미지 조회를 수행
            Set<Long> imageListIds = imageList.stream().map(BreweryImage::getId).collect(Collectors.toSet());

            // dto에 담긴 '삭제 이미지 개수': del, '추가 이미지 개수': add
            // 이미지 수정사항 반영 가능 여부 검증: curImageCount - del + add <= 5
            if(imageList.size() - reqBreweryDto.getRemove_images().size() + reqBreweryDto.getAdd_images().size() > 5) {
                throw new ApplicationException(ApplicationError.IMAGE_COUNT_INVALID);
            }

            // 이미지 삭제 로직
            for(Long removeImageId : reqBreweryDto.getRemove_images()) {
                if(!imageListIds.contains(removeImageId)) {
                    throw new ApplicationException(ApplicationError.REQUEST_FORBIDDEN); // 자신의 이미지가 아닌것은 삭제 불가
                }
                BreweryImage breweryImage = breweryImageRepository.findById(removeImageId).orElseThrow(() ->
                        new ApplicationException(ApplicationError.IMAGE_NOT_FOUND));
                storageService.remove(breweryImage.getImageKey()); // 스토리지에서 이미지 삭제
                breweryImageRepository.delete(breweryImage); // DB에서 이미지 정보 삭제
                imageListIds.remove(removeImageId); // set에 이미지 삭제 반영
            }

            breweryImageRepository.flush(); // 삭제 정보 선반영: 이미지 순서 수정 혹은 생성 시 uk 제약조건 위배를 피하기 위함

            // 이미지 순서 정보 수정 로직 - saveAndFlush() 메소드 사용하여 Entity 수정사항을 즉시 DB로 전송
                 // 더티체킹하도록 그대로 놔두면 아래의 insert문보다 늦게 실행되므로 uk 제약조건 위배 발생
                // save()는 새로운 엔티티에 대해 호출되는것이 아니라면 더티체킹하는 것과 다를 바 없으므로 사용하지 않는다.
            for(ModifySeqImageDto cur : reqBreweryDto.getModify_images()) {
                if(!imageListIds.contains(cur.getImage_id())) {
                    throw new ApplicationException(ApplicationError.REQUEST_FORBIDDEN); // 자신의 이미지가 아닌 것은 수정 불가
                }
                if(cur.getSeq() > 5 || cur.getSeq() < 1) {
                    throw new ApplicationException(ApplicationError.IMAGE_SEQ_INVALID);
                }
                BreweryImage breweryImage = breweryImageRepository.findById(cur.getImage_id()).orElseThrow(() ->
                        new ApplicationException(ApplicationError.IMAGE_NOT_FOUND));
                breweryImage.updateSeq(cur.getSeq());
                // 수정되자마자 즉시 DB로 변경사항 전송(추후 새 이미지 insert 시 uk 제약조건 위배를 방지하기 위함)
                try{
                    breweryImageRepository.save(breweryImage);
                } catch (DataIntegrityViolationException e) {
                    // 중복된 seq 정보 존재할 경우 db insert 시 uk 제약조건 위배 예외 발생
                    throw new ApplicationException(ApplicationError.IMAGE_SEQ_INVALID);
                }
                }

            // 이미지 업로드 로직
            for(AddImageDto cur : reqBreweryDto.getAdd_images()) {
                if(cur.getSeq() > 5 || cur.getSeq() < 1) {
                    throw new ApplicationException(ApplicationError.IMAGE_SEQ_INVALID);
                }
                // 스토리지에 업로드
                String imageKey = storageService.upload(cur.getImage(), ImageType.BREWERY_IMAGE);
                // DB에 업로드
                try{
                    breweryImageRepository.save(BreweryImage.breweryKeySeqVolume(brewery, imageKey, cur.getSeq(), cur.getImage().getSize()));
                } catch (DataIntegrityViolationException e) {
                    // 중복된 seq 정보 존재할 경우 db insert 시 uk 제약조건 위배 예외 발생
                    throw new ApplicationException(ApplicationError.IMAGE_SEQ_INVALID);
                }
            }
        }

        brewery.updateBrewery(reqBreweryDto);
    }


    // 양조장 탈퇴(비활성화)
    @Transactional
    public void breweryQuit(Long userId, VerifyAuthDto quitRequestDto) {
        Users users = usersRepository.findById(userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.USER_NOT_FOUND));
        if(!bCryptPasswordEncoder.matches(quitRequestDto.getPassword(), users.getPassword())) {
            throw new ApplicationException(ApplicationError.NOT_MATCH_CUR_PASSWORD);
        }
        Brewery brewery = breweryRepository.findByUserId(users.getId()).orElseThrow(() ->
                new ApplicationException(ApplicationError.BREWERY_NOT_FOUND));
        brewery.setDeleted();
    }


    // 양조장 개별 상세 조회: 양조장 모든 이미지, 양조장 소개글, 주소, 주종 태그, 연락처(회원 entity), 이메일(회원 entity), 홈페이지 링크
    // 체험 프로그램 리스트 반환 api: 체험 이름, 체험 장소, 체험 내용, 체험 인당 비용(단위: 원), 매진 여부
        // 삭제처리된 체험 프로그램은 취급하지 않음.
    // 판매 상품 조회 api: 상품 이미지, 상품 이름, 상품 평점, 상품 리뷰 수, 상품 도수(double), 상품 용량, 상품 가격(단위: 원)
    // 체험 리뷰 조회 api: 체험 평점, 체험 총 리뷰 수, 해당 체험의 리뷰의 총 좋아요 수, 해당 체험 리뷰의 총 조회수
        // 리뷰 대표 이미지, 리뷰 제목, 리뷰 평가 점수, 리뷰 본문, 리뷰 작성자 유저네임, 리뷰 작성일자, 체험 장소, 해당 리뷰 조회수, 해당 리뷰 좋아요 수, 해당 리뷰 댓글 수
            // 해당 리뷰 태그 종, 해당 리뷰 이미지 수

    // 페이징 단위: 12개



}
