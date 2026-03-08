package jh.postsnotification.domain.repository

import jh.postsnotification.domain.entity.Community
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface CommunityRepository : JpaRepository<Community, Long> {
    @Query("SELECT c.id FROM Community c WHERE c.deletedAt IS NULL")
    fun findAllActiveIds(): List<Long>
}
