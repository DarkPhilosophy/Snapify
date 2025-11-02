package com.ko.app.worker

import android.content.Context
import androidx.core.net.toUri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ko.app.util.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File

@HiltWorker
class ScreenshotDeletionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: com.ko.app.data.repository.ScreenshotRepository,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val currentTime = System.currentTimeMillis()
            val expiredScreenshots = repository.getExpiredScreenshots(currentTime)

            expiredScreenshots.forEach { screenshot ->
                deleteScreenshot(screenshot)
            }

            Result.success()
        } catch (_: SecurityException) {
            Result.failure()
        } catch (e: Exception) {
            // Log the exception
            android.util.Log.e("ScreenshotDeletionWorker", "Work failed", e)
            Result.retry()
        }
    }

    private suspend fun deleteScreenshot(screenshot: com.ko.app.data.entity.Screenshot) {
        var deleted = false

        // Prefer deleting via contentUri when available
        screenshot.contentUri?.let { uriStr ->
            try {
                val uri = uriStr.toUri()
                val rows = applicationContext.contentResolver.delete(uri, null, null)
                deleted = rows > 0
            } catch (_: Exception) {
                // ignore and fallback
            }
        }

        if (!deleted) {
            val file = File(screenshot.filePath)
            deleted = !file.exists() || file.delete()
        }

        if (deleted) {
            repository.delete(screenshot)
            notificationHelper.cancelNotification(screenshot.id.toInt())
        }
    }
}
