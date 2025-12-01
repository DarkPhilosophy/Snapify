package ro.snapify.util

import android.os.Environment
import java.net.URLDecoder

/**
 * Utility for converting and parsing media folder URIs to file system paths.
 * Handles both primary storage and SAF (Storage Access Framework) URIs.
 * Eliminates duplicate code and provides testable path conversion logic.
 */
object UriPathConverter {

    /**
     * Converts a configured media folder URI (from preferences) to an absolute file path.
     * Handles multiple URI formats:
     * - Empty string → default Pictures/Screenshots folder
     * - primary:path/to/folder → external storage path
     * - tree/primary:folder:name → SAF-style path
     *
     * @param uri The URI string to convert
     * @return Absolute file path, or original URI if parsing fails
     */
    fun decodeMediaFolderUri(uri: String): String {
        return when {
            uri.isEmpty() -> {
                getDefaultScreenshotsPath()
            }
            uri.contains("primary:") -> {
                val path = uri.substringAfter("primary:").replace(":", "/")
                "${Environment.getExternalStorageDirectory().absolutePath}/$path"
            }
            uri.contains("tree/") -> {
                val parts = uri.substringAfter("tree/").split(":")
                if (parts.size >= 2) {
                    "${Environment.getExternalStorageDirectory().absolutePath}/${parts[1]}"
                } else {
                    uri // Fallback to original if parsing fails
                }
            }
            else -> {
                // Try to decode as URL-encoded string
                try {
                    URLDecoder.decode(uri, "UTF-8")
                } catch (e: Exception) {
                    uri // Return original if decoding fails
                }
            }
        }
    }

    /**
     * Converts a list of configured media folder URIs to file paths.
     * Returns default Screenshots folder if list is empty.
     */
    fun decodeMediaFolderUris(uris: List<String>): List<String> {
        return if (uris.isEmpty()) {
            listOf(getDefaultScreenshotsPath())
        } else {
            uris.map { decodeMediaFolderUri(it) }
        }
    }

    /**
     * Gets the default Screenshots folder path.
     * Typically: /storage/emulated/0/Pictures/Screenshots
     */
    fun getDefaultScreenshotsPath(): String {
        return "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath}/Screenshots"
    }

    /**
     * Checks if a file path belongs to any of the configured media folders.
     */
    fun isInMediaFolder(filePath: String, mediaFolders: List<String>): Boolean {
        if (filePath.isEmpty() || mediaFolders.isEmpty()) return false
        val lowerPath = filePath.lowercase()
        return mediaFolders.any { folder -> lowerPath.contains(folder.lowercase()) }
    }

    /**
     * Validates that a file path is safe and not trying to escape the media folder.
     * Prevents directory traversal attacks.
     */
    fun validateFilePath(filePath: String, allowedFolder: String): Boolean {
        if (filePath.isEmpty() || filePath.length > 4096) return false

        // Normalize paths for comparison
        val normalizedPath = try {
            java.io.File(filePath).canonicalPath
        } catch (e: Exception) {
            return false
        }

        val normalizedFolder = try {
            java.io.File(allowedFolder).canonicalPath
        } catch (e: Exception) {
            return false
        }

        // Check if file is inside the allowed folder
        return normalizedPath.startsWith(normalizedFolder)
    }
}
