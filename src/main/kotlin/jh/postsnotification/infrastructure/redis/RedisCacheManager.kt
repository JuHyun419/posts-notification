package jh.postsnotification.infrastructure.redis

import org.springframework.data.redis.core.StringRedisTemplate
import java.time.Duration

class RedisCacheManager(
    private val redisTemplate: StringRedisTemplate,
) {
    fun addToSet(key: String, ttl: Duration, vararg values: String) {
        val addedCount = redisTemplate.opsForSet().add(key, *values)
        if (addedCount == NEW_ELEMENT_COUNT) redisTemplate.expire(key, ttl)
    }

    fun getMembers(key: String): Set<String>? =
        redisTemplate.opsForSet().members(key)

    fun isMember(key: String, value: String): Boolean =
        redisTemplate.opsForSet().isMember(key, value) == true

    companion object {
        private const val NEW_ELEMENT_COUNT = 1L
    }
}
