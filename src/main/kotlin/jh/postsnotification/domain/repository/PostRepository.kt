package jh.postsnotification.domain.repository

import jh.postsnotification.domain.entity.Post
import org.springframework.data.jpa.repository.JpaRepository

interface PostRepository : JpaRepository<Post, Long> {

}
