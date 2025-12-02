package ro.snapify.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ro.snapify.data.preferences.AppPreferences
import ro.snapify.data.repository.MediaRepository
import ro.snapify.service.ScreenshotMonitorService
import ro.snapify.ui.theme.ThemeMode
import ro.snapify.util.UriPathConverter
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: AppPreferences,
    private val mediaRepository: MediaRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val currentTheme = preferences.themeMode.map { themeString ->
        when (themeString) {
            "light" -> ThemeMode.LIGHT
            "dark" -> ThemeMode.DARK
            "dynamic" -> ThemeMode.DYNAMIC
            "oled" -> ThemeMode.OLED
            else -> ThemeMode.SYSTEM
        }
    }

    val deletionTime = preferences.deletionTimeMillis

    val isManualMode = preferences.isManualMarkMode

    val language = preferences.language

    val mediaFolderUris = preferences.mediaFolderUris

    val autoCleanupEnabled = preferences.autoCleanupEnabled

    val developerModeEnabled = preferences.developerModeEnabled
    val notificationsEnabled = preferences.notificationsEnabled
    val liveVideoPreviewEnabled = preferences.liveVideoPreviewEnabled
    val permanentSettingMenuEnabled = preferences.permanentSettingMenuEnabled

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            val themeString = when (themeMode) {
                ThemeMode.LIGHT -> "light"
                ThemeMode.DARK -> "dark"
                ThemeMode.SYSTEM -> "system"
                ThemeMode.DYNAMIC -> "dynamic"
                ThemeMode.OLED -> "oled"
            }
            preferences.setThemeMode(themeString)
        }
    }

    fun setDeletionTime(timeMillis: Long) {
        viewModelScope.launch {
            preferences.setDeletionTimeMillis(timeMillis)
        }
    }

    fun setManualMode(manual: Boolean) {
        viewModelScope.launch {
            preferences.setManualMarkMode(manual)
        }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch {
            preferences.setLanguage(language)
        }
    }

    fun setAutoCleanupEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setAutoCleanupEnabled(enabled)
        }
    }

    fun setDeveloperModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setDeveloperModeEnabled(enabled)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setNotificationsEnabled(enabled)
        }
    }

    fun setLiveVideoPreviewEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setLiveVideoPreviewEnabled(enabled)
        }
    }

    fun setPermanentSettingMenuEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setPermanentSettingMenuEnabled(enabled)
        }
    }

    fun addMediaFolder(uri: String) {
        viewModelScope.launch {
            val current = preferences.mediaFolderUris.first()
            val updated = current + uri
            preferences.setMediaFolderUris(updated)

            // If monitoring is enabled, start the service to scan the new folder
            val isEnabled = preferences.serviceEnabled.first()
            if (isEnabled) {
                val serviceIntent = Intent(context, ScreenshotMonitorService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        }
    }

    fun removeMediaFolder(uri: String) {
        viewModelScope.launch {
            val current = preferences.mediaFolderUris.first()
            val updated = current - uri
            preferences.setMediaFolderUris(updated)

            // Remove media items from the database that are in the removed folder
            val folderPath = UriPathConverter.uriToFilePath(uri)
            if (folderPath != null) {
                val normalizedFolderPath = folderPath.removeSuffix("/").lowercase()
                val allItems = mediaRepository.getAllMediaItems().first()
                val itemsToDelete = allItems.filter { item ->
                    val normalizedItemPath = item.filePath.removeSuffix("/").lowercase()
                    normalizedItemPath.startsWith(normalizedFolderPath) &&
                    (normalizedItemPath.length == normalizedFolderPath.length ||
                     normalizedItemPath[normalizedFolderPath.length] == '/')
                }
                itemsToDelete.forEach { 
                    mediaRepository.deleteById(it.id)
                }
            }

            // Also clean up any items that are no longer in configured folders
            // (in case there are orphaned items from previously removed folders)
            val allItems = mediaRepository.getAllMediaItems().first()
            if (allItems.isNotEmpty()) {
                val itemsNotInFolders = allItems.filter { item ->
                    !UriPathConverter.isInMediaFolder(item.filePath, updated.toList())
                }
                itemsNotInFolders.forEach { 
                    mediaRepository.deleteById(it.id)
                }
            }

            // If no folders left, stop the service
            if (updated.isEmpty()) {
                val serviceIntent = Intent(context, ScreenshotMonitorService::class.java)
                context.stopService(serviceIntent)
                preferences.setServiceEnabled(false)
            } else {
                // If monitoring is enabled, restart the service to update folders
                val isEnabled = preferences.serviceEnabled.first()
                if (isEnabled) {
                    val serviceIntent = Intent(context, ScreenshotMonitorService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                }
            }
        }
    }

    fun setMediaFolders(uris: Set<String>) {
        viewModelScope.launch {
            preferences.setMediaFolderUris(uris)
        }
    }

}
