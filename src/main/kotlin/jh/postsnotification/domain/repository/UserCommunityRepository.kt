package jh.postsnotification.domain.repository

import jh.postsnotification.domain.entity.UserCommunity
import org.springframework.data.jpa.repository.JpaRepository

interface UserCommunityRepository : JpaRepository<UserCommunity, Long> {
    fun findCommunityIdsByUserId(userId: Long): List<Long>
}
