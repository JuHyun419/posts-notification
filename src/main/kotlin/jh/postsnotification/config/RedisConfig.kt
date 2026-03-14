package jh.postsnotification.config

import jh.postsnotification.infrastructure.redis.RedisCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate

@Configuration
class RedisConfig {

    @Bean
    fun stringRedisTemplate(connectionFactory: RedisConnectionFactory): StringRedisTemplate {
        return StringRedisTemplate(connectionFactory)
    }

    @Bean
    fun redisCacheManager(stringRedisTemplate: StringRedisTemplate): RedisCacheManager {
        return RedisCacheManager(stringRedisTemplate)
    }
}
