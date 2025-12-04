package com.example.monghyang.devtest;

import com.example.monghyang.domain.auth.dto.*;
import com.example.monghyang.domain.auth.service.AuthService;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    UsersRepository usersRepository;
    @Mock
    BCryptPasswordEncoder bCryptPasswordEncoder; // 패스워드 암호화 모듈
    @Mock
    RoleRepository roleRepository;
    @Mock
    JwtUtil jwtUtil;
    @Mock
    RedisService redisService;
    @Mock
    SessionUtil sessionUtil;
    @Mock
    SellerRepository sellerRepository;
    @Mock
    BreweryRepository breweryRepository;
    @Mock
    RegionTypeRepository regionTypeRepository;
    @Mock
    StorageService storageService;
    @Mock
    BreweryImageRepository breweryImageRepository;
    @Mock
    SellerImageRepository sellerImageRepository;
    @InjectMocks
    AuthService authService;

    @Test
    @DisplayName("commonJoin - 일반 회원 가입 시 ROLE_USER, 인코딩된 비밀번호, gender 매핑이 올바르게 저장되는 경우")
    void commonJoin_success() {
        JoinDto joinDto = new JoinDto();
        joinDto.setEmail("test@example.com");
        joinDto.setPassword("password");
        joinDto.setNickname("닉네임");
        joinDto.setName("홍길동");
        joinDto.setPhone("010-1111-1111");
        joinDto.setBirth(LocalDate.of(2001, 1, 1));
        joinDto.setGender("man");
        joinDto.setAddress("부산 어딘가");
        joinDto.setAddress_detail("000동 1111호");
        joinDto.setIs_agreed(true);

        Role role = new Role();
        role.setName(RoleType.ROLE_USER);
        given(roleRepository.findByName(RoleType.ROLE_USER))
                .willReturn(Optional.of(role));
        given(bCryptPasswordEncoder.encode("password"))
                .willReturn("encodedPW");

        ArgumentCaptor<Users> usersCaptor = ArgumentCaptor.forClass(Users.class);
        authService.commonJoin(joinDto); // when

        // then
        verify(roleRepository).findByName(RoleType.ROLE_USER);
        verify(bCryptPasswordEncoder).encode("password");
        verify(usersRepository).save(usersCaptor.capture());

        Users savedUsers = usersCaptor.getValue();

        assertEquals(savedUsers.getEmail(), "test@example.com");
        assertEquals(savedUsers.getPassword(), "encodedPW");
        assertEquals(savedUsers.getNickname(), "닉네임");
        assertEquals(savedUsers.getName(), "홍길동");
        assertEquals(savedUsers.getPhone(), "010-1111-1111");
        assertEquals(savedUsers.getBirth(), LocalDate.of(2001, 1, 1));
        assertEquals(savedUsers.getAddress(), "부산 어딘가");
        assertEquals(savedUsers.getAddressDetail(), "000동 1111호");
        assertEquals(savedUsers.getIsAgreed(), true);
        assertEquals(Boolean.FALSE, savedUsers.getGender());

        assertSame(role, savedUsers.getRole());
    }

    @Test
    @DisplayName("commonJoin - ROLE_USER가 존재하지 않으면 ApplicationError Throw")
    void commonJoin_roleNotFound() {
        JoinDto joinDto = new JoinDto();
        joinDto.setEmail("test@example.com");
        joinDto.setPassword("password");
        joinDto.setGender("woman");

        given(roleRepository.findByName(RoleType.ROLE_USER))
            .willReturn(Optional.empty());

        // when & then
        ApplicationException ex = assertThrows(
                ApplicationException.class,
                () -> authService.commonJoin(joinDto)
        );
        assertEquals(ApplicationError.ROLE_NOT_FOUND, ex.getApplicationError());
        verify(usersRepository, never()).save(any());
    }

    /**
     * Test용 SellerJoinDto 생성 후 반환
     * @return
     */
    private SellerJoinDto createSellerJoinDto() {
        SellerJoinDto joinDto = new SellerJoinDto();
        joinDto.setEmail("test@example.com");
        joinDto.setPassword("password");
        joinDto.setNickname("닉네임");
        joinDto.setName("홍길동");
        joinDto.setPhone("010-1111-1111");
        joinDto.setBirth(LocalDate.of(2001, 1, 1));
        joinDto.setGender("woman");
        joinDto.setAddress("부산 어딘가");
        joinDto.setAddress_detail("000동 1111호");
        joinDto.setIs_agreed(true);
        joinDto.setBusiness_registration_number("test business registration number");
        joinDto.setSeller_account_number("test seller account number");
        joinDto.setSeller_depositor("test seller depositor");
        joinDto.setSeller_bank_name("test seller bank name");
        joinDto.setIntroduction("test introduction");
        joinDto.setIs_agreed_seller(true);
        List<AddImageDto> imageList = new ArrayList<>();
        AddImageDto imageDto = new AddImageDto();
        imageDto.setSeq(1);
        MockMultipartFile mockImage = new MockMultipartFile(
                "image",
                "test.png",
                "image/png",
                "mock image bytes".getBytes()
        );
        imageDto.setImage(mockImage);
        imageList.add(imageDto);
        joinDto.setImages(imageList);
        return joinDto;
    }

    @Test
    @DisplayName("sellerJoin 판매자 회원가입 성공")
    void sellerJoin_success() {
        SellerJoinDto joinDto = createSellerJoinDto();
        List<AddImageDto> imageList = joinDto.getImages();
        AddImageDto imageDto = imageList.getFirst();

        Role role = new Role();
        role.setName(RoleType.ROLE_SELLER);
        given(roleRepository.findByName(RoleType.ROLE_SELLER))
                .willReturn(Optional.of(role));
        given(bCryptPasswordEncoder.encode("password"))
                .willReturn("encodedPW");
        given(storageService.upload(imageDto.getImage(), ImageType.SELLER_IMAGE))
                .willReturn("imageKey");

        ArgumentCaptor<Users> usersCaptor = ArgumentCaptor.forClass(Users.class);
        ArgumentCaptor<Seller> sellerCaptor = ArgumentCaptor.forClass(Seller.class);
        ArgumentCaptor<SellerImage> sellerImageCaptor = ArgumentCaptor.forClass(SellerImage.class);
        authService.sellerJoin(joinDto);

        verify(roleRepository).findByName(RoleType.ROLE_SELLER);
        verify(bCryptPasswordEncoder).encode("password");
        verify(usersRepository).save(usersCaptor.capture());
        verify(sellerRepository).save(sellerCaptor.capture());
        verify(sellerImageRepository).save(sellerImageCaptor.capture());
        verify(storageService).upload(imageDto.getImage(), ImageType.SELLER_IMAGE);

        Users savedUsers = usersCaptor.getValue();
        Seller savedSeller = sellerCaptor.getValue();
        SellerImage savedSellerImage = sellerImageCaptor.getValue();
        assertEquals(Boolean.TRUE, savedUsers.getGender());
        assertSame(savedUsers.getRole(), role);

        assertEquals(savedSeller.getBusinessRegistrationNumber(), "test business registration number");
        assertEquals(savedSeller.getSellerAccountNumber(), "test seller account number");
        assertEquals(savedSeller.getSellerDepositor(), "test seller depositor");
        assertEquals(savedSeller.getSellerBankName(), "test seller bank name");
        assertEquals(savedSeller.getIntroduction(), "test introduction");
        assertEquals(savedSeller.getIsAgreedSeller(), true);

        assertEquals(savedSellerImage.getImageKey(), "imageKey");
        assertEquals(savedSellerImage.getSeq(), 1);
        assertSame(savedSellerImage.getSeller(), savedSeller);
    }

    @Test
    @DisplayName("sellerJoin 이미지 없는 회원가입")
    void sellerJoin_No_Image() {
        SellerJoinDto joinDto = createSellerJoinDto();
        joinDto.setImages(null);

        Role role = new Role();
        role.setName(RoleType.ROLE_SELLER);
        given(roleRepository.findByName(RoleType.ROLE_SELLER))
                .willReturn(Optional.of(role));
        given(bCryptPasswordEncoder.encode("password"))
                .willReturn("encodedPW");

        ArgumentCaptor<Users> usersCaptor = ArgumentCaptor.forClass(Users.class);
        ArgumentCaptor<Seller> sellerCaptor = ArgumentCaptor.forClass(Seller.class);
        authService.sellerJoin(joinDto);

        verify(roleRepository).findByName(RoleType.ROLE_SELLER);
        verify(bCryptPasswordEncoder).encode("password");
        verify(usersRepository).save(usersCaptor.capture());
        verify(sellerRepository).save(sellerCaptor.capture());
        verify(storageService, never()).upload(any(), any());
        verify(sellerImageRepository, never()).save(any());

        Users savedUsers = usersCaptor.getValue();

        assertEquals(Boolean.TRUE, savedUsers.getGender());
        assertSame(savedUsers.getRole(), role);
    }
    @Test
    @DisplayName("sellerJoin - 잘못된 이미지 seq 값 입력")
    void sellerJoin_Invalid_Image_Seq() {
        SellerJoinDto joinDto = createSellerJoinDto();
        joinDto.getImages().getFirst().setSeq(6);

        Role role = new Role();
        given(roleRepository.findByName(RoleType.ROLE_SELLER))
                .willReturn(Optional.of(role));
        given(bCryptPasswordEncoder.encode("password"))
                .willReturn("encodedPW");

        ApplicationException ex = assertThrows(ApplicationException.class, () ->
                authService.sellerJoin(joinDto));
        assertEquals(ApplicationError.IMAGE_SEQ_INVALID, ex.getApplicationError());
    }
    @Test
    @DisplayName("sellerJoin - 이미지 seq null")
    void sellerJoin_Null_Image_Seq() {
        SellerJoinDto joinDto = createSellerJoinDto();
        joinDto.getImages().getFirst().setSeq(null);
        Role role = new Role();
        given(roleRepository.findByName(RoleType.ROLE_SELLER))
                .willReturn(Optional.of(role));
        given(bCryptPasswordEncoder.encode("password"))
                .willReturn("encodedPW");
        ApplicationException ex = assertThrows(ApplicationException.class, () ->
                authService.sellerJoin(joinDto));
        assertEquals(ApplicationError.IMAGE_SEQ_NULL, ex.getApplicationError());
    }
    @Test
    @DisplayName("sellerJoin - 이미지 파일 저장 db uk 무결성 위배")
    void sellerJoin_Image_save_DB_Error() {
        SellerJoinDto joinDto = createSellerJoinDto();
        List<AddImageDto> imageList = joinDto.getImages();
        AddImageDto imageDto = imageList.get(0);
        Role role = new Role();
        given(roleRepository.findByName(RoleType.ROLE_SELLER))
                .willReturn(Optional.of(role));
        given(bCryptPasswordEncoder.encode("password"))
                .willReturn("encodedPW");
        given(sellerImageRepository.save(any()))
                .willThrow(new DataIntegrityViolationException("test"));

        ApplicationException ex = assertThrows(ApplicationException.class, () ->
                authService.sellerJoin(joinDto));
        assertEquals(ApplicationError.IMAGE_SEQ_INVALID, ex.getApplicationError());
    }

    private Users createMockUsers() {
        return Users.generalBuilder()
                .email("test@example.com")
                .role(new Role()).password("PW").nickname("nickname")
                .name("name").phone("phone").birth(LocalDate.of(2000,1,1))
                .gender(true).address("address").address_detail("address_detail")
                .isAgreed(true).build();
    }
    @Test
    @DisplayName("비밀번호 변경")
    void rest_password() {
        ReqResetPwDto dto = new ReqResetPwDto();
        dto.setEmail("test@example.com");
        dto.setNewPassword("newPW");
        Users user = createMockUsers();
        given(bCryptPasswordEncoder.encode("newPW"))
                .willReturn("newEncodedPW");
        given(usersRepository.findByEmail("test@example.com"))
            .willReturn(Optional.of(user));
        given(usersRepository.save(user))
                .willReturn(user);

        ArgumentCaptor<Users> usersCaptor = ArgumentCaptor.forClass(Users.class);
        authService.resetPassword(dto);
        verify(bCryptPasswordEncoder).encode("newPW");
        verify(usersRepository).findByEmail("test@example.com");
        verify(usersRepository).save(usersCaptor.capture());

        Users savedUser = usersCaptor.getValue();
        assertEquals(savedUser.getPassword(), "newEncodedPW");
    }
    @Test
    @DisplayName("checkEmail - 이메일이 존재하지 않으면 예외 없이 통과한다")
    void checkEmail_notExists_success() {
        // given
        String email = "test@example.com";
        given(usersRepository.existsByEmail(email))
                .willReturn(false);

        // when & then (예외가 발생하지 않아야 함)
        authService.checkEmail(email);

        verify(usersRepository).existsByEmail(email);
    }
    @Test
    @DisplayName("checkEmail - 이메일이 이미 존재하면 EMAIL_DUPLICATE 예외를 던진다")
    void checkEmail_exists_throwsDuplicate() {
        // given
        String email = "test@example.com";
        given(usersRepository.existsByEmail(email))
                .willReturn(true);

        // when
        ApplicationException ex = assertThrows(
                ApplicationException.class,
                () -> authService.checkEmail(email)
        );

        // then
        assertEquals(ApplicationError.EMAIL_DUPLICATE, ex.getApplicationError());
        verify(usersRepository).existsByEmail(email);
    }
    @Test
    @DisplayName("checkPassword - 유저가 존재하고 비밀번호가 일치하면 예외 없이 통과한다")
    void checkPassword_success() {
        // given
        Long userId = 1L;
        Users user = createMockUsers(); // 비밀번호: "PW"
        VerifyAuthDto dto = new VerifyAuthDto();
        dto.setPassword("rawPw");       // 사용자가 입력한 비밀번호

        given(usersRepository.findById(userId))
                .willReturn(Optional.of(user));
        // rawPw 와 DB 비밀번호("PW")가 일치한다고 가정
        given(bCryptPasswordEncoder.matches("rawPw", "PW"))
                .willReturn(true);

        // when & then (예외 없어야 함)
        authService.checkPassword(userId, dto);

        verify(usersRepository).findById(userId);
        verify(bCryptPasswordEncoder).matches("rawPw", "PW");
    }
    @Test
    @DisplayName("checkPassword - userId로 유저를 찾지 못하면 USER_NOT_FOUND 예외를 던진다")
    void checkPassword_userNotFound() {
        // given
        Long userId = 1L;
        VerifyAuthDto dto = new VerifyAuthDto();
        dto.setPassword("rawPw");

        given(usersRepository.findById(userId))
                .willReturn(Optional.empty());

        // when
        ApplicationException ex = assertThrows(
                ApplicationException.class,
                () -> authService.checkPassword(userId, dto)
        );

        // then
        assertEquals(ApplicationError.USER_NOT_FOUND, ex.getApplicationError());
        verify(usersRepository).findById(userId);
        verify(bCryptPasswordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("checkPassword - 비밀번호가 일치하지 않으면 NOT_MATCH_CUR_PASSWORD 예외를 던진다")
    void checkPassword_notMatch() {
        // given
        Long userId = 1L;
        Users user = createMockUsers(); // DB 비밀번호: "PW"
        VerifyAuthDto dto = new VerifyAuthDto();
        dto.setPassword("wrongPw");     // 틀린 비밀번호

        given(usersRepository.findById(userId))
                .willReturn(Optional.of(user));
        // wrongPw 와 "PW"가 일치하지 않는다고 가정
        given(bCryptPasswordEncoder.matches("wrongPw", "PW"))
                .willReturn(false);

        // when
        ApplicationException ex = assertThrows(
                ApplicationException.class,
                () -> authService.checkPassword(userId, dto)
        );

        // then
        assertEquals(ApplicationError.NOT_MATCH_CUR_PASSWORD, ex.getApplicationError());
        verify(usersRepository).findById(userId);
        verify(bCryptPasswordEncoder).matches("wrongPw", "PW");
    }

    private BreweryJoinDto createBreweryJoinDto() {
        BreweryJoinDto dto = new BreweryJoinDto();
        dto.setEmail("test@example.com");
        dto.setPassword("password");
        dto.setNickname("양조장닉");
        dto.setName("홍길동");
        dto.setPhone("010-1111-1111");
        dto.setBirth(LocalDate.of(2001, 1, 1));
        dto.setGender("woman");
        dto.setAddress("부산 어딘가");
        dto.setAddress_detail("000동 1111호");
        dto.setIs_agreed(true);

        dto.setBusiness_registration_number("brewery business reg no");
        dto.setBrewery_depositor("brewery depositor");
        dto.setBrewery_account_number("brewery account number");
        dto.setBrewery_bank_name("brewery bank name");
        dto.setIntroduction("brewery introduction");
        dto.setBrewery_website("https://brewery.test");
        dto.setIs_regular_visit(true);
        dto.setIs_agreed_brewery(true);

        dto.setRegion_type_id(1); // RegionType id 1 가정

        // 영업 시작/종료 시간 (start < end)
        dto.setStart_time(LocalTime.of(9, 0));
        dto.setEnd_time(LocalTime.of(18, 0));

        // 이미지 1장 추가
        List<AddImageDto> imageList = new ArrayList<>();
        AddImageDto imageDto = new AddImageDto();
        imageDto.setSeq(1);
        MockMultipartFile mockImage = new MockMultipartFile(
                "image",
                "brewery.png",
                "image/png",
                "mock brewery image bytes".getBytes()
        );
        imageDto.setImage(mockImage);
        imageList.add(imageDto);
        dto.setImages(imageList);

        return dto;
    }
    @Test
    @DisplayName("breweryJoin - 양조장 회원가입 성공 (이미지 1장 포함)")
    void breweryJoin_success() {
        // given
        BreweryJoinDto joinDto = createBreweryJoinDto();
        AddImageDto imageDto = joinDto.getImages().getFirst();

        // ROLE_BREWERY 조회
        Role role = new Role();
        role.setName(RoleType.ROLE_BREWERY);
        given(roleRepository.findByName(RoleType.ROLE_BREWERY))
                .willReturn(Optional.of(role));

        // 비밀번호 인코딩
        given(bCryptPasswordEncoder.encode("password"))
                .willReturn("encodedPW");

        // RegionType 조회
        RegionType regionType = RegionType.nameFrom("서울");
        given(regionTypeRepository.findById(1))
                .willReturn(Optional.of(regionType));

        // 이미지 업로드
        given(storageService.upload(imageDto.getImage(), ImageType.BREWERY_IMAGE))
                .willReturn("breweryImageKey");

        // 캡쳐
        ArgumentCaptor<Users> usersCaptor = ArgumentCaptor.forClass(Users.class);
        ArgumentCaptor<Brewery> breweryCaptor = ArgumentCaptor.forClass(Brewery.class);
        ArgumentCaptor<BreweryImage> breweryImageCaptor = ArgumentCaptor.forClass(BreweryImage.class);

        // when
        authService.breweryJoin(joinDto);

        // then
        // 의존성 호출 검증
        verify(roleRepository).findByName(RoleType.ROLE_BREWERY);
        verify(bCryptPasswordEncoder).encode("password");
        verify(usersRepository).save(usersCaptor.capture());
        verify(regionTypeRepository).findById(1);
        verify(breweryRepository).save(breweryCaptor.capture());
        verify(breweryImageRepository).save(breweryImageCaptor.capture());
        verify(storageService).upload(imageDto.getImage(), ImageType.BREWERY_IMAGE);

        Users savedUser = usersCaptor.getValue();
        Brewery savedBrewery = breweryCaptor.getValue();
        BreweryImage savedBreweryImage = breweryImageCaptor.getValue();

        // Users 매핑 검증 (여기서는 핵심 포인트만)
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals("encodedPW", savedUser.getPassword());
        assertEquals("양조장닉", savedUser.getNickname());
        assertEquals("홍길동", savedUser.getName());
        assertEquals("010-1111-1111", savedUser.getPhone());
        assertEquals(LocalDate.of(2001, 1, 1), savedUser.getBirth());
        assertEquals("부산 어딘가", savedUser.getAddress());
        assertEquals("000동 1111호", savedUser.getAddressDetail());
        assertTrue(savedUser.getIsAgreed());
        // gender: "woman" → true
        assertEquals(Boolean.TRUE, savedUser.getGender());
        assertSame(role, savedUser.getRole());

        // Brewery 매핑 검증
        assertSame(savedUser, savedBrewery.getUser());
        assertEquals("양조장닉", savedBrewery.getBreweryName());
        assertSame(regionType, savedBrewery.getRegionType());
        assertEquals("부산 어딘가", savedBrewery.getBreweryAddress());
        assertEquals("000동 1111호", savedBrewery.getBreweryAddressDetail());
        assertEquals("brewery business reg no", savedBrewery.getBusinessRegistrationNumber());
        assertEquals("brewery depositor", savedBrewery.getBreweryDepositor());
        assertEquals("brewery account number", savedBrewery.getBreweryAccountNumber());
        assertEquals("brewery bank name", savedBrewery.getBreweryBankName());
        assertEquals("brewery introduction", savedBrewery.getIntroduction());
        assertEquals("https://brewery.test", savedBrewery.getBreweryWebsite());
        assertTrue(savedBrewery.getIsRegularVisit());
        assertTrue(savedBrewery.getIsAgreedBrewery());
        assertEquals(joinDto.getStart_time(), savedBrewery.getStartTime());
        assertEquals(joinDto.getEnd_time(), savedBrewery.getEndTime());

        // BreweryImage 매핑 검증
        assertEquals("breweryImageKey", savedBreweryImage.getImageKey());
        assertEquals(1, savedBreweryImage.getSeq());
        assertSame(savedBrewery, savedBreweryImage.getBrewery());
    }

    @Test
    @DisplayName("breweryJoin - 시작 시간이 종료 시간보다 늦으면 BREWERY_OPENING_TIME_INVALID")
    void breweryJoin_invalidOpeningTime_throws() {
        // given
        BreweryJoinDto dto = createBreweryJoinDto();
        // start_time > end_time 이 되도록 세팅
        dto.setStart_time(LocalTime.of(18, 0));
        dto.setEnd_time(LocalTime.of(9, 0));

        // when
        ApplicationException ex = assertThrows(
                ApplicationException.class,
                () -> authService.breweryJoin(dto)
        );

        // then
        assertEquals(ApplicationError.BREWERY_OPENING_TIME_INVALID, ex.getApplicationError());

        // 영업시간 검증에서 바로 예외가 나기 때문에, 그 이후 로직은 전혀 호출되면 안 된다.
        verify(usersRepository, never()).save(any());
        verify(regionTypeRepository, never()).findById(anyInt());
        verify(breweryRepository, never()).save(any());
        verify(breweryImageRepository, never()).save(any());
        verify(storageService, never()).upload(any(), any());
    }
    @Test
    @DisplayName("breweryJoin - 존재하지 않는 지역 ID면 REGION_NOT_FOUND")
    void breweryJoin_regionNotFound_throws() {
        // given
        BreweryJoinDto dto = createBreweryJoinDto();
        dto.setRegion_type_id(999); // 존재하지 않는 지역 ID 라고 가정
        dto.setStart_time(LocalTime.of(9, 0));   // 정상 시간
        dto.setEnd_time(LocalTime.of(18, 0));    // 정상 시간

        // createUser 내부에서 호출되는 Role, 비밀번호 인코딩 stubbing
        Role role = new Role();
        role.setName(RoleType.ROLE_BREWERY);
        given(roleRepository.findByName(RoleType.ROLE_BREWERY))
                .willReturn(Optional.of(role));
        given(bCryptPasswordEncoder.encode("password"))
                .willReturn("encodedPW");

        // RegionType 조회는 실패하게 stubbing
        given(regionTypeRepository.findById(999))
                .willReturn(Optional.empty());

        // when
        ApplicationException ex = assertThrows(
                ApplicationException.class,
                () -> authService.breweryJoin(dto)
        );

        // then
        assertEquals(ApplicationError.REGION_NOT_FOUND, ex.getApplicationError());

        // createUser 까지는 정상 수행되므로 Users 는 한 번 저장되어야 한다.
        verify(usersRepository).save(any(Users.class));

        // regionTypeRepository.findById 는 호출되지만, null 이라 예외 발생
        verify(regionTypeRepository).findById(999);

        // 지역을 못 찾고 예외가 터졌으므로, Brewery 및 이미지 관련 저장/업로드는 수행되지 않아야 한다.
        verify(breweryRepository, never()).save(any());
        verify(breweryImageRepository, never()).save(any());
        verify(storageService, never()).upload(any(), any());
    }

    @Test
    @DisplayName("토큰 refresh")
    void refresh_success() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        String token = "refreshToken";
        given(request.getHeader("X-Refresh-Token"))
                .willReturn(token);
        JwtClaimsDto jwtDto = JwtClaimsDto.tidUserIdDeviceTypeRoleOf("t1234", 1L, "ROLE_UESR");
        Long userId = jwtDto.getUserId();
        String tid = jwtDto.getTid();
        String role = jwtDto.getRole();
        given(jwtUtil.parseRefreshToken(token)).willReturn(jwtDto);
        given(redisService.verifyRefreshTokenTid(userId, tid))
                .willReturn(true);
        authService.updateRefreshToken(request, response);
    }

    @Test
    @DisplayName("refresh 토큰 null")
    void refresh_token_null() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        given(request.getHeader("X-Refresh-Token"))
                .willReturn(null);
        ApplicationException ex = assertThrows(ApplicationException.class, () -> authService.updateRefreshToken(request, response));
        assertEquals(ex.getApplicationError(), ApplicationError.TOKEN_EXPIRED);
    }
    @Test
    @DisplayName("refresh - 동시 접속 감지")
    void refresh_concurrent_connection() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        String token = "refreshToken";
        given(request.getHeader("X-Refresh-Token"))
                .willReturn(token);
        JwtClaimsDto jwtDto = JwtClaimsDto.tidUserIdDeviceTypeRoleOf("t1234", 1L, "ROLE_UESR");
        Long userId = jwtDto.getUserId();
        String tid = jwtDto.getTid();
        String role = jwtDto.getRole();
        given(jwtUtil.parseRefreshToken(token)).willReturn(jwtDto);
        given(redisService.verifyRefreshTokenTid(userId, tid))
                .willReturn(false);
        ApplicationException ex = assertThrows(ApplicationException.class, () -> authService.updateRefreshToken(request, response));
        assertEquals(ex.getApplicationError(), ApplicationError.CONCURRENT_CONNECTION);
    }
}
