package jh.postsnotification.infrastructure.kafka

import jh.postsnotification.application.dto.PushNotificationBatchMessage
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class PushNotificationProducer(
    private val kafkaTemplate: KafkaTemplate<String, PushNotificationBatchMessage>,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun send(message: PushNotificationBatchMessage) {
        kafkaTemplate.send(TOPIC, message)
            .whenComplete { _, ex ->
                if (ex != null) {
                    log.error("이벤트 발행 실패 - userCount: {}, startId: {}", message.userIds.size, message.userIds.firstOrNull(), ex)
                    // TODO: 모니터링 or DLT

                    log.error("푸시 이벤트 발행 실패 :: TOPIC: ${TOPIC}, userCount: ${message.userIds.size}, startId: ${message.userIds.firstOrNull()}", ex)
                }
            }
    }

    companion object {
        const val TOPIC = "push-notification-batch"
    }
}
