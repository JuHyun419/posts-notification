package jh.postsnotification.infrastructure.kafka

import jh.postsnotification.application.dto.PushNotificationBatchMessage
import jh.postsnotification.application.service.CandidatePostService
import jh.postsnotification.application.service.PushHistoryService
import jh.postsnotification.domain.enums.PushTime
import jh.postsnotification.infrastructure.push.PushClient
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class PushNotificationConsumer(
    private val candidatePostService: CandidatePostService,
    private val pushHistoryService: PushHistoryService,
    private val pushClient: PushClient,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = [PushNotificationProducer.TOPIC],
        groupId = "push-notification-group",
    )
    fun receive(message: PushNotificationBatchMessage, ack: Acknowledgment) {
        val pushTime = PushTime.from(message.publishedAt.hour)

        runBlocking {
            message.userIds.forEach { userId ->
                try {
                    process(userId, pushTime)
                } catch (e: Exception) {
                    log.error("푸시 발송 실패 :: userId: $userId", e)
                    // TODO: DLQ & redrive
                }
            }
        }
        ack.acknowledge()
    }

    /**
     * 추천글 푸시 발송 Flow
     * 1. 유저별 후보 게시글 조회
     *   - 점심: 후보 게시글에서 랜덤 조회
     *   - 저녁: 점심에 푸시 발송 중복 푸시글 제외
     * 2. 푸시 & 알림 발송
     * 3. 발송 이력 저장 (점심만 저장)
     */
    private suspend fun process(userId: Long, pushTime: PushTime) {
        val postIds = candidatePostService.getCandidatePostIds(userId)

        val postId = when (pushTime) {
            PushTime.LUNCH -> postIds.randomOrNull() ?: return
            PushTime.DINNER -> postIds
                .filter { pushHistoryService.isAvailablePost(userId, it) }
                .randomOrNull() ?: return
        }

        coroutineScope {
            async { pushClient.sendPush(userId, postId) }
            async { pushClient.sendNotification(userId, postId) }
        }

        if (pushTime == PushTime.LUNCH) {
            pushHistoryService.markAsSent(userId, postId)
        }
    }
}
