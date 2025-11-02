package com.ko.app.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.ContentUris
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.ko.app.ScreenshotApp
import com.ko.app.data.entity.Screenshot
import com.ko.app.events.AppEvents
import com.ko.app.ui.MainActivity
import com.ko.app.util.DebugLogger
import com.ko.app.util.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

private const val MILLIS_PER_SECOND = 1000L
private const val PROCESSING_DELAY_MS = 500L
private const val CLEANUP_DELAY_MS = 1000L

@AndroidEntryPoint
class ScreenshotMonitorService : Service() {

    @Inject
    lateinit var repository: com.ko.app.data.repository.ScreenshotRepository

    @Inject
    lateinit var preferences: com.ko.app.data.preferences.AppPreferences

    @Inject
    lateinit var notificationHelperInjected: NotificationHelper

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private lateinit var contentObserver: ContentObserver

    override fun onCreate() {
        super.onCreate()
        DebugLogger.info("ScreenshotMonitorService", "Service onCreate() called")
        startForeground(NOTIFICATION_ID, createForegroundNotification())
        setupContentObserver()

        serviceScope.launch {
            DebugLogger.info("ScreenshotMonitorService", "Scanning existing screenshots on service start")
            scanExistingScreenshots()

            // Clean up expired screenshots whose files are gone
            val currentTime = System.currentTimeMillis()
            val expired = repository.getExpiredScreenshots(currentTime)
            expired.filter { screenshot ->
                screenshot.contentUri?.let { uriStr ->
                    try {
                        val uri = uriStr.toUri()
                        val cursor = contentResolver.query(uri, arrayOf(MediaStore.Images.Media._ID), null, null, null)
                        cursor?.use { it.count > 0 } ?: !File(screenshot.filePath).exists()
                    } catch (e: Exception) {
                        !File(screenshot.filePath).exists()
                    }
                } ?: !File(screenshot.filePath).exists()
            }.forEach { screenshot ->
                repository.delete(screenshot)
                DebugLogger.info("ScreenshotMonitorService", "Cleaned up expired screenshot: ${'$'}{screenshot.fileName}")
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        DebugLogger.info("ScreenshotMonitorService", "onStartCommand called")

        if (intent?.getBooleanExtra("rescan", false) == true) {
            serviceScope.launch {
                DebugLogger.info("ScreenshotMonitorService", "Rescanning triggered from intent")
                scanExistingScreenshots()
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createForegroundNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, ScreenshotApp.CHANNEL_ID_SERVICE)
            .setContentTitle("Screenshot Monitor Active")
            .setContentText("Monitoring screenshots for automatic deletion")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun setupContentObserver() {
        contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)
                uri?.let { handleNewScreenshot(it) }
            }
        }

        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        contentResolver.registerContentObserver(uri, true, contentObserver)
        DebugLogger.info("ScreenshotMonitorService", "Content observer registered for $uri")
    }

    private fun handleNewScreenshot(uri: Uri) {
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATA
        )
        serviceScope.launch {
            try {
                DebugLogger.debug("ScreenshotMonitorService", "New media detected: $uri")
                contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                        val fileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME))
                        val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                        val dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                        val dataIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA)

                        val id = cursor.getLong(idIndex)
                        val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id).toString()

                        val filePath = if (dataIndex != -1) cursor.getString(dataIndex) else null
                        val fileSize = cursor.getLong(sizeIndex)
                        val dateAdded = cursor.getLong(dateIndex) * MILLIS_PER_SECOND

                        if (filePath?.contains(".pending") == true) {
                            DebugLogger.debug("ScreenshotMonitorService", "Ignoring pending file: $fileName")
                            return@use
                        }

                        val isScreenshot = if (filePath != null) {
                            isScreenshotFile(filePath)
                        } else {
                            fileName.lowercase().contains("screenshot")
                        }

                        if (isScreenshot) {
                            DebugLogger.info("ScreenshotMonitorService", "Screenshot file detected: $fileName")
                            delay(PROCESSING_DELAY_MS)

                            val existing = filePath?.let { repository.getByFilePath(it) } ?: null
                            if (existing == null) {
                                DebugLogger.info("ScreenshotMonitorService", "Processing new screenshot: $fileName")
                                processNewScreenshot(filePath, contentUri, fileName, fileSize, dateAdded)
                            } else {
                                DebugLogger.debug("ScreenshotMonitorService", "Screenshot already exists in DB: $fileName")
                            }
                        } else {
                            DebugLogger.debug("ScreenshotMonitorService", "Not a screenshot file: $fileName")
                        }
                    }
                }
            } catch (e: Exception) {
                DebugLogger.error("ScreenshotMonitorService", "Error handling new screenshot", e)
            }
        }
    }

    private suspend fun isScreenshotFile(filePath: String): Boolean {
        val lowerPath = filePath.lowercase()

        // Check if it's in the configured screenshot folder
        val configuredFolder = preferences.screenshotFolder.first()
        val screenshotFolder = run {
            if (configuredFolder.isNotEmpty() && configuredFolder != com.ko.app.data.preferences.AppPreferences.DEFAULT_SCREENSHOT_FOLDER) {
                try {
                    val decoded = java.net.URLDecoder.decode(configuredFolder, "UTF-8")
                    when {
                        decoded.contains("primary:") -> Environment.getExternalStorageDirectory().absolutePath + "/" + decoded.substringAfter("primary:")
                        decoded.contains("tree/") -> {
                            val parts = decoded.substringAfter("tree/").split(":" )
                            if (parts.size >= 2) Environment.getExternalStorageDirectory().absolutePath + "/" + parts[1] else decoded
                        }
                        else -> decoded
                    }
                } catch (e: Exception) {
                    configuredFolder
                }
            } else {
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath + "/Screenshots"
            }
        }

        val isInFolder = lowerPath.contains(screenshotFolder.lowercase())
        val hasScreenshotName = lowerPath.contains("screenshot")
        val isImage = lowerPath.endsWith(".png") || lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg")

        return (isInFolder || hasScreenshotName) && isImage
    }

    private suspend fun processNewScreenshot(
        filePath: String?,
        contentUri: String?,
        fileName: String,
        fileSize: Long,
        createdAt: Long
    ) {
        // Validate existence: prefer contentUri, fallback to file path
        val exists = contentUri?.let { uriStr ->
            try {
                val uri = uriStr.toUri()
                contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                    pfd.statSize > 0
                } ?: false
            } catch (e: Exception) {
                false
            }
        } ?: run {
            filePath?.let { path ->
                val f = File(path)
                f.exists() && f.length() > 0L
            } ?: false
        }

        if (!exists) {
            DebugLogger.warning("ScreenshotMonitorService", "File doesn't exist or is empty: $fileName")
            return
        }

        val actualFileSize = filePath?.let { File(it).length() } ?: fileSize
        val isManualMode = preferences.isManualMarkMode.first()
        val mode = if (isManualMode) "MANUAL" else "AUTOMATIC"
        DebugLogger.info("ScreenshotMonitorService", "Processing screenshot in $mode mode: $fileName")

        if (isManualMode) {
            val screenshot = Screenshot(
                filePath = filePath ?: "",
                fileName = fileName,
                fileSize = actualFileSize,
                createdAt = createdAt,
                deletionTimestamp = null,
                isMarkedForDeletion = false,
                isKept = false,
                contentUri = contentUri
            )
            val id = repository.insert(screenshot)
            DebugLogger.info("ScreenshotMonitorService", "Screenshot inserted to DB with ID: ${'$'}id (Manual Mode)")

            val intent = Intent(this, OverlayService::class.java).apply {
                putExtra("screenshot_id", id)
                putExtra("file_path", filePath)
                putExtra("content_uri", contentUri)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            DebugLogger.info("ScreenshotMonitorService", "OverlayService started for screenshot ID: ${'$'}id")
        } else {
            val deletionTime = preferences.getDeletionTimeMillisSync()
            val deletionTimestamp = System.currentTimeMillis() + deletionTime

            val screenshot = Screenshot(
                filePath = filePath ?: "",
                fileName = fileName,
                fileSize = actualFileSize,
                createdAt = createdAt,
                deletionTimestamp = deletionTimestamp,
                isMarkedForDeletion = true,
                isKept = false,
                contentUri = contentUri
            )
            val id = repository.insert(screenshot)
            DebugLogger.info(
                "ScreenshotMonitorService",
                "Screenshot inserted to DB with ID: ${'$'}id (Automatic Mode, marked for deletion)"
            )

            notificationHelperInjected.showScreenshotNotification(id, fileName, deletionTimestamp)
            DebugLogger.info("ScreenshotMonitorService", "Notification shown for screenshot ID: ${'$'}id")

            AppEvents.notifyScreenshotsScanned()
        }
    }

    private fun scanExistingScreenshots() {
        serviceScope.launch {
            try {
                DebugLogger.info("ScreenshotMonitorService", "Starting scan of existing screenshots")

                val configuredFolder = preferences.screenshotFolder.first()
                val screenshotFolder = if (configuredFolder.isNotEmpty() && configuredFolder != com.ko.app.data.preferences.AppPreferences.DEFAULT_SCREENSHOT_FOLDER) {
                    // Decode URI to path
                    java.net.URLDecoder.decode(configuredFolder, "UTF-8").let { decoded ->
                        when {
                            decoded.contains("primary:") -> Environment.getExternalStorageDirectory().absolutePath + "/" + decoded.substringAfter("primary:")
                            decoded.contains("tree/") -> {
                                val parts = decoded.substringAfter("tree/").split(":" )
                                if (parts.size >= 2) {
                                    val path = parts[1]
                                    Environment.getExternalStorageDirectory().absolutePath + "/" + path
                                } else decoded
                            }
                            else -> decoded
                        }
                    }
                } else {
                    // Default
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath + "/Screenshots"
                }

                DebugLogger.info("ScreenshotMonitorService", "Scanning folder: $screenshotFolder")
                val folder = File(screenshotFolder)
                if (!folder.exists() || !folder.isDirectory) {
                    DebugLogger.warning(
                        "ScreenshotMonitorService",
                        "Screenshot folder doesn't exist: $screenshotFolder, exists=${'$'}{folder.exists()}, isDir=${'$'}{folder.isDirectory}"
                    )
                    return@launch
                }

                val imageFiles = folder.listFiles { file ->
                    file.isFile && (file.extension.lowercase() in listOf("png", "jpg", "jpeg"))
                }

                val count = imageFiles?.size ?: 0
                DebugLogger.info("ScreenshotMonitorService", "Found $count existing screenshot files")

                val screenshotsToImport = imageFiles?.mapNotNull { file ->
                    if (file.exists() && file.length() > 0) {
                        Screenshot(
                            filePath = file.absolutePath,
                            fileName = file.name,
                            fileSize = file.length(),
                            createdAt = file.lastModified(),
                            deletionTimestamp = null,
                            isMarkedForDeletion = false,
                            isKept = false,
                            contentUri = null
                        )
                    } else null
                } ?: emptyList()

                val imported = if (screenshotsToImport.isNotEmpty()) {
                    screenshotsToImport.chunked(500).sumOf { chunk ->
                        repository.insertAll(chunk).count { it > 0 }
                    }
                } else 0

                DebugLogger.info("ScreenshotMonitorService", "Imported ${'$'}imported new screenshots from existing files")

                AppEvents.notifyScreenshotsScanned()
                DebugLogger.info("ScreenshotMonitorService", "Broadcast sent: SCREENSHOTS_SCANNED")
            } catch (e: Exception) {
                DebugLogger.error("ScreenshotMonitorService", "Error scanning existing screenshots", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        DebugLogger.info("ScreenshotMonitorService", "Service onDestroy() called")
        contentResolver.unregisterContentObserver(contentObserver)
        serviceJob.cancel()
        serviceScope.launch {
            delay(CLEANUP_DELAY_MS)
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
    }
}
