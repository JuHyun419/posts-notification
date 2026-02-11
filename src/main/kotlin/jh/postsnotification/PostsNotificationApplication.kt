package jh.postsnotification

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PostsNotificationApplication

fun main(args: Array<String>) {
    runApplication<PostsNotificationApplication>(*args)
}
