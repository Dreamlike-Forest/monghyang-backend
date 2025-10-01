package com.example.monghyang.domain.joy.service;

import com.example.monghyang.domain.joy.dto.ReqJoyDto;
import com.example.monghyang.domain.joy.dto.ReqUpdateJoyDto;
import com.example.monghyang.domain.joy.entity.Joy;
import com.example.monghyang.domain.joy.repository.JoyRepository;
import com.example.monghyang.domain.brewery.entity.Brewery;
import com.example.monghyang.domain.brewery.repository.BreweryRepository;
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.image.service.ImageType;
import com.example.monghyang.domain.image.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class JoyService {
    private final JoyRepository joyRepository;
    private final BreweryRepository breweryRepository;
    private final StorageService storageService;

    @Autowired
    public JoyService(JoyRepository joyRepository, BreweryRepository breweryRepository, StorageService storageService) {
        this.joyRepository = joyRepository;
        this.breweryRepository = breweryRepository;
        this.storageService = storageService;
    }

    // 체험 등록
    @Transactional
    public void createJoy(Long userId, ReqJoyDto reqJoyDto) {
        Brewery brewery = breweryRepository.findByUserId(userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.BREWERY_NOT_FOUND));
        String imageKey = null;
        if(reqJoyDto.getImage() != null) {
            imageKey = storageService.upload(reqJoyDto.getImage(), ImageType.JOY_IMAGE);
        }
        Joy joy = Joy.joyBuilder()
                .brewery(brewery).name(reqJoyDto.getName())
                .place(reqJoyDto.getPlace()).detail(reqJoyDto.getDetail())
                .originPrice(reqJoyDto.getOrigin_price()).timeUnit(reqJoyDto.getTime_unit())
                .imageKey(imageKey)
                .build();
        joyRepository.save(joy);
        if(brewery.getJoyCount() == 0) {
            brewery.updateMinJoyPrice(joy.getFinalPrice());
        } else if(joy.getFinalPrice().compareTo(brewery.getMinJoyPrice()) < 0){
            // 양조장 최소 체험 가격 갱신 여부 검증 후 갱신
            brewery.updateMinJoyPrice(joy.getFinalPrice());
        }

        brewery.increaseJoyCount(); // 양조장의 체험 개수 카운트 1 증가
    }

    // 체험 삭제 처리
    public void deleteJoy(Long userId, Long joyId) {
        Brewery brewery = breweryRepository.findByUserId(userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.BREWERY_NOT_FOUND));
        Joy joy = joyRepository.findByBreweryIdAndJoyId(brewery.getId(), joyId).orElseThrow(() ->
                new ApplicationException(ApplicationError.JOY_NOT_FOUND));
        joy.setDeleted();
        joyRepository.save(joy);
        brewery.decreaseJoyCount(); // 양조장의 체험 개수 카운트 1 감소
    }

    // 체험 삭제 복구
    public void restoreJoy(Long userId, Long joyId) {
        Brewery brewery = breweryRepository.findByUserId(userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.BREWERY_NOT_FOUND));
        Joy joy = joyRepository.findByBreweryIdAndJoyId(brewery.getId(), joyId).orElseThrow(() ->
                new ApplicationException(ApplicationError.JOY_NOT_FOUND));
        joy.unSetDeleted();
        joyRepository.save(joy);
    }

    // 체험 수정(가격 및 할인율, 기타 체험 정보, 매진 처리 등)
    @Transactional
    public void updateJoy(Long userId, ReqUpdateJoyDto reqUpdateJoyDto) {
        Brewery brewery = breweryRepository.findByUserId(userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.BREWERY_NOT_FOUND));
        Joy joy = joyRepository.findByBreweryIdAndJoyId(brewery.getId(), reqUpdateJoyDto.getId()).orElseThrow(() ->
                new ApplicationException(ApplicationError.JOY_NOT_FOUND));
        if(reqUpdateJoyDto.getImage() != null) {
            storageService.remove(joy.getImageKey());
            String newImageKey = storageService.upload(reqUpdateJoyDto.getImage(), ImageType.JOY_IMAGE);
            joy.updateImageKey(newImageKey);
        }
        if(reqUpdateJoyDto.getName() != null) {
            joy.updateName(reqUpdateJoyDto.getName());
        }
        if(reqUpdateJoyDto.getPlace() != null) {
            joy.updatePlace(reqUpdateJoyDto.getPlace());
        }
        if(reqUpdateJoyDto.getDetail() != null) {
            joy.updateDetail(reqUpdateJoyDto.getDetail());
        }
        if(reqUpdateJoyDto.getOrigin_price() != null) {
            joy.updateOriginPrice(reqUpdateJoyDto.getOrigin_price());
        }
        if(reqUpdateJoyDto.getDiscount_rate() != null) {
            joy.updateDiscountRate(reqUpdateJoyDto.getDiscount_rate());
        }
        if(reqUpdateJoyDto.getIs_soldout() != null) {
            joy.updateSoldout(reqUpdateJoyDto.getIs_soldout());
        }
        if(reqUpdateJoyDto.getTime_unit() != null) {
            joy.updateTimeUnit(reqUpdateJoyDto.getTime_unit());
        }
    }
}
