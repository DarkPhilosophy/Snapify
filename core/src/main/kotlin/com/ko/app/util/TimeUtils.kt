package com.ko.app.util

import java.util.concurrent.TimeUnit

object TimeUtils {
    fun formatTimeRemaining(ms: Long): String {
        if (ms <= 0) return "0s"
        val seconds = TimeUnit.MILLISECONDS.toSeconds(ms)
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return if (minutes > 0) "${'$'}minutes m ${'$'}remainingSeconds s" else "${'$'}remainingSeconds s"
    }
}

