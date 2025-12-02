package ro.snapify.util

import android.content.ContentResolver
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import java.net.URLDecoder

/**
 * Utility for converting Android content URIs to file system paths
 * Handles formats like:
 * - primary:Folder/Path  -> /storage/emulated/0/Folder/Path
 * - tree/B68D-37C9:Photos/Videos -> /storage/emulated/0/Photos/Videos
 * - tree/primary:DCIM/Camera -> /storage/emulated/0/DCIM/Camera
 */
object UriPathConverter {
    
    /**
     * Converts a content:// URI or tree URI to an actual file path
     * Returns null if the URI format is not recognized
     * Handles formats:
     * - primary:Path -> /storage/emulated/0/Path
     * - tree/primary:Path -> /storage/emulated/0/Path
     * - tree/B68D-37C9:Path -> /storage/emulated/0/Path
     * - B68D-37C9:Path -> /storage/emulated/0/Path (direct SAF format)
     * - 53FC-3FF3:Path -> /storage/emulated/0/Path (direct SAF format)
     * - content://com.mixplorer.doc/tree/B68D-37C9:Seal -> uses context-aware reconstruction
     */
    fun uriToFilePath(uri: String, context: Context? = null): String? {
        return try {
            val decoded = URLDecoder.decode(uri, "UTF-8")
            
            when {
                // Handle primary: format (direct folder selection)
                decoded.contains("primary:") && !decoded.contains("tree/") -> {
                    val folderPath = decoded.substringAfter("primary:")
                        .replace("%2F", "/")
                        .replace("%3A", ":")
                    "/storage/emulated/0/$folderPath"
                }
                
                // Handle tree/ format (Storage Access Framework URIs)
                decoded.contains("tree/") -> {
                    val treeContent = decoded.substringAfter("tree/")
                    val parts = treeContent.split(":")
                    
                    if (parts.size >= 2) {
                        val volume = parts[0]
                        val path = parts.drop(1).joinToString(":")
                            .replace("%2F", "/")
                            .replace("%3A", ":")
                        
                        // Check if path is incomplete (single folder name only, from MixPlorer or similar)
                        val isIncomplete = !path.contains("/") && context != null
                        
                        if (isIncomplete) {
                            // Try to find the actual path in MediaStore
                            val resolvedPath = findMediaFolderPath(context, volume, path)
                            if (resolvedPath != null) {
                                return resolvedPath
                            }
                        }
                        
                        // Map volume IDs to their actual mount points
                        val basePath = when {
                            volume == "primary" -> "/storage/emulated/0"
                            // Hex volume IDs (e.g., B68D-37C9) are primary storage
                            volume.matches(Regex("[A-F0-9]{4}-[A-F0-9]{4}|[A-Fa-f0-9]+")) -> "/storage/emulated/0"
                            else -> "/storage/$volume"
                        }
                        "$basePath/$path"
                    } else {
                        null
                    }
                }
                
                // Handle direct SAF format without tree/ (e.g., "B68D-37C9:Download/Seal" or "53FC-3FF3:Download/Seal")
                decoded.contains(":") && !decoded.startsWith("/") && !decoded.contains("content://") -> {
                    val parts = decoded.split(":")
                    if (parts.size >= 2) {
                        val volume = parts[0]
                        val path = parts.drop(1).joinToString(":")
                            .replace("%2F", "/")
                            .replace("%3A", ":")
                        
                        // Map volume IDs to their actual mount points
                        val basePath = when {
                            volume == "primary" -> "/storage/emulated/0"
                            // Hex volume IDs are primary storage
                            volume.matches(Regex("[A-F0-9]{4}-[A-F0-9]{4}|[A-Fa-f0-9]+")) -> "/storage/emulated/0"
                            else -> "/storage/$volume"
                        }
                        "$basePath/$path"
                    } else {
                        null
                    }
                }
                
                // If it's already a file path, return as-is
                decoded.startsWith("/") -> decoded
                
                else -> null
            }?.removeSuffix("/") // Remove trailing slash if any
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Finds the actual media folder path by searching MediaStore for a folder name
     * E.g., findMediaFolderPath(context, "B68D-37C9", "Seal") -> "/storage/emulated/0/Download/Seal"
     */
    private fun findMediaFolderPath(context: Context, volumeId: String, folderName: String): String? {
        try {
            val contentResolver = context.contentResolver
            val projection = arrayOf(
                MediaStore.MediaColumns.RELATIVE_PATH,
                MediaStore.MediaColumns.DATA
            )
            
            // Query both images and videos
            for (mediaUri in listOf(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            )) {
                contentResolver.query(
                    mediaUri,
                    projection,
                    null,
                    null,
                    null
                )?.use { cursor ->
                    while (cursor.moveToNext()) {
                        try {
                            val dataPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
                            
                            // Check if this path contains our folder name
                            if (dataPath.contains("/$folderName/") || dataPath.endsWith("/$folderName")) {
                                // Found it! Extract the relative path
                                val relativePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH))
                                if (!relativePath.isNullOrEmpty()) {
                                    val cleanPath = relativePath.trimEnd('/').replace("%2F", "/")
                                    return "/storage/emulated/0/$cleanPath"
                                }
                            }
                        } catch (e: Exception) {
                            continue
                        }
                    }
                }
            }
        } catch (e: Exception) {
            DebugLogger.debug("UriPathConverter", "Error finding media folder path: ${e.message}")
        }
        
        return null
    }
    
    /**
     * Converts a URI to a display name in the format "Volume:Path"
     * Used for folder filter chips and settings display
     */
    fun uriToDisplayName(uri: String): String {
        return try {
            val decoded = URLDecoder.decode(uri, "UTF-8")
            
            when {
                decoded.contains("primary:") -> {
                    "Primary:" + decoded.substringAfter("primary:")
                        .replace("%2F", "/")
                        .replace("%3A", ":")
                }
                
                decoded.contains("tree/") -> {
                    val treeContent = decoded.substringAfter("tree/")
                    val parts = treeContent.split(":")
                    
                    if (parts.size >= 2) {
                        val volume = parts[0]
                        val path = parts.drop(1).joinToString(":")
                            .replace("%2F", "/")
                            .replace("%3A", ":")
                        // Map volume IDs: primary is shown as "Primary", others as their hex ID
                        val displayVolume = if (volume == "primary") "Primary" else volume
                        "$displayVolume:$path"
                    } else {
                        decoded
                    }
                }
                
                // Handle file paths (old format migration - should be rare after migration)
                decoded.startsWith("/storage") -> {
                    val path = decoded.removePrefix("/storage/emulated/0/")
                        .removePrefix("/storage/")
                    // Always prefix with Primary for storage paths
                    "Primary:$path".takeIf { path.isNotEmpty() } ?: "Primary:Pictures/Screenshots"
                }
                
                else -> decoded
            }
        } catch (e: Exception) {
            uri
        }
    }
    
    /**
     * Checks if a file path is within any of the configured media folders
     * Handles both exact paths and URI-based folder selections (e.g., Primary:Download/Seal, B68D-37C9:Download/Seal)
     */
    fun isInMediaFolder(filePath: String, mediaFolders: Set<String>): Boolean {
        val normalizedPath = normalizePath(filePath)
        return mediaFolders.any { folder ->
            // Convert folder URI to actual file path for comparison
            val folderPath = if (folder.contains(":")) {
                uriToFilePath(folder) ?: folder
            } else {
                folder
            }
            
            val normalizedFolder = normalizePath(folderPath)
            normalizedPath.startsWith(normalizedFolder) && 
            (normalizedPath.length == normalizedFolder.length || 
             normalizedPath[normalizedFolder.length] == '/')
        }
    }
    
    /**
     * Checks if a file path is within any of the configured media folders (accepts List)
     * Handles both exact paths and URI-based folder selections (e.g., Primary:Download/Seal, B68D-37C9:Download/Seal)
     */
    fun isInMediaFolder(filePath: String, mediaFolders: List<String>): Boolean {
        val normalizedPath = normalizePath(filePath)
        return mediaFolders.any { folder ->
            // Convert folder URI to actual file path for comparison
            val folderPath = if (folder.contains(":")) {
                uriToFilePath(folder) ?: folder
            } else {
                folder
            }
            
            val normalizedFolder = normalizePath(folderPath)
            normalizedPath.startsWith(normalizedFolder) && 
            (normalizedPath.length == normalizedFolder.length || 
             normalizedPath[normalizedFolder.length] == '/')
        }
    }
    
    /**
     * Normalizes a path by:
     * - Converting to lowercase
     * - Removing trailing slashes
     * - Converting backslashes to forward slashes (Windows compatibility)
     * - Removing redundant slashes
     */
    private fun normalizePath(path: String): String {
        return path.lowercase()
            .replace("\\", "/")
            .replace(Regex("/+"), "/")
            .removeSuffix("/")
    }
    
    /**
     * Gets the default Screenshots folder as a SAF URI
     * Format: content://com.android.externalstorage.documents/tree/primary%3APictures%2FScreenshots
     */
    fun getDefaultScreenshotUri(): String {
        return "content://com.android.externalstorage.documents/tree/primary%3APictures%2FScreenshots"
    }
    
    /**
     * Decodes a list of media folder URIs to file paths
     */
    fun decodeMediaFolderUris(uris: List<String>): List<String> {
        return uris.mapNotNull { uri -> uriToFilePath(uri) }
    }
    
    /**
     * Deduplicates and normalizes a list of media folder URIs.
     * Removes URIs that point to the same file path, keeping the first occurrence.
     * This handles cases where different SAF volume IDs (e.g., 53FC-3FF3 vs B68D-37C9)
     * point to the same physical location (e.g., /storage/emulated/0/Download).
     */
    fun deduplicateMediaFolderUris(uris: Set<String>): Set<String> {
        val seenPaths = mutableSetOf<String>()
        val deduplicated = mutableSetOf<String>()
        
        uris.forEach { uri ->
            val filePath = uriToFilePath(uri)
            if (filePath != null) {
                val normalizedPath = normalizePath(filePath)
                if (!seenPaths.contains(normalizedPath)) {
                    seenPaths.add(normalizedPath)
                    deduplicated.add(uri)
                }
            } else {
                // If we can't convert to path, keep the original
                deduplicated.add(uri)
            }
        }
        
        return deduplicated
    }
    
    /**
     * Reconstructs a complete SAF URI from an incomplete one by querying MediaStore
     * E.g., "B68D-37C9:Seal" → "tree/B68D-37C9:Download/Seal"
     * Returns the original URI if reconstruction fails
     */
    fun reconstructSafUri(context: Context, incompleteSafUri: String): String {
        // If it already looks complete, return as-is
        if (incompleteSafUri.contains("tree/") || incompleteSafUri.startsWith("content://")) {
            return incompleteSafUri
        }
        
        // Try to find the folder in MediaStore
        try {
            val parts = incompleteSafUri.split(":")
            if (parts.size < 2) return incompleteSafUri
            
            val volumeId = parts[0]
            val folderName = parts.drop(1).joinToString(":").split("/").lastOrNull() ?: return incompleteSafUri
            
            // Query MediaStore to find where this folder actually is
            val contentResolver = context.contentResolver
            val projection = arrayOf(
                MediaStore.MediaColumns.RELATIVE_PATH,
                MediaStore.MediaColumns.DATA
            )
            
            // Query both images and videos
            for (mediaUri in listOf(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            )) {
                contentResolver.query(
                    mediaUri,
                    projection,
                    null,
                    null,
                    null
                )?.use { cursor ->
                    while (cursor.moveToNext()) {
                        val dataPath = try {
                            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
                        } catch (e: Exception) {
                            null
                        } ?: continue
                        
                        // Check if this path contains our folder name
                        if (dataPath.contains("/$folderName/") || dataPath.endsWith("/$folderName")) {
                            // Found it! Extract the relative path
                            val relativePath = try {
                                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH))
                            } catch (e: Exception) {
                                null
                            }
                            
                            if (!relativePath.isNullOrEmpty()) {
                                // Reconstruct: tree/volumeId:relativePath
                                val cleanPath = relativePath.trimEnd('/').replace("%2F", "/")
                                return "tree/$volumeId:$cleanPath"
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            DebugLogger.debug("UriPathConverter", "Error reconstructing SAF URI: ${e.message}")
        }
        
        return incompleteSafUri
    }
}
