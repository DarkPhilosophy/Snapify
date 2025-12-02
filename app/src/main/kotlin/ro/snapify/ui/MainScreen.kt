@file:OptIn(androidx.media3.common.util.UnstableApi::class)
@file:Suppress("UnstableApiUsage")

package ro.snapify.ui

// import ro.snapify.ui.components.DeletionTimeDialog
// import ro.snapify.ui.components.LanguageDialog
// import ro.snapify.ui.components.OperationModeDialog
// import ro.snapify.ui.components.ThemeDialog
// import ro.snapify.ui.components.WelcomeDialog

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import kotlin.math.pow
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.animation.Crossfade
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Shortcut
import ro.snapify.ui.components.DuoDrawer
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import my.nanihadesuka.compose.LazyColumnScrollbar
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStore
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import androidx.core.content.ContextCompat
import java.io.File
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ro.snapify.BuildConfig
import ro.snapify.R
import ro.snapify.data.entity.MediaItem
import ro.snapify.data.model.FilterState
import ro.snapify.data.model.ScreenshotTab
import ro.snapify.ui.components.EmptyStateScreen
import ro.snapify.ui.components.FolderFilterBar
import ro.snapify.ui.components.LoadingBar
import ro.snapify.ui.components.LoadingScreen
import ro.snapify.ui.components.MediaInfoDialog
import ro.snapify.ui.components.NewScreenshotDetector
import ro.snapify.ui.components.PermissionDialog
import ro.snapify.ui.components.PicturePreviewDialog
import ro.snapify.ui.components.ScreenshotCard
import ro.snapify.ui.components.ServiceStatusIndicator
import ro.snapify.ui.components.TagFilterBar
import ro.snapify.ui.components.VideoPreviewDialog
import ro.snapify.ui.components.rememberVideoLifecycleManager
import ro.snapify.ui.theme.AppTheme
import ro.snapify.ui.theme.ErrorRed
import ro.snapify.ui.theme.SuccessGreen
import ro.snapify.ui.theme.ThemeMode
import ro.snapify.ui.theme.WarningOrange
import ro.snapify.util.DebugLogger
import ro.snapify.util.UriPathConverter

// Reusable bounce animation function
suspend fun animatedBounce(
    animatable: Animatable<Float, AnimationVector1D>,
    startDistance: Float,
    numBounces: Int,
    baseDuration: Long
) {
    animatable.snapTo(startDistance)
    for (i in 0 until numBounces) {
        // Bounce to left (0)
        animatable.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = (baseDuration * 0.8.pow(i)).toLong().coerceAtLeast(150L).toInt())
        )
        if (i < numBounces - 1) {
            // Bounce back to right with decreasing amplitude
            val nextDist = startDistance * 0.6.pow(i + 1).toFloat()
            animatable.animateTo(
                targetValue = nextDist,
                animationSpec = tween(durationMillis = (baseDuration * 0.85.pow(i)).toLong().coerceAtLeast(150L).toInt())
            )
        }
    }
}

// Reusable typewriter animation function
suspend fun animateTypewriter(
    alphas: List<Animatable<Float, AnimationVector1D>>,
    delays: List<Long>
) {
    alphas.forEach { it.snapTo(0f) }
    alphas.forEachIndexed { index, animatable ->
        kotlinx.coroutines.delay(delays.getOrElse(index) { 0L })
        animatable.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 100)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel? = null,
    onOpenDrawer: () -> Unit = {},
    preferences: ro.snapify.data.preferences.AppPreferences? = null,
    isDrawerOpen: Boolean = false,
    navigator: DestinationsNavigator? = null
) {
    DebugLogger.info("MainScreen", "RECOMPOSING")

    val actualViewModel = viewModel ?: hiltViewModel()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val uiState by actualViewModel.uiState.collectAsStateWithLifecycle(initialValue = MainUiState())
    val themeMode by preferences?.themeMode?.collectAsState(initial = "system")
        ?: remember { mutableStateOf("system") }
    val language by preferences?.language?.collectAsState(initial = "en")
        ?: remember { mutableStateOf("en") }
    val currentFilterState by actualViewModel.currentFilterState.collectAsStateWithLifecycle(initialValue = FilterState())
    val currentTime by actualViewModel.currentTime.collectAsStateWithLifecycle(initialValue = System.currentTimeMillis())

    // Debug currentFilterState changes
    // Observe mediaItems list changes - force recomposition when SnapshotStateList mutates
    var mediaItemsSnapshot by remember { mutableStateOf(actualViewModel.mediaItems.toList()) }
    
    LaunchedEffect(actualViewModel.mediaItems.size) {
        mediaItemsSnapshot = actualViewModel.mediaItems.toList()
        DebugLogger.info("MainScreen", "mediaItems updated: ${mediaItemsSnapshot.size} items")
    }
    
    LaunchedEffect(currentFilterState) {
        DebugLogger.info(
            "MainScreen",
            "currentFilterState changed: folders=${currentFilterState.selectedFolders.size}, tags=${currentFilterState.selectedTags}"
        )
    }
    val mediaItems = actualViewModel.mediaItems
    val refreshTrigger by actualViewModel.refreshTrigger.collectAsStateWithLifecycle(initialValue = 0L)

    // Debug refreshTrigger changes
    LaunchedEffect(refreshTrigger) {
        DebugLogger.info("MainScreen", "RefreshTrigger changed to: $refreshTrigger")
    }
    val mediaFolderUris by actualViewModel.mediaFolderUris.collectAsStateWithLifecycle(initialValue = emptySet())
    val monitoringStatus by actualViewModel.monitoringStatus.collectAsState(initial = MonitoringStatus.STOPPED)
    val liveVideoPreviewEnabled by actualViewModel.liveVideoPreviewEnabled.collectAsStateWithLifecycle(
        initialValue = false
    )
    val deletingIds by actualViewModel.deletingIds.collectAsStateWithLifecycle(initialValue = emptySet())

    // Dialog states
    var showInfoDialog by remember { mutableStateOf(false) }
    var selectedMediaItem by remember { mutableStateOf<MediaItem?>(null) }

    // Exit handling
    var backPressedOnce by remember { mutableStateOf(false) }

    // Handle back press: exit app with confirmation
    BackHandler {
        if (backPressedOnce) {
            // Exit app - TODO: implement proper exit
            // (LocalContext.current as? android.app.Activity)?.finishAffinity()
        } else {
            backPressedOnce = true
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Press back again to exit",
                    duration = SnackbarDuration.Short
                )
                delay(2000)
                backPressedOnce = false
            }
        }
    }
    val permanentSettingMenuEnabled by preferences?.permanentSettingMenuEnabled?.collectAsState(
        initial = false
    ) ?: remember { mutableStateOf(false) }

    // Calculate filtered item count for UI logic
    val filteredItemCount by remember(mediaItems.size, currentFilterState, mediaFolderUris) {
        derivedStateOf {
            mediaItems.count { item ->
               // Folder filter logic:
                // - If no folders configured: show all (user can't filter)
                // - If folders configured but none selected: show nothing (explicit filter)
                // - If folders selected: show only those folders
                val folderMatches = when {
                    mediaFolderUris.isEmpty() -> true // No folders configured, show all
                    currentFilterState.selectedFolders.isEmpty() -> false // Folders exist but none selected, show nothing
                    else -> UriPathConverter.isInMediaFolder(item.filePath, currentFilterState.selectedFolders)
                }

                // Tag filter: if selectedTags is empty or contains all, include all; otherwise filter by tags
                val tagMatches =
                    if (currentFilterState.selectedTags.isEmpty() || currentFilterState.isAllTagsSelected()) {
                        true
                    } else {
                        when {
                            ScreenshotTab.MARKED in currentFilterState.selectedTags && item.deletionTimestamp != null && !item.isKept -> true
                            ScreenshotTab.KEPT in currentFilterState.selectedTags && item.isKept -> true
                            ScreenshotTab.UNMARKED in currentFilterState.selectedTags && item.deletionTimestamp == null && !item.isKept -> true
                            ScreenshotTab.ALL in currentFilterState.selectedTags -> true
                            else -> false
                        }
                    }

                folderMatches && tagMatches
            }
        }
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Refresh screenshots and monitoring status when app resumes
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            DebugLogger.info("MainScreen", "Lifecycle event: $event")
            if (event == Lifecycle.Event.ON_RESUME) {
                DebugLogger.info(
                    "MainScreen",
                    "ON_RESUME: App regained focus - refreshing media items and monitoring status"
                )
                actualViewModel.refreshMediaItems()
                actualViewModel.refreshMonitoringStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Video lifecycle management for multiple simultaneous videos
    rememberVideoLifecycleManager()

    // Check if all permissions are granted to hide permission button
    var allPermissionsGranted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        val readPerm =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE

        val permissions = listOfNotNull(
            readPerm,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) "manage" else null,
            "overlay",
            "battery"
            // Notifications are now optional, removed from required permissions
        )

        updatePermissionStatuses(context, permissions) { statusMap ->
            allPermissionsGranted = statusMap.values.all { it }
        }
    }

    val listState = rememberLazyListState()

    val isScrolled by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }

    val visiblePageText by remember {
        derivedStateOf {
            val visibleItems = listState.layoutInfo.visibleItemsInfo
            val viewportHeight = listState.layoutInfo.viewportSize.height
            var startIndex = -1
            var endIndex = -1
            for (item in visibleItems) {
                val visibleHeight = when {
                    item.offset < 0 -> minOf(item.size + item.offset, viewportHeight)
                    item.offset + item.size > viewportHeight -> viewportHeight - item.offset
                    else -> item.size
                }.coerceAtLeast(0)
                val fraction = visibleHeight.toFloat() / item.size
                if (fraction > 0.5f) {
                    if (startIndex == -1) startIndex = item.index
                    endIndex = item.index
                }
            }
            if (startIndex != -1 && endIndex != -1) {
                "${startIndex + 1}:${endIndex + 1}"
            } else {
                ""
            }
        }
    }

    // Track if user has manually scrolled away from top
    var userHasScrolled by remember { mutableStateOf(false) }

    // Detect when user scrolls away from position 0 (manual scrolling)
    @Suppress("FrequentlyChangingValueReadInComposition")
    LaunchedEffect(listState.firstVisibleItemIndex) {
        if (listState.firstVisibleItemIndex > 0) {
            userHasScrolled = true
        } else if (listState.firstVisibleItemIndex == 0) {
            // Reset the flag when user scrolls back to top
            userHasScrolled = false
        }
    }

    // Show snackbar for status messages
    LaunchedEffect(uiState.message) {
        uiState.message?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
                actualViewModel.clearMessage()
            }
        }
    }

    // Available folders are the URIs themselves for display
    val availableUris = remember(mediaFolderUris) {
        mediaFolderUris.toList()
    }

    val availablePaths = remember(mediaFolderUris) {
        mediaFolderUris.mapNotNull { uri ->
            UriPathConverter.uriToFilePath(uri)
        }.toList()
    }

    // Auto-scroll to top when filter changes
    LaunchedEffect(currentFilterState) {
        scope.launch {
            listState.animateScrollToItem(0)
        }
        userHasScrolled = false // Reset scroll tracking on filter change
    }

    // Auto-scroll to top when new media items are added - simulate user scroll behavior
    LaunchedEffect(refreshTrigger) {
        if (mediaItems.isNotEmpty() && !userHasScrolled) {
            scope.launch {
                // Simulate user scroll: smooth scroll to top without delay
                listState.animateScrollToItem(0)
            }
        }
    }



    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    var isVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        isVisible = true
                    }
                    val alpha by animateFloatAsState(
                        targetValue = if (isVisible) 1f else 0f,
                        label = "titleAlpha"
                    )
                    Text(
                    text = BuildConfig.APP_DISPLAY_NAME,
                    style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                    ),
                    modifier = Modifier
                    .alpha(alpha)
                    .padding(start = 72.dp)
        )
                },

                actions = {
                    IconButton(onClick = {
                        when (monitoringStatus) {
                            MonitoringStatus.STOPPED -> actualViewModel.startMonitoring()
                            MonitoringStatus.ACTIVE -> actualViewModel.stopMonitoring()
                            MonitoringStatus.MISSING_PERMISSIONS -> actualViewModel.startMonitoring() // Will check permissions and start or show dialog
                        }
                    }) {
                        val (icon, color) = when (monitoringStatus) {
                            MonitoringStatus.STOPPED -> Icons.Default.PlayArrow to ErrorRed
                            MonitoringStatus.ACTIVE -> Icons.Default.Pause to SuccessGreen
                            MonitoringStatus.MISSING_PERMISSIONS -> Icons.Default.Pause to WarningOrange
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = when (monitoringStatus) {
                                MonitoringStatus.STOPPED -> stringResource(R.string.start_service)
                                MonitoringStatus.ACTIVE -> stringResource(R.string.stop_service)
                                MonitoringStatus.MISSING_PERMISSIONS -> stringResource(R.string.grant_permissions)
                            },
                            tint = color
                        )
                    }
                    IconButton(
                        onClick = { actualViewModel.refreshMediaItems() }, enabled = true
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.refresh)
                        )
                    }

                    if (!allPermissionsGranted) {
                        IconButton(onClick = { actualViewModel.showPermissionsDialog() }) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = stringResource(R.string.permissions)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },

        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            // Right side: page indicator, back to top and settings
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Page indicator (appears when scrolled)
                AnimatedVisibility(
                    visible = isScrolled && visiblePageText.isNotEmpty(),
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
                ) {
                    Text(
                        text = visiblePageText,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                // Back to top FAB (appears when scrolled)
                AnimatedVisibility(
                    visible = isScrolled,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
                ) {
                    FloatingActionButton(
                        onClick = {
                            scope.launch {
                                listState.animateScrollToItem(0)
                            }
                            // Reset scroll tracking when user uses the button
                            userHasScrolled = false
                        },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Icon(
                            Icons.Filled.KeyboardArrowUp,
                            contentDescription = "Back to top"
                        )
                    }
                }

                // Settings FAB (only visible when permanent setting menu is enabled)
                if (permanentSettingMenuEnabled) {
                    ExtendedFloatingActionButton(
                        onClick = { onOpenDrawer() },
                        icon = {
                            Icon(
                                Icons.Default.Settings,
                                stringResource(R.string.settings_button)
                            )
                        },
                        text = { Text(stringResource(R.string.settings_button)) },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Service status indicator
            ServiceStatusIndicator(
                monitoringStatus = monitoringStatus,
                allPermissionsGranted = allPermissionsGranted,
                onStatusClick = {
                    when (monitoringStatus) {
                        MonitoringStatus.STOPPED -> actualViewModel.startMonitoring()
                        MonitoringStatus.ACTIVE -> actualViewModel.stopMonitoring()
                        MonitoringStatus.MISSING_PERMISSIONS -> {} // Should not happen if permissions granted
                    }
                },
                onPermissionsClick = { actualViewModel.showPermissionsDialog() }
            )


            // Filters (always visible)
            TagFilterBar(
                selectedTags = currentFilterState.selectedTags,
                onTagSelectionChanged = { selected ->
                    val effective = if (selected.isEmpty()) setOf(
                        ScreenshotTab.MARKED,
                        ScreenshotTab.KEPT,
                        ScreenshotTab.UNMARKED
                    ) else selected
                    actualViewModel.updateTagSelection(effective)
                }
            )

            FolderFilterBar(
                availableUris = availableUris,
                availablePaths = availablePaths,
                selectedPaths = currentFilterState.selectedFolders,
                onFolderSelectionChanged = { actualViewModel.updateFolderSelection(it) }
            )

            // Content
            when {
                uiState.isLoading && filteredItemCount == 0 -> {
                    LoadingScreen()
                }

                filteredItemCount == 0 -> {
                    // Selected folders may be either file paths or URIs - handle both
                    val selectedFolderDisplayPaths = remember(currentFilterState.selectedFolders) {
                        val paths = currentFilterState.selectedFolders.mapNotNull { folderItem ->
                            val path = when {
                                // Already a file path (starts with /)
                                folderItem.startsWith("/") -> {
                                    DebugLogger.debug("EmptyState", "Already a file path: '$folderItem'")
                                    folderItem
                                }
                                // It's a URI - reconstruct and convert
                                else -> {
                                    val reconstructed = UriPathConverter.reconstructSafUri(context, folderItem)
                                    DebugLogger.debug("EmptyState", "Original URI: '$folderItem' -> Reconstructed: '$reconstructed'")
                                    
                                    val filePath = UriPathConverter.uriToFilePath(reconstructed, context)
                                    DebugLogger.debug("EmptyState", "Reconstructed URI: '$reconstructed' -> File path: '$filePath'")
                                    filePath
                                }
                            }
                            path
                        }
                        DebugLogger.info("EmptyState", "Selected folders: ${currentFilterState.selectedFolders}, Final paths: $paths")
                        paths
                    }
                    EmptyStateScreen(
                        tab = ScreenshotTab.ALL,
                        filterState = currentFilterState,
                        selectedFolderPaths = selectedFolderDisplayPaths
                    )
                }

                else -> {
                    var localLoading by remember { mutableStateOf(false) }
                    LaunchedEffect(uiState.isLoading) {
                        if (uiState.isLoading) {
                            localLoading = true
                        } else {
                            delay(
                                (200..1200).random().toLong()
                            ) // Show for 1.5 seconds after loading stops
                            localLoading = false
                        }
                    }
                    // NewScreenshotDetector with no loading to avoid UI shift
                    NewScreenshotDetector(
                        newScreenshotFlow = actualViewModel.newScreenshotDetected,
                        onLoadingChange = { }, // No-op to prevent loading bar
                        onNewScreenshot = {
                            scope.launch {
                                if (listState.firstVisibleItemIndex <= 1) { // At top or near top
                                    listState.animateScrollToItem(0)
                                }
                            }
                        }
                    )
                    val refreshState = rememberPullToRefreshState()
                    PullToRefreshBox(
                        isRefreshing = uiState.isLoading,
                        onRefresh = { actualViewModel.refreshMediaItems() },
                        state = refreshState
                    ) {
                        Column {
                            if (localLoading) {
                                Spacer(modifier = Modifier.height(8.dp))
                                LoadingBar()
                            }

                            // Remember callback functions to prevent unnecessary recompositions
                            val onScreenshotClickCallback =
                                remember {
                                    { item: MediaItem, position: androidx.compose.ui.geometry.Offset ->
                                        actualViewModel.openMediaItem(
                                            item,
                                            position
                                        )
                                    }
                                }
                            val onKeepClickCallback =
                                remember { { item: MediaItem -> actualViewModel.keepMediaItem(item) } }
                            val onUnkeepClickCallback =
                                remember { { item: MediaItem -> actualViewModel.unkeepMediaItem(item) } }
                            val onDeleteClickCallback =
                                remember { { item: MediaItem -> actualViewModel.deleteMediaItem(item) } }
                            val onLoadMoreCallback = remember { { actualViewModel.loadMoreMediaItems() } }

                            ScreenshotListComposable(
                                mediaItems = mediaItems,
                                currentFilterState = currentFilterState,
                                currentTime = currentTime,
                                listState = listState,
                                isLoading = uiState.isLoading,
                                liveVideoPreviewEnabled = liveVideoPreviewEnabled,
                                deletingIds = deletingIds,
                                mediaFolderUris = mediaFolderUris,
                                onScreenshotClick = onScreenshotClickCallback,
                                onKeepClick = onKeepClickCallback,
                                onUnkeepClick = onUnkeepClickCallback,
                                onDeleteClick = onDeleteClickCallback,
                                onLoadMore = onLoadMoreCallback,
                                showInfoDialog = showInfoDialog,
                                selectedMediaItem = selectedMediaItem,
                                onShowInfoDialog = { item ->
                                    selectedMediaItem = item
                                    showInfoDialog = true
                                },
                                onDismissInfoDialog = {
                                    showInfoDialog = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Video preview dialog
    uiState.videoPreviewItem?.let { mediaItem ->
        VideoPreviewDialog(
            mediaItem = mediaItem,
            position = uiState.videoPreviewPosition,
            onDismiss = { actualViewModel.closeVideoPreview() }
        )
    }

    // Image preview dialog
    uiState.imagePreviewItem?.let { mediaItem ->
        PicturePreviewDialog(
            mediaItem = mediaItem,
            onDismiss = { actualViewModel.closeImagePreview() }
        )
    }
}

fun updatePermissionStatuses(
    context: Context,
    permissions: List<String>,
    onUpdate: (Map<String, Boolean>) -> Unit
) {
    val newStatuses = permissions.associate { perm ->
        perm to when (perm) {
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_MEDIA_IMAGES -> ContextCompat.checkSelfPermission(
                context,
                perm
            ) == PackageManager.PERMISSION_GRANTED

            Manifest.permission.WRITE_EXTERNAL_STORAGE -> ContextCompat.checkSelfPermission(
                context,
                perm
            ) == PackageManager.PERMISSION_GRANTED

            Manifest.permission.POST_NOTIFICATIONS -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    perm
                ) == PackageManager.PERMISSION_GRANTED
            } else true // Assume granted for older versions
            "manage" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Environment.isExternalStorageManager()
                } else true
            }

            "overlay" -> Settings.canDrawOverlays(context)
            "battery" -> {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                powerManager.isIgnoringBatteryOptimizations(context.packageName)
            }

            else -> false
        }
    }
    onUpdate(newStatuses)
}

// New composable for previewing MainScreen content without HiltViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreenContentPreview() {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("App Title (Preview)") })
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Main Screen Preview Content",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
            }
        }
    )
}

// Preview functions
@Suppress("UnstableApiUsage")
@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    AppTheme {
        MainScreenContentPreview()
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyStatePreview() {
    AppTheme {
        EmptyStateScreen(tab = ScreenshotTab.ALL)
    }
}

@Preview(showBackground = true)
@Composable
fun ScreenshotCardPreview() {
    val sampleScreenshot = MediaItem(
        filePath = "/storage/emulated/0/Pictures/Screenshots/Screenshot_20241201_123456.png",
        fileName = "Screenshot_20241201_123456.png",
        fileSize = 1024000L,
        createdAt = System.currentTimeMillis() - 3600000L, // 1 hour ago
        deletionTimestamp = System.currentTimeMillis() + 60000L, // 1 minute from now
        isKept = false,
        contentUri = "content://media/external/images/media/12345"
    )

    AppTheme {
        ScreenshotCard(
            screenshot = sampleScreenshot,
            currentTime = System.currentTimeMillis(),
            onClick = {},
            onLongPress = {},
            onKeepClick = {},
            onUnkeepClick = {},
            onDeleteClick = {}
        )
    }
}



internal fun LazyListScope.SettingsContent(
    settingsViewModel: SettingsViewModel,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    folderPicker: ManagedActivityResultLauncher<Uri?, Uri?>,
    permissionLauncher: ManagedActivityResultLauncher<String, Boolean>,
    developerUnlocked: Boolean,
    navigator: DestinationsNavigator? = null,
    onDeveloperUnlocked: () -> Unit,
    onDeveloperLocked: () -> Unit,
    onOpenPermissions: () -> Unit = {},
    onShowFolderDialog: () -> Unit = {},
    onNavigateToConsole: () -> Unit = {},
    onThemeChangeCallback: (String) -> Unit = {}
) {
    // Interface & Appearance
    item {
        SettingsSectionHeader("Interface & Appearance")
    }

    item {
        ThemeSelector(
            currentTheme = settingsViewModel.currentTheme.collectAsState(initial = ThemeMode.SYSTEM).value,
            onThemeSelected = { theme ->
                scope.launch {
                    settingsViewModel.setThemeMode(theme)
                }
            },
            onThemeChange = { /* callback if needed */ }
        )
    }

    item {
        LanguageSelector(
            currentLanguage = settingsViewModel.language.collectAsState(initial = "en").value,
            onLanguageSelected = { lang ->
                scope.launch {
                    settingsViewModel.setLanguage(lang)
                }
            }
        )
    }

    // Permissions
    item {
        SettingsSectionHeader("Permissions")
    }

    item {
        PermissionsSection(onOpenPermissions = onOpenPermissions)
    }

    // Screenshot Management
    item {
        SettingsSectionHeader("Screenshot Management")
    }

    item {
        ModeSelector(
            isManualMode = settingsViewModel.isManualMode.collectAsState(initial = false).value,
            onModeChanged = { manual ->
                scope.launch {
                    settingsViewModel.setManualMode(manual)
                }
            },
            currentTime = settingsViewModel.deletionTime.collectAsState(initial = 15 * 60 * 1000L).value,
            onTimeSelected = { time ->
                scope.launch {
                    settingsViewModel.setDeletionTime(time)
                }
            }
        )
    }

    item {
        NotificationToggle(
            enabled = settingsViewModel.notificationsEnabled.collectAsState(initial = true).value,
            onToggle = { enabled ->
                scope.launch {
                    settingsViewModel.setNotificationsEnabled(enabled)
                }
            },
            onRequestPermission = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
        )
    }

    // Storage
    item {
        SettingsSectionHeader("Storage")
    }

    item {
        val folderUris =
            settingsViewModel.mediaFolderUris.collectAsState(initial = emptySet()).value
        val folderCount = folderUris.size
        val context = androidx.compose.ui.platform.LocalContext.current
        val summaryText = when {
            folderCount == 0 -> "No folders configured"
            folderCount == 1 -> {
                val uri = folderUris.first()
                UriPathConverter.uriToDisplayName(uri, context)
            }

            else -> "$folderCount folders selected"
        }

        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onShowFolderDialog)
        ) {
            ListItem(
                headlineContent = { Text("Media Folders") },
                supportingContent = { Text(summaryText) },
                trailingContent = {
                    Icon(Icons.Default.Folder, contentDescription = "Manage folders")
                }
            )
        }
    }

    // Experimental Features
    item {
        SettingsSectionHeader("Experimental Features")
    }

    item {
        AutoCleanupToggle(
            enabled = settingsViewModel.autoCleanupEnabled.collectAsState(initial = false).value,
            onToggle = { enabled ->
                scope.launch {
                    settingsViewModel.setAutoCleanupEnabled(enabled)
                }
            }
        )
    }

    item {
        LiveVideoPreviewToggle(
            enabled = settingsViewModel.liveVideoPreviewEnabled.collectAsState(initial = false).value,
            onToggle = { enabled ->
                scope.launch {
                    settingsViewModel.setLiveVideoPreviewEnabled(enabled)
                }
            }
        )
    }

    item {
        PermanentSettingMenuToggle(
            enabled = settingsViewModel.permanentSettingMenuEnabled.collectAsState(initial = false).value,
            onToggle = { enabled ->
                scope.launch {
                    settingsViewModel.setPermanentSettingMenuEnabled(enabled)
                }
            }
        )
    }

    // Developer options
    if (developerUnlocked) {
        item {
            CrashTestButton()
        }

        item {
            ConsoleButton(onNavigateToConsole = onNavigateToConsole)
        }

        item {
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text("Turn off developer mode") },
                    supportingContent = {
                        Text(
                            "Disable developer options and features.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingContent = {
                        OutlinedButton(onClick = onDeveloperLocked) {
                            Text("Turn Off")
                        }
                    }
                )
            }


        }
    }

    item {
        var recomposeTrigger by remember { mutableStateOf(0) }
        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
            ListItem(
                headlineContent = { Text("Recompose Layout") },
                supportingContent = {
                    Text(
                        "Force a layout recomposition for testing purposes. Triggered: $recomposeTrigger",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingContent = {
                    OutlinedButton(onClick = {
                        recomposeTrigger++
                        scope.launch {
                            snackbarHostState.showSnackbar("Layout recomposition triggered: $recomposeTrigger")
                        }
                    }) {
                        Text("Recompose")
                    }
                }
            )
        }
    }

    // Information Section
    item {
        SettingsSectionHeader(stringResource(R.string.information_section))
    }

    item {
        VersionInfo(
            isDeveloperMode = developerUnlocked,
            snackbarHostState = snackbarHostState,
            onActivateDeveloperMode = onDeveloperUnlocked
        )
    }

    item {
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuContent(
    isOpen: Boolean,
    onHomeClick: () -> Unit = {},
    onCloseDrawer: () -> Unit = {},
    isDrawerOpen: Boolean = false,
    preferences: ro.snapify.data.preferences.AppPreferences? = null,
    mainViewModel: MainViewModel? = null,
    settingsViewModel: SettingsViewModel? = null,
    navigator: DestinationsNavigator? = null,
    dialogContent: @Composable () -> Unit = {}
) {
    val actualMainViewModel = mainViewModel ?: hiltViewModel()
    val actualSettingsViewModel = settingsViewModel ?: hiltViewModel()
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showFolderDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val developerUnlocked by actualSettingsViewModel.developerModeEnabled.collectAsState(initial = false)

    // Handle back button to close drawer
    BackHandler {
        onCloseDrawer()
    }

    // Folder picker launcher
    val folderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            val contentResolver = context.contentResolver
            // Take persistable permission
            contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            // Save the URI
            scope.launch {
                actualSettingsViewModel.addMediaFolder(it.toString())
            }
        }
    }

    // Permission launcher for notifications
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Update permission status for UI
        // The NotificationToggle will check the actual permission
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    // Add padding to avoid FAB overlap when drawer closed
                    val titlePadding = if (isOpen) 0.dp else 80.dp

                    // Reusable bounce animation function
                    suspend fun animatedBounce(
                        animatable: Animatable<Float, AnimationVector1D>,
                        startDistance: Float,
                        numBounces: Int,
                        baseDuration: Long
                    ) {
                        animatable.snapTo(startDistance)
                        for (i in 0 until numBounces) {
                            // Bounce to left (0)
                            animatable.animateTo(
                                targetValue = 0f,
                                animationSpec = tween(durationMillis = (baseDuration * 0.8.pow(i)).toLong().coerceAtLeast(150L).toInt())
                            )
                            if (i < numBounces - 1) {
                                // Bounce back to right with decreasing amplitude
                                val nextDist = startDistance * 0.6.pow(i + 1).toFloat()
                                animatable.animateTo(
                                    targetValue = nextDist,
                                    animationSpec = tween(durationMillis = (baseDuration * 0.85.pow(i)).toLong().coerceAtLeast(150L).toInt())
                                )
                            }
                        }
                    }

                    // Reusable typewriter animation function
                    suspend fun animateTypewriter(
                        alphas: List<Animatable<Float, AnimationVector1D>>,
                        delays: List<Long>
                    ) {
                        alphas.forEach { it.snapTo(0f) }
                        alphas.forEachIndexed { index, animatable ->
                            kotlinx.coroutines.delay(delays.getOrElse(index) { 0L })
                            animatable.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(durationMillis = 100)
                            )
                        }
                    }

                    val textXAnimatable = remember { Animatable(0f) }
                    // Animated Settings title
                    LaunchedEffect(isOpen) {
                        if (isOpen) {
                            animatedBounce(textXAnimatable, 150.dp.value, 4, 500L)
                        }
                    }

                    val text = "Settings"
                    val letterAlphas = remember { List(text.length) { Animatable(0f) } }
                    val typewriterDelays = text.indices.map { 200 - it * 20L }
                    LaunchedEffect(isOpen) {
                        if (isOpen) {
                            animateTypewriter(letterAlphas, typewriterDelays)
                        }
                    }
                    Box(modifier = Modifier.padding(start = titlePadding)) {
                        Row(
                            modifier = Modifier.offset(x = textXAnimatable.value.dp)
                        ) {
                            text.forEachIndexed { index, char ->
                                Text(
                                    text = char.toString(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.alpha(letterAlphas[index].value),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
    val listState = rememberLazyListState()
    LazyColumnScrollbar(
    state = listState,
    settings = my.nanihadesuka.compose.ScrollbarSettings(alwaysShowScrollbar = true)
    ) {
    LazyColumn(
    state = listState,
    modifier = Modifier
    .fillMaxSize()
    .background(MaterialTheme.colorScheme.surface)
        .padding(horizontal = 16.dp, vertical = 16.dp)
            .padding(padding),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

    // Settings Content
    SettingsContent(
            settingsViewModel = actualSettingsViewModel,
                    scope = scope,
                    snackbarHostState = snackbarHostState,
                    folderPicker = folderPicker,
            permissionLauncher = permissionLauncher,
        developerUnlocked = developerUnlocked,
            navigator = navigator,
                onDeveloperUnlocked = { actualSettingsViewModel.setDeveloperModeEnabled(true) },
                    onDeveloperLocked = { actualSettingsViewModel.setDeveloperModeEnabled(false) },
                    onOpenPermissions = { showPermissionDialog = true },
                    onShowFolderDialog = { showFolderDialog = true },
                    onNavigateToConsole = {
                        context.startActivity(Intent(context, DebugConsoleActivity::class.java))
                    }
                )



                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    if (showFolderDialog) {
        FolderManagementDialog(
            mediaFolderUris = actualSettingsViewModel.mediaFolderUris.collectAsState(initial = emptySet()).value,
            onAddFolder = { folderPicker.launch(null) },
            onAddUri = { uri -> actualSettingsViewModel.addMediaFolder(uri) },
            onRemoveFolder = { actualSettingsViewModel.removeMediaFolder(it) },
            onDismiss = { showFolderDialog = false }
        )
    }

    if (showPermissionDialog) {
        PermissionDialog(
            onDismiss = { showPermissionDialog = false },
            onPermissionsUpdated = {
                // Start the service when permissions are granted
                val intent = android.content.Intent(
                    context,
                    ro.snapify.service.ScreenshotMonitorService::class.java
                )
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            },
            autoCloseWhenGranted = false
        )
    }

    dialogContent()
}



@Composable
private fun FolderManagementDialog(
    mediaFolderUris: Set<String>,
    onAddFolder: () -> Unit,
    onAddUri: (String) -> Unit,
    onRemoveFolder: (String) -> Unit,
    onDismiss: () -> Unit
) {


    // Create a stable list of URI-path pairs, sorted by formatted path for consistent display
    val folderItems = remember(mediaFolderUris) {
        mediaFolderUris.map { uri ->
            Pair(uri, uri) // Simple mapping for now: display URI as-is
        }.sortedBy { it.first }
    }

    val isOLED = MaterialTheme.colorScheme.surface == Color.Black
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = if (isOLED) Modifier.border(
            1.dp,
            Color.White,
            RoundedCornerShape(28.dp)
        ) else Modifier,
        title = {
            Text(
                "Manage Media Folders",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            MaterialTheme(colorScheme = MaterialTheme.colorScheme) {
                Column {
                    if (mediaFolderUris.isEmpty()) {
                        Text(
                            "No folders selected.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            "Selected folders:",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp)
                        ) {
                            folderItems.forEach { item ->
                                val (uri, _) = item
                                item {
                                    val context = androidx.compose.ui.platform.LocalContext.current
                                    val formattedPath = if (uri.isEmpty()) {
                                        "Default (Pictures/Screenshots)"
                                    } else {
                                        UriPathConverter.uriToDisplayName(uri, context)
                                    }

                                    MediaFolderItem(
                                        path = formattedPath,
                                        onRemove = { onRemoveFolder(uri) }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = onAddFolder,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Folder")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
private fun MediaFolderItem(
    path: String,
    onRemove: () -> Unit
) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {


            Icon(
                Icons.Default.Folder,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = path,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove folder",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimatedScreenshotCard(
    screenshot: MediaItem,
    currentTime: Long,
    isVisible: Boolean,
    isRefreshing: Boolean,
    liveVideoPreviewEnabled: Boolean,
    onClick: (androidx.compose.ui.geometry.Offset) -> Unit,
    onLongPress: () -> Unit,
    onKeepClick: () -> Unit,
    onUnkeepClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    key(screenshot.id) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(1000)
            ),
            exit = fadeOut(animationSpec = tween(500)) + scaleOut(
                targetScale = 0.8f,
                animationSpec = tween(500)
            )
        ) {
            ScreenshotCard(
                screenshot = screenshot,
                currentTime = currentTime,
                isRefreshing = isRefreshing,
                liveVideoPreviewEnabled = liveVideoPreviewEnabled,
                onClick = onClick,
                onLongPress = onLongPress,
                onKeepClick = onKeepClick,
                onUnkeepClick = onUnkeepClick,
                onDeleteClick = onDeleteClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenshotListComposable(
    mediaItems: SnapshotStateList<MediaItem>,
    currentFilterState: ro.snapify.data.model.FilterState,
    currentTime: Long,
    listState: androidx.compose.foundation.lazy.LazyListState,
    isLoading: Boolean,
    liveVideoPreviewEnabled: Boolean,
    deletingIds: Set<Long>,
    mediaFolderUris: Set<String>,
    onScreenshotClick: (MediaItem, androidx.compose.ui.geometry.Offset) -> Unit,
    onKeepClick: (MediaItem) -> Unit,
    onUnkeepClick: (MediaItem) -> Unit,
    onDeleteClick: (MediaItem) -> Unit,
    onLoadMore: () -> Unit,
    showInfoDialog: Boolean,
    selectedMediaItem: MediaItem?,
    onShowInfoDialog: (MediaItem) -> Unit,
    onDismissInfoDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Use remember with mediaItems.size as key to force recomposition when list changes
    val filteredMediaItems by remember(mediaItems.size, currentFilterState, mediaFolderUris) {
        derivedStateOf {
            mediaItems.filter { item ->
                // Folder filter logic:
                // - If no folders configured: show all (user can't filter)
                // - If folders configured but none selected: show nothing (explicit filter)
                // - If folders selected: show only those folders
                val folderMatches = when {
                    mediaFolderUris.isEmpty() -> true // No folders configured, show all
                    currentFilterState.selectedFolders.isEmpty() -> false // Folders exist but none selected, show nothing
                    else -> UriPathConverter.isInMediaFolder(item.filePath, currentFilterState.selectedFolders)
                }

                // Tag filter: if selectedTags is empty or contains all, include all; otherwise filter by tags
                val tagMatches =
                    if (currentFilterState.selectedTags.isEmpty() || currentFilterState.isAllTagsSelected()) {
                        true
                    } else {
                        when {
                            ScreenshotTab.MARKED in currentFilterState.selectedTags && item.deletionTimestamp != null && !item.isKept -> true
                            ScreenshotTab.KEPT in currentFilterState.selectedTags && item.isKept -> true
                            ScreenshotTab.UNMARKED in currentFilterState.selectedTags && item.deletionTimestamp == null && !item.isKept -> true
                            ScreenshotTab.ALL in currentFilterState.selectedTags -> true
                            else -> false
                        }
                    }

                folderMatches && tagMatches
            }
        }
    }
    
    DebugLogger.info(
        "ScreenshotListComposable",
        "Filtering: mediaItems.size=${mediaItems.size}, filtered=${filteredMediaItems.size}, folders=${currentFilterState.selectedFolders}"
    )

    DebugLogger.info(
        "ScreenshotListComposable",
        "RECOMPOSING: filteredItems=${filteredMediaItems.size}"
    )

    // Content
    val isAtBottom by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem?.index == listState.layoutInfo.totalItemsCount - 1
        }
    }

    LaunchedEffect(isAtBottom) {
        if (isAtBottom) {
            onLoadMore()
        }
    }

    LazyColumnScrollbar(
        state = listState,
        indicatorContent = { index, isThumbSelected ->
            // Paused for now
            /*
            val visibleItems = listState.layoutInfo.visibleItemsInfo
            val viewportHeight = listState.layoutInfo.viewportSize.height
            var startIndex = -1
            var endIndex = -1
            for (item in visibleItems) {
                val visibleHeight = when {
                    item.offset < 0 -> minOf(item.size + item.offset, viewportHeight)
                    item.offset + item.size > viewportHeight -> viewportHeight - item.offset
                    else -> item.size
                }.coerceAtLeast(0)
                val fraction = visibleHeight.toFloat() / item.size
                if (fraction > 0.5f) {
                    if (startIndex == -1) startIndex = item.index
                    endIndex = item.index
                }
            }
            val displayText = if (startIndex != -1 && endIndex != -1) {
                "${startIndex + 1}:${endIndex + 1}"
            } else {
                ""
            }
            Text(
                text = displayText,
                Modifier.background(if (isThumbSelected) Color.Red else Color.Black, CircleShape)
            )
            */
        }
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                count = filteredMediaItems.size,
                key = { index -> filteredMediaItems[index].id }
            ) { index ->
                val screenshot = filteredMediaItems[index]

                // Remember callbacks to prevent unnecessary recompositions
                val onClickCallback = remember(screenshot.id) {
                    { position: androidx.compose.ui.geometry.Offset ->
                        onScreenshotClick(
                            screenshot,
                            position
                        )
                    }
                }
                val onKeepCallback = remember(screenshot.id) { { onKeepClick(screenshot) } }
                val onUnkeepCallback = remember(screenshot.id) { { onUnkeepClick(screenshot) } }
                val onDeleteCallback = remember(screenshot.id) { { onDeleteClick(screenshot) } }
                val onLongPressCallback = remember(screenshot.id) {
                    {
                        onShowInfoDialog(screenshot)
                    }
                }

                AnimatedScreenshotCard(
                    screenshot = screenshot,
                    currentTime = currentTime,
                    isVisible = screenshot.id !in deletingIds,
                    isRefreshing = isLoading,
                    liveVideoPreviewEnabled = liveVideoPreviewEnabled,
                    onClick = onClickCallback,
                    onLongPress = onLongPressCallback,
                    onKeepClick = onKeepCallback,
                    onUnkeepClick = onUnkeepCallback,
                    onDeleteClick = onDeleteCallback
                )
            }

            // Loading indicator at bottom
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
        // Media Info Dialog
        if (showInfoDialog && selectedMediaItem != null) {
            MediaInfoDialog(
                mediaItem = selectedMediaItem!!,
                onKeep = {
                    onKeepClick(selectedMediaItem!!)
                    onDismissInfoDialog()
                },
                onUnkeep = {
                    onUnkeepClick(selectedMediaItem!!)
                    onDismissInfoDialog()
                },
                onDelete = {
                    onDeleteClick(selectedMediaItem!!)
                    onDismissInfoDialog()
                },
                onDismiss = onDismissInfoDialog
            )
        }
    }
}




@Destination
@RootNavGraph(start = true)
@Composable
fun MainScreenDestination(
    navigator: DestinationsNavigator
) {
    val viewModel = androidx.hilt.navigation.compose.hiltViewModel<MainViewModel>()
    val uiState by viewModel.uiState.collectAsState()

    val localContext = LocalContext.current
    val preferences = ro.snapify.data.preferences.AppPreferences(localContext)

    var isDrawerOpen by remember { mutableStateOf(false) }

    val fabX by animateDpAsState(
        targetValue = if (isDrawerOpen) (LocalConfiguration.current.screenWidthDp.dp - 72.dp) else 16.dp,
        animationSpec = tween(durationMillis = 600)
    )

    BackHandler(enabled = isDrawerOpen) {
        isDrawerOpen = false
    }

    DuoDrawer(
        isOpen = isDrawerOpen,
        onOpenDrawer = { isDrawerOpen = true },
        onCloseDrawer = { isDrawerOpen = false },
        showDialog = uiState.showPermissionDialog,
        menuContent = { drawerOpen ->
        MenuContent(
        isOpen = drawerOpen,
        onHomeClick = { isDrawerOpen = false },
        onCloseDrawer = { isDrawerOpen = false },
        isDrawerOpen = isDrawerOpen,
        preferences = preferences,
        mainViewModel = viewModel,
        navigator = navigator,
            dialogContent = {}
            )
        },
        content = {
            MainScreen(
                viewModel = viewModel,
                onOpenDrawer = { isDrawerOpen = true },
                preferences = preferences,
                isDrawerOpen = isDrawerOpen
            )
        },
        dialogContent = {
            if (uiState.showPermissionDialog) {
                PermissionDialog(
                    onDismiss = { viewModel.hidePermissionsDialog() },
                    onPermissionsUpdated = {
                        // Permissions were just granted, start the service
                        viewModel.startMonitoring()
                    },
                    autoCloseWhenGranted = true
                )
            }
        }
    )

    // FAB for drawer toggle
    FloatingActionButton(
        onClick = { isDrawerOpen = !isDrawerOpen },
        modifier = Modifier.offset(
            x = fabX,
            y = 40.dp
        ),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Crossfade(targetState = isDrawerOpen) { open ->
            Icon(
                imageVector = if (open) Icons.AutoMirrored.Filled.Shortcut else Icons.AutoMirrored.Filled.List,
                contentDescription = if (open) "Close drawer" else "Open drawer"
            )
        }
    }


}

@Preview(showBackground = true)
@Composable
fun PermissionDialogPreview() {
    AppTheme {
        PermissionDialog(onDismiss = {})
    }
}