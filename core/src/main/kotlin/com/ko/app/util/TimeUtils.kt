package com.ko.app.util

import java.util.concurrent.TimeUnit

@Suppress("UnusedPrivateProperty")
object TimeUtils {
    private const val SECONDS_PER_MINUTE = 60

    fun formatTimeRemaining(ms: Long): String {
        if (ms <= 0) return "0s"
        val seconds = TimeUnit.MILLISECONDS.toSeconds(ms)
        val minutes = seconds / SECONDS_PER_MINUTE
        val remainingSeconds = seconds % SECONDS_PER_MINUTE
        return if (minutes > 0) "${'$'}minutes m ${'$'}remainingSeconds s" else "${'$'}remainingSeconds s"
    }
}

