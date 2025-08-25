package com.example.monghyang.domain.brewery.main.controller;

import com.example.monghyang.domain.brewery.main.service.BreweryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/brewery") // 모두가 접근할 수 있는 양조장 관련 api(조회 등)
public class BreweryController {
    private final BreweryService breweryService;
    @Autowired
    public BreweryController(BreweryService breweryService) {
        this.breweryService = breweryService;
    }

    // 양
}
