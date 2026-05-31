package com.example.monghyang.devtest;

import com.example.monghyang.domain.joy.service.JoyOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

@SpringBootTest
public class JoyOrderRefundSchedulerTest {
    @Autowired
    private JoyOrderService joyOrderService;

    @Test
    @Rollback(false)
    void 환불_처리_스케줄링_메서드_테스트() {
        joyOrderService.joyOrderRefundScheduling();
    }
}
