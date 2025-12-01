package ro.snapify.util

import ro.snapify.config.MediaMonitorConfig
import java.io.File

/**
 * Validates if a file is a valid media file (image/video) that should be monitored.
 * Provides centralized validation logic for media type checking and filtering.
 */
object MediaFileValidator {

    /**
     * Determines if a file path represents a video file.
     */
    fun isVideoFile(filePath: String): Boolean {
        if (filePath.isEmpty()) return false
        val extension = filePath.substringAfterLast(".").lowercase()
        return ".${extension}" in MediaMonitorConfig.VIDEO_EXTENSIONS
    }

    /**
     * Determines if a file path represents an image file.
     */
    fun isImageFile(filePath: String): Boolean {
        if (filePath.isEmpty()) return false
        val extension = filePath.substringAfterLast(".").lowercase()
        return ".${extension}" in MediaMonitorConfig.IMAGE_EXTENSIONS
    }

    /**
     * Determines if a file path represents a media file (image or video).
     */
    fun isMediaFile(filePath: String): Boolean {
        return isImageFile(filePath) || isVideoFile(filePath)
    }

    /**
     * Checks if a file path is pending (incomplete write).
     * Pending files have ".pending" suffix added by MediaStore during write.
     */
    fun isPendingFile(filePath: String): Boolean {
        return filePath.contains(".pending")
    }

    /**
     * Validates a media file for processing.
     * Checks:
     * - Not empty
     * - Is valid media type
     * - Is not pending
     * - File exists and is non-empty
     */
    fun isValidMediaFile(filePath: String?, mediaFolders: List<String> = emptyList()): Boolean {
        if (filePath.isNullOrEmpty()) return false
        if (filePath.length > MediaMonitorConfig.MAX_FILE_PATH_LENGTH) return false
        if (isPendingFile(filePath)) return false
        if (!isMediaFile(filePath)) return false

        // Check folder if configured
        if (mediaFolders.isNotEmpty() && !UriPathConverter.isInMediaFolder(filePath, mediaFolders)) {
            return false
        }

        // Verify file exists and has content
        return try {
            val file = File(filePath)
            file.exists() && file.length() >= MediaMonitorConfig.MIN_FILE_SIZE_BYTES
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Gets the file extension (without dot).
     */
    fun getFileExtension(filePath: String): String {
        if (filePath.isEmpty()) return ""
        val ext = filePath.substringAfterLast(".")
        return if (ext.isEmpty() || ext == filePath) "" else ext.lowercase()
    }

    /**
     * Gets the file name from a path.
     */
    fun getFileName(filePath: String): String {
        return try {
            File(filePath).name
        } catch (e: Exception) {
            filePath.substringAfterLast("/")
        }
    }
}
