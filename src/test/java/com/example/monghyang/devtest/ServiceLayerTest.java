package com.example.monghyang.devtest;

import com.example.monghyang.domain.brewery.joy.service.JoyService;
import com.example.monghyang.domain.brewery.main.repository.BreweryImageRepository;
import com.example.monghyang.domain.brewery.main.repository.BreweryRepository;
import com.example.monghyang.domain.brewery.main.service.BreweryService;
import com.example.monghyang.domain.product.service.ProductService;
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
    @Autowired
    private ProductService productService;

    @Test
    @Transactional
    void test() {

    }
}
