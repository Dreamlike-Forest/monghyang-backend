package com.example.monghyang;

import com.example.monghyang.domain.brewery.main.repository.BreweryImageRepository;
import com.example.monghyang.domain.brewery.main.repository.BreweryRepository;
import com.example.monghyang.domain.seller.repository.SellerRepository;
import com.example.monghyang.domain.tag.entity.TagCategory;
import com.example.monghyang.domain.tag.repository.TagCategoryRepository;
import com.example.monghyang.domain.tag.service.TagCategoryService;
import com.example.monghyang.domain.users.repository.UsersRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

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
    private TagCategoryService tagCategoryService;
    @Autowired
    private TagCategoryRepository tagCategoryRepository;

    @Test
    @Transactional
    void test() {
        tagCategoryService.createTagCategory("카테고리1");
        tagCategoryService.createTagCategory("카테고리2");

        TagCategory tagCategory = tagCategoryRepository.findById(1).orElse(null);
        TagCategory tagCategory2 = tagCategoryRepository.findById(2).orElse(null);
        if(tagCategory != null) {
            tagCategoryService.updateTagCategoryName(tagCategory.getId(), "업데이트된 카테고리1");
        }

        if(tagCategory2 != null) {
            tagCategoryService.deleteTagCategory(tagCategory2.getId());
        }

        tagCategoryService.getTagCategoryListLatest(0);
        tagCategoryService.getTagCategoryListKeyword("카테", 0);

    }
}
