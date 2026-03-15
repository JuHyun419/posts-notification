package jh.postsnotification.application.service

import jh.postsnotification.domain.entity.Post
import jh.postsnotification.domain.repository.CommunityRepository
import jh.postsnotification.domain.repository.PostRepository
import jh.postsnotification.infrastructure.redis.RedisCacheManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private typealias CommunityPosts = Pair<Long, List<Post>>

@Service
class CacheWarmUpService(
    private val communityRepository: CommunityRepository,
    private val postRepository: PostRepository,
    private val redisCacheManager: RedisCacheManager,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun warmUp() = runBlocking {
        val communityIds = communityRepository.findAllActiveIds()
        log.info("CacheWarmUp community count: {}", communityIds.size)

        fetchPostsByCommunity(communityIds)
            .forEach { (communityId, posts) -> cachePostIds(communityId, posts) }

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

    private fun cachePostIds(communityId: Long, posts: List<Post>) {
        if (posts.isEmpty()) return

        val key = warmUpCacheKey(communityId)
        val postIds = posts.map { it.id.toString() }.toTypedArray()
        redisCacheManager.addToSet(key, CACHE_TTL, *postIds)
    }

    private fun warmUpCacheKey(communityId: Long): String {
        val date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
        return "$CACHE_KEY_PREFIX:$date:$communityId"
    }

    companion object {
        const val CACHE_KEY_PREFIX = "community:candidate_post:cache_warmup"
        private val CACHE_TTL = Duration.ofHours(4)
    }
}
