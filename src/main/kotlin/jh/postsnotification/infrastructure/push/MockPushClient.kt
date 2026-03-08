package jh.postsnotification.infrastructure.push

import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class MockPushClient : PushClient {

    private val log = LoggerFactory.getLogger(javaClass)

    override suspend fun sendPush(userId: Long, postId: Long) {
        delay(50)
        log.info("Push 발송 - userId={}, postId={}", userId, postId)
    }

    override suspend fun sendNotification(userId: Long, postId: Long) {
        delay(50)
        log.info("Notification 발송 - userId={}, postId={}", userId, postId)
    }
}
