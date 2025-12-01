@file:OptIn(androidx.media3.common.util.UnstableApi::class)
package ro.snapify.ui

import android.app.LocaleManager
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Shortcut
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ramcosta.composedestinations.DestinationsNavHost

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ro.snapify.data.preferences.AppPreferences
import ro.snapify.ui.components.DuoDrawer
import ro.snapify.ui.components.PermissionDialog
import ro.snapify.ui.theme.AppTheme
import ro.snapify.ui.theme.ThemeMode
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferences: AppPreferences

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make the app fullscreen and draw over all system UI
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        // Allow drawing over display cutouts
        window.attributes.layoutInDisplayCutoutMode =
            android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        window.attributes = window.attributes

        // Make content go behind system bars
        window.setDecorFitsSystemWindows(false)

        // Make status and navigation bars transparent
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        // Debug: Check if camera cutout exists
        val cutout = window.decorView.rootWindowInsets?.displayCutout
        android.util.Log.d("MainActivity", "Camera cutout: ${cutout != null}")
        if (cutout != null) {
            android.util.Log.d("MainActivity", "Cutout bounds: ${cutout.boundingRects}")
            android.util.Log.d(
                "MainActivity",
                "Cutout insets: L=${cutout.safeInsetLeft}, T=${cutout.safeInsetTop}, R=${cutout.safeInsetRight}, B=${cutout.safeInsetBottom}"
            )
        }

        // Handle language changes
        lifecycleScope.launch {
            preferences.language.collect { language ->
                val localeTag = if (language == "ro") "ro" else "en"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val localeManager = getSystemService(LocaleManager::class.java)
                    localeManager?.applicationLocales = LocaleList.forLanguageTags(localeTag)
                } else {
                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(localeTag)
                    )
                }
            }
        }

        // Handle auto cleanup worker
        lifecycleScope.launch {
            preferences.autoCleanupEnabled.collect { enabled ->
                val workManager = WorkManager.getInstance(this@MainActivity)
                if (enabled) {
                    val constraints = Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                    val request =
                        PeriodicWorkRequestBuilder<ro.snapify.worker.AutoCleanupWorker>(
                            24,
                            java.util.concurrent.TimeUnit.HOURS
                        )
                            .setConstraints(constraints)
                            .build()
                    workManager.enqueueUniquePeriodicWork(
                        "auto_cleanup",
                        ExistingPeriodicWorkPolicy.REPLACE,
                        request
                    )
                } else {
                    workManager.cancelUniqueWork("auto_cleanup")
                }
            }
        }

        setContent {
            val themeModeString by preferences.themeMode.collectAsState(initial = "system")
            val themeMode = when (themeModeString) {
                "light" -> ThemeMode.LIGHT
                "dark" -> ThemeMode.DARK
                "dynamic" -> ThemeMode.DYNAMIC
                "oled" -> ThemeMode.OLED
                else -> ThemeMode.SYSTEM
            }

            var uiState by remember { mutableStateOf(MainUiState()) }
            LaunchedEffect(Unit) {
                viewModel.uiState.collect { uiState = it }
            }

            // Add default folder if none configured and not initialized
            LaunchedEffect(Unit) {
                val hasInit = preferences.hasInitializedFolders.first()
                if (!hasInit) {
                    val current = preferences.mediaFolderUris.first()
                    if (current.isEmpty()) {
                        val defaultUri =
                            "content://com.android.externalstorage.documents/tree/primary%3APictures%2FScreenshots"
                        preferences.setMediaFolderUris(setOf(defaultUri))
                    }
                    preferences.setHasInitializedFolders(true)
                }
            }

            AppTheme(themeMode = themeMode) {
                val navController = rememberNavController()

                DestinationsNavHost(
                navController = navController,
                startRoute = NavGraphs.root.startRoute,
                    navGraph = NavGraphs.root
                )
            }

            // Broadcast receiver removed - use refresh button for now

            // Handle first launch
            // lifecycleScope.launch {
            //     val isFirstLaunch = preferences.isFirstLaunch.first()
            //     if (isFirstLaunch) {
            //         // Show welcome dialog or handle first launch
            //         preferences.setFirstLaunch(false)
            //     }
            // }
        }
    }
}
