package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.HistoryEntity
import com.example.data.model.AiChatMessage
import com.example.data.model.AiMediaAnalysis
import com.example.data.model.CompressionConfig
import com.example.data.model.CompressionProgress
import com.example.data.model.CompressionStatus
import com.example.data.model.MediaItem
import com.example.data.model.MessageSender
import com.example.data.model.ResolutionPreset
import com.example.data.model.UserSession
import com.example.data.remote.GeminiService
import com.example.data.repository.CompressionRepository
import com.example.data.repository.MediaRepository
import com.example.data.repository.StorageSummary
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

import com.example.util.ExoPlayerCacheManager

enum class AppTab {
    EXPLORE,
    PLAYER,
    LIBRARY,
    HISTORY,
    AI_ASSISTANT,
    ABOUT_RNI
}

data class MainUiState(
    val currentTab: AppTab = AppTab.EXPLORE,
    val isLowDataMode: Boolean = true, // Default to true for 2G/3G / "Internet para Todos"
    val isAudioOnlyPlayback: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val selectedQuality: String = "360p (Ahorro)",
    val currentPlayingItem: MediaItem? = null,
    val isPlayerControlsVisible: Boolean = true,
    val cacheSizeBytes: Long = 0L,
    
    // Explore tab
    val catalogItems: List<MediaItem> = emptyList(),
    val isCatalogLoading: Boolean = false,
    val catalogError: String? = null,
    val searchQuery: String = "",
    val searchResults: List<MediaItem> = emptyList(),
    val isSearching: Boolean = false,
    val selectedCategory: String = "Todo",
    val currentCatalogVersionTitle: String = "En Vivo (Servidor)",
    val isOfflineCachedMode: Boolean = false,
    
    // Downloads & Library
    val downloadedItems: List<MediaItem> = emptyList(),
    val activeDownloadingId: String? = null,
    val downloadProgress: Float = 0f,
    val storageSummary: StorageSummary = StorageSummary(0L, 0L, 0L, 0L),
    
    // Compression
    val isCompressing: Boolean = false,
    val compressionProgress: CompressionProgress = CompressionProgress(),
    val showCompressionDialog: Boolean = false,
    val mediaToCompress: MediaItem? = null,
    val activeCompressionConfig: CompressionConfig = CompressionConfig(),
    
    // History & Session
    val historyItems: List<HistoryEntity> = emptyList(),
    val userSession: UserSession = UserSession(isOfflineMode = true),
    val isSyncingHistory: Boolean = false,
    
    // AI Assistant
    val aiAnalysis: AiMediaAnalysis? = null,
    val isAnalyzingAi: Boolean = false,
    val chatMessages: List<AiChatMessage> = listOf(
        AiChatMessage(
            sender = MessageSender.GEMINI,
            text = "¡Hola! Soy tu Asistente IA de StreamLite. Puedo resumirte videos para ahorrar datos en redes 2G/3G, extraer puntos clave, o responder cualquier duda sobre tus contenidos multimedia."
        )
    ),
    val isAskingGemini: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val mediaRepo = MediaRepository(application)
    private val compressionRepo = CompressionRepository(application)
    private val geminiService = GeminiService()

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var compressionJob: Job? = null
    private var downloadJob: Job? = null

    init {
        loadCatalog()
        observeDownloadedItems()
        observeHistory()
        refreshStorage()
    }

    fun selectTab(tab: AppTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun toggleLowDataMode() {
        _uiState.update {
            val nextState = !it.isLowDataMode
            it.copy(
                isLowDataMode = nextState,
                // Automatically switch to 360p or Audio-only when entering low data mode
                selectedQuality = if (nextState) "360p (Ahorro)" else "720p HD",
                isAudioOnlyPlayback = if (nextState) it.isAudioOnlyPlayback else false
            )
        }
    }

    fun toggleAudioOnly() {
        _uiState.update { it.copy(isAudioOnlyPlayback = !it.isAudioOnlyPlayback) }
    }

    fun setPlaybackSpeed(speed: Float) {
        _uiState.update { it.copy(playbackSpeed = speed) }
    }

    fun setPlaybackQuality(quality: String) {
        val audioOnly = quality.contains("Audio", ignoreCase = true)
        _uiState.update {
            it.copy(
                selectedQuality = quality,
                isAudioOnlyPlayback = audioOnly
            )
        }
    }

    fun selectCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
        if (category == "Todo") {
            loadCatalog()
        } else {
            searchCatalog(category)
        }
    }

    fun loadCatalog() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCatalogLoading = true, catalogError = null) }
            try {
                val items = mediaRepo.getTrendingMedia()
                _uiState.update {
                    it.copy(
                        catalogItems = items,
                        isCatalogLoading = false,
                        currentCatalogVersionTitle = "En Vivo (Servidor)",
                        isOfflineCachedMode = false
                    )
                }
            } catch (e: Exception) {
                val fallbackItems = mediaRepo.getPreviousSavedVersion()
                _uiState.update {
                    it.copy(
                        catalogItems = fallbackItems,
                        isCatalogLoading = false,
                        catalogError = "Sin conexión con el servidor. Mostrando versión guardada anterior.",
                        currentCatalogVersionTitle = "Última Sesión Guardada (Caché)",
                        isOfflineCachedMode = true
                    )
                }
            }
        }
    }

    fun loadPreviousSavedVersion() {
        val previousItems = mediaRepo.getPreviousSavedVersion()
        _uiState.update {
            it.copy(
                catalogItems = previousItems,
                currentCatalogVersionTitle = "Última Sesión Guardada (${previousItems.size} videos)",
                isOfflineCachedMode = true,
                catalogError = "Mostrando versión previa guardada en memoria local."
            )
        }
    }

    fun loadOfflineEmergencyCatalog() {
        val offlineItems = mediaRepo.getOfflineEmergencyCatalog()
        _uiState.update {
            it.copy(
                catalogItems = offlineItems,
                currentCatalogVersionTitle = "Catálogo Offline 2G/3G (${offlineItems.size} videos)",
                isOfflineCachedMode = true,
                catalogError = "Mostrando catálogo preinstalado para modo sin datos."
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
        } else {
            searchCatalog(query)
        }
    }

    private fun searchCatalog(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            val cleanedQuery = query.trim()
            val parsedVideoId = extractYoutubeVideoId(cleanedQuery)
            if (parsedVideoId != null) {
                // If they search a YouTube URL, immediately return a search result for that video
                val resolvedItem = MediaItem(
                    id = parsedVideoId,
                    title = "Video Enlazado ($parsedVideoId)",
                    author = "YouTube / JHTUBE",
                    streamUrl = "https://videorecoptilet-ipt-serv1.rejh.workers.dev/video?id=$parsedVideoId",
                    downloadUrl = "https://videorecoptilet-ipt-serv1.rejh.workers.dev/dl?id=$parsedVideoId",
                    fallbackUrl = "https://videorecoptilet-ipt-serv1.rejh.workers.dev/video?id=$parsedVideoId",
                    durationSeconds = 240L,
                    resolutionLabel = "144p Ultra-Ahorro"
                )
                _uiState.update {
                    it.copy(
                        searchResults = listOf(resolvedItem),
                        isSearching = false
                    )
                }
                return@launch
            }
            try {
                val results = mediaRepo.searchMedia(cleanedQuery)
                _uiState.update {
                    it.copy(
                        searchResults = results,
                        isSearching = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSearching = false) }
            }
        }
    }

    private fun extractYoutubeVideoId(url: String): String? {
        val pattern = "^(?:https?:\\/\\/)?(?:www\\.|m\\.)?(?:youtube\\.com\\/(?:watch\\?\\S*v=|embed\\/|v\\/)|youtu\\.be\\/)([a-zA-Z0-9_-]{11})"
        val regex = Regex(pattern)
        val matchResult = regex.find(url)
        return matchResult?.groupValues?.get(1)
    }

    fun playMedia(item: MediaItem, forceAudioOnly: Boolean = false) {
        _uiState.update {
            it.copy(
                currentPlayingItem = item,
                isAudioOnlyPlayback = forceAudioOnly || it.isAudioOnlyPlayback || item.isAudioOnly,
                currentTab = AppTab.PLAYER
            )
        }
        // Save to playback history
        recordHistory(item, 0L, item.durationSeconds * 1000L)
    }

    fun playDirectUrl(url: String) {
        if (url.isBlank()) return
        val cleanedUrl = url.trim()
        val videoId = extractYoutubeVideoId(cleanedUrl)
        if (videoId != null) {
            val customItem = MediaItem(
                id = videoId,
                title = "Video Enlazado",
                author = "YouTube / JHTUBE",
                streamUrl = "https://videorecoptilet-ipt-serv1.rejh.workers.dev/video?id=$videoId",
                downloadUrl = "https://videorecoptilet-ipt-serv1.rejh.workers.dev/stream?id=$videoId",
                fallbackUrl = "https://videorecoptilet-ipt-serv1.rejh.workers.dev/video?id=$videoId",
                durationSeconds = 240L,
                resolutionLabel = "144p Ultra-Ahorro"
            )
            playMedia(customItem)
        } else {
            val customItem = MediaItem(
                id = "direct_${System.currentTimeMillis()}",
                title = "Enlace Directo",
                author = "Transmisión Personalizada",
                streamUrl = cleanedUrl,
                downloadUrl = cleanedUrl,
                fallbackUrl = cleanedUrl,
                durationSeconds = 180L,
                resolutionLabel = "Directo"
            )
            playMedia(customItem)
        }
    }

    private fun recordHistory(item: MediaItem, positionMs: Long, durationMs: Long) {
        viewModelScope.launch {
            mediaRepo.recordHistory(item, positionMs, durationMs, _uiState.value.userSession.token)
        }
    }

    fun startDownload(item: MediaItem) {
        if (_uiState.value.activeDownloadingId != null) return
        downloadJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    activeDownloadingId = item.id,
                    downloadProgress = 0.05f
                )
            }
            val result = mediaRepo.downloadMediaFile(item) { progress, _ ->
                _uiState.update { it.copy(downloadProgress = progress) }
            }
            _uiState.update {
                it.copy(
                    activeDownloadingId = null,
                    downloadProgress = 0f
                )
            }
            refreshStorage()
        }
    }

    fun openCompressionDialog(item: MediaItem) {
        _uiState.update {
            it.copy(
                showCompressionDialog = true,
                mediaToCompress = item,
                activeCompressionConfig = CompressionConfig(
                    targetResolution = ResolutionPreset.RES_360P,
                    videoBitrateKbps = 600,
                    audioBitrateKbps = 64
                )
            )
        }
    }

    fun closeCompressionDialog() {
        _uiState.update { it.copy(showCompressionDialog = false) }
    }

    fun updateCompressionConfig(config: CompressionConfig) {
        _uiState.update { it.copy(activeCompressionConfig = config) }
    }

    fun runCompression() {
        val item = _uiState.value.mediaToCompress ?: return
        val config = _uiState.value.activeCompressionConfig
        closeCompressionDialog()

        compressionJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isCompressing = true,
                    compressionProgress = CompressionProgress(
                        mediaId = item.id,
                        status = CompressionStatus.PREPARING,
                        progress = 0.05f,
                        statusMessage = "Iniciando compresión de video local..."
                    )
                )
            }

            // Determine input file with guarantee of valid media bytes
            val inputFile = mediaRepo.ensureLocalMediaFile(item)

            val result = compressionRepo.compressVideo(inputFile, config) { progress, message ->
                _uiState.update {
                    it.copy(
                        compressionProgress = it.compressionProgress.copy(
                            progress = progress,
                            status = CompressionStatus.TRANSCODING,
                            statusMessage = message
                        )
                    )
                }
            }

            result.fold(
                onSuccess = { outputFile ->
                    mediaRepo.saveCompressedMediaItem(
                        originalItem = item,
                        compressedFile = outputFile,
                        resolutionLabel = config.targetResolution.label,
                        codec = config.codec.displayName
                    )
                    _uiState.update {
                        it.copy(
                            isCompressing = false,
                            compressionProgress = it.compressionProgress.copy(
                                status = CompressionStatus.COMPLETED,
                                progress = 1.0f,
                                statusMessage = "¡Guardado como ${config.targetResolution.label}! Espacio ahorrado.",
                                outputSizeBytes = outputFile.length(),
                                outputPath = outputFile.absolutePath
                            )
                        )
                    }
                    refreshStorage()
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isCompressing = false,
                            compressionProgress = it.compressionProgress.copy(
                                status = CompressionStatus.FAILED,
                                errorMessage = err.localizedMessage
                            )
                        )
                    }
                }
            )
        }
    }

    fun cancelCompression() {
        compressionJob?.cancel()
        _uiState.update {
            it.copy(
                isCompressing = false,
                compressionProgress = it.compressionProgress.copy(
                    status = CompressionStatus.CANCELLED,
                    statusMessage = "Compresión cancelada por el usuario"
                )
            )
        }
    }

    fun deleteDownloadedItem(id: String) {
        viewModelScope.launch {
            mediaRepo.deleteDownloadedMedia(id)
            refreshStorage()
        }
    }

    private fun observeDownloadedItems() {
        viewModelScope.launch {
            mediaRepo.getDownloadedMediaFlow()
                .catch { e -> android.util.Log.e("MainViewModel", "Error downloads: ${e.message}") }
                .collect { list ->
                    _uiState.update { it.copy(downloadedItems = list) }
                }
        }
    }

    private fun observeHistory() {
        viewModelScope.launch {
            mediaRepo.getHistoryFlow()
                .catch { e -> android.util.Log.e("MainViewModel", "Error history: ${e.message}") }
                .collect { list ->
                    _uiState.update { it.copy(historyItems = list) }
                }
        }
    }

    fun loginServ0(username: String, pass: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncingHistory = true) }
            val session = mediaRepo.loginServ0(username, pass)
            _uiState.update {
                it.copy(
                    userSession = session,
                    isSyncingHistory = false
                )
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            mediaRepo.clearHistory()
        }
    }

    fun refreshStorage() {
        viewModelScope.launch {
            val summary = compressionRepo.getStorageSummary()
            val cacheSize = ExoPlayerCacheManager.getCacheSizeBytes(getApplication())
            _uiState.update { it.copy(storageSummary = summary, cacheSizeBytes = cacheSize) }
        }
    }

    fun clearMediaCache() {
        viewModelScope.launch {
            ExoPlayerCacheManager.clearCache(getApplication())
            refreshStorage()
        }
    }

    // AI Features
    fun analyzeMediaWithAi(item: MediaItem) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isAnalyzingAi = true,
                    currentTab = AppTab.AI_ASSISTANT
                )
            }
            val analysis = geminiService.analyzeMedia(
                title = item.title,
                author = item.author,
                durationSec = item.durationSeconds
            )
            _uiState.update {
                it.copy(
                    aiAnalysis = analysis,
                    isAnalyzingAi = false,
                    chatMessages = it.chatMessages + AiChatMessage(
                        sender = MessageSender.GEMINI,
                        text = "📋 **Resumen de bajo consumo:**\n${analysis.summary}\n\n💡 **Ahorro de datos:** Has evitado descargar ~${String.format("%.1f", analysis.estimatedStreamingDataSavedMb)} MB de streaming de video.\n\n🎯 **Puntos clave:**\n" +
                                analysis.keyPoints.joinToString("\n") { p -> "• $p" }
                    )
                )
            }
        }
    }

    fun sendAiQuestion(question: String) {
        if (question.isBlank()) return
        val currentMediaTitle = _uiState.value.currentPlayingItem?.title
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    chatMessages = it.chatMessages + AiChatMessage(sender = MessageSender.USER, text = question),
                    isAskingGemini = true
                )
            }
            val answer = geminiService.askAssistant(question, currentMediaTitle)
            _uiState.update {
                it.copy(
                    chatMessages = it.chatMessages + AiChatMessage(sender = MessageSender.GEMINI, text = answer),
                    isAskingGemini = false
                )
            }
        }
    }
}
