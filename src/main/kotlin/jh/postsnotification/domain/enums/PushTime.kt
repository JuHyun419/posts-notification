package jh.postsnotification.domain.enums

enum class PushTime(val hour: Int) {
    LUNCH(12),
    DINNER(18);

    companion object {
        fun from(hour: Int): PushTime {
            return when (hour) {
                LUNCH.hour -> LUNCH
                DINNER.hour -> DINNER
                else -> throw IllegalArgumentException("유효하지 않은 시간: $hour")
            }
        }
    }
}
