package jh.postsnotification.domain.repository

import jh.postsnotification.domain.entity.Member
import org.springframework.data.jpa.repository.JpaRepository

interface MemberRepository : JpaRepository<Member, Long> {

}
