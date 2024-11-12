package org.example.batch.config;

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
    @Primary
    @ConfigurationProperties(prefix = "spring.redis-meta") // redis-meta라는 접두어로 설정을 받음
    public RedisConnectionFactory metaRedisConnectionFactory() {
        return new LettuceConnectionFactory();  // Lettuce를 사용한 Redis 연결 (Jedis도 선택 가능)
    }

    // RedisTemplate을 사용하여 Redis와의 데이터 입출력 관리
    @Primary
    @Bean
    public RedisTemplate<String, Object> metaRedisTemplate(RedisConnectionFactory metaRedisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(metaRedisConnectionFactory);
        // 필요한 경우 직렬화/역직렬화 설정을 추가할 수 있습니다.
        return redisTemplate;
    }

}
