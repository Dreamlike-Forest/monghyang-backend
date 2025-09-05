package com.example.monghyang;

import com.example.monghyang.domain.brewery.joy.dto.ReqJoyDto;
import com.example.monghyang.domain.brewery.joy.dto.ReqUpdateJoyDto;
import com.example.monghyang.domain.brewery.joy.dto.ResJoyDto;
import com.example.monghyang.domain.brewery.joy.service.JoyService;
import com.example.monghyang.domain.brewery.main.dto.ResBreweryDto;
import com.example.monghyang.domain.brewery.main.dto.ResBreweryImageDto;
import com.example.monghyang.domain.brewery.main.repository.BreweryImageRepository;
import com.example.monghyang.domain.brewery.main.repository.BreweryRepository;
import com.example.monghyang.domain.brewery.main.service.BreweryService;
import com.example.monghyang.domain.seller.repository.SellerRepository;
import com.example.monghyang.domain.tag.repository.TagCategoryRepository;
import com.example.monghyang.domain.tag.service.TagCategoryService;
import com.example.monghyang.domain.tag.service.TagsService;
import com.example.monghyang.domain.users.repository.UsersRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ServiceLayerTest {
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private SellerRepository sellerRepository;
    @Autowired
    private BreweryImageRepository breweryImageRepository;
    @Autowired
    private BreweryRepository breweryRepository;
    @Autowired
    private TagCategoryService tagCategoryService;
    @Autowired
    private TagCategoryRepository tagCategoryRepository;
    @Autowired
    private TagsService tagsService;
    @Autowired
    private JoyService joyService;
    @Autowired
    private BreweryService breweryService;

    @Test
    @Transactional
    void test() {
//        ReqJoyDto reqJoyDto = new ReqJoyDto();
//        reqJoyDto.setName("service layer test joy");
//        reqJoyDto.setDetail("service layer test joy detail");
//        reqJoyDto.setPlace("service layer test joy place");
//        reqJoyDto.setOrigin_price(12341234);
//        joyService.createJoy(1L, reqJoyDto);
//
//        System.out.println("=============================================================");
//
//        joyService.deleteJoy(1L, 10L);
//
//        System.out.println("=============================================================");
//
//        List<ResJoyDto> list = joyService.getJoyListByBreweryId(1L);
//        for(ResJoyDto resJoyDto : list){
//            System.out.println(resJoyDto.getJoy_id()+" "+resJoyDto.getBrewery_id()+" "+resJoyDto.getJoy_name()+" "+resJoyDto.getJoy_origin_price()+" "+ resJoyDto.getJoy_discount_rate()+"% "+resJoyDto.getJoy_final_price());
//        }
//
//        System.out.println("=============================================================");
//
//        joyService.restoreJoy(1L, 10L);
//
//        System.out.println("=============================================================");
//
//        List<ResJoyDto> list2 = joyService.getJoyListByBreweryId(1L);
//        for(ResJoyDto resJoyDto : list2){
//            System.out.println(resJoyDto.getJoy_id()+" "+resJoyDto.getBrewery_id()+" "+resJoyDto.getJoy_name()+" "+resJoyDto.getJoy_origin_price()+" "+ resJoyDto.getJoy_discount_rate()+"% "+resJoyDto.getJoy_final_price());
//        }
//
//        System.out.println("=============================================================");
//
//        ReqUpdateJoyDto reqUpdateJoyDto = new ReqUpdateJoyDto();
//        reqUpdateJoyDto.setId(10L);
//        reqUpdateJoyDto.setName("service layer test joy");
//        reqUpdateJoyDto.setDetail("service layer test joy detail");
//        reqUpdateJoyDto.setPlace("service layer test joy place");
//        reqUpdateJoyDto.setOrigin_price(90);
//        reqUpdateJoyDto.setDiscount_rate(10);
//        joyService.updateJoy(1L, reqUpdateJoyDto);

        List<ResJoyDto> result = joyService.getJoyListByBreweryId(1L);
        for(ResJoyDto resJoyDto : result){
            System.out.println(resJoyDto.getJoy_id()+" "+resJoyDto.getJoy_name());
        }


    }
}
