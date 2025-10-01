package com.example.monghyang.domain.global.pg.gateway;

import com.example.monghyang.domain.global.pg.component.Account;
import com.example.monghyang.domain.global.pg.component.Company;
import com.example.monghyang.domain.global.pg.component.Individual;
import com.example.monghyang.domain.global.pg.status.PGSellerStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PGSeller { // 오픈마켓에 입점되어 있는 셀러의 사업자, 계좌 정보
    // 토스페이먼츠에서 발급하는 셀러의 고유 식별자 (셀러 조회, 삭제, 지급대행 요청에 사용)
    private String id;

    // 오픈마켓 상점에서 직접 등록하는 셀러의 고유 식별자 (등록 이후 수정 불가)
    private String refSellerId;

    // 사업자 유형: INDIVIDUAL(개인), INDIVIDUAL_BUSINESS(개인사업자), CORPORATE(법인사업자)
    private String businessType;

    // 법인사업자 또는 개인사업자 셀러의 정보
    private Company company;

    // 개인 셀러의 정보
    private Individual individual;

    // 셀러 상태 (APPROVAL_REQUIRED, PARTIALLY_APPROVED, KYC_REQUIRED, APPROVED)
    private PGSellerStatus status;

    // 셀러가 정산금을 지급받을 계좌 정보
    private Account account;

    // 셀러와 관련된 추가 정보 (최대 5개의 키-값 쌍)
    private Map<String, String> metadata;
}
