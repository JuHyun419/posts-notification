package jh.postsnotification.application.service

import jh.postsnotification.domain.entity.Post
import jh.postsnotification.domain.enums.PushTime
import jh.postsnotification.domain.repository.CommunityRepository
import jh.postsnotification.domain.repository.PostRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private typealias CommunityPosts = Pair<Long, List<Post>>

@Service
class CacheWarmUpService(
    private val communityRepository: CommunityRepository,
    private val postRepository: PostRepository,
    private val stringRedisTemplate: StringRedisTemplate,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun warmUp(pushTime: PushTime) = runBlocking {
        val communityIds = communityRepository.findAllActiveIds()
        log.info("CacheWarmUp community count: {}", communityIds.size)

        fetchPostsByCommunity(communityIds)
            .forEach { (communityId, posts) -> cachePostIds(pushTime, communityId, posts) }

        log.info("CacheWarmUp End!")
    }

    private suspend fun fetchPostsByCommunity(communityIds: List<Long>): List<CommunityPosts> =
        coroutineScope {
            communityIds
                .map { communityId: Long ->
                    async(Dispatchers.IO) {
                        communityId to postRepository.findRandomByCommunityId(
                            communityId = communityId,
                            pageable = PageRequest.of(0, 10),
                        )
                    }
                }
                .awaitAll()
        }

    // TODO: Redis 모듈 분리
    private fun cachePostIds(pushTime: PushTime, communityId: Long, posts: List<Post>) {
        if (posts.isEmpty()) return

        val key = warmUpCacheKey(pushTime, communityId)
        val postIds = posts.map { it.id.toString() }.toTypedArray()
        stringRedisTemplate.opsForSet().add(key, *postIds)
        stringRedisTemplate.expire(key, CACHE_TTL)
    }

    private fun warmUpCacheKey(pushTime: PushTime, communityId: Long): String {
        val date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
        return "$CACHE_KEY_PREFIX:$date:${pushTime.name}:$communityId"
    }

    companion object {
        private const val CACHE_KEY_PREFIX = "community:candidate_post:cache_warmup"
        private val CACHE_TTL = Duration.ofHours(4)
    }
}
