package com.example.monghyang.domain.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;
import java.util.TimeZone;

@Configuration
public class TimeZoneConfig {
    private static final ZoneId SEOUL_ZONE_ID = ZoneId.of("Asia/Seoul");

    // 스프링 애플리케이션 타임존 설정
    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone(SEOUL_ZONE_ID));
    }

    /**
     * 현재 시간 판단이 필요한 서비스에서 사용하는 애플리케이션 기준 시계를 제공합니다.
     *
     * @return Asia/Seoul 시간대 기준 시스템 시계
     */
    @Bean
    public Clock clock() {
        return Clock.system(SEOUL_ZONE_ID);
    }
}
