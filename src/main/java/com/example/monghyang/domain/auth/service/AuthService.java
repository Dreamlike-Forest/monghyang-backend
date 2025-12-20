package com.example.monghyang.domain.auth.service;

import com.example.monghyang.domain.auth.dto.*;
import com.example.monghyang.domain.brewery.entity.*;
import com.example.monghyang.domain.brewery.repository.*;
import com.example.monghyang.domain.global.DayOfWeek;
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.image.dto.AddImageDto;
import com.example.monghyang.domain.image.service.ImageType;
import com.example.monghyang.domain.image.service.StorageService;
import com.example.monghyang.domain.redis.RedisService;
import com.example.monghyang.domain.seller.entity.Seller;
import com.example.monghyang.domain.seller.entity.SellerImage;
import com.example.monghyang.domain.seller.repository.SellerImageRepository;
import com.example.monghyang.domain.seller.repository.SellerRepository;
import com.example.monghyang.domain.users.entity.Role;
import com.example.monghyang.domain.users.entity.RoleType;
import com.example.monghyang.domain.users.entity.Users;
import com.example.monghyang.domain.users.repository.RoleRepository;
import com.example.monghyang.domain.users.repository.UsersRepository;
import com.example.monghyang.domain.util.JwtUtil;
import com.example.monghyang.domain.util.SessionUtil;
import com.example.monghyang.domain.util.dto.JwtClaimsDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    // 회원 가입 및 토큰 갱신에 관련된 서비스
    private final UsersRepository usersRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder; // 패스워드 암호화 모듈
    private final RoleRepository roleRepository;
    private final JwtUtil jwtUtil;
    private final RedisService redisService;
    private final SessionUtil sessionUtil;
    private final SellerRepository sellerRepository;
    private final BreweryRepository breweryRepository;
    private final RegionTypeRepository regionTypeRepository;
    private final StorageService storageService;
    private final BreweryImageRepository breweryImageRepository;
    private final SellerImageRepository sellerImageRepository;
    private final BreweryWeeklyOpenTimeRepository breweryWeeklyOpenTimeRepository;
    private final BreweryWeeklyBreakTimeRepository breweryWeeklyBreakTimeRepository;


    public void resetPassword(ReqResetPwDto dto) {
        String password = bCryptPasswordEncoder.encode(dto.getNewPassword());
        Users users = usersRepository.findByEmail(dto.getEmail()).orElseThrow(() ->
                new ApplicationException(ApplicationError.USER_NOT_FOUND));
        users.updatePassword(password);
        usersRepository.save(users);
    }

    public void checkEmail(String email) {
        if(usersRepository.existsByEmail(email)) {
            throw new ApplicationException(ApplicationError.EMAIL_DUPLICATE);
        }
    }

    public void checkPassword(Long userId, VerifyAuthDto verifyAuthDto) {
        Users users = usersRepository.findById(userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.USER_NOT_FOUND));
        if(!bCryptPasswordEncoder.matches(verifyAuthDto.getPassword(), users.getPassword())) {
            throw new ApplicationException(ApplicationError.NOT_MATCH_CUR_PASSWORD);
        }
    }

    // RT을 이용한 세션 및 RT 갱신
    @Transactional
    public void updateRefreshToken(HttpServletRequest request, HttpServletResponse response) {
        // 토큰에서 userid, devicetype 추출해서 세션 및 토큰 갱신에 사용
        String refreshToken = request.getHeader("X-Refresh-Token");
        if(refreshToken == null || refreshToken.isEmpty()) {
            throw new ApplicationException(ApplicationError.TOKEN_EXPIRED);
        }

        JwtClaimsDto jwtClaimsDto = jwtUtil.parseRefreshToken(refreshToken);
        Long userId = jwtClaimsDto.getUserId();
        String tid = jwtClaimsDto.getTid();
        String role = jwtClaimsDto.getRole();

        // 갱신 전의 refresh token, session 제거
        redisService.deleteRefreshTokenAndSession(userId, tid);

        // 세션 및 토큰 갱신
        sessionUtil.createNewAuthInfo(request, response, userId, role);
    }

    private Users createUser(JoinDto joinDto, RoleType roleType) {
        // 회원 엔티티 생성 공통 로직
        Role role = roleRepository.findByName(roleType).orElseThrow(() ->
                new ApplicationException(ApplicationError.ROLE_NOT_FOUND));
        String password = bCryptPasswordEncoder.encode(joinDto.getPassword()); // 비밀번호 해싱
        joinDto.setPassword(password);
        Boolean gender = (joinDto.getGender().equals("man")) ? Boolean.FALSE : Boolean.TRUE; // false: 남자, true: 여자

        return Users.generalBuilder().email(joinDto.getEmail())
                .password(password)
                .role(role)
                .nickname(joinDto.getNickname()).name(joinDto.getName())
                .phone(joinDto.getPhone()).birth(joinDto.getBirth())
                .gender(gender).address(joinDto.getAddress())
                .address_detail(joinDto.getAddress_detail())
                .isAgreed(joinDto.getIs_agreed()).build();
    }

    @Transactional
    public void commonJoin(JoinDto joinDto) {
        // 일반 회원 플랫폼 회원가입
        Users users = createUser(joinDto, RoleType.ROLE_USER);
        usersRepository.save(users);
    }

    @Transactional
    public void sellerJoin(SellerJoinDto sellerJoinDto) {
        if(!sellerJoinDto.getIs_agreed() || !sellerJoinDto.getIs_agreed_seller()) {
            throw new ApplicationException(ApplicationError.TERMS_AND_CONDITIONS_NOT_AGREED);
        }
        if(sellerJoinDto.getImages() != null && sellerJoinDto.getImages().size() > 5) {
            throw new ApplicationException(ApplicationError.IMAGE_SEQ_INVALID);
        }
        // 판매자 회원 플랫폼 회원가입
        Users users = createUser(sellerJoinDto, RoleType.ROLE_SELLER);
        usersRepository.save(users);
        Seller seller = Seller.sellerBuilder()
                .user(users).sellerName(sellerJoinDto.getNickname())
                .sellerAddress(sellerJoinDto.getAddress()).sellerAddressDetail(sellerJoinDto.getAddress_detail())
                .businessRegistrationNumber(sellerJoinDto.getBusiness_registration_number())
                .sellerAccountNumber(sellerJoinDto.getSeller_account_number()).sellerDepositor(sellerJoinDto.getSeller_depositor())
                .sellerBankName(sellerJoinDto.getSeller_bank_name()).introduction(sellerJoinDto.getIntroduction())
                .isAgreedSeller(sellerJoinDto.getIs_agreed_seller()).build();
        sellerRepository.save(seller);

        // 판매자 이미지 추가 로직
        if(sellerJoinDto.getImages() != null) {
            for(AddImageDto image : sellerJoinDto.getImages()) {
                Integer seq = image.getSeq();
                if(seq == null) {
                    // 이미지 순서 정보 누락되면 업로드 로직 수행 x
                    throw new ApplicationException(ApplicationError.IMAGE_SEQ_NULL);
                } else if(seq > 5 || seq < 1) {
                    throw new ApplicationException(ApplicationError.IMAGE_SEQ_INVALID);
                }
                String imageKey = storageService.upload(image.getImage(), ImageType.SELLER_IMAGE);
                Long volume = image.getImage().getSize();
                try {
                    sellerImageRepository.save(SellerImage.sellerKeySeqVolume(seller, imageKey, seq, volume));
                } catch (DataIntegrityViolationException e) {
                    // 중복된 seq 정보 존재할 경우 db insert 시 uk 제약조건 위배 예외 발생
                    throw new ApplicationException(ApplicationError.IMAGE_SEQ_INVALID);
                }

            }
        }
    }

    @Transactional
    public void breweryJoin(BreweryJoinDto breweryJoinDto) {
        if(!breweryJoinDto.getIs_agreed_brewery() || !breweryJoinDto.getIs_agreed()) {
            // 약관에 모두 동의하지 않으면 가입 불가
            throw new ApplicationException(ApplicationError.TERMS_AND_CONDITIONS_NOT_AGREED);
        }
        if(breweryJoinDto.getSchedules() == null) {
            // 양조장 운영 시간대 정보가 존재하지 않는다면 예외 발생
            throw new ApplicationException(ApplicationError.BREWERY_SCHEDULE_NOT_FOUND);
        }
        if(breweryJoinDto.getImages() != null && breweryJoinDto.getImages().size() > 5) {
            throw new ApplicationException(ApplicationError.IMAGE_SEQ_INVALID);
        }

        Set<DayOfWeek> dayOfWeekSet = new HashSet<>(); // 요일 별로 하나의 스케줄 정보만 입력받기 위한 검증용 set
        // 양조장 엔티티 생성 전 검증하는 이유: 무결성 검증으로 인한 DB 롤백을 최소화하기 위함
        for(BreweryScheduleDto schedule : breweryJoinDto.getSchedules()) {
            if(dayOfWeekSet.contains(schedule.getDay_of_week())) {
                throw new ApplicationException(ApplicationError.BREWERY_OPENING_TIME_INVALID);
            }
            if(schedule.getBreak_start() != null && schedule.getBreak_end() != null) {
                if(schedule.getBreak_start().isBefore(schedule.getOpen_time()) || schedule.getBreak_end().isAfter(schedule.getClose_time())) {
                    // 휴게 시간 범위가 양조장 운영 시간 범위를 벗어나는 경우 예외 발생
                    throw new ApplicationException(ApplicationError.BREWERY_OPENING_TIME_INVALID);
                }
            }
            // 요일 당 1번의 운영/휴게시간 정보 입력만 허용하기 위해 set을 통해 검증
            dayOfWeekSet.add(schedule.getDay_of_week());
        }

        // 양조장 회원 플랫폼 회원가입
        Users users = createUser(breweryJoinDto, RoleType.ROLE_BREWERY);
        usersRepository.save(users);

        RegionType regionType = regionTypeRepository.findById(breweryJoinDto.getRegion_type_id()).orElseThrow(() ->
                new ApplicationException(ApplicationError.REGION_NOT_FOUND));

        Brewery brewery = Brewery.breweryBuilder()
                .user(users).breweryName(breweryJoinDto.getNickname()).regionType(regionType)
                .breweryAddress(breweryJoinDto.getAddress())
                .breweryAddressDetail(breweryJoinDto.getAddress_detail()).businessRegistrationNumber(breweryJoinDto.getBusiness_registration_number())
                .breweryDepositor(breweryJoinDto.getBrewery_depositor()).breweryAccountNumber(breweryJoinDto.getBrewery_account_number())
                .breweryBankName(breweryJoinDto.getBrewery_bank_name()).introduction(breweryJoinDto.getIntroduction())
                .breweryWebsite(breweryJoinDto.getBrewery_website()).isRegularVisit(breweryJoinDto.getIs_regular_visit()).isAgreedBrewery(breweryJoinDto.getIs_agreed_brewery())
                .build();
        breweryRepository.save(brewery);

        for(BreweryScheduleDto schedule : breweryJoinDto.getSchedules()) {
            // 적용 일자: 가입 일자
            // 요일별 양조장 운영시간 insert
            breweryWeeklyOpenTimeRepository.save(BreweryWeeklyOpenTime.builder()
                    .brewery(brewery)
                    .dayOfWeek(schedule.getDay_of_week())
                    .openTime(schedule.getOpen_time())
                    .closeTime(schedule.getClose_time())
                    .effectiveDate(LocalDate.now())
                    .build());
            // 요일별 양조장 휴게시간 insert
            if(schedule.getBreak_start() != null && schedule.getBreak_end() != null) {
                breweryWeeklyBreakTimeRepository.save(BreweryWeeklyBreakTime.builder()
                        .brewery(brewery)
                        .dayOfWeek(schedule.getDay_of_week())
                        .breakStart(schedule.getBreak_start())
                        .breakEnd(schedule.getBreak_end())
                        .effectiveDate(LocalDate.now())
                        .build());
            }
        }

        // 양조장 이미지 추가 로직
        if(breweryJoinDto.getImages() != null) {
            for(AddImageDto image : breweryJoinDto.getImages()) {
                Integer seq = image.getSeq();
                if(seq == null) {
                    // 이미지 순서 정보 누락되면 업로드 로직 수행 x
                    throw new ApplicationException(ApplicationError.IMAGE_SEQ_NULL);
                } else if(seq > 5 || seq < 1) {
                    throw new ApplicationException(ApplicationError.IMAGE_SEQ_INVALID);
                }
                String imageKey = storageService.upload(image.getImage(), ImageType.BREWERY_IMAGE);
                Long volume = image.getImage().getSize();
                try {
                    breweryImageRepository.save(BreweryImage.breweryKeySeqVolume(brewery, imageKey, seq, volume));
                } catch (DataIntegrityViolationException e) {
                    // 중복된 seq 정보 존재할 경우 db insert 시 uk 제약조건 위배 예외 발생
                    throw new ApplicationException(ApplicationError.IMAGE_SEQ_INVALID);
                }

            }
        }
    }

}
