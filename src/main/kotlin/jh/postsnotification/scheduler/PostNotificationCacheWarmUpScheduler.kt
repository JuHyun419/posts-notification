package jh.postsnotification.scheduler

import jh.postsnotification.application.service.CacheWarmUpService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class PostNotificationCacheWarmUpScheduler(
    private val cacheWarmUpService: CacheWarmUpService,
) {
    @Scheduled(cron = "0 45 11 * * *")
    fun warmUpForLunch() {
        cacheWarmUpService.warmUp()
    }

    @Scheduled(cron = "0 45 17 * * *")
    fun warmUpForDinner() {
        cacheWarmUpService.warmUp()
    }
}
