package org.deadog.springsecurityhomework.config;

import org.deadog.springsecurityhomework.model.RefreshToken;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory(RedisProperties redisProperties) {
        return new LettuceConnectionFactory(redisProperties.getHost(), redisProperties.getPort());
    }

    @Bean
    public ReactiveRedisTemplate<String, RefreshToken> reactiveRedisTemplate(ReactiveRedisConnectionFactory reactiveRedisConnectionFactory) {
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        Jackson2JsonRedisSerializer<RefreshToken> jackson2JsonRedisSerializer =
                new Jackson2JsonRedisSerializer<>(RefreshToken.class);

        RedisSerializationContext.RedisSerializationContextBuilder<String, RefreshToken> builder =
                RedisSerializationContext.newSerializationContext(stringRedisSerializer);

        RedisSerializationContext<String, RefreshToken> context = builder.value(jackson2JsonRedisSerializer).build();
        return new ReactiveRedisTemplate<>(reactiveRedisConnectionFactory, context);
    }


}
