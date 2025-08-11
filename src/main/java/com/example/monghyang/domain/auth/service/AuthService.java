package com.example.monghyang.domain.auth.service;

import com.example.monghyang.domain.auth.dto.BreweryJoinDto;
import com.example.monghyang.domain.brewery.main.entity.Brewery;
import com.example.monghyang.domain.brewery.main.entity.RegionType;
import com.example.monghyang.domain.brewery.main.repository.BreweryRepository;
import com.example.monghyang.domain.brewery.main.repository.RegionTypeRepository;
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
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

    @Autowired
    public AuthService(UsersRepository usersRepository, BCryptPasswordEncoder bCryptPasswordEncoder, RoleRepository roleRepository, JwtUtil jwtUtil, SessionUtil sessionUtil, RedisService redisService, SellerRepository sellerRepository, BreweryRepository breweryRepository, RegionTypeRepository regionTypeRepository) {
        this.usersRepository = usersRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.roleRepository = roleRepository;
        this.jwtUtil = jwtUtil;
        this.sessionUtil = sessionUtil;
        this.redisService = redisService;
        this.sellerRepository = sellerRepository;
        this.breweryRepository = breweryRepository;
        this.regionTypeRepository = regionTypeRepository;
    }

    public void checkEmail(String email) {
        if(usersRepository.existsByEmail(email)) {
            throw new ApplicationException(ApplicationError.EMAIL_DUPLICATE);
        }
    }

    // RT을 이용한 세션 및 RT 갱신
    @Transactional
    public void updateRefreshToken(HttpServletRequest request, HttpServletResponse response) {
        // 토큰에서 userid, devicetype 추출해서 세션 및 토큰 갱신에 사용
        String refreshToken = request.getHeader("X-Refresh-Token");

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
                .user(users).sellerName(sellerJoinDto.getName())
                .sellerPhone(sellerJoinDto.getPhone()).sellerEmail(sellerJoinDto.getSeller_email())
                .sellerAddress(sellerJoinDto.getSeller_address()).sellerAddressDetail(sellerJoinDto.getSeller_address_detail())
                .businessRegistrationNumber(sellerJoinDto.getBusiness_registration_number())
                .sellerAccountNumber(sellerJoinDto.getSeller_account_number()).sellerDepositor(sellerJoinDto.getSeller_depositor())
                .sellerBankName(sellerJoinDto.getSeller_bank_name()).introduction(sellerJoinDto.getIntroduction())
                .isAgreedSeller(sellerJoinDto.getIs_agreed_seller()).build();
        sellerRepository.save(seller);
    }

    @Transactional
    public void breweryJoin(BreweryJoinDto breweryJoinDto) {
        // 양조장 회원 플랫폼 회원가입
        Users users = createUser(breweryJoinDto, RoleType.ROLE_BREWERY);
        usersRepository.save(users);

        RegionType regionType = regionTypeRepository.findById(breweryJoinDto.getRegion_type_id()).orElseThrow(() ->
                new ApplicationException(ApplicationError.REGION_NOT_FOUND));

        Brewery brewery = Brewery.breweryBuilder()
                .user(users).breweryName(breweryJoinDto.getBrewery_name()).regionType(regionType)
                .breweryPhone(breweryJoinDto.getBrewery_phone())
                .breweryEmail(breweryJoinDto.getEmail()).breweryAddress(breweryJoinDto.getBrewery_address())
                .breweryAddressDetail(breweryJoinDto.getBrewery_address_detail()).businessRegistrationNumber(breweryJoinDto.getBusiness_registration_number())
                .breweryDepositor(breweryJoinDto.getBrewery_depositor()).breweryAccountNumber(breweryJoinDto.getBrewery_account_number())
                .breweryBankName(breweryJoinDto.getBrewery_bank_name()).introduction(breweryJoinDto.getIntroduction())
                .breweryWebsite(breweryJoinDto.getBrewery_website()).isRegularVisit(breweryJoinDto.getIs_regular_visit()).isAgreedBrewery(breweryJoinDto.getIs_agreed_brewery())
                .build();
        breweryRepository.save(brewery);
    }
}
