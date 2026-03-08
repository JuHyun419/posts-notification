package jh.postsnotification.application.service

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class PushHistoryService(
    private val redisTemplate: StringRedisTemplate,
) {
    fun isAvailablePost(userId: Long, postId: Long): Boolean {
        return !isAlreadySent(userId, postId)
    }

    fun isAlreadySent(userId: Long, postId: Long): Boolean {
        return redisTemplate
            .opsForSet()
            .isMember(pushedPostKey(postId), userId.toString()) == true
    }

    fun markAsSent(userId: Long, postId: Long) {
        val key = pushedPostKey(postId)
        val isNewKey = redisTemplate.opsForSet().add(key, userId.toString()) == NEW_ELEMENT_COUNT
        if (isNewKey) {
            redisTemplate.expire(key, HISTORY_TTL)
        }
    }

    private fun pushedPostKey(postId: Long) = "community:pushed_post:$postId"

    companion object {
        private val HISTORY_TTL = Duration.ofHours(2)
        private const val NEW_ELEMENT_COUNT = 1L
    }
}
