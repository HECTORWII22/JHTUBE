package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppTab
import com.example.ui.MainViewModel
import com.example.ui.components.CompressionDialog
import com.example.ui.components.MiniPlayerBar
import com.example.ui.components.NoInternetScreen
import com.example.util.NetworkMonitor
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.PlayerScreen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.IconButton
import com.example.ui.screens.AboutRniScreen
import com.example.ui.theme.IconGray
import com.example.ui.theme.JhTubeRed
import com.example.ui.theme.LightSurfaceVariant
import com.example.ui.theme.PureWhite
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.PureBlack
import com.example.ui.theme.TextDisabled
import com.example.ui.theme.TextHighEmphasis
import com.example.ui.theme.TextMediumEmphasis

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val isConnected by NetworkMonitor.observeNetwork(applicationContext).collectAsStateWithLifecycle(initialValue = true)
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                // Hardware back handler returns to Explore tab
                BackHandler(enabled = uiState.currentTab != AppTab.EXPLORE) {
                    viewModel.selectTab(AppTab.EXPLORE)
                }

                if (!isConnected) {
                    NoInternetScreen(onRetry = {
                        // Retry action
                    })
                } else {
                    Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PureWhite)
                        .testTag("main_scaffold"),
                    containerColor = PureWhite,
                    topBar = {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = PureWhite,
                                titleContentColor = PureBlack
                            ),
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // JHTube Iconic Red Play Badge
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = JhTubeRed,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = null,
                                                tint = PureWhite,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "JHTUBE",
                                        color = PureBlack,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = (-0.5).sp
                                    )
                                }
                            },
                            actions = {
                                IconButton(
                                    onClick = { viewModel.selectTab(AppTab.ABOUT_RNI) },
                                    modifier = Modifier.testTag("top_bar_about_rni_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Sobre JHTUBE",
                                        tint = IconGray
                                    )
                                }
                                // Battery / Data Saver indicator
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(end = 12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BatterySaver,
                                        contentDescription = "Ahorro de batería y datos",
                                        tint = if (uiState.isLowDataMode) JhTubeRed else IconGray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (uiState.isLowDataMode) "2G/3G Eco" else "Normal",
                                        color = if (uiState.isLowDataMode) JhTubeRed else TextMediumEmphasis,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        )
                    },
                    bottomBar = {
                        Column {
                            // Mini Player Bar if video active and currently on another screen
                            if (uiState.currentPlayingItem != null && uiState.currentTab != AppTab.PLAYER) {
                                MiniPlayerBar(
                                    item = uiState.currentPlayingItem!!,
                                    isAudioOnly = uiState.isAudioOnlyPlayback,
                                    onClick = { viewModel.selectTab(AppTab.PLAYER) }
                                )
                            }

                            // Bottom Navigation (Clean White with JhTube Red & Gray Icons)
                            NavigationBar(
                                containerColor = PureWhite,
                                modifier = Modifier.testTag("bottom_nav_bar")
                            ) {
                                val navItems = listOf(
                                    Triple(AppTab.EXPLORE, "Explorar", Icons.Default.Explore),
                                    Triple(AppTab.PLAYER, "Reproductor", Icons.Default.PlayCircle),
                                    Triple(AppTab.LIBRARY, "Biblioteca", Icons.Default.VideoLibrary),
                                    Triple(AppTab.HISTORY, "Historial", Icons.Default.History),
                                    Triple(AppTab.ABOUT_RNI, "Sobre JHTube", Icons.Default.Info)
                                )

                                navItems.forEach { (tab, label, icon) ->
                                    val isSelected = uiState.currentTab == tab
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { viewModel.selectTab(tab) },
                                        icon = {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = label,
                                                tint = if (isSelected) JhTubeRed else IconGray
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = label,
                                                color = if (isSelected) JhTubeRed else IconGray,
                                                fontSize = 10.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            indicatorColor = LightSurfaceVariant,
                                            selectedIconColor = JhTubeRed,
                                            selectedTextColor = JhTubeRed,
                                            unselectedIconColor = IconGray,
                                            unselectedTextColor = IconGray
                                        ),
                                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (uiState.currentTab) {
                            AppTab.EXPLORE -> {
                                ExploreScreen(
                                    catalogItems = if (uiState.searchQuery.isNotBlank()) uiState.searchResults else uiState.catalogItems,
                                    isLoading = uiState.isCatalogLoading || uiState.isSearching,
                                    isLowDataMode = uiState.isLowDataMode,
                                    searchQuery = uiState.searchQuery,
                                    selectedCategory = uiState.selectedCategory,
                                    activeDownloadingId = uiState.activeDownloadingId,
                                    currentCatalogVersionTitle = uiState.currentCatalogVersionTitle,
                                    isOfflineCachedMode = uiState.isOfflineCachedMode,
                                    onToggleLowDataMode = { viewModel.toggleLowDataMode() },
                                    onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) },
                                    onCategorySelected = { viewModel.selectCategory(it) },
                                    onRefresh = { viewModel.loadCatalog() },
                                    onLoadPreviousVersion = { viewModel.loadPreviousSavedVersion() },
                                    onLoadOfflineCatalog = { viewModel.loadOfflineEmergencyCatalog() },
                                    onOpenDownloads = { viewModel.selectTab(AppTab.LIBRARY) },
                                    onOpenHistory = { viewModel.selectTab(AppTab.HISTORY) },
                                    onPlayMedia = { item, forceAudio -> viewModel.playMedia(item, forceAudio) },
                                    onDownloadMedia = { viewModel.startDownload(it) },
                                    onCompressMedia = { viewModel.openCompressionDialog(it) },
                                    onAiSummarize = { viewModel.analyzeMediaWithAi(it) },
                                    onPlayDirectUrl = { viewModel.playDirectUrl(it) }
                                )
                            }
                            AppTab.PLAYER -> {
                                PlayerScreen(
                                    currentMedia = uiState.currentPlayingItem,
                                    isAudioOnly = uiState.isAudioOnlyPlayback,
                                    playbackSpeed = uiState.playbackSpeed,
                                    selectedQuality = uiState.selectedQuality,
                                    downloadedItems = uiState.downloadedItems,
                                    onPlayMedia = { viewModel.playMedia(it, it.isAudioOnly) },
                                    onToggleAudioOnly = { viewModel.toggleAudioOnly() },
                                    onSpeedChanged = { viewModel.setPlaybackSpeed(it) },
                                    onQualitySelected = { viewModel.setPlaybackQuality(it) },
                                    onDownload = { viewModel.startDownload(it) },
                                    onCompress = { viewModel.openCompressionDialog(it) },
                                    onAiSummarize = { viewModel.analyzeMediaWithAi(it) },
                                    onNavigateExplore = { viewModel.selectTab(AppTab.EXPLORE) }
                                )
                            }
                            AppTab.LIBRARY -> {
                                LibraryScreen(
                                    downloadedItems = uiState.downloadedItems,
                                    storageSummary = uiState.storageSummary,
                                    compressionProgress = uiState.compressionProgress,
                                    isCompressing = uiState.isCompressing,
                                    cacheSizeBytes = uiState.cacheSizeBytes,
                                    onClearCache = { viewModel.clearMediaCache() },
                                    onCancelCompression = { viewModel.cancelCompression() },
                                    onPlayMedia = { viewModel.playMedia(it, it.isAudioOnly) },
                                    onCompressMedia = { viewModel.openCompressionDialog(it) },
                                    onDeleteMedia = { viewModel.deleteDownloadedItem(it) },
                                    onNavigateExplore = { viewModel.selectTab(AppTab.EXPLORE) }
                                )
                            }
                            AppTab.HISTORY -> {
                                HistoryScreen(
                                    historyItems = uiState.historyItems,
                                    userSession = uiState.userSession,
                                    isSyncing = uiState.isSyncingHistory,
                                    onLogin = { u, p -> viewModel.loginServ0(u, p) },
                                    onClearHistory = { viewModel.clearHistory() },
                                    onPlayHistoryItem = { hist ->
                                        // Resume playback
                                        val match = uiState.catalogItems.find { it.id == hist.mediaId }
                                            ?: uiState.downloadedItems.find { it.id == hist.mediaId }
                                            ?: com.example.data.model.MediaItem(
                                                id = hist.mediaId,
                                                title = hist.title,
                                                author = hist.author,
                                                streamUrl = "https://videorecoptilet-ipt-serv1.rejh.workers.dev/dl?id=${hist.mediaId}",
                                                downloadUrl = "https://videorecoptilet-ipt-serv1.rejh.workers.dev/dl?id=${hist.mediaId}"
                                            )
                                        viewModel.playMedia(match)
                                    }
                                )
                            }
                            AppTab.AI_ASSISTANT -> {
                                AiAssistantScreen(
                                    aiAnalysis = uiState.aiAnalysis,
                                    isAnalyzing = uiState.isAnalyzingAi,
                                    chatMessages = uiState.chatMessages,
                                    isAskingGemini = uiState.isAskingGemini,
                                    onSendMessage = { viewModel.sendAiQuestion(it) }
                                )
                            }
                            AppTab.ABOUT_RNI -> {
                                AboutRniScreen()
                            }
                        }

                        // Compression Dialog
                        if (uiState.showCompressionDialog && uiState.mediaToCompress != null) {
                            CompressionDialog(
                                mediaItem = uiState.mediaToCompress!!,
                                currentConfig = uiState.activeCompressionConfig,
                                onConfigChanged = { viewModel.updateCompressionConfig(it) },
                                onStartCompression = { viewModel.runCompression() },
                                onDismiss = { viewModel.closeCompressionDialog() }
                            )
                        }
                    }
                }
            }
        }
    }
}
}
