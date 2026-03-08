package jh.postsnotification.domain.repository

import jh.postsnotification.domain.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserRepository : JpaRepository<User, Long> {

    @Query(
        """
        SELECT u.id FROM User u 
        WHERE u.id BETWEEN :startId AND :endId 
            AND u.deletedAt IS NULL
            """
    )
    fun findActiveIdsBetween(
        @Param("startId") startId: Long,
        @Param("endId") endId: Long,
    ): List<Long>

    @Query("SELECT MAX(u.id) FROM User u")
    fun findMaxId(): Long?
}
