package ro.snapify.service

import android.annotation.SuppressLint
import android.app.Service
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
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
import kotlin.math.abs
import kotlin.math.min

private const val FIFTEEN_MINUTES = 15L
private const val THREE_DAYS = 3L
private const val ONE_WEEK = 7L


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



class OverlayService : Service(), LifecycleOwner, SavedStateRegistryOwner {

    private lateinit var windowManager: WindowManager
    private var overlayView: ComposeView? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var repository: MediaRepository
    private lateinit var recomposeFlow: kotlinx.coroutines.flow.MutableSharedFlow<RecomposeReason>
    private lateinit var preferences: AppPreferences
    private var mediaId: Long = -1L
    private var filePath: String = ""

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
    }

    override fun onBind(intent: android.content.Intent?): IBinder? = null

    override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
        mediaId = intent?.getLongExtra("media_id", -1L) ?: -1L
        filePath = intent?.getStringExtra("file_path") ?: ""

        DebugLogger.info(
            "OverlayService",
            "onStartCommand called with screenshot ID: $mediaId, path: $filePath"
        )

        // Initialize dependencies from application
        val app = application as ScreenshotApp
        repository = app.repository
        preferences = app.preferences
        recomposeFlow = app.recomposeFlow

        if (mediaId > 0L) {
            try {
                if (PermissionUtils.hasOverlayPermission(this)) {
                    showOverlay()
                } else {
                    DebugLogger.error(
                        "OverlayService",
                        "Overlay permission not granted - manual mode requires overlay permission"
                    )
                    NotificationHelper.showErrorNotification(
                        this,
                        "Manual Mode Error",
                        "Overlay permission required. Grant in app settings."
                    )
                    showFallbackNotification()
                    stopSelf()
                }
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                DebugLogger.error(
                    "OverlayService",
                    "CRASH in manual mode: ${e.javaClass.simpleName} - ${e.message}",
                    e
                )
                NotificationHelper.showErrorNotification(
                    this,
                    "Manual Mode Crashed",
                    "Error: ${e.javaClass.simpleName} - ${e.message}"
                )
                showFallbackNotification()
                stopSelf()
            }
        } else {
            DebugLogger.error("OverlayService", "Invalid screenshot ID: $mediaId")
            stopSelf()
        }

        return START_NOT_STICKY
    }

    @SuppressLint("InflateParams")
    private fun showOverlay() {
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
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.CENTER
            }

            val mediaItem = runBlocking { repository.getById(mediaId) }
            val bitmap = mediaItem?.contentUri?.let { contentUriStr ->
                val uri = android.net.Uri.parse(contentUriStr)
                try {
                    if (mediaItem.filePath.lowercase().endsWith(".mp4") || mediaItem.filePath.lowercase().endsWith(".avi") || mediaItem.filePath.lowercase().endsWith(".mkv")) {
                        // Video thumbnail
                        val retriever = MediaMetadataRetriever()
                        retriever.setDataSource(this@OverlayService, uri)
                        val bmp = retriever.frameAtTime
                        retriever.release()
                        bmp
                    } else {
                        // Image
                        this@OverlayService.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
                    }
                } catch (e: Exception) {
                    DebugLogger.error("OverlayService", "Error loading bitmap from URI", e)
                    null
                }
            } ?: run {
                // Fallback to file path
                if (filePath.endsWith(".mp4", ignoreCase = true) || filePath.endsWith(".avi", ignoreCase = true) || filePath.endsWith(".mkv", ignoreCase = true)) {
                    try {
                        val retriever = MediaMetadataRetriever()
                        retriever.setDataSource(filePath)
                        val bmp = retriever.frameAtTime
                        retriever.release()
                        bmp
                    } catch (e: Exception) {
                        null
                    }
                } else {
                    BitmapFactory.decodeFile(filePath)
                }
            }
            val imageBitmap = bitmap?.asImageBitmap()

            // Create ComposeView for the overlay
            overlayView = ComposeView(this).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setViewTreeLifecycleOwner(this@OverlayService)
                setViewTreeSavedStateRegistryOwner(this@OverlayService)
                setContent {
                    val themeModeString = runBlocking { preferences.themeMode.first() }
                    val themeMode = when (themeModeString) {
                        "light" -> ThemeMode.LIGHT
                        "dark" -> ThemeMode.DARK
                        "dynamic" -> ThemeMode.DYNAMIC
                        "oled" -> ThemeMode.OLED
                        else -> ThemeMode.SYSTEM
                    }

                    AppTheme(themeMode = themeMode, skipWindowSetup = true) {
                        val dismissWithNotification =
                            { dismissOverlay(showFallbackNotification = true) }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .background(Color.Black.copy(alpha = 0.5f))
                                .clickable { dismissWithNotification() },
                            contentAlignment = Alignment.Center
                        ) {
                            ScreenshotDetectionOverlay(
                                detectedImage = imageBitmap,
                                on15Minutes = {
                                    handleDeletionTime(
                                        java.util.concurrent.TimeUnit.MINUTES.toMillis(
                                            FIFTEEN_MINUTES
                                        )
                                    )
                                },
                                on2Hours = {
                                    handleDeletionTime(
                                        java.util.concurrent.TimeUnit.HOURS.toMillis(
                                            2
                                        )
                                    )
                                },
                                on3Days = {
                                    handleDeletionTime(
                                        java.util.concurrent.TimeUnit.DAYS.toMillis(
                                            THREE_DAYS
                                        )
                                    )
                                },
                                on1Week = {
                                    handleDeletionTime(
                                        java.util.concurrent.TimeUnit.DAYS.toMillis(
                                            ONE_WEEK
                                        )
                                    )
                                },
                                onKeep = { handleKeep() },
                                onShare = { handleShare() },
                                onClose = { handleClose() },
                                onDismiss = dismissWithNotification,
                                onCustomTime = { minutes ->
                                    handleDeletionTime(
                                        java.util.concurrent.TimeUnit.MINUTES.toMillis(
                                            minutes.toLong()
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }

            val wm = windowManager
            wm.addView(overlayView, params)

            // CRITICAL: Set lifecycle to RESUMED so Compose can render
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
                e
            )
            overlayView = null
            stopSelf()
        }
    }


    private fun handleDeletionTime(timeMillis: Long) {
        serviceScope.launch(Dispatchers.IO) {
            val deletionTimestamp = System.currentTimeMillis() + timeMillis

            // Update DB synchronously
            runBlocking {
                repository.markForDeletion(mediaId, deletionTimestamp)
            }

            // The service's deletion check timer will handle the deletion

            // Show notification to confirm the scheduled deletion
            val screenshot = repository.getById(mediaId)
            screenshot?.let {
            NotificationHelper.showScreenshotNotification(
            this@OverlayService,
            mediaId,
            it.fileName,
            it.filePath,
            deletionTimestamp,
            timeMillis,
            isManualMode = false, // Show countdown like automatic mode
            preferences = preferences
            )
            DebugLogger.info(
            "OverlayService",
            "Notification shown after time selection in manual mode"
            )

                // Launch notification update job for live countdown updates
                kotlinx.coroutines.GlobalScope.launch {
                    while (System.currentTimeMillis() < deletionTimestamp) {
                        kotlinx.coroutines.delay(1000L) // Update every 1 second
                        try {
                            NotificationHelper.showScreenshotNotification(
                                this@OverlayService,
                                mediaId,
                            it.fileName,
                            it.filePath,
                            deletionTimestamp,
                            timeMillis,
                            isManualMode = false,
                            preferences = preferences
                        )
                } catch (e: Exception) {
                    DebugLogger.error(
                            "OverlayService",
                                "Error updating notification for $mediaId",
                                e
                        )
                }
                }
                }

                withContext(Dispatchers.Main) {
                    // Notify UI to update the specific item
                    DebugLogger.info(
                    "OverlayService",
                    "Emitting ItemUpdated event after marking for deletion"
                    )
                    MainViewModel.mediaEventFlow.tryEmit(MediaEvent.ItemUpdated(it))
                    dismissOverlay(showFallbackNotification = false)
                }
            }
        }
    }

    private fun handleKeep() {
        serviceScope.launch(Dispatchers.IO) {
                repository.markAsKept(mediaId)
        val updatedItem = repository.getById(mediaId)

                withContext(Dispatchers.Main) {
                    // Notify UI to update the specific item
                    DebugLogger.info("OverlayService", "Emitting ItemUpdated event after keeping media")
                    updatedItem?.let { MainViewModel.mediaEventFlow.tryEmit(MediaEvent.ItemUpdated(it)) }
                dismissOverlay(showFallbackNotification = false)
            }
        }
    }

    private fun handleShare() {
        val file = java.io.File(filePath)
        if (file.exists()) {
            // Get the media item for deletion
            val mediaItem = runBlocking { repository.getById(mediaId) }
            // Copy to cache for sharing
            val cacheDir = cacheDir
            val shareFile = java.io.File(cacheDir, "share_${System.currentTimeMillis()}.png")
            try {
                file.inputStream().use { input ->
                    shareFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                val shareUri = androidx.core.content.FileProvider.getUriForFile(this, "${packageName}.fileprovider", shareFile)
                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "image/*"
                    putExtra(android.content.Intent.EXTRA_STREAM, shareUri)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                val chooserIntent = android.content.Intent.createChooser(intent, "Share Screenshot").apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(chooserIntent)
                // Delete the original file immediately
                file.delete()
                // Delete from database and notify UI to remove item
                runBlocking {
                    mediaItem?.let { repository.delete(it) }
                    DebugLogger.info("OverlayService", "Emitting ItemDeleted event after sharing and deleting media")
                    MainViewModel.mediaEventFlow.tryEmit(MediaEvent.ItemDeleted(mediaId))
                }
                // Delete the cached file after a delay to allow sharing
                serviceScope.launch {
                    kotlinx.coroutines.delay(30000L) // 30 seconds
                    shareFile.delete()
                }
                dismissOverlay(showFallbackNotification = false)
            } catch (e: Exception) {
                DebugLogger.error("OverlayService", "Error sharing screenshot", e)
            }
        }
    }

    private fun handleClose() {
        serviceScope.launch(Dispatchers.IO) {
            repository.markAsKept(mediaId)

            withContext(Dispatchers.Main) {
                dismissOverlay(showFallbackNotification = false)
            }
        }
    }

    private fun animateOverlayIn() {
        // Animation is now handled by Compose
        DebugLogger.info("OverlayService", "Overlay animation in (handled by Compose)")
    }

    private fun dismissOverlay(showFallbackNotification: Boolean = true) {
        overlayView?.let { view ->
            try {
                if (::windowManager.isInitialized) {
                    windowManager.removeView(view)
                }
                overlayView = null
            } catch (
                @Suppress(
                    "TooGenericExceptionCaught",
                    "PrintStackTrace"
                ) e: Exception
            ) {
                e.printStackTrace()
                overlayView = null
            }

            // If overlay was dismissed without user interaction, show notification as fallback
            if (showFallbackNotification) {
                showFallbackNotification()
            } else {
                stopSelf()
            }
        } ?: run {
            stopSelf()
        }
    }

    private fun showFallbackNotification() {
        serviceScope.launch(Dispatchers.IO) {
            val screenshot = repository.getById(mediaId)
            screenshot?.let {
                val deletionTime = preferences.deletionTimeMillis.first()
                val deletionTimestamp = System.currentTimeMillis() + deletionTime
                NotificationHelper.showScreenshotNotification(
                    this@OverlayService,
                    mediaId,
                    it.fileName,
                    it.filePath,
                    deletionTimestamp,
                    deletionTime,
                    isManualMode = true,
                    preferences = preferences
                )
                DebugLogger.info("OverlayService", "Fallback notification shown for manual mode")
            }
        }
    }

    override fun onDestroy() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        super.onDestroy()
        // Clean up any remaining share files
        cacheDir?.listFiles { file -> file.name.startsWith("share_") }?.forEach { it.delete() }
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
