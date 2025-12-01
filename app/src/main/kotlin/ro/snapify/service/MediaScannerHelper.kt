package ro.snapify.service

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import ro.snapify.config.MediaMonitorConfig
import ro.snapify.data.entity.MediaItem
import ro.snapify.data.preferences.AppPreferences
import ro.snapify.data.repository.MediaRepository
import ro.snapify.util.DebugLogger
import ro.snapify.util.MediaFileValidator
import ro.snapify.util.UriPathConverter
import java.io.File

/**
 * Handles media scanning operations.
 * Scans MediaStore for existing media items and processes new files.
 * Extracts scanning logic from ScreenshotMonitorService for better testability.
 */
class MediaScannerHelper(
    private val contentResolver: ContentResolver,
    private val repository: MediaRepository,
    private val preferences: AppPreferences,
    private val scope: CoroutineScope
) {

    private val TAG = "MediaScannerHelper"

    /**
     * Scans all existing media in configured folders.
     * Returns count of newly inserted items.
     */
    suspend fun scanExistingMedia(): Int {
        try {
            DebugLogger.info(TAG, "Starting existing media scan")

            // Preload existing file paths for performance
            val existingFilePaths = repository.getAllMediaItems()
                .first()
                .map { it.filePath }
                .toSet()

            var totalInserted = 0

            // Get configured media folders
            val mediaFolders = getConfiguredMediaFolders()

            // Scan images
            totalInserted += scanMediaType(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                isVideo = false,
                existingFilePaths,
                mediaFolders
            )

            // Scan videos
            totalInserted += scanMediaType(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                isVideo = true,
                existingFilePaths,
                mediaFolders
            )

            DebugLogger.info(TAG, "Media scan completed, inserted $totalInserted new items")
            return totalInserted
        } catch (e: Exception) {
            DebugLogger.error(TAG, "Error during media scan", e)
            return 0
        }
    }

    /**
     * Scans a specific media type (images or videos).
     * Returns count of newly inserted items.
     */
    private suspend fun scanMediaType(
        uri: Uri,
        isVideo: Boolean,
        existingFilePaths: Set<String>,
        mediaFolders: List<String>
    ): Int {
        var insertedCount = 0
        val mediaTypeLabel = if (isVideo) "videos" else "images"

        try {
            val projection = getMediaProjection(isVideo)

            contentResolver.query(
                uri,
                projection,
                null,
                null,
                "${MediaStore.MediaColumns.DATE_ADDED} DESC"
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    try {
                        val mediaData = extractMediaData(cursor, isVideo, uri)

                        // Validate media file
                        if (MediaFileValidator.isValidMediaFile(mediaData.filePath, mediaFolders)) {
                            val wasInserted = mediaData.filePath !in existingFilePaths

                            if (wasInserted) {
                                insertedCount++
                                processExistingMedia(mediaData)
                            }
                        }
                    } catch (e: Exception) {
                        DebugLogger.warning(TAG, "Error processing $mediaTypeLabel entry", e)
                        continue
                    }
                }
            }
        } catch (e: Exception) {
            DebugLogger.error(TAG, "Error scanning $mediaTypeLabel", e)
        }

        return insertedCount
    }

    /**
     * Processes an existing media item for insertion into database.
     */
    private suspend fun processExistingMedia(mediaData: MediaData) {
        try {
            // Validate existence one more time before insertion
            val exists = validateMediaExists(mediaData)

            if (exists) {
                val mediaItem = MediaItem(
                    filePath = mediaData.filePath,
                    fileName = mediaData.fileName,
                    fileSize = mediaData.fileSize,
                    createdAt = mediaData.createdAt,
                    deletionTimestamp = null,
                    isKept = false,
                    contentUri = mediaData.contentUri
                )

                repository.insert(mediaItem)
                DebugLogger.info(TAG, "Inserted media item: ${mediaData.fileName}")
            } else {
                DebugLogger.warning(TAG, "Media item doesn't exist or is empty: ${mediaData.fileName}")
            }
        } catch (e: Exception) {
            DebugLogger.error(TAG, "Error processing existing media ${mediaData.fileName}", e)
        }
    }

    /**
     * Validates that a media file exists and is accessible.
     */
    private fun validateMediaExists(mediaData: MediaData): Boolean {
        // Try ContentUri first
        mediaData.contentUri?.let { uriStr ->
            try {
                val uri = uriStr.toUri()
                contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                    return pfd.statSize > 0
                }
            } catch (e: Exception) {
                DebugLogger.debug(TAG, "ContentUri validation failed: ${e.message}")
            }
        }

        // Fallback to file system check
        return try {
            val file = File(mediaData.filePath)
            file.exists() && file.length() >= MediaMonitorConfig.MIN_FILE_SIZE_BYTES
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Cleans up expired media items from database.
     * Removes items past their deletion timestamp.
     */
    suspend fun cleanUpExpiredMediaItems() {
        try {
            val currentTime = System.currentTimeMillis()
            val expired = repository.getExpiredMediaItems(currentTime)

            expired.forEach { mediaItem ->
                try {
                    repository.delete(mediaItem)
                    DebugLogger.info(TAG, "Cleaned up expired media: ${mediaItem.fileName}")
                } catch (e: Exception) {
                    DebugLogger.error(TAG, "Error cleaning up expired item ${mediaItem.fileName}", e)
                }
            }

            if (expired.isNotEmpty()) {
                DebugLogger.info(TAG, "Cleaned up ${expired.size} expired media items")
            }
        } catch (e: Exception) {
            DebugLogger.error(TAG, "Error in cleanup of expired items", e)
        }
    }

    /**
     * Cleans up media items whose files no longer exist.
     */
    suspend fun cleanUpMissingMediaItems() {
        try {
            val allItems = repository.getAllMediaItems().first()
            val missing = allItems.filter { !File(it.filePath).exists() }

            missing.forEach { mediaItem ->
                try {
                    repository.delete(mediaItem)
                    DebugLogger.info(TAG, "Cleaned up missing media: ${mediaItem.fileName}")
                } catch (e: Exception) {
                    DebugLogger.error(TAG, "Error cleaning up missing item ${mediaItem.fileName}", e)
                }
            }

            if (missing.isNotEmpty()) {
                DebugLogger.info(TAG, "Cleaned up ${missing.size} missing media items")
            }
        } catch (e: Exception) {
            DebugLogger.error(TAG, "Error in cleanup of missing items", e)
        }
    }

    /**
     * Gets configured media folder paths.
     */
    private suspend fun getConfiguredMediaFolders(): List<String> {
        return try {
            val configuredUris = preferences.mediaFolderUris.first()
            UriPathConverter.decodeMediaFolderUris(configuredUris.toList())
        } catch (e: Exception) {
            DebugLogger.warning(TAG, "Error getting configured folders, using default", e)
            listOf(UriPathConverter.getDefaultScreenshotsPath())
        }
    }

    /**
     * Gets the projection (columns) to query for a media type.
     */
    private fun getMediaProjection(isVideo: Boolean): Array<String> {
        return if (isVideo) {
            arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.DATA
            )
        } else {
            arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.DATA
            )
        }
    }

    /**
     * Extracts media data from a cursor row.
     */
    private fun extractMediaData(cursor: android.database.Cursor, isVideo: Boolean, baseUri: Uri): MediaData {
        val id = cursor.getLong(
            cursor.getColumnIndexOrThrow(if (isVideo) MediaStore.Video.Media._ID else MediaStore.Images.Media._ID)
        )
        val contentUri = ContentUris.withAppendedId(baseUri, id).toString()
        val fileName = cursor.getString(
            cursor.getColumnIndexOrThrow(if (isVideo) MediaStore.Video.Media.DISPLAY_NAME else MediaStore.Images.Media.DISPLAY_NAME)
        )
        val fileSize = cursor.getLong(
            cursor.getColumnIndexOrThrow(if (isVideo) MediaStore.Video.Media.SIZE else MediaStore.Images.Media.SIZE)
        )
        val dateAdded = cursor.getLong(
            cursor.getColumnIndexOrThrow(if (isVideo) MediaStore.Video.Media.DATE_ADDED else MediaStore.Images.Media.DATE_ADDED)
        ) * 1000L // Convert to milliseconds
        val dataIndex = cursor.getColumnIndex(if (isVideo) MediaStore.Video.Media.DATA else MediaStore.Images.Media.DATA)
        val filePath = if (dataIndex != -1) cursor.getString(dataIndex) else null

        return MediaData(
            contentUri = contentUri,
            fileName = fileName,
            fileSize = fileSize,
            createdAt = dateAdded,
            filePath = filePath ?: ""
        )
    }

    /**
     * Data class for holding media information during scanning.
     */
    data class MediaData(
        val contentUri: String,
        val fileName: String,
        val fileSize: Long,
        val createdAt: Long,
        val filePath: String
    )
}
