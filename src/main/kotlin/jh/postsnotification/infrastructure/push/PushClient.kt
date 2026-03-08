package jh.postsnotification.infrastructure.push

interface PushClient {
    suspend fun sendPush(userId: Long, postId: Long)
    suspend fun sendNotification(userId: Long, postId: Long)
}
