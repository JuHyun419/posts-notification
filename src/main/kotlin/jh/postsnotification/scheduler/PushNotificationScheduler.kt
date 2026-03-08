package jh.postsnotification.scheduler

import jh.postsnotification.application.dto.PushNotificationBatchMessage
import jh.postsnotification.domain.enums.PushTime
import jh.postsnotification.domain.repository.UserRepository
import jh.postsnotification.infrastructure.kafka.PushNotificationProducer
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class PushNotificationScheduler(
    private val userRepository: UserRepository,
    private val pushNotificationProducer: PushNotificationProducer,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "0 0 12 * * *")
    fun lunchPush() {
        execute(PushTime.LUNCH)
    }

    @Scheduled(cron = "0 0 18 * * *")
    fun dinnerPush() {
        execute(PushTime.DINNER)
    }

    private fun execute(pushTime: PushTime) {
        val now = LocalDateTime.now()
        val maxId = userRepository.findMaxId() ?: throw IllegalStateException("User not exist!")

        log.info("푸시 발송 시작 - pushTime={}", pushTime)

        var startId = 1L
        while (startId <= maxId) {
            val endId = startId + BATCH_SIZE - 1

            val userIds = userRepository.findActiveIdsBetween(startId, endId)
            if (userIds.isNotEmpty()) {
                pushNotificationProducer.send(
                    PushNotificationBatchMessage(userIds = userIds, publishedAt = now)
                )
            }

            startId += BATCH_SIZE
        }

        log.info("푸시 발송 배치 발행 완료 - maxId={}", maxId)
    }

    companion object {
        const val BATCH_SIZE = 1000
    }
}
