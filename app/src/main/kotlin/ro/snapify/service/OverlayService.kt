package ro.snapify.service

import android.annotation.SuppressLint
import android.app.Service
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ro.snapify.ScreenshotApp
import ro.snapify.data.preferences.AppPreferences
import ro.snapify.data.repository.MediaRepository
import ro.snapify.events.MediaEvent
import ro.snapify.ui.MainViewModel
import ro.snapify.ui.RecomposeReason
import ro.snapify.ui.components.ScreenshotDetectionOverlay
import ro.snapify.ui.theme.AppTheme
import ro.snapify.ui.theme.ThemeMode
import ro.snapify.util.DebugLogger
import ro.snapify.util.NotificationHelper
import ro.snapify.util.PermissionUtils

private const val FIFTEEN_MINUTES = 15L
private const val THREE_DAYS = 3L
private const val ONE_WEEK = 7L

internal data class OverlayRequest(
    val mediaId: Long,
    val filePath: String,
)

internal fun overlayPreviewCandidates(contentUri: String?, filePath: String): List<String> =
    listOfNotNull(
        contentUri?.takeIf { it.isNotBlank() },
        filePath.takeIf { it.isNotBlank() },
    ).distinct()

private fun formatTime(minutes: Int): String {
    val parts = mutableListOf<String>()
    var remaining = minutes

    val years = remaining / 525600
    if (years > 0) {
        parts.add("$years year${if (years > 1) "s" else ""}")
        remaining %= 525600
    }

    val weeks = remaining / 10080
    if (weeks > 0) {
        parts.add("$weeks week${if (weeks > 1) "s" else ""}")
        remaining %= 10080
    }

    val days = remaining / 1440
    if (days > 0) {
        parts.add("$days day${if (days > 1) "s" else ""}")
        remaining %= 1440
    }

    val hours = remaining / 60
    if (hours > 0) {
        parts.add("$hours hour${if (hours > 1) "s" else ""}")
        remaining %= 60
    }

    if (remaining > 0 || parts.isEmpty()) {
        parts.add("$remaining minute${if (remaining != 1) "s" else ""}")
    }

    return parts.joinToString(" ")
}

class OverlayService :
    Service(),
    LifecycleOwner,
    SavedStateRegistryOwner {

    private lateinit var windowManager: WindowManager
    private var overlayView: ComposeView? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private val overlayCreationMutex = Mutex()
    private lateinit var repository: MediaRepository
    private lateinit var recomposeFlow: kotlinx.coroutines.flow.MutableSharedFlow<RecomposeReason>
    private lateinit var preferences: AppPreferences

    private fun handleOverlayException(e: Exception, tag: String = "OverlayService") {
        DebugLogger.error(tag, "Exception: ${e.javaClass.simpleName} - ${e.message}", e)
        overlayView = null
        stopSelf()
    }

    private val lifecycleRegistry = LifecycleRegistry(this)
    private lateinit var savedStateRegistryController: SavedStateRegistryController

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController = SavedStateRegistryController.create(this)
        savedStateRegistryController.performRestore(android.os.Bundle())
        lifecycleRegistry.currentState = Lifecycle.State.CREATED

        // Cleanup all share files on service start
        serviceScope.launch(Dispatchers.IO) {
            cacheDir?.listFiles { file -> file.name.startsWith("share_") }?.forEach {
                try {
                    it.delete()
                } catch (_: Exception) {
                }
            }
        }
    }

    override fun onBind(intent: android.content.Intent?): IBinder? = null

    override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
        val request = OverlayRequest(
            mediaId = intent?.getLongExtra("media_id", -1L) ?: -1L,
            filePath = intent?.getStringExtra("file_path") ?: "",
        )

        DebugLogger.info(
            "OverlayService",
            "onStartCommand called with screenshot ID: ${request.mediaId}, path: ${request.filePath}",
        )

        // Initialize dependencies from application
        val app = application as ScreenshotApp
        repository = app.repository
        preferences = app.preferences
        recomposeFlow = app.recomposeFlow

        if (request.mediaId > 0L) {
            try {
                if (PermissionUtils.hasOverlayPermission(this)) {
                    serviceScope.launch {
                        overlayCreationMutex.withLock { showOverlay(request) }
                    }
                } else {
                    DebugLogger.error(
                        "OverlayService",
                        "Overlay permission not granted - manual mode requires overlay permission",
                    )
                    NotificationHelper.showErrorNotification(
                        this,
                        "Manual Mode Error",
                        "Overlay permission required. Grant in app settings.",
                    )
                    showFallbackNotification(request)
                    stopSelf()
                }
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                DebugLogger.error(
                    "OverlayService",
                    "CRASH in manual mode: ${e.javaClass.simpleName} - ${e.message}",
                    e,
                )
                NotificationHelper.showErrorNotification(
                    this,
                    "Manual Mode Crashed",
                    "Error: ${e.javaClass.simpleName} - ${e.message}",
                )
                showFallbackNotification(request)
                stopSelf()
            }
        } else {
            DebugLogger.error("OverlayService", "Invalid screenshot ID: ${request.mediaId}")
            stopSelf()
        }

        return START_NOT_STICKY
    }

    @SuppressLint("InflateParams")
    private suspend fun showOverlay(request: OverlayRequest) {
        try {
            DebugLogger.info("OverlayService", "Attempting to show overlay")

            if (overlayView != null) {
                DebugLogger.warning("OverlayService", "Overlay already shown, skipping")
                return
            }

            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT,
            ).apply {
                gravity = Gravity.CENTER
            }

            val mediaItem = repository.getById(request.mediaId)
            val thumbnailBitmap = loadOverlayPreview(
                sources = overlayPreviewCandidates(mediaItem?.contentUri, request.filePath),
                isVideo = isVideoPath(mediaItem?.filePath ?: request.filePath),
            )
            val imageBitmap = thumbnailBitmap?.asImageBitmap()
            val themeMode = when (preferences.themeMode.first()) {
                "light" -> ThemeMode.LIGHT
                "dark" -> ThemeMode.DARK
                "dynamic" -> ThemeMode.DYNAMIC
                "oled" -> ThemeMode.OLED
                else -> ThemeMode.SYSTEM
            }

            overlayView = ComposeView(this).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setViewTreeLifecycleOwner(this@OverlayService)
                setViewTreeSavedStateRegistryOwner(this@OverlayService)
                setContent {
                    AppTheme(themeMode = themeMode, skipWindowSetup = true) {
                        val dismissWithNotification =
                            { dismissOverlay(request, showFallbackNotification = true) }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .background(Color.Black.copy(alpha = 0.5f))
                                .clickable { dismissWithNotification() },
                            contentAlignment = Alignment.Center,
                        ) {
                            ScreenshotDetectionOverlay(
                                detectedImage = imageBitmap,
                                on15Minutes = {
                                    handleDeletionTime(
                                        request,
                                        java.util.concurrent.TimeUnit.MINUTES.toMillis(
                                            FIFTEEN_MINUTES,
                                        ),
                                    )
                                },
                                on2Hours = {
                                    handleDeletionTime(
                                        request,
                                        java.util.concurrent.TimeUnit.HOURS.toMillis(2),
                                    )
                                },
                                on3Days = {
                                    handleDeletionTime(
                                        request,
                                        java.util.concurrent.TimeUnit.DAYS.toMillis(THREE_DAYS),
                                    )
                                },
                                on1Week = {
                                    handleDeletionTime(
                                        request,
                                        java.util.concurrent.TimeUnit.DAYS.toMillis(ONE_WEEK),
                                    )
                                },
                                onKeep = { handleKeep(request) },
                                onShare = { handleShare(request) },
                                onClose = { handleClose(request) },
                                onDismiss = dismissWithNotification,
                                onCustomTime = { minutes ->
                                    handleDeletionTime(
                                        request,
                                        java.util.concurrent.TimeUnit.MINUTES.toMillis(minutes.toLong()),
                                    )
                                },
                            )
                        }
                    }
                }
            }

            windowManager.addView(overlayView, params)
            lifecycleRegistry.currentState = Lifecycle.State.RESUMED

            DebugLogger.info("OverlayService", "Overlay view added successfully")
            animateOverlayIn()
        } catch (e: WindowManager.BadTokenException) {
            handleOverlayException(e)
        } catch (e: SecurityException) {
            handleOverlayException(e)
        } catch (e: IllegalStateException) {
            handleOverlayException(e)
        } catch (e: IllegalArgumentException) {
            handleOverlayException(e)
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            DebugLogger.error(
                "OverlayService",
                "Crash prevented: ${e.javaClass.simpleName} - ${e.message}",
                e,
            )
            overlayView = null
            stopSelf()
        }
    }

    private suspend fun loadOverlayPreview(
        sources: List<String>,
        isVideo: Boolean,
    ): Bitmap? = withContext(Dispatchers.IO) {
        sources.firstNotNullOfOrNull { source ->
            runCatching {
                if (isVideo) {
                    if (source.startsWith("content://")) {
                        val uri = android.net.Uri.parse(source)
                        contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                            extractVideoFrame { setDataSource(descriptor.fileDescriptor) }
                        }
                    } else {
                        extractVideoFrame { setDataSource(source) }
                    }
                } else if (source.startsWith("content://")) {
                    val uri = android.net.Uri.parse(source)
                    contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream)
                } else {
                    BitmapFactory.decodeFile(source)
                }
            }.getOrNull()
        }
    }

    private fun isVideoPath(path: String): Boolean =
        path.endsWith(".mp4", ignoreCase = true) ||
                path.endsWith(".avi", ignoreCase = true) ||
                path.endsWith(".mkv", ignoreCase = true)

    private fun extractVideoFrame(configure: MediaMetadataRetriever.() -> Unit): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.configure()
            retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        } finally {
            retriever.release()
        }
    }


    private fun handleDeletionTime(request: OverlayRequest, timeMillis: Long) {
        serviceScope.launch(Dispatchers.IO) {
            val deletionTimestamp = System.currentTimeMillis() + timeMillis

            // Update DB synchronously
            repository.markForDeletion(request.mediaId, deletionTimestamp)

            // The service's deletion check timer will handle the deletion

            // Show notification to confirm the scheduled deletion
            val screenshot = repository.getById(request.mediaId)
            screenshot?.let {
                NotificationHelper.showScreenshotNotification(
                    this@OverlayService,
                    request.mediaId,
                    it.fileName,
                    it.filePath,
                    deletionTimestamp,
                    timeMillis,
                    isManualMode = false, // Show countdown like automatic mode
                    preferences = preferences,
                )
                DebugLogger.info(
                    "OverlayService",
                    "Notification shown after time selection in manual mode",
                )


                withContext(Dispatchers.Main) {
                    // Notify UI to update the specific item
                    DebugLogger.info(
                        "OverlayService",
                        "Emitting ItemUpdated event after marking for deletion",
                    )
                    MainViewModel.mediaEventFlow.tryEmit(MediaEvent.ItemUpdated(it))
                    dismissOverlay(request, showFallbackNotification = false)
                }
            }
        }
    }

    private fun handleKeep(request: OverlayRequest) {
        serviceScope.launch(Dispatchers.IO) {
            repository.markAsKept(request.mediaId)
            val updatedItem = repository.getById(request.mediaId)

            withContext(Dispatchers.Main) {
                // Notify UI to update the specific item
                DebugLogger.info("OverlayService", "Emitting ItemUpdated event after keeping media")
                updatedItem?.let { MainViewModel.mediaEventFlow.tryEmit(MediaEvent.ItemUpdated(it)) }
                dismissOverlay(request, showFallbackNotification = false)
            }
        }
    }

    private fun handleShare(request: OverlayRequest) {
        serviceScope.launch(Dispatchers.IO) {
            try {
                cacheDir.listFiles { file -> file.name.startsWith("share_") }?.forEach { it.delete() }

                val sourceFile = java.io.File(request.filePath)
                if (!sourceFile.exists()) {
                    DebugLogger.error("OverlayService", "Cannot share missing media: ${request.filePath}")
                    return@launch
                }

                val mediaItem = repository.getById(request.mediaId)
                val extension = sourceFile.extension.ifEmpty { "png" }
                val shareFile = java.io.File(cacheDir, "share_${System.currentTimeMillis()}.$extension")
                val mimeType = when (extension.lowercase()) {
                    "mp4", "avi", "mkv", "mov" -> "video/*"
                    else -> "image/*"
                }

                sourceFile.inputStream().use { input ->
                    shareFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                val shareUri =
                    androidx.core.content.FileProvider.getUriForFile(
                        this@OverlayService,
                        "$packageName.fileprovider",
                        shareFile
                    )
                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = mimeType
                    putExtra(android.content.Intent.EXTRA_STREAM, shareUri)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                val chooserIntent = android.content.Intent.createChooser(intent, "Share Screenshot").apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                withContext(Dispatchers.Main) {
                    startActivity(chooserIntent)
                }

                sourceFile.delete()
                mediaItem?.let { repository.delete(it) }
                DebugLogger.info("OverlayService", "Emitting ItemDeleted event after sharing and deleting media")
                MainViewModel.mediaEventFlow.tryEmit(MediaEvent.ItemDeleted(request.mediaId))

                withContext(Dispatchers.Main) {
                    dismissOverlay(request, showFallbackNotification = false)
                }
                // The cached share file must persist until the receiving app has read it.
            } catch (e: Exception) {
                DebugLogger.error("OverlayService", "Error sharing screenshot", e)
            }
        }
    }

    private fun handleClose(request: OverlayRequest) {
        serviceScope.launch(Dispatchers.IO) {
            repository.markAsKept(request.mediaId)

            withContext(Dispatchers.Main) {
                dismissOverlay(request, showFallbackNotification = false)
            }
        }
    }

    private fun animateOverlayIn() {
        // Animation is now handled by Compose
        DebugLogger.info("OverlayService", "Overlay animation in (handled by Compose)")
    }

    private fun dismissOverlay(request: OverlayRequest, showFallbackNotification: Boolean = true) {
        overlayView?.let { view ->
            try {
                if (::windowManager.isInitialized) {
                    windowManager.removeView(view)
                }
                overlayView = null
            } catch (
                @Suppress(
                    "TooGenericExceptionCaught",
                    "PrintStackTrace",
                ) e: Exception,
            ) {
                e.printStackTrace()
                overlayView = null
            }

            // If overlay was dismissed without user interaction, show notification as fallback
            if (showFallbackNotification) {
                showFallbackNotification(request)
            } else {
                stopSelf()
            }
        } ?: run {
            stopSelf()
        }
    }

    private fun showFallbackNotification(request: OverlayRequest) {
        serviceScope.launch(Dispatchers.IO) {
            val screenshot = repository.getById(request.mediaId)
            screenshot?.let {
                val deletionTime = preferences.deletionTimeMillis.first()
                val deletionTimestamp = System.currentTimeMillis() + deletionTime
                NotificationHelper.showScreenshotNotification(
                    this@OverlayService,
                    request.mediaId,
                    it.fileName,
                    it.filePath,
                    deletionTimestamp,
                    deletionTime,
                    isManualMode = true,
                    preferences = preferences,
                )
                DebugLogger.info("OverlayService", "Fallback notification shown for manual mode")
            }
        }
    }

    override fun onDestroy() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        super.onDestroy()
        // Cancel any running coroutines to avoid leaks
        try {
            serviceScope.cancel()
        } catch (_: Exception) {
            // ignore
        }
        overlayView?.let {
            try {
                if (::windowManager.isInitialized) {
                    windowManager.removeView(it)
                }
            } catch (@Suppress("TooGenericExceptionCaught", "PrintStackTrace") e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
