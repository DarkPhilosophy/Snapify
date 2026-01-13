@file:OptIn(androidx.media3.common.util.UnstableApi::class)

package ro.snapify.ui

import android.app.LocaleManager
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
        enableEdgeToEdge()

        // Debug: Check if camera cutout exists
        val cutout = window.decorView.rootWindowInsets?.displayCutout
        android.util.Log.d("MainActivity", "Camera cutout: ${cutout != null}")
        if (cutout != null) {
            android.util.Log.d("MainActivity", "Cutout bounds: ${cutout.boundingRects}")
            android.util.Log.d(
                "MainActivity",
                "Cutout insets: L=${cutout.safeInsetLeft}, T=${cutout.safeInsetTop}, R=${cutout.safeInsetRight}, B=${cutout.safeInsetBottom}",
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
                        LocaleListCompat.forLanguageTags(localeTag),
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
                            java.util.concurrent.TimeUnit.HOURS,
                        )
                            .setConstraints(constraints)
                            .build()
                    workManager.enqueueUniquePeriodicWork(
                        "auto_cleanup",
                        ExistingPeriodicWorkPolicy.REPLACE,
                        request,
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

            // Initialize folder URIs and migrate old file paths to SAF URIs
            LaunchedEffect(Unit) {
                val hasInit = preferences.hasInitializedFolders.first()
                if (!hasInit) {
                    val current = preferences.mediaFolderUris.first()
                    val migratedUris = current
                        .filter { it.isNotEmpty() }
                        .map { uri ->
                            // Migrate old file paths (starting with /) to SAF URIs
                            if (uri.startsWith("/storage")) {
                                // Convert file path back to SAF URI format
                                // /storage/emulated/0/Pictures/Screenshots -> primary:Pictures/Screenshots
                                val relativePath = uri
                                    .removePrefix("/storage/emulated/0/")
                                    .removePrefix("/storage/")
                                "primary:$relativePath"
                            } else {
                                uri // Already in URI format
                            }
                        }
                        .toSet()

                    val finalUris = if (migratedUris.isEmpty()) {
                        setOf(ro.snapify.util.UriPathConverter.getDefaultScreenshotUri())
                    } else {
                        migratedUris
                    }

                    preferences.setMediaFolderUris(finalUris)
                    preferences.setHasInitializedFolders(true)
                }
            }

            AppTheme(themeMode = themeMode) {
                val navController = rememberNavController()

                DestinationsNavHost(
                    navController = navController,
                    startRoute = NavGraphs.root.startRoute,
                    navGraph = NavGraphs.root,
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
