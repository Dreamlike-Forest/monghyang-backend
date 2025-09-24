package com.example.monghyang.devtest;

import com.example.monghyang.domain.redis.RedisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MonghyangApplicationTests {

    @Autowired
    private RedisService redisService;

	@Test
	void contextLoads() {
		redisService.deleteSessionId("2022e458-edbe-4c64-ab6b-0404feb1c87b");
	}

}
