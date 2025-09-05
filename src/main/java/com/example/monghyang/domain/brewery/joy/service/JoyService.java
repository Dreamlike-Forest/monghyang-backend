package com.example.monghyang.domain.brewery.joy.service;

import com.example.monghyang.domain.brewery.joy.dto.ReqJoyDto;
import com.example.monghyang.domain.brewery.joy.dto.ReqUpdateJoyDto;
import com.example.monghyang.domain.brewery.joy.dto.ResJoyDto;
import com.example.monghyang.domain.brewery.joy.entity.Joy;
import com.example.monghyang.domain.brewery.joy.repository.JoyRepository;
import com.example.monghyang.domain.brewery.main.entity.Brewery;
import com.example.monghyang.domain.brewery.main.repository.BreweryRepository;
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class JoyService {
    private final JoyRepository joyRepository;
    private final BreweryRepository breweryRepository;

    @Autowired
    public JoyService(JoyRepository joyRepository, BreweryRepository breweryRepository) {
        this.joyRepository = joyRepository;
        this.breweryRepository = breweryRepository;
    }

    // 체험 등록
    @Transactional
    public void createJoy(Long userId, ReqJoyDto reqJoyDto) {
        Brewery brewery = breweryRepository.findByUserId(userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.BREWERY_NOT_FOUND));
        Joy joy = Joy.joyBuilder()
                .brewery(brewery).name(reqJoyDto.getName())
                .place(reqJoyDto.getPlace()).detail(reqJoyDto.getDetail())
                .originPrice(reqJoyDto.getOrigin_price()).build();
        joyRepository.save(joy);
        if(joy.getFinalPrice() < brewery.getMinJoyPrice()) {
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

        if(joy.getFinalPrice() < brewery.getMinJoyPrice()) {
            // 양조장 최소 체험 가격 갱신 여부 검증 후 갱신
            brewery.updateMinJoyPrice(joy.getFinalPrice());
        }
    }
}
