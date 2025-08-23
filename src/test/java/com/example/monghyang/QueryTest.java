package com.example.monghyang;

import com.example.monghyang.domain.brewery.main.entity.Brewery;
import com.example.monghyang.domain.brewery.main.entity.BreweryImage;
import com.example.monghyang.domain.brewery.main.repository.BreweryImageRepository;
import com.example.monghyang.domain.brewery.main.repository.BreweryRepository;
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.seller.entity.Seller;
import com.example.monghyang.domain.seller.repository.SellerRepository;
import com.example.monghyang.domain.users.entity.Users;
import com.example.monghyang.domain.users.repository.UsersRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class QueryTest {
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private SellerRepository sellerRepository;
    @Autowired
    private BreweryImageRepository breweryImageRepository;
    @Autowired
    private BreweryRepository breweryRepository;

    @Test
    @Transactional
    void test() {
        Brewery brewery = breweryRepository.findById(1L).orElseThrow(() ->
                new ApplicationException(ApplicationError.BREWERY_NOT_FOUND));
        UUID imageKey = UUID.randomUUID();
        Integer seq = 1;
        Long volume = 10L;
        breweryImageRepository.save(BreweryImage.breweryKeySeqVolume(brewery, imageKey, seq, volume));
        breweryImageRepository.saveAndFlush(BreweryImage.breweryKeySeqVolume(brewery, imageKey, seq, volume));
    }
}
