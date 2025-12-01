package ro.snapify.service

import android.content.Context
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ro.snapify.config.MediaMonitorConfig
import ro.snapify.data.entity.MediaItem
import ro.snapify.data.repository.MediaRepository
import ro.snapify.util.DebugLogger
import ro.snapify.util.NotificationHelper

/**
 * Manages deletion timers for media items.
 * Handles:
 * - Starting and canceling deletion timers
 * - Performing actual deletion with retry logic
 * - Cleanup of completed timers
 */
class DeletionTimerManager(
    private val context: Context,
    private val repository: MediaRepository,
    private val serviceScope: CoroutineScope,
    private val onItemDeleted: (Long) -> Unit
) {

    private val deletionJobs = mutableMapOf<Long, Job>()
    private val updateJobs = mutableMapOf<Long, Job>()
    private val TAG = "DeletionTimerManager"

    /**
     * Launches a deletion timer for a media item.
     * Cancels any existing timer for the same item.
     */
    fun launchDeletionTimer(mediaId: Long, delayMillis: Long) {
        // Cancel existing timer
        cancelDeletionTimer(mediaId)

        val job = serviceScope.launch {
            try {
                DebugLogger.info(TAG, "Starting deletion timer for item $mediaId, delay: ${delayMillis}ms")
                delay(delayMillis)
                DebugLogger.info(TAG, "Deletion timer expired for item $mediaId")

                val mediaItem = repository.getById(mediaId)
                if (mediaItem != null && mediaItem.deletionTimestamp != null && !mediaItem.isKept) {
                    DebugLogger.info(TAG, "Deleting expired media item $mediaId: ${mediaItem.fileName}")
                    deleteMediaWithRetry(mediaItem)
                } else {
                    DebugLogger.info(TAG, "Item $mediaId not found or not marked for deletion")
                }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    DebugLogger.error(TAG, "Error in deletion timer for $mediaId", e)
                }
            } finally {
                deletionJobs.remove(mediaId)
                updateJobs[mediaId]?.cancel()
                updateJobs.remove(mediaId)
            }
        }

        deletionJobs[mediaId] = job
    }

    /**
     * Cancels deletion timer for a media item.
     */
    fun cancelDeletionTimer(mediaId: Long) {
        deletionJobs[mediaId]?.cancel()
        deletionJobs.remove(mediaId)
        updateJobs[mediaId]?.cancel()
        updateJobs.remove(mediaId)
        DebugLogger.info(TAG, "Cancelled deletion timer for item $mediaId")
    }

    /**
     * Registers an update job that runs while deletion timer is active.
     * Used for updating notifications with countdown.
     */
    fun registerUpdateJob(mediaId: Long, job: Job) {
        updateJobs[mediaId] = job
    }

    /**
     * Deletes a media item with exponential backoff retry logic.
     * Attempts deletion via:
     * 1. ContentUri (preferred, uses MediaStore)
     * 2. File system
     * 3. WorkManager (for background retry if needed)
     */
    private suspend fun deleteMediaWithRetry(mediaItem: MediaItem) {
        var lastException: Exception? = null

        for (attempt in 1..MediaMonitorConfig.MAX_DELETION_RETRIES) {
            try {
                if (deleteMediaItem(mediaItem)) {
                    DebugLogger.info(TAG, "Successfully deleted media item ${mediaItem.id}: ${mediaItem.fileName}")
                    onItemDeleted(mediaItem.id)
                    return
                }
            } catch (e: Exception) {
                lastException = e
                DebugLogger.warning(
                    TAG,
                    "Deletion attempt $attempt/${MediaMonitorConfig.MAX_DELETION_RETRIES} failed for ${mediaItem.fileName}: ${e.message}"
                )

                // Calculate backoff delay
                if (attempt < MediaMonitorConfig.MAX_DELETION_RETRIES) {
                    val delayMs = (MediaMonitorConfig.INITIAL_RETRY_DELAY_MS *
                            Math.pow(MediaMonitorConfig.RETRY_BACKOFF_MULTIPLIER, (attempt - 1).toDouble())).toLong()
                    delay(delayMs)
                }
            }
        }

        // Final attempt: remove from database even if file deletion failed
        DebugLogger.warning(
            TAG,
            "All deletion retries exhausted for ${mediaItem.fileName}. " +
                    "Removing from database. Last error: ${lastException?.message}"
        )
        removeFromDatabase(mediaItem)
        onItemDeleted(mediaItem.id)
    }

    /**
     * Performs the actual deletion of a media item.
     * Tries ContentUri first (MediaStore), then file system.
     *
     * @return true if deletion succeeded, false otherwise
     */
    private suspend fun deleteMediaItem(mediaItem: MediaItem): Boolean {
        var deleted = false

        // Try ContentUri first (MediaStore deletion)
        mediaItem.contentUri?.let { uriStr ->
            try {
                val uri = uriStr.toUri()
                val rows = context.contentResolver.delete(uri, null, null)
                deleted = rows > 0
                DebugLogger.debug(
                    TAG,
                    "ContentResolver.delete() result: $rows rows for ${mediaItem.fileName}"
                )
                if (deleted) return true
            } catch (e: Exception) {
                DebugLogger.warning(
                    TAG,
                    "ContentResolver deletion failed for ${mediaItem.id}: ${e.message}"
                )
            }
        }

        // Fallback: try file system deletion
        if (!deleted) {
            try {
                val file = java.io.File(mediaItem.filePath)
                if (file.exists()) {
                    deleted = file.delete()
                    DebugLogger.debug(
                        TAG,
                        "File.delete() result: $deleted for ${mediaItem.filePath}"
                    )
                } else {
                    // File doesn't exist - consider it deleted
                    deleted = true
                    DebugLogger.info(TAG, "File doesn't exist, considering deleted: ${mediaItem.filePath}")
                }
            } catch (e: Exception) {
                DebugLogger.error(TAG, "File deletion error for ${mediaItem.filePath}", e)
                throw e
            }
        }

        return deleted
    }

    /**
     * Removes a media item from the database and cancels its notification.
     */
    private suspend fun removeFromDatabase(mediaItem: MediaItem) {
        try {
            repository.delete(mediaItem)
            NotificationHelper.cancelNotification(context, mediaItem.id.toInt())
            DebugLogger.info(TAG, "Removed media item from database: ${mediaItem.fileName}")
        } catch (e: Exception) {
            DebugLogger.error(TAG, "Error removing media item from database", e)
        }
    }

    /**
     * Returns currently active deletion jobs.
     */
    fun getActiveJobIds(): Set<Long> = deletionJobs.keys

    /**
     * Cleanup: Removes stale jobs that are no longer active.
     * Call this periodically to prevent memory leaks.
     */
    fun cleanupStaleJobs() {
        deletionJobs.entries.removeIf { (_, job) -> !job.isActive }
        updateJobs.entries.removeIf { (_, job) -> !job.isActive }
    }

    /**
     * Cancels all deletion timers and cleanup jobs.
     * Call this in service onDestroy().
     */
    fun cancelAll() {
        deletionJobs.values.forEach { it.cancel() }
        deletionJobs.clear()
        updateJobs.values.forEach { it.cancel() }
        updateJobs.clear()
        DebugLogger.info(TAG, "Cancelled all deletion timers and update jobs")
    }
}
