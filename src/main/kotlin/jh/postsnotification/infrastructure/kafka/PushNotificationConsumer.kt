package jh.postsnotification.infrastructure.kafka

import jh.postsnotification.application.dto.PushNotificationBatchMessage
import jh.postsnotification.application.service.CandidatePostService
import jh.postsnotification.application.service.PushHistoryService
import jh.postsnotification.domain.enums.PushTime
import jh.postsnotification.infrastructure.push.PushClient
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
                    log.error("푸시 처리 실패 - userId={}", userId, e)
                    // TODO: DLQ & redrive
                }
            }
        }
        ack.acknowledge()
    }

    /**
     * 추천글 푸시 발송 Flow
     * 1. 유저별 후보 게시글 조회
     * 2. 중복 발송 체크 & 후보글 랜덤 선택
     * 3. 푸시 발송
     * 4. 발송 이력 저장
     */
    private suspend fun process(userId: Long, pushTime: PushTime) {
        val postIds = candidatePostService.getCandidatePostIds(userId, pushTime)

        val postId = postIds
            .filter { pushHistoryService.isAvailablePost(userId, it) }
            .randomOrNull() ?: return

        pushClient.sendPush(userId, postId)

        pushHistoryService.markAsSent(userId, postId)
    }
}
