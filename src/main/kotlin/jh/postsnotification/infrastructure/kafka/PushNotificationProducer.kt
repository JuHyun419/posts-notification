package jh.postsnotification.infrastructure.kafka

import jh.postsnotification.application.dto.PushNotificationBatchMessage
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class PushNotificationProducer(
    private val kafkaTemplate: KafkaTemplate<String, PushNotificationBatchMessage>,
) {
    fun send(message: PushNotificationBatchMessage) {
        kafkaTemplate.send(TOPIC, message)
    }

    companion object {
        const val TOPIC = "push-notification-batch"
    }
}
