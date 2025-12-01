package ro.snapify.config

/**
 * Centralized configuration for media monitoring service.
 * All magic numbers are defined here for easy tuning and maintenance.
 */
object MediaMonitorConfig {
    // Timing constants (milliseconds)
    const val PROCESSING_DELAY_MS = 500L
    const val CLEANUP_DELAY_MS = 1000L
    const val NOTIFICATION_DEDUPE_WINDOW = 5000L
    const val DELETION_CHECK_INTERVAL_MS = 5000L
    const val NOTIFICATION_UPDATE_INTERVAL_MS = 1000L
    const val JOB_CLEANUP_INTERVAL_MS = 3600000L // 1 hour

    // Retry configuration
    const val MAX_DELETION_RETRIES = 3
    const val INITIAL_RETRY_DELAY_MS = 1000L
    const val RETRY_BACKOFF_MULTIPLIER = 2.0

    // Media file extensions
    val VIDEO_EXTENSIONS = setOf(
        ".mp4", ".avi", ".mov", ".mkv", ".webm",
        ".3gp", ".m4v", ".mpg", ".flv", ".wmv"
    )

    val IMAGE_EXTENSIONS = setOf(
        ".png", ".jpg", ".jpeg", ".gif", ".bmp", ".webp", ".heic", ".heif"
    )

    // Query limits
    const val MAX_QUERY_RESULTS = 1000
    const val CURSOR_TIMEOUT_MS = 30000L // 30 seconds

    // Validation rules
    const val MIN_FILE_SIZE_BYTES = 1L
    const val MAX_FILE_PATH_LENGTH = 4096
}
