package jh.postsnotification.application.service

import jh.postsnotification.infrastructure.redis.RedisCacheManager
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class PushHistoryService(
    private val redisCacheManager: RedisCacheManager,
) {
    fun isAvailablePost(userId: Long, postId: Long): Boolean {
        return !isAlreadySent(userId, postId)
    }

    fun isAlreadySent(userId: Long, postId: Long): Boolean {
        return redisCacheManager.isMember(pushedPostKey(postId), userId.toString())
    }

    fun markAsSent(userId: Long, postId: Long) {
        val key = pushedPostKey(postId)

        redisCacheManager.addToSet(key, HISTORY_TTL, userId.toString())
    }

    private fun pushedPostKey(postId: Long) = "community:pushed_post:$postId"

    companion object {
        private val HISTORY_TTL = Duration.ofHours(8)
    }
}
