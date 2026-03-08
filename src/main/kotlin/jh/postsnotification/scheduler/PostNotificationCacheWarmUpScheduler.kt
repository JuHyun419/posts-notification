package jh.postsnotification.scheduler

import jh.postsnotification.application.service.CacheWarmUpService
import jh.postsnotification.domain.enums.PushTime
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class PostNotificationCacheWarmUpScheduler(
    private val cacheWarmUpService: CacheWarmUpService,
) {
    @Scheduled(cron = "0 45 11 * * *")
    fun warmUpForLunch() {
        cacheWarmUpService.warmUp(PushTime.LUNCH)
    }

    @Scheduled(cron = "0 45 17 * * *")
    fun warmUpForDinner() {
        cacheWarmUpService.warmUp(PushTime.DINNER)
    }
}
