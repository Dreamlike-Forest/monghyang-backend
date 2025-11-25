package com.example.monghyang.domain.brewery.service;

import com.example.monghyang.domain.auth.dto.VerifyAuthDto;
import com.example.monghyang.domain.brewery.dto.ResRegionDto;
import com.example.monghyang.domain.brewery.entity.RegionType;
import com.example.monghyang.domain.brewery.repository.RegionTypeRepository;
import com.example.monghyang.domain.joy.dto.ResJoyDto;
import com.example.monghyang.domain.joy.entity.Joy;
import com.example.monghyang.domain.joy.repository.JoyRepository;
import com.example.monghyang.domain.brewery.dto.ReqUpdateBreweryDto;
import com.example.monghyang.domain.brewery.dto.ResBreweryDto;
import com.example.monghyang.domain.brewery.dto.ResBreweryListDto;
import com.example.monghyang.domain.brewery.entity.Brewery;
import com.example.monghyang.domain.brewery.entity.BreweryImage;
import com.example.monghyang.domain.brewery.repository.BreweryImageRepository;
import com.example.monghyang.domain.brewery.tag.BreweryTagRepository;
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.brewery.repository.BreweryRepository;
import com.example.monghyang.domain.image.dto.AddImageDto;
import com.example.monghyang.domain.image.dto.ModifySeqImageDto;
import com.example.monghyang.domain.image.service.ImageType;
import com.example.monghyang.domain.image.service.StorageService;
import com.example.monghyang.domain.product.service.ProductService;
import com.example.monghyang.domain.tag.dto.TagNameDto;
import com.example.monghyang.domain.users.entity.Users;
import com.example.monghyang.domain.users.repository.UsersRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BreweryService {
    private final BreweryRepository breweryRepository;
    private final UsersRepository usersRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final BreweryImageRepository breweryImageRepository;
    private final StorageService storageService;
    @Getter
    private final int BREWERY_PAGE_SIZE = 6;
    private final BreweryTagRepository breweryTagRepository;
    private final JoyRepository joyRepository;
    private final ProductService productService;
    private final RegionTypeRepository regionTypeRepository;

    /**
     * 양조장 지역 종류 전체를 반환
     * @return
     */
    public List<ResRegionDto> getAllRegions() {
        List<RegionType> regionTypeList = regionTypeRepository.findAll();
        return regionTypeList.stream().map(ResRegionDto::new).toList();
    }

    private void addTagListToResult(Page<ResBreweryListDto> result) {
        List<Long> breweryIdList = result.getContent().stream().map(ResBreweryListDto::getBrewery_id).toList();
        List<TagNameDto> breweryTagList = breweryTagRepository.findTagListByBreweryIdList(breweryIdList);
        HashMap<Long, List<String>> breweryIdTagMap = new HashMap<>();
        // key가 존재하면 기존 리스트에 값 삽입, 존재하지 않으면 key값으로 리스트 생성 후 값 삽입
        for(TagNameDto cur : breweryTagList) {
            breweryIdTagMap.computeIfAbsent(cur.ownerId(), k -> new ArrayList<>()).add(cur.tagName());
        }
        // 조회된 양조장 별 태그 정보를 반환 dto에 삽입
        for(ResBreweryListDto dto : result) {
            // getOrDefault()를 통해 태그가 없는 양조장은 해당 필드에 비어있는 리스트를 삽입
            dto.setTag_name(breweryIdTagMap.getOrDefault(dto.getBrewery_id(), Collections.emptyList()));
        }
    }


    // 양조장 수정 api
    // 'brewery' 엔티티의 내용만 수정할 수 있다.
    // 'users' 엔티티와 겹치는 컬럼(주소, 주소상세, 상호명)이 수정될 경우 users 테이블도 수정
    // 동일한 이미지 순서값을 여러 개 받을 경우(ex: 순서가 1인 이미지를 2개 이상 받는 경우) DB 레벨 예외 발생 -> 처리 로직 필요
    // 로직을 실제로 수행하기 전 애플리케이션 레벨에서 '삭제 리스트 삭제' -> '추가 리스트 추가'를 실행한 시나리오를 실행하여 이미지 개수가 5개 이하인지 검증
    @Transactional
    public void breweryUpdate(Long userId, ReqUpdateBreweryDto reqBreweryDto) {
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

            // 이미지 삭제
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

            // 이미지 순서 정보 수정
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

            // 이미지 업로드
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

        // Brewery 테이블 컬럼에 대한 수정사항 반영
        if(reqBreweryDto.getBrewery_name() != null){
            brewery.updateBreweryName(reqBreweryDto.getBrewery_name());
        }
        if(reqBreweryDto.getBrewery_address() != null){
            brewery.updateBreweryAddress(reqBreweryDto.getBrewery_address());
        }
        if(reqBreweryDto.getBrewery_address_detail() != null){
            brewery.updateBreweryAddressDetail(reqBreweryDto.getBrewery_address_detail());
        }
        if(reqBreweryDto.getBusiness_registration_number() != null){
            brewery.updateBusinessRegistrationNumber(reqBreweryDto.getBusiness_registration_number());
        }
        if(reqBreweryDto.getBrewery_depositor() != null){
            brewery.updateBreweryDepositor(reqBreweryDto.getBrewery_depositor());
        }
        if(reqBreweryDto.getBrewery_account_number() != null){
            brewery.updateBreweryAccountNumber(reqBreweryDto.getBrewery_account_number());
        }
        if(reqBreweryDto.getBrewery_bank_name() != null){
            brewery.updateBreweryBankName(reqBreweryDto.getBrewery_bank_name());
        }
        if(reqBreweryDto.getIntroduction() != null){
            brewery.updateIntroduction(reqBreweryDto.getIntroduction());
        }
        if(reqBreweryDto.getBrewery_website() != null){
            brewery.updateBreweryWebsite(reqBreweryDto.getBrewery_website());
        }
        if(reqBreweryDto.getIs_regular_visit() != null){
            brewery.updateRegularVisit(reqBreweryDto.getIs_regular_visit());
        }

        // 운영 시간대 수정 사항 존재 시 반영
        if(reqBreweryDto.getStart_time() != null){
            LocalTime endTime = (reqBreweryDto.getEnd_time() == null) ? brewery.getEndTime() : reqBreweryDto.getEnd_time();
            if(reqBreweryDto.getStart_time().isAfter(endTime)) {
                throw new ApplicationException(ApplicationError.BREWERY_OPENING_TIME_INVALID);
            }
        }
        if(reqBreweryDto.getEnd_time() != null){
            LocalTime startTime = (reqBreweryDto.getStart_time() == null) ? brewery.getStartTime() : reqBreweryDto.getStart_time();
            if(startTime.isAfter(reqBreweryDto.getEnd_time())) {
                throw new ApplicationException(ApplicationError.BREWERY_OPENING_TIME_INVALID);
            }
        }
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

    @Transactional
    public void breweryRestore(Long userId, VerifyAuthDto restoreRequestDto) {
        Users users = usersRepository.findById(userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.USER_NOT_FOUND));
        if(!bCryptPasswordEncoder.matches(restoreRequestDto.getPassword(), users.getPassword())) {
            throw new ApplicationException(ApplicationError.NOT_MATCH_CUR_PASSWORD);
        }
        Brewery brewery = breweryRepository.findByUserId(userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.BREWERY_NOT_FOUND));
        brewery.unSetDeleted();
    }


    // 양조장 개별 상세 조회: 양조장 모든 이미지, 양조장 소개글, 주소, 주종 태그, 연락처(회원 entity), 이메일(회원 entity), 홈페이지 링크
    // 체험 프로그램 리스트 반환 api: 체험 이름, 체험 장소, 체험 내용, 체험 인당 비용(단위: 원), 매진 여부
        // 삭제처리된 체험 프로그램은 취급하지 않음.
    // 판매 상품 조회 api: 상품 이미지, 상품 이름, 상품 평점, 상품 리뷰 수, 상품 도수(double), 상품 용량, 상품 가격(단위: 원)
    // 체험 리뷰 조회 api: 체험 평점, 체험 총 리뷰 수, 해당 체험의 리뷰의 총 좋아요 수, 해당 체험 리뷰의 총 조회수
        // 리뷰 대표 이미지, 리뷰 제목, 리뷰 평가 점수, 리뷰 본문, 리뷰 작성자 유저네임, 리뷰 작성일자, 체험 장소, 해당 리뷰 조회수, 해당 리뷰 좋아요 수, 해당 리뷰 댓글 수
            // 해당 리뷰 태그 종, 해당 리뷰 이미지 수

    // 페이징 단위: 12개
    @Transactional(readOnly = true)
    public Page<ResBreweryListDto> getFilteringSearch(Integer startOffset, String keyword, Integer minPrice, Integer maxPrice,
                                                      List<Integer> tagIdList, List<Integer> regionIdList) {
        if (startOffset == null) {
            startOffset = 0;
        }
        // 플래그 계산
        boolean tagListIsEmpty = tagIdList == null || tagIdList.isEmpty();
        boolean regionListIsEmpty = regionIdList == null || regionIdList.isEmpty();

        // 1. 필터링과 페이징을 적용해서 양조장 조회
        Sort sort = Sort.by(Sort.Direction.DESC, "registeredAt"); // 정렬 기준: 등록일자 기준으로 내림차순 정렬
        Pageable pageable = PageRequest.of(startOffset, BREWERY_PAGE_SIZE, sort);
        Page<ResBreweryListDto> result = breweryRepository.findByDynamicFiltering(pageable, tagListIsEmpty, regionListIsEmpty,
                keyword, minPrice, maxPrice, tagIdList, regionIdList);
        if(result.isEmpty()) {
            throw new ApplicationException(ApplicationError.BREWERY_NOT_FOUND);
        }

        // 2. 각 양조장의 태그 정보를 조회하기 위해 추가적인 쿼리문 실행
        addTagListToResult(result);

        return result;
    }

    // 최신순 조회: 필터링 조회와 로직 흡사
    public Page<ResBreweryListDto> getLatest(Integer startOffset) {
        if(startOffset == null) {
            startOffset = 0;
        }
        Sort sort = Sort.by(Sort.Direction.DESC, "registeredAt");
        Pageable pageable = PageRequest.of(startOffset, BREWERY_PAGE_SIZE, sort);
        Page<ResBreweryListDto> result = breweryRepository.findBreweryLatest(pageable);
        if(result.isEmpty()) {
            throw new ApplicationException(ApplicationError.BREWERY_NOT_FOUND);
        }

        addTagListToResult(result);
        return result;
    }

    public ResBreweryDto getBreweryById(Long breweryId) {
        Brewery brewery = breweryRepository.findActiveById(breweryId).orElseThrow(() ->
                new ApplicationException(ApplicationError.BREWERY_NOT_FOUND));
        ResBreweryDto result = ResBreweryDto.activeBreweryFrom(brewery);
        result.setBrewery_image_image_key(breweryImageRepository.findImageKeyByBrewery(brewery.getId()));
        result.setTags_name(breweryTagRepository.findTagListByBreweryId(breweryId));
        List<Joy> joyList = joyRepository.findActiveByBreweryId(breweryId);
        result.setJoy(joyList.stream().map(ResJoyDto::joyFrom).toList());
        try{
            result.setProduct_list(productService.getProductByUserId(brewery.getUser().getId(), 0));
        } catch (ApplicationException e) {
            Pageable pageable = PageRequest.of(0, productService.getPRODUCT_PAGE_SIZE());
            result.setProduct_list(Page.empty(pageable));
        }

        return result;
    }

}
