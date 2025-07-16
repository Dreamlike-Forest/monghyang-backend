package com.example.monghyang.domain.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Bean // Redis 커넥션 설정 정보 Bean
    @ConfigurationProperties(prefix = "spring.data.redis") // 이 경로의 설정 내용을 Bean에 주입
    public RedisStandaloneConfiguration redisStandaloneConfiguration() {
        return new RedisStandaloneConfiguration();
    }
    // Redis 커넥션 생성하고 관리하는 팩토리 클래스 생성. Lettuce는 비동기 논블로킹 기반의 Redis 클라이언트(spring 기본값)
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(redisStandaloneConfiguration());
    }
    // RedisTemplate: 명령어 사용 편의 도구. Redis 연산을 Java 코드로 쉽게 수행할 수 있도록 하는 기능 제공
    @Bean // access token tid 저장용 템플릿
    public RedisTemplate<String, String> redisTemplate(LettuceConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }

    // 추후 json 객체 저장용 템플릿을 별도로 구현할 예정
}
