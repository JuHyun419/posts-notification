package jh.postsnotification.domain.repository

import jh.postsnotification.domain.entity.Post
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PostRepository : JpaRepository<Post, Long> {
    @Query(
        """
        SELECT p FROM Post p 
        WHERE p.communityId = :communityId 
          AND p.deletedAt IS NULL 
        ORDER BY FUNCTION('RAND')
    """
    )
    fun findRandomByCommunityId(
        @Param("communityId") communityId: Long,
        pageable: Pageable,
    ): List<Post>
}
