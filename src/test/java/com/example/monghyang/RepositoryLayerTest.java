package com.example.monghyang;

import com.example.monghyang.domain.brewery.main.dto.ResBreweryListDto;
import com.example.monghyang.domain.brewery.main.repository.BreweryImageRepository;
import com.example.monghyang.domain.brewery.main.repository.BreweryRepository;
import com.example.monghyang.domain.brewery.tag.BreweryTagRepository;
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.product.dto.ResProductListDto;
import com.example.monghyang.domain.product.dto.ResProductOwnerDto;
import com.example.monghyang.domain.product.entity.Product;
import com.example.monghyang.domain.product.repository.ProductRepository;
import com.example.monghyang.domain.product.review.ProductReviewRepository;
import com.example.monghyang.domain.seller.repository.SellerRepository;
import com.example.monghyang.domain.tag.repository.TagCategoryRepository;
import com.example.monghyang.domain.users.dto.UserSimpleInfoDto;
import com.example.monghyang.domain.users.repository.UsersRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
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
    @Autowired
    private ProductReviewRepository productReviewRepository;
    @Autowired
    private ProductRepository productRepository;

    @Test
    @Transactional
    void test() {
        Long productId = 152L;
        Product product = productRepository.findById(productId).orElseThrow(() ->
                new ApplicationException(ApplicationError.PRODUCT_NOT_FOUND));
        UserSimpleInfoDto userInfo = usersRepository.findNicknameAndRoleType(product.getUser().getId()).orElseThrow(() ->
                new ApplicationException(ApplicationError.USER_NOT_FOUND));

        System.out.println(userInfo.nickname()+" "+userInfo.roleType());

        ResProductOwnerDto owner = breweryRepository.findSimpleInfoByUserId(product.getUser().getId()).orElseThrow(() ->
                new ApplicationException(ApplicationError.BREWERY_NOT_FOUND));
        System.out.println(owner.getOwner_id()+" "+owner.getOwner_region());

        List<String> tagList = breweryTagRepository.findTagListByBreweryId(owner.getOwner_id());
        for(String tag : tagList){
            System.out.println(tag);
        }
    }
}
