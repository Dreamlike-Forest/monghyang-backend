package com.example.monghyang.domain.auth.service;

import com.example.monghyang.domain.auth.dto.BreweryJoinDto;
import com.example.monghyang.domain.auth.dto.VerifyAuthDto;
import com.example.monghyang.domain.brewery.entity.Brewery;
import com.example.monghyang.domain.brewery.entity.BreweryImage;
import com.example.monghyang.domain.brewery.entity.RegionType;
import com.example.monghyang.domain.brewery.repository.BreweryImageRepository;
import com.example.monghyang.domain.brewery.repository.BreweryRepository;
import com.example.monghyang.domain.brewery.repository.RegionTypeRepository;
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.image.dto.AddImageDto;
import com.example.monghyang.domain.image.service.ImageType;
import com.example.monghyang.domain.image.service.StorageService;
import com.example.monghyang.domain.redis.RedisService;
import com.example.monghyang.domain.seller.entity.Seller;
import com.example.monghyang.domain.auth.dto.JoinDto;
import com.example.monghyang.domain.auth.dto.SellerJoinDto;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
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

    @Autowired
    public AuthService(UsersRepository usersRepository, BCryptPasswordEncoder bCryptPasswordEncoder, RoleRepository roleRepository, JwtUtil jwtUtil, SessionUtil sessionUtil, RedisService redisService, SellerRepository sellerRepository, BreweryRepository breweryRepository, RegionTypeRepository regionTypeRepository, StorageService storageService, BreweryImageRepository breweryImageRepository) {
        this.usersRepository = usersRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.roleRepository = roleRepository;
        this.jwtUtil = jwtUtil;
        this.sessionUtil = sessionUtil;
        this.redisService = redisService;
        this.sellerRepository = sellerRepository;
        this.breweryRepository = breweryRepository;
        this.regionTypeRepository = regionTypeRepository;
        this.storageService = storageService;
        this.breweryImageRepository = breweryImageRepository;
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
        if(refreshToken == null) {
            throw new ApplicationException(ApplicationError.TOKEN_EXPIRED);
        }

        JwtClaimsDto jwtClaimsDto = jwtUtil.parseRefreshToken(refreshToken);
        Long userId = jwtClaimsDto.getUserId();
        String deviceType = jwtClaimsDto.getDeviceType();
        String tid = jwtClaimsDto.getTid();
        String role = jwtClaimsDto.getRole();

        if(!redisService.verifyRefreshTokenTid(userId, deviceType, tid)) {
            // 동시접속 감지 기준: 해당 유저의 해당 디바이스 타입의 리프레시 토큰 tid와 요청에 담긴 RT의 tid가 동일하지 않은 경우
            throw new ApplicationException(ApplicationError.CONCURRENT_CONNECTION);
        }

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
    }

    @Transactional
    public void breweryJoin(BreweryJoinDto breweryJoinDto) {
        if(breweryJoinDto.getStart_time().isAfter(breweryJoinDto.getEnd_time())) {
            // 양조장 운영 종료 시간대가 운영 시작 시간대보다 더 크지 않다면 예외 발생
            throw new ApplicationException(ApplicationError.BREWERY_OPENING_TIME_INVALID);
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
                .startTime(breweryJoinDto.getStart_time()).endTime(breweryJoinDto.getEnd_time())
                .build();
        breweryRepository.save(brewery);

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
