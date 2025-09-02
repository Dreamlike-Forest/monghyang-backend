package com.example.monghyang;

import com.example.monghyang.domain.brewery.main.dto.ResBreweryListDto;
import com.example.monghyang.domain.brewery.main.repository.BreweryImageRepository;
import com.example.monghyang.domain.brewery.main.repository.BreweryRepository;
import com.example.monghyang.domain.brewery.tag.BreweryTagRepository;
import com.example.monghyang.domain.seller.repository.SellerRepository;
import com.example.monghyang.domain.tag.repository.TagCategoryRepository;
import com.example.monghyang.domain.users.repository.UsersRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

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

    @Test
    @Transactional
    void test() {
        Sort sort = Sort.by(Sort.Direction.DESC, "id"); // 정렬 기준: 기본키 기준으로 내림차순 정렬
        Pageable pageable = PageRequest.of(0, 10, sort);
        List<Integer> tagIdList = new ArrayList<>();
        tagIdList.add(1);
        tagIdList.add(10);
        List<Integer> regionIdList = new ArrayList<>();
//        regionIdList.add(1);
        String keyword = "hi";
        Integer minPrice = null;
        Integer maxPrice = null;
        boolean tagListIsEmpty = tagIdList.isEmpty() || tagIdList == null;
        boolean regionListIsEmpty = regionIdList.isEmpty() || regionIdList == null;

        List<ResBreweryListDto> result = breweryRepository.findBreweryIdByDynamicFiltering(pageable, tagListIsEmpty, regionListIsEmpty,
                keyword, minPrice, maxPrice, tagIdList, regionIdList);

        for(ResBreweryListDto resBreweryListDto : result){
            System.out.println(resBreweryListDto.getBrewery_id()+" "+resBreweryListDto.getBrewery_name()+" "+resBreweryListDto.getRegion_type_name()+" "+resBreweryListDto.getBrewery_joy_min_price()+" "+resBreweryListDto.getBrewery_joy_count());
        }
    }
}
