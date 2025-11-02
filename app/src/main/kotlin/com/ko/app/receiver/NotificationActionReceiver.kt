package com.ko.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ko.app.ScreenshotApp
import com.ko.app.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_KEEP -> {
                val screenshotId = intent.getLongExtra("screenshot_id", -1L)
                if (screenshotId != -1L) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val app = context.applicationContext as ScreenshotApp
                            app.repository.markAsKept(screenshotId)

                            val notificationHelper = NotificationHelper(context)
                            notificationHelper.cancelNotification(screenshotId.toInt())
                        } catch (_: Exception) {
                            // ignore errors in broadcast handling
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val ACTION_KEEP = "com.ko.app.ACTION_KEEP"
    }
}
