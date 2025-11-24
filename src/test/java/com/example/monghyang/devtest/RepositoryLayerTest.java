package com.example.monghyang.devtest;

import com.example.monghyang.domain.brewery.repository.BreweryImageRepository;
import com.example.monghyang.domain.brewery.repository.BreweryRepository;
import com.example.monghyang.domain.brewery.tag.BreweryTagRepository;
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.joy.dto.JoyScheduleCountDto;
import com.example.monghyang.domain.joy.dto.slot.UnavailableJoySlotTimeCountDto;
import com.example.monghyang.domain.joy.repository.JoySlotRepository;
import com.example.monghyang.domain.product.repository.ProductRepository;
import com.example.monghyang.domain.product.review.ProductReviewRepository;
import com.example.monghyang.domain.product.tag.ProductTagRepository;
import com.example.monghyang.domain.seller.repository.SellerRepository;
import com.example.monghyang.domain.tag.dto.TagNameDto;
import com.example.monghyang.domain.tag.repository.TagCategoryRepository;
import com.example.monghyang.domain.users.repository.UsersRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RepositoryLayerTest {
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private SellerRepository sellerRepository;
    @Autowired
    private BreweryImageRepository breweryImageRepository;
    @Autowired
    private BreweryRepository breweryRepository;
    @Autowired
    private TagCategoryRepository tagCategoryRepository;
    @Autowired
    private BreweryTagRepository breweryTagRepository;
    @Autowired
    private ProductReviewRepository productReviewRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductTagRepository productTagRepository;
    @Autowired
    private JoySlotRepository joySlotRepository;

    @Test
    @Disabled
    @Transactional
    void test() {
        List<Long> idList = new ArrayList<>();
        idList.add(155L);
        idList.add(156L);
        List<TagNameDto> list = productTagRepository.findAuthTagListByProductIdList(idList);
        for(TagNameDto dto : list){
            System.out.println(dto.ownerId()+" "+dto.tagName());
        }

    }

    @Test
    @Disabled
    @DisplayName("예약가능 슬롯 조회 테스트")
    void getJoySlotTest() {
        JoyScheduleCountDto dto = joySlotRepository.findJoyScheduleCountByJoyId(21L).orElseThrow(() ->
                new ApplicationException(ApplicationError.JOY_NOT_FOUND));
        System.out.println(dto.getTimeUnit()+" "+dto.getStartTime()+" "+dto.getEndTime());
        System.out.println("일별 체험 운영 횟수: "+Duration.between(dto.getStartTime(), dto.getEndTime()).toMinutes() / dto.getTimeUnit());

        LocalDate startDate = LocalDate.of(2025, 11, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 1);
        List<UnavailableJoySlotTimeCountDto> list = joySlotRepository.findUnavailableJoySlotTimeCountByJoyIdAndMonth(21L, startDate, endDate);
        for (UnavailableJoySlotTimeCountDto dto1 : list) {
            System.out.println(dto1.getReservationDate()+" "+dto1.getCount());
        }
    }

    @Test
    @Disabled
    @DisplayName("주문 대상 상품 재고 수량 일괄 감소")
    void decreaseInvenForOrder() {
        int targetEa = 3;
        int ret = productRepository.decreaseInventoryForOrderByProductIds(List.of(1L, 2L, 3L));
        System.out.println("재고 감소된 상품의 수: "+ret);
    }
}
