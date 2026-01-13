package ro.snapify.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.ContentUris
import android.content.Intent
import android.content.pm.ServiceInfo
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ro.snapify.ScreenshotApp
import ro.snapify.config.MediaMonitorConfig
import ro.snapify.data.entity.MediaItem
import ro.snapify.events.MediaEvent
import ro.snapify.ui.MainActivity
import ro.snapify.ui.MainViewModel
import ro.snapify.ui.RecomposeReason
import ro.snapify.util.DebugLogger
import ro.snapify.util.MediaFileValidator
import ro.snapify.util.NotificationHelper
import ro.snapify.util.PermissionUtils
import ro.snapify.util.UriPathConverter
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class ScreenshotMonitorService : Service() {

    private fun performInitialSetup() {
        setupContentObserver()
        startDeletionCheckTimer()
        startGlobalNotificationUpdater()
        startJobCleanupTimer()

        serviceScope.launch {
            DebugLogger.info(
                "ScreenshotMonitorService",
                "Scanning existing media on service start",
            )
            mediaScanner.scanExistingMedia()
            observeConfiguredFolders()
            mediaScanner.cleanUpExpiredMediaItems()
            mediaScanner.cleanUpMissingMediaItems()
        }
    }

    @Inject
    lateinit var repository: ro.snapify.data.repository.MediaRepository

    @Inject
    lateinit var recomposeFlow: MutableSharedFlow<RecomposeReason>

    @Inject
    lateinit var preferences: ro.snapify.data.preferences.AppPreferences

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private lateinit var contentObserver: ContentObserver
    private var deletionCheckJob: Job? = null
    private var globalNotificationUpdateJob: Job? = null
    private var jobCleanupJob: Job? = null

    // Use DeletionTimerManager for managing deletion timers
    private lateinit var deletionTimerManager: DeletionTimerManager
    private lateinit var mediaScanner: MediaScannerHelper

    // Deduplication for screenshot notifications - track recent notifications
    private val recentNotifications = mutableMapOf<String, Long>() // filePath -> timestamp

    override fun onCreate() {
        super.onCreate()

        DebugLogger.info("ScreenshotMonitorService", "Service onCreate() called")

        // Initialize helpers
        mediaScanner = MediaScannerHelper(contentResolver, repository, preferences, serviceScope)
        deletionTimerManager = DeletionTimerManager(
            this,
            repository,
            serviceScope,
            onItemDeleted = { mediaId ->
                MainViewModel.mediaEventFlow.tryEmit(MediaEvent.ItemDeleted(mediaId))
            },
        )

        // For foreground service, we must call startForeground within a few seconds
        // So call it immediately, even if we'll stop due to missing permissions
        startForeground(
            NOTIFICATION_ID,
            createForegroundNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
        )

        // Check permissions after starting foreground to avoid system timeout
        if (PermissionUtils.getMissingPermissions(this).isNotEmpty()) {
            DebugLogger.error(
                "ScreenshotMonitorService",
                "Required permissions not granted, stopping service",
            )
            stopSelf()
            return
        }

        performInitialSetup()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        DebugLogger.info("ScreenshotMonitorService", "onStartCommand called")

        if (intent?.getBooleanExtra("rescan", false) == true) {
            serviceScope.launch {
                DebugLogger.info("ScreenshotMonitorService", "Rescanning triggered from intent")
                mediaScanner.scanExistingMedia()
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
            PendingIntent.FLAG_IMMUTABLE,
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
                uri?.let { handleNewMedia(it) }
            }
        }

        val imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        contentResolver.registerContentObserver(imageUri, true, contentObserver)
        contentResolver.registerContentObserver(videoUri, true, contentObserver)
        DebugLogger.info(
            "ScreenshotMonitorService",
            "Content observers registered for $imageUri and $videoUri",
        )
    }

    private fun handleNewMedia(uri: Uri) {
        val isVideo = uri.toString().contains("video")
        val projection = if (isVideo) {
            arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.DATA,
                MediaStore.MediaColumns.RELATIVE_PATH, // For Android 11+
            )
        } else {
            arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.DATA,
                MediaStore.MediaColumns.RELATIVE_PATH, // For Android 11+
            )
        }
        serviceScope.launch {
            try {
                DebugLogger.debug("ScreenshotMonitorService", "New media detected: $uri")
                contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idIndex =
                            cursor.getColumnIndexOrThrow(if (isVideo) MediaStore.Video.Media._ID else MediaStore.Images.Media._ID)
                        val fileName =
                            cursor.getString(cursor.getColumnIndexOrThrow(if (isVideo) MediaStore.Video.Media.DISPLAY_NAME else MediaStore.Images.Media.DISPLAY_NAME))
                        val sizeIndex =
                            cursor.getColumnIndexOrThrow(if (isVideo) MediaStore.Video.Media.SIZE else MediaStore.Images.Media.SIZE)
                        val dateIndex =
                            cursor.getColumnIndexOrThrow(if (isVideo) MediaStore.Video.Media.DATE_ADDED else MediaStore.Images.Media.DATE_ADDED)
                        val dataIndex =
                            cursor.getColumnIndex(if (isVideo) MediaStore.Video.Media.DATA else MediaStore.Images.Media.DATA)

                        val id = cursor.getLong(idIndex)
                        val contentUri = ContentUris.withAppendedId(
                            if (isVideo) MediaStore.Video.Media.EXTERNAL_CONTENT_URI else MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            id,
                        ).toString()

                        // Extract file path with fallbacks for Android 11+
                        var filePath: String? = null
                        try {
                            // Try DATA column first
                            if (dataIndex != -1) {
                                filePath = cursor.getString(dataIndex)
                            }

                            // Fallback: try RELATIVE_PATH for Android 11+
                            if (filePath.isNullOrEmpty()) {
                                val relativePathIndex = cursor.getColumnIndex(MediaStore.MediaColumns.RELATIVE_PATH)
                                if (relativePathIndex != -1) {
                                    val relativePath = cursor.getString(relativePathIndex)
                                    if (!relativePath.isNullOrEmpty()) {
                                        filePath = "${android.os.Environment.getExternalStorageDirectory().absolutePath}/${relativePath}$fileName"
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            DebugLogger.warning("ScreenshotMonitorService", "Error extracting file path: ${e.message}")
                        }

                        val fileSize = cursor.getLong(sizeIndex)
                        val dateAdded = cursor.getLong(dateIndex) * 1000L

                        if (MediaFileValidator.isPendingFile(filePath ?: "")) {
                            DebugLogger.debug("ScreenshotMonitorService", "Ignoring pending file: $fileName")
                            return@use
                        }

                        val isMedia = filePath != null && MediaFileValidator.isMediaFile(filePath)

                        if (isMedia) {
                            DebugLogger.info("ScreenshotMonitorService", "Media file detected: $fileName")
                            delay(MediaMonitorConfig.PROCESSING_DELAY_MS)

                            val existing = filePath?.let { repository.getByFilePath(it) }
                            DebugLogger.debug("ScreenshotMonitorService", "Existing screenshot for $filePath: ${existing?.id}")
                            if (existing == null) {
                                // Check for recent notification to prevent duplicates
                                val currentTime = System.currentTimeMillis()
                                val dedupeKey = filePath ?: contentUri ?: "unknown_$currentTime"
                                val lastNotificationTime = recentNotifications[dedupeKey] ?: 0L

                                if (currentTime - lastNotificationTime > MediaMonitorConfig.NOTIFICATION_DEDUPE_WINDOW) {
                                    // Update recent notifications
                                    recentNotifications[dedupeKey] = currentTime

                                    // Clean up old entries
                                    recentNotifications.entries.removeIf { (_, timestamp) ->
                                        currentTime - timestamp > MediaMonitorConfig.NOTIFICATION_DEDUPE_WINDOW
                                    }

                                    DebugLogger.info("ScreenshotMonitorService", "Processing new screenshot: $fileName")
                                    processNewScreenshot(filePath, contentUri, fileName, fileSize, dateAdded)
                                } else {
                                    DebugLogger.info(
                                        "ScreenshotMonitorService",
                                        "Skipping duplicate notification for $fileName",
                                    )
                                }
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

    private fun observeConfiguredFolders() {
        serviceScope.launch {
            preferences.mediaFolderUris.collect { uris ->
                DebugLogger.info("ScreenshotMonitorService", "Configured folders changed: $uris")
                mediaScanner.scanExistingMedia()
                recomposeFlow.emit(RecomposeReason.Other)
            }
        }
    }

    private suspend fun processNewScreenshot(
        filePath: String?,
        contentUri: String?,
        fileName: String,
        fileSize: Long,
        createdAt: Long,
    ) {
        // Validate existence
        val exists = validateMediaExists(contentUri, filePath)
        if (!exists) {
            DebugLogger.warning("ScreenshotMonitorService", "File doesn't exist or is empty: $fileName")
            return
        }

        // Get configured media folder paths and validate file is in one of them
        val configuredFolderPaths = try {
            preferences.mediaFolderPaths.first()
        } catch (e: Exception) {
            DebugLogger.warning("ScreenshotMonitorService", "Error getting configured folder paths: ${e.message}")
            setOf(UriPathConverter.getDefaultScreenshotUri())
        }

        // If no resolved paths available, fall back to URIs
        val foldersToCheck = if (configuredFolderPaths.isNotEmpty()) {
            configuredFolderPaths.toList()
        } else {
            try {
                preferences.mediaFolderUris.first().let { uris ->
                    UriPathConverter.decodeMediaFolderUris(uris.toList())
                }
            } catch (e: Exception) {
                listOf(UriPathConverter.getDefaultScreenshotUri())
            }
        }

        // Validate file is in one of the configured folders
        if (!filePath.isNullOrEmpty() && !UriPathConverter.isInMediaFolder(filePath, foldersToCheck)) {
            DebugLogger.debug("ScreenshotMonitorService", "File not in configured folders, ignoring: $fileName (checked: $foldersToCheck)")
            return
        }

        val actualFileSize = filePath?.let { File(it).length() } ?: fileSize
        val isManualMode = preferences.isManualMarkMode.first()
        val mode = if (isManualMode) "MANUAL" else "AUTOMATIC"
        DebugLogger.info("ScreenshotMonitorService", "Processing screenshot in $mode mode: $fileName")

        if (isManualMode) {
            val mediaItem = MediaItem(
                filePath = filePath ?: "",
                fileName = fileName,
                fileSize = actualFileSize,
                createdAt = createdAt,
                deletionTimestamp = null,
                isKept = false,
                contentUri = contentUri,
            )

            // Emit detection event
            MainViewModel.mediaEventFlow.tryEmit(MediaEvent.ItemDetected(-1L, filePath ?: ""))
            DebugLogger.info("ScreenshotMonitorService", "Emitted ItemDetected for ${mediaItem.fileName}")

            val id = repository.insert(mediaItem)
            DebugLogger.info("ScreenshotMonitorService", "Media item inserted to DB with ID: $id (Manual Mode)")

            if (id <= 0) {
                DebugLogger.error("ScreenshotMonitorService", "Failed to insert media item, invalid ID: $id")
                return
            }

            // Notify UI of new item
            MainViewModel.mediaEventFlow.tryEmit(MediaEvent.ItemAdded(mediaItem.copy(id = id)))
            DebugLogger.info("ScreenshotMonitorService", "Emitted new item to UI: ${mediaItem.fileName}")

            // Show overlay for manual mode
            val overlayIntent = Intent(this, OverlayService::class.java).apply {
                putExtra("media_id", id)
                putExtra("file_path", filePath)
            }
            startService(overlayIntent)
            DebugLogger.info("ScreenshotMonitorService", "Overlay shown for manual mode media item ID: $id")
        } else {
            // Automatic mode
            val deletionTime = preferences.deletionTimeMillis.first()
            val deletionTimestamp = System.currentTimeMillis() + deletionTime

            val mediaItem = MediaItem(
                filePath = filePath ?: "",
                fileName = fileName,
                fileSize = actualFileSize,
                createdAt = createdAt,
                deletionTimestamp = deletionTimestamp,
                isKept = false,
                contentUri = contentUri,
            )

            // Emit detection event
            MainViewModel.mediaEventFlow.tryEmit(MediaEvent.ItemDetected(-1L, filePath ?: ""))
            DebugLogger.info("ScreenshotMonitorService", "Emitted ItemDetected for ${mediaItem.fileName} (Automatic Mode)")

            val id = repository.insert(mediaItem)
            DebugLogger.info("ScreenshotMonitorService", "Media item inserted to DB with ID: $id (Automatic Mode)")

            // Notify UI of new item
            MainViewModel.mediaEventFlow.tryEmit(MediaEvent.ItemAdded(mediaItem.copy(id = id)))
            DebugLogger.info("ScreenshotMonitorService", "Emitted new item to UI: ${mediaItem.fileName}")

            // Launch deletion timer using manager
            deletionTimerManager.launchDeletionTimer(id, deletionTime)

            // Show initial notification
            val calculatedDeletionTimestamp = System.currentTimeMillis() + deletionTime
            NotificationHelper.showScreenshotNotification(
                this,
                id,
                fileName,
                mediaItem.filePath,
                calculatedDeletionTimestamp,
                deletionTime,
                isManualMode = false,
                preferences = preferences,
            )
            DebugLogger.info("ScreenshotMonitorService", "Notification shown for media item ID: $id")
        }
    }

    /**
     * Validates media file exists and is accessible.
     */
    private fun validateMediaExists(contentUri: String?, filePath: String?): Boolean {
        contentUri?.let { uriStr ->
            try {
                val uri = uriStr.toUri()
                contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                    return pfd.statSize > 0
                }
            } catch (e: Exception) {
                DebugLogger.debug("ScreenshotMonitorService", "ContentUri validation failed: ${e.message}")
            }
        }

        return try {
            val file = File(filePath ?: "")
            file.exists() && file.length() >= MediaMonitorConfig.MIN_FILE_SIZE_BYTES
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Single global notification updater.
     * Updates all active notifications in one loop instead of per-item.
     * Reduces CPU usage and battery drain.
     */
    private fun startGlobalNotificationUpdater() {
        globalNotificationUpdateJob = serviceScope.launch {
            while (true) {
                try {
                    delay(MediaMonitorConfig.NOTIFICATION_UPDATE_INTERVAL_MS)

                    val activeJobIds = deletionTimerManager.getActiveJobIds()
                    if (activeJobIds.isEmpty()) continue

                    // Batch update all active notifications
                    activeJobIds.forEach { mediaId ->
                        try {
                            val mediaItem = repository.getById(mediaId) ?: return@forEach
                            if (mediaItem.deletionTimestamp != null && !mediaItem.isKept) {
                                val deletionTime = mediaItem.deletionTimestamp!! - System.currentTimeMillis()
                                NotificationHelper.showScreenshotNotification(
                                    this@ScreenshotMonitorService,
                                    mediaItem.id,
                                    mediaItem.fileName,
                                    mediaItem.filePath,
                                    mediaItem.deletionTimestamp!!,
                                    preferences.deletionTimeMillis.first(),
                                    isManualMode = false,
                                    preferences = preferences,
                                )
                            }
                        } catch (e: Exception) {
                            DebugLogger.warning(
                                "ScreenshotMonitorService",
                                "Error updating notification for $mediaId: ${e.message}",
                            )
                        }
                    }
                } catch (e: Exception) {
                    if (e !is kotlinx.coroutines.CancellationException) {
                        DebugLogger.error("ScreenshotMonitorService", "Error in notification updater", e)
                    }
                }
            }
        }
        DebugLogger.info("ScreenshotMonitorService", "Global notification updater started")
    }

    /**
     * Deletion check timer: Validates and processes expired items.
     */
    private fun startDeletionCheckTimer() {
        deletionCheckJob = serviceScope.launch {
            while (true) {
                try {
                    val currentTime = System.currentTimeMillis()
                    val expiredMediaItems = repository.getExpiredMediaItems(currentTime)

                    if (expiredMediaItems.isNotEmpty()) {
                        DebugLogger.info(
                            "ScreenshotMonitorService",
                            "Found ${expiredMediaItems.size} expired media items, initiating deletion",
                        )
                        expiredMediaItems.forEach { mediaItem ->
                            // Cancel the timer first
                            deletionTimerManager.cancelDeletionTimer(mediaItem.id)
                            // Then delete the item (this will be handled by the deletion manager)
                            // Trigger immediate deletion since timer has expired
                            launchDeletionTimer(mediaItem.id, 0L)
                        }
                    }

                    // Check for media items no longer marked for deletion
                    val allMarked = repository.getMarkedMediaItems().first()
                    val currentlyTracked = deletionTimerManager.getActiveJobIds()
                    val stillMarked = allMarked.map { it.id }.toSet()

                    (currentlyTracked - stillMarked).forEach { mediaItemId ->
                        DebugLogger.info(
                            "ScreenshotMonitorService",
                            "Cancelling timer for item $mediaItemId - no longer marked for deletion",
                        )
                        deletionTimerManager.cancelDeletionTimer(mediaItemId)
                    }

                    delay(MediaMonitorConfig.DELETION_CHECK_INTERVAL_MS)
                } catch (e: Exception) {
                    if (e !is kotlinx.coroutines.CancellationException) {
                        DebugLogger.error("ScreenshotMonitorService", "Error in deletion check", e)
                    }
                }
            }
        }
        DebugLogger.info("ScreenshotMonitorService", "Deletion check timer started")
    }

    /**
     * Launches a deletion timer (wrapper for deletion manager).
     * Used for both automatic and manual mode deletions.
     */
    private fun launchDeletionTimer(mediaId: Long, delayMillis: Long) {
        deletionTimerManager.launchDeletionTimer(mediaId, delayMillis)
    }

    /**
     * Periodic job cleanup: Remove stale jobs that are no longer active.
     * Prevents memory leaks from accumulating completed jobs.
     */
    private fun startJobCleanupTimer() {
        jobCleanupJob = serviceScope.launch {
            while (true) {
                try {
                    delay(MediaMonitorConfig.JOB_CLEANUP_INTERVAL_MS)
                    deletionTimerManager.cleanupStaleJobs()
                    DebugLogger.debug("ScreenshotMonitorService", "Cleaned up stale jobs")
                } catch (e: Exception) {
                    if (e !is kotlinx.coroutines.CancellationException) {
                        DebugLogger.warning("ScreenshotMonitorService", "Error in job cleanup", e)
                    }
                }
            }
        }
        DebugLogger.info("ScreenshotMonitorService", "Job cleanup timer started")
    }

    override fun onDestroy() {
        super.onDestroy()
        DebugLogger.info("ScreenshotMonitorService", "Service onDestroy() called")

        // Cancel all timers and jobs
        deletionTimerManager.cancelAll()
        globalNotificationUpdateJob?.cancel()
        deletionCheckJob?.cancel()
        jobCleanupJob?.cancel()

        // Clear notification deduplication cache
        recentNotifications.clear()

        if (::contentObserver.isInitialized) {
            contentResolver.unregisterContentObserver(contentObserver)
        }

        serviceJob.cancel()
        DebugLogger.info("ScreenshotMonitorService", "All timers and observers cleaned up")
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
    }
}
