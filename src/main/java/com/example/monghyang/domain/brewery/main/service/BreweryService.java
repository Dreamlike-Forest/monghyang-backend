package com.example.monghyang.domain.brewery.main.service;

import com.example.monghyang.domain.auth.dto.QuitRequestDto;
import com.example.monghyang.domain.brewery.main.entity.Brewery;
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.image.dto.ImageUpdateDto;
import com.example.monghyang.domain.brewery.main.repository.BreweryRepository;
import com.example.monghyang.domain.users.entity.Users;
import com.example.monghyang.domain.users.repository.UsersRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class BreweryService {
    private final BreweryRepository breweryRepository;
    private final UsersRepository usersRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public BreweryService(BreweryRepository breweryRepository, UsersRepository usersRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.breweryRepository = breweryRepository;
        this.usersRepository = usersRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    // 양조장 검색 (필터링 옵션 종류: 지역 타입, 가격 범위, 주종(태그), 배지(태그)): 이때 각 양조장의 이미지는 '대표 이미지'만 조회되도록
    // 삭제처리된 양조장은 취급하지 않음


    // 양조장 수정 api
        // 'brewery' 엔티티의 내용만 수정할 수 있다.
        // 'users' 엔티티와 겹치는 컬럼(주소, 주소상세, 상호명
        // 동일한 이미지 순서값을 여러 개 받을 경우(ex: 순서가 1인 이미지를 2개 이상 받는 경우) DB 레벨 예외 발생 -> 처리 로직 필요
        // 로직을 실제로 수행하기 전 애플리케이션 레벨에서 '삭제 리스트 삭제' -> '추가 리스트 추가'를 실행한 시나리오를 실행하여 이미지 개수가 5개 이하인지 검증


    // 양조장 탈퇴(비활성화) api
    @Transactional
    public void breweryQuit(Long userId, QuitRequestDto quitRequestDto) {
        Users users = usersRepository.findById(userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.USER_NOT_FOUND));
        if(!bCryptPasswordEncoder.matches(quitRequestDto.getPassword(), users.getPassword())) {
            throw new ApplicationException(ApplicationError.NOT_MATCH_CUR_PASSWORD);
        }
        Brewery brewery = breweryRepository.findByUser(users).orElseThrow(() ->
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



}
