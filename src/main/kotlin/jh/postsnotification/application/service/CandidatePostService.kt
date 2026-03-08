package jh.postsnotification.application.service

import jh.postsnotification.domain.enums.PushTime
import jh.postsnotification.domain.repository.UserCommunityRepository
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class CandidatePostService(
    private val userCommunityRepository: UserCommunityRepository,
    private val stringRedisTemplate: StringRedisTemplate,
) {
    fun getCandidatePostIds(userId: Long, pushTime: PushTime): List<Long> {
        val communityIds = userCommunityRepository.findCommunityIdsByUserId(userId)
        if (communityIds.isEmpty()) return emptyList()

        return communityIds
            .mapNotNull { communityId ->
                stringRedisTemplate.opsForSet().members(candidateCacheKey(communityId, pushTime))
            }
            .flatten()
            .map { it.toLong() }
    }

    private fun candidateCacheKey(communityId: Long, pushTime: PushTime): String {
        val date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
        return "$CACHE_KEY_PREFIX:$date:${pushTime.name}:$communityId"
    }

    companion object {
        private const val CACHE_KEY_PREFIX = "community:candidate_post:cache_warmup"
    }
}
