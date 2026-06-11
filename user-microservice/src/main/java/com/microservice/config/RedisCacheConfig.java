package com.microservice.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis Cache Configuration
 * Enables caching with Redis as the backend store
 */
@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.cache.ttl:3600}")
    private long cacheTtlSeconds;

    @Value("${spring.redis.cache.null-ttl:300}")
    private long nullCacheTtlSeconds;

    /**
     * Configure Lettuce connection factory for Redis
     */
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory();
    }

    /**
     * Configure ObjectMapper for JSON serialization
     */
    private ObjectMapper getObjectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfBaseType(Object.class)
                        .build(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);
        return om;
    }

    /**
     * Configure cache manager with Redis backend
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(cacheTtlSeconds))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer(getObjectMapper())))
                .disableCachingNullValues();

        // Configure specific caches with different TTLs
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .withCacheConfiguration("users",
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofSeconds(cacheTtlSeconds))
                                .serializeKeysWith(RedisSerializationContext.SerializationPair
                                        .fromSerializer(new StringRedisSerializer()))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair
                                        .fromSerializer(new GenericJackson2JsonRedisSerializer(getObjectMapper()))))
                .withCacheConfiguration("userById",
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofSeconds(cacheTtlSeconds))
                                .serializeKeysWith(RedisSerializationContext.SerializationPair
                                        .fromSerializer(new StringRedisSerializer()))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair
                                        .fromSerializer(new GenericJackson2JsonRedisSerializer(getObjectMapper()))))
                .withCacheConfiguration("userByEmail",
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofSeconds(cacheTtlSeconds))
                                .serializeKeysWith(RedisSerializationContext.SerializationPair
                                        .fromSerializer(new StringRedisSerializer()))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair
                                        .fromSerializer(new GenericJackson2JsonRedisSerializer(getObjectMapper()))))
                .withCacheConfiguration("usersByCity",
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofSeconds(cacheTtlSeconds))
                                .serializeKeysWith(RedisSerializationContext.SerializationPair
                                        .fromSerializer(new StringRedisSerializer()))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair
                                        .fromSerializer(new GenericJackson2JsonRedisSerializer(getObjectMapper()))))
                .withCacheConfiguration("activeUsers",
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofSeconds(cacheTtlSeconds))
                                .serializeKeysWith(RedisSerializationContext.SerializationPair
                                        .fromSerializer(new StringRedisSerializer()))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair
                                        .fromSerializer(new GenericJackson2JsonRedisSerializer(getObjectMapper()))))
                .build();
    }
}
