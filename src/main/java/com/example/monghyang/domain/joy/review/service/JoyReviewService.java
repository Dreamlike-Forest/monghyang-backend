package com.example.monghyang.domain.joy.review.service;

import com.example.monghyang.domain.joy.review.repository.JoyReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class JoyReviewService {
    private final JoyReviewRepository joyReviewRepository;
}
