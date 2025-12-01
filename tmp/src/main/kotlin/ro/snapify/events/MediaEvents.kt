package ro.snapify.events

import ro.snapify.data.entity.MediaItem

/**
 * Sealed class representing various media-related events in the application.
 * This provides a type-safe way to handle different media operations.
 */
sealed class MediaEvent {

    /**
     * Event fired when a new media file is detected on the device.
     * @param mediaId The ID of the detected media item.
     * @param filePath The file path of the detected media.
     */
    data class ItemDetected(val mediaId: Long, val filePath: String) : MediaEvent()

    /**
     * Event fired when a media item is successfully added to the database and UI.
     * @param mediaItem The full MediaItem object that was added.
     */
    data class ItemAdded(val mediaItem: MediaItem) : MediaEvent()

    /**
     * Event fired when a media item is deleted from the database.
     * @param mediaId The ID of the deleted media item.
     */
    data class ItemDeleted(val mediaId: Long) : MediaEvent()

    /**
     * Event fired when a media item's properties are updated (e.g., marked as kept, deletion timestamp set).
     * @param mediaItem The updated MediaItem object.
     */
    data class ItemUpdated(val mediaItem: MediaItem) : MediaEvent()
}
