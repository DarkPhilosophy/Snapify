package ro.snapify.util

import android.os.Environment
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
     */
    fun uriToFilePath(uri: String): String? {
        return try {
            val decoded = URLDecoder.decode(uri, "UTF-8")
            
            when {
                // Handle primary: format (direct folder selection)
                decoded.contains("primary:") -> {
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
                
                // If it's already a file path, return as-is
                decoded.startsWith("/") -> decoded
                
                else -> null
            }?.removeSuffix("/") // Remove trailing slash if any
        } catch (e: Exception) {
            null
        }
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
}
