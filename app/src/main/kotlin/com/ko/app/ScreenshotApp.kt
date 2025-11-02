package com.ko.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.ko.app.data.database.ScreenshotDatabase
import com.ko.app.data.preferences.AppPreferences
import com.ko.app.data.repository.ScreenshotRepository
import com.ko.app.util.DebugLogger
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.work.Configuration
import androidx.hilt.work.HiltWorkerFactory
import javax.inject.Inject

@HiltAndroidApp
class ScreenshotApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var database: ScreenshotDatabase

    @Inject
    lateinit var repository: ScreenshotRepository

    @Inject
    lateinit var preferences: AppPreferences

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Preferences, database and repository are injected by Hilt

        // Read language asynchronously to avoid blocking application start
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val lang = preferences.getLanguageSync()
                val localeTag = if (lang == "ro") "ro" else "en"
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(localeTag))
            } catch (e: Exception) {
                // If anything fails, default locale is used
                DebugLogger.warning("ScreenshotApp", "Failed to set application locale", e)
            }
        }

        DebugLogger.init(this)

        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            val serviceChannel = NotificationChannel(
                CHANNEL_ID_SERVICE,
                "Screenshot Monitor Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors screenshots in the background"
                setShowBadge(false)
            }

            val screenshotChannel = NotificationChannel(
                CHANNEL_ID_SCREENSHOT,
                "Screenshot Deletion Timers",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Shows countdown timers for screenshot deletion"
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(serviceChannel)
            notificationManager.createNotificationChannel(screenshotChannel)
        }
    }

    // Replace getWorkManagerConfiguration() method with WorkManager's property API
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    companion object {
        const val CHANNEL_ID_SERVICE = "screenshot_monitor_service"
        const val CHANNEL_ID_SCREENSHOT = "screenshot_deletion"

        lateinit var instance: ScreenshotApp
            private set
    }
}
