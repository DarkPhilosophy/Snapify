package ro.snapify.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.ContextCompat

object PermissionUtils {

    fun hasOverlayPermission(context: Context): Boolean = Settings.canDrawOverlays(context)

    fun getMissingPermissions(context: Context): List<String> {
        val missingPermissions = mutableListOf<String>()

        // 1. Storage Permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                missingPermissions.add("android.permission.MANAGE_EXTERNAL_STORAGE")
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                missingPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                missingPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        // Tiramisu Read Media Images
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                missingPermissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
        }

        // 2. Overlay Permission
        if (!Settings.canDrawOverlays(context)) {
            missingPermissions.add(Manifest.permission.SYSTEM_ALERT_WINDOW)
        }

        // 3. Battery Optimization
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        if (!powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
            missingPermissions.add(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
        }

        // 4. Notifications (OPTIONAL - EXCLUDED FROM MISSING LIST TO PREVENT BLOCKING)
        // DialogComponents.kt treats notifications as optional.
        // We do NOT add POST_NOTIFICATIONS here.

        return missingPermissions
    }

    fun updatePermissionStatuses(
        context: Context,
        permissions: List<String>,
        onResult: (Map<String, Boolean>) -> Unit
    ) {
        val statusMap = permissions.associateWith { permission ->
            if (permission == "overlay" || permission == Manifest.permission.SYSTEM_ALERT_WINDOW) {
                Settings.canDrawOverlays(context)
            } else {
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
            }
        }
        onResult(statusMap)
    }
}
