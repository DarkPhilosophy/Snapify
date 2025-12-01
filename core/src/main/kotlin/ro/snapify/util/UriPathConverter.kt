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
                        val basePath = when (volume) {
                            "primary" -> "/storage/emulated/0"
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
                        "$volume:$path"
                    } else {
                        decoded
                    }
                }
                
                else -> decoded
            }
        } catch (e: Exception) {
            uri
        }
    }
    
    /**
     * Checks if a file path is within any of the configured media folders
     */
    fun isInMediaFolder(filePath: String, mediaFolders: Set<String>): Boolean {
        return mediaFolders.any { folder ->
            filePath.startsWith(folder) && 
            (filePath.length == folder.length || 
             filePath[folder.length] == '/')
        }
    }
    
    /**
     * Checks if a file path is within any of the configured media folders (accepts List)
     */
    fun isInMediaFolder(filePath: String, mediaFolders: List<String>): Boolean {
        return mediaFolders.any { folder ->
            filePath.startsWith(folder) && 
            (filePath.length == folder.length || 
             filePath[folder.length] == '/')
        }
    }
    
    /**
     * Gets the default Screenshots folder path
     */
    fun getDefaultScreenshotsPath(): String {
        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return "${picturesDir.absolutePath}/Screenshots"
    }
    
    /**
     * Decodes a list of media folder URIs to file paths
     */
    fun decodeMediaFolderUris(uris: List<String>): List<String> {
        return uris.mapNotNull { uri -> uriToFilePath(uri) }
    }
}
