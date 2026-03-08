package jh.postsnotification.application.dto

import java.time.LocalDateTime

data class PushNotificationBatchMessage(
    val userIds: List<Long>,
    val publishedAt: LocalDateTime,
)
