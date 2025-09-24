package com.example.monghyang.domain.joy.review.service;

import com.example.monghyang.domain.joy.review.repository.JoyReviewRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class JoyReviewService {
    private final JoyReviewRepository joyReviewRepository;
    @Autowired
    public JoyReviewService(JoyReviewRepository joyReviewRepository) {
        this.joyReviewRepository = joyReviewRepository;
    }
}
