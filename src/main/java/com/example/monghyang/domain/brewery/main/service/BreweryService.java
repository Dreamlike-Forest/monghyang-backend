package com.example.monghyang.domain.brewery.main.service;

import com.example.monghyang.domain.image.dto.ImageUpdateDto;
import com.example.monghyang.domain.brewery.main.repository.BreweryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BreweryService {
    private final BreweryRepository breweryRepository;
    @Autowired
    public BreweryService(BreweryRepository breweryRepository) {
        this.breweryRepository = breweryRepository;
    }

    // 양조장 검색 (필터링 옵션 종류: 지역 타입, 가격 범위, 주종(태그), 배지(태그)): 이때 각 양조장의 이미지는 '대표 이미지'만 조회되도록
    // 삭제처리된 양조장은 취급하지 않음


    // 양조장 개별 상세 조회: 양조장 모든 이미지, 양조장 소개글, 주소, 주종 태그, 연락처(회원 entity), 이메일(회원 entity), 홈페이지 링크
    // 체험 프로그램 리스트 반환 api: 체험 이름, 체험 장소, 체험 내용, 체험 인당 비용(단위: 원), 매진 여부
        // 삭제처리된 체험 프로그램은 취급하지 않음.
    // 판매 상품 조회 api: 상품 이미지, 상품 이름, 상품 평점, 상품 리뷰 수, 상품 도수(double), 상품 용량, 상품 가격(단위: 원)
    // 체험 리뷰 조회 api: 체험 평점, 체험 총 리뷰 수, 해당 체험의 리뷰의 총 좋아요 수, 해당 체험 리뷰의 총 조회수
        // 리뷰 대표 이미지, 리뷰 제목, 리뷰 평가 점수, 리뷰 본문, 리뷰 작성자 유저네임, 리뷰 작성일자, 체험 장소, 해당 리뷰 조회수, 해당 리뷰 좋아요 수, 해당 리뷰 댓글 수
            // 해당 리뷰 태그 종, 해당 리뷰 이미지 수



}
