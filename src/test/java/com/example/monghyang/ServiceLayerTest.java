package com.example.monghyang;

import com.example.monghyang.domain.brewery.main.repository.BreweryImageRepository;
import com.example.monghyang.domain.brewery.main.repository.BreweryRepository;
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.seller.repository.SellerRepository;
import com.example.monghyang.domain.tag.dto.ResTagDto;
import com.example.monghyang.domain.tag.entity.TagCategory;
import com.example.monghyang.domain.tag.repository.TagCategoryRepository;
import com.example.monghyang.domain.tag.service.TagCategoryService;
import com.example.monghyang.domain.tag.service.TagsService;
import com.example.monghyang.domain.users.repository.UsersRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

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

    @Test
    @Transactional
    void test() {
        Page<ResTagDto> result2 = tagsService.getTagListByCategory(1, 0);
        result2.getContent().forEach(t -> System.out.println("tag id: "+t.getId()+" tag name: "+t.getName()));
    }
}
