package com.example.monghyang.devtest;

import com.example.monghyang.domain.brewery.dto.ReqUpdateBreweryDto;
import com.example.monghyang.domain.brewery.dto.ResRegionDto;
import com.example.monghyang.domain.brewery.entity.Brewery;
import com.example.monghyang.domain.brewery.entity.BreweryImage;
import com.example.monghyang.domain.brewery.entity.RegionType;
import com.example.monghyang.domain.brewery.repository.BreweryImageRepository;
import com.example.monghyang.domain.brewery.repository.BreweryRepository;
import com.example.monghyang.domain.brewery.repository.RegionTypeRepository;
import com.example.monghyang.domain.brewery.service.BreweryService;
import com.example.monghyang.domain.brewery.tag.BreweryTagRepository;
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.image.dto.AddImageDto;
import com.example.monghyang.domain.image.dto.ModifySeqImageDto;
import com.example.monghyang.domain.image.service.StorageService;
import com.example.monghyang.domain.joy.repository.JoyRepository;
import com.example.monghyang.domain.product.service.ProductService;
import com.example.monghyang.domain.users.entity.Role;
import com.example.monghyang.domain.users.entity.RoleType;
import com.example.monghyang.domain.users.entity.Users;
import com.example.monghyang.domain.users.repository.UsersRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class BreweryServiceTest {
    final int BREWERY_PAGE_SIZE = 6;
    @Mock
    BreweryRepository breweryRepository;
    @Mock
    UsersRepository usersRepository;
    @Mock
    BCryptPasswordEncoder bCryptPasswordEncoder;
    @Mock
    BreweryImageRepository breweryImageRepository;
    @Mock
    StorageService storageService;
    @Mock
    BreweryTagRepository breweryTagRepository;
    @Mock
    JoyRepository joyRepository;
    @Mock
    ProductService productService;
    @Mock
    RegionTypeRepository regionTypeRepository;
    @InjectMocks
    BreweryService breweryService;

    private Users createMockUsers() {
        Role role = new Role();
        role.setName(RoleType.ROLE_BREWERY);
        return Users.generalBuilder()
                .email("test@example.com")
                .role(role).password("PW").nickname("brewery")
                .name("name").phone("phone").birth(LocalDate.of(2000,1,1))
                .gender(true).address("address").address_detail("address_detail")
                .isAgreed(true).build();
    }
    private Brewery createMockBrewery(Users users, RegionType regionType) {
        return Brewery.breweryBuilder()
                .user(users).regionType(regionType)
                .breweryName("brewery").breweryAddress("address").breweryAddressDetail("address detail")
                .businessRegistrationNumber("test registration number").breweryDepositor("test depositor")
                .breweryAccountNumber("test account").breweryBankName("test bank").introduction("test introduction")
                .breweryWebsite("test web site url").isAgreedBrewery(true)
                .isRegularVisit(true).build();
    }

    @Test
    @DisplayName("모든 양조장 지역 정보 조회")
    void get_all_region() {
        List<RegionType> list = new ArrayList<>();
        list.add(RegionType.nameFrom("서울"));
        list.add(RegionType.nameFrom("경상도"));
        given(regionTypeRepository.findAll()).willReturn(list);
        List<ResRegionDto> result = breweryService.getAllRegions();
        assertNotNull(result);
        assertEquals(result.get(0).getRegion_type_name(), "서울");
        assertEquals(result.get(1).getRegion_type_name(), "경상도");
    }

    @Test
    @DisplayName("양조장 정보 수정 - 이미지 수정 없음, 컬럼만 수정")
    void brewery_update_success_basic_fields_only() {
        // given
        Users users = createMockUsers();
        RegionType regionType = RegionType.nameFrom("서울");
        Brewery brewery = createMockBrewery(users, regionType);
        Long userId = 1L;

        given(breweryRepository.findByUserId(userId))
                .willReturn(Optional.of(brewery));

        ReqUpdateBreweryDto dto = new ReqUpdateBreweryDto();
        dto.setBrewery_name("새 양조장 이름");
        dto.setBrewery_address("새 주소");
        dto.setBrewery_address_detail("새 상세 주소");
        dto.setBusiness_registration_number("새 사업자번호");
        dto.setBrewery_depositor("새 예금주");
        dto.setBrewery_account_number("새 계좌번호");
        dto.setBrewery_bank_name("새 은행명");
        dto.setIntroduction("새 소개글");
        dto.setBrewery_website("https://new-brewery.com");
        dto.setIs_regular_visit(false);

        // 이미지 관련 리스트는 비워둠
        dto.setAdd_images(Collections.emptyList());
        dto.setRemove_images(Collections.emptyList());
        dto.setModify_images(Collections.emptyList());

        // when
        breweryService.breweryUpdate(userId, dto);

        // then
        // 이미지 관련 동작은 수행되지 않아야 한다.
        verify(breweryImageRepository, never()).findByBrewery(any());
        verify(storageService, never()).upload(any(), any());
        verify(storageService, never()).remove(anyString());
        verify(breweryImageRepository, never()).save(any());
        verify(breweryImageRepository, never()).delete(any());

        // Brewery 엔티티에 컬럼만 잘 반영되었는지 확인 (동일 객체가 변경된다)
        assertEquals("새 양조장 이름", brewery.getBreweryName());
        assertEquals("새 주소", brewery.getBreweryAddress());
        assertEquals("새 상세 주소", brewery.getBreweryAddressDetail());
        assertEquals("새 사업자번호", brewery.getBusinessRegistrationNumber());
        assertEquals("새 예금주", brewery.getBreweryDepositor());
        assertEquals("새 계좌번호", brewery.getBreweryAccountNumber());
        assertEquals("새 은행명", brewery.getBreweryBankName());
        assertEquals("새 소개글", brewery.getIntroduction());
        assertEquals("https://new-brewery.com", brewery.getBreweryWebsite());
        assertFalse(brewery.getIsRegularVisit());
    }

    @Test
    @DisplayName("양조장 정보 수정 - 이미지 개수 5장 초과 시 IMAGE_COUNT_INVALID")
    void brewery_update_image_count_invalid() {
        // given
        Users users = createMockUsers();
        RegionType regionType = RegionType.nameFrom("서울");
        Brewery brewery = createMockBrewery(users, regionType);
        Long userId = 1L;

        given(breweryRepository.findByUserId(userId))
                .willReturn(Optional.of(brewery));

        // 현재 이미지 3장 있다고 가정
        BreweryImage img1 = BreweryImage.breweryKeySeqVolume(brewery, "key1", 1, 100L);
        BreweryImage img2 = BreweryImage.breweryKeySeqVolume(brewery, "key2", 2, 100L);
        BreweryImage img3 = BreweryImage.breweryKeySeqVolume(brewery, "key3", 3, 100L);

        given(breweryImageRepository.findByBrewery(brewery))
                .willReturn(List.of(img1, img2, img3));

        // 삭제 0장, 추가 3장 → 3 - 0 + 3 = 6 > 5 → 예외
        ReqUpdateBreweryDto dto = new ReqUpdateBreweryDto();
        dto.setRemove_images(Collections.emptyList());

        List<AddImageDto> addImages = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            AddImageDto addDto = new AddImageDto();
            addDto.setSeq(4 + i); // 대충 4,5,6
            MockMultipartFile mockFile = new MockMultipartFile(
                    "image", "img" + i + ".png", "image/png", ("bytes" + i).getBytes()
            );
            addDto.setImage(mockFile);
            addImages.add(addDto);
        }
        dto.setAdd_images(addImages);
        dto.setModify_images(Collections.emptyList());

        // when
        ApplicationException ex = assertThrows(
                ApplicationException.class,
                () -> breweryService.breweryUpdate(userId, dto)
        );

        // then
        assertEquals(ApplicationError.IMAGE_COUNT_INVALID, ex.getApplicationError());

        // 이미지 삭제/업로드/저장은 수행되면 안 됨
        verify(storageService, never()).remove(anyString());
        verify(storageService, never()).upload(any(), any());
        verify(breweryImageRepository, never()).delete(any());
        verify(breweryImageRepository, never()).save(any());
    }

    @Test
    @DisplayName("양조장 정보 수정 - 해당 유저의 양조장이 없으면 BREWERY_NOT_FOUND")
    void brewery_update_brewery_not_found() {
        // given
        Long userId = 1L;
        ReqUpdateBreweryDto dto = new ReqUpdateBreweryDto();
        dto.setAdd_images(Collections.emptyList());
        dto.setRemove_images(Collections.emptyList());
        dto.setModify_images(Collections.emptyList());

        given(breweryRepository.findByUserId(userId))
                .willReturn(Optional.empty());

        // when
        ApplicationException ex = assertThrows(
                ApplicationException.class,
                () -> breweryService.breweryUpdate(userId, dto)
        );

        // then
        assertEquals(ApplicationError.BREWERY_NOT_FOUND, ex.getApplicationError());
        verify(breweryImageRepository, never()).findByBrewery(any());
        verify(storageService, never()).upload(any(), any());
        verify(storageService, never()).remove(anyString());
    }



    @Test
    @DisplayName("양조장 정보 수정 - 자신의 이미지 삭제 성공")
    void brewery_update_remove_images_success() {
        // given
        Users users = createMockUsers();
        RegionType regionType = RegionType.nameFrom("서울");
        Brewery brewery = createMockBrewery(users, regionType);
        Long userId = 1L;

        given(breweryRepository.findByUserId(userId))
                .willReturn(Optional.of(brewery));

        // 양조장이 보유한 이미지 2장 mock(pk만 포함)
        BreweryImage img1 = mock(BreweryImage.class);
        BreweryImage img2 = mock(BreweryImage.class);
        when(img1.getId()).thenReturn(1L);
        when(img1.getImageKey()).thenReturn("key1");
        when(img2.getId()).thenReturn(2L);

        given(breweryImageRepository.findByBrewery(brewery))
                .willReturn(List.of(img1, img2));

        // 삭제 대상: 1번 이미지만 삭제
        ReqUpdateBreweryDto dto = new ReqUpdateBreweryDto();
        dto.setRemove_images(List.of(1L));
        dto.setModify_images(Collections.emptyList());
        dto.setAdd_images(Collections.emptyList());

        // 삭제 대상 이미지 조회
        given(breweryImageRepository.findById(1L))
                .willReturn(Optional.of(img1));

        // when
        breweryService.breweryUpdate(userId, dto);

        // then
        // 스토리지에서 key1 삭제
        verify(storageService).remove("key1");
        // DB에서 해당 이미지 삭제
        verify(breweryImageRepository).delete(img1);

        // 삭제하지 않은 이미지에 대해서는 delete 호출이 없어야 한다.
        verify(breweryImageRepository, never()).delete(img2);
    }
    @Test
    @DisplayName("양조장 정보 수정 - 이미지 순서 정보 수정 성공 (mock 엔티티 사용)")
    void brewery_update_modify_seq_success() {
        // given
        Users users = createMockUsers();
        RegionType regionType = RegionType.nameFrom("서울");
        Brewery brewery = createMockBrewery(users, regionType);
        Long userId = 1L;

        given(breweryRepository.findByUserId(userId))
                .willReturn(Optional.of(brewery));

        // mock 이미지
        BreweryImage img1 = mock(BreweryImage.class);
        when(img1.getId()).thenReturn(1L);

        given(breweryImageRepository.findByBrewery(brewery))
                .willReturn(List.of(img1));

        ReqUpdateBreweryDto dto = new ReqUpdateBreweryDto();
        dto.setRemove_images(Collections.emptyList());
        dto.setAdd_images(Collections.emptyList());

        // seq를 3으로 수정 요청
        ModifySeqImageDto modifyDto = new ModifySeqImageDto();
        modifyDto.setImage_id(1L);
        modifyDto.setSeq(3);
        dto.setModify_images(List.of(modifyDto));

        given(breweryImageRepository.findById(1L))
                .willReturn(Optional.of(img1));
        given(breweryImageRepository.save(any(BreweryImage.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        breweryService.breweryUpdate(userId, dto);

        // then
        // img1.updateSeq(3)이 호출되었는지 검증
        verify(img1).updateSeq(-3);
        // 그리고 save에도 img1이 넘어갔는지
        verify(breweryImageRepository).save(img1);
    }
}
