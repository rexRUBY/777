package org.example.ranking.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class MetaDBConfig {

    // Redis 연결을 위한 RedisConnectionFactory 설정
    @Bean
    @ConfigurationProperties(prefix = "spring.redis-meta")
    public RedisConnectionFactory metaRedisConnectionFactory() {
        return new LettuceConnectionFactory();
    }

    // RedisTemplate을 사용하여 Redis와의 데이터 입출력 관리
    @Bean
    public RedisTemplate<String, Object> metaRedisTemplate(RedisConnectionFactory metaRedisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(metaRedisConnectionFactory);
        return redisTemplate;
    }

}
