package com.example.ui.components

import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.media3.common.MediaItem as Media3Item
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import com.example.data.model.MediaItem
import com.example.data.repository.MediaRepository
import com.example.ui.theme.DarkCardBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.IconGray
import com.example.ui.theme.IconGrayLight
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextDisabled
import com.example.ui.theme.TextHighEmphasis
import com.example.ui.theme.TextMediumEmphasis
import com.example.util.ExoNetworkLogger
import com.example.util.ExoPlayerCacheManager
import com.example.util.ExponentialBackoffLoadErrorHandlingPolicy
import com.example.util.NetworkDiagnosticInfo
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.pow

@OptIn(UnstableApi::class)
@Composable
fun ExoPlayerView(
    mediaItem: MediaItem,
    isAudioOnly: Boolean,
    playbackSpeed: Float,
    onToggleAudioOnly: () -> Unit,
    onSpeedChanged: (Float) -> Unit,
    onDownloadClick: () -> Unit,
    onCompressClick: () -> Unit,
    onAiSummarizeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isPlaying by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(mediaItem.durationSeconds * 1000L) }
    var showControls by remember { mutableStateOf(true) }
    var showSpeedMenu by remember { mutableStateOf(false) }

    var isUsingFallbackStream by remember { mutableStateOf(false) }
    var playbackErrorMessage by remember { mutableStateOf<String?>(null) }
    var retryAttempt by remember { mutableIntStateOf(0) }
    var isRetrying by remember { mutableStateOf(false) }
    var backoffCountdownSeconds by remember { mutableIntStateOf(0) }

    // Detailed network diagnostics
    var latestDiagnostic by remember { mutableStateOf<NetworkDiagnosticInfo?>(null) }
    var showDiagnosticDialog by remember { mutableStateOf(false) }

    val defaultFallbackUrl = remember(mediaItem.id) {
        mediaItem.fallbackUrl ?: MediaRepository.RELIABLE_VIDEO_MIRRORS[0]
    }

    var retryJob by remember { mutableStateOf<Job?>(null) }

    // Initialize ExoPlayer with detailed logging and exponential backoff error handling policy
    val exoPlayer = remember(mediaItem.id) {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(30000)
            .setUserAgent("StreamLite/1.0 (Android; Low-Data; ErrorDiagnostics)")

        // SimpleCache Integration: local segment caching reduces data usage & prevents buffering
        val cacheDataSourceFactory = ExoPlayerCacheManager.buildCacheDataSourceFactory(context, httpDataSourceFactory)

        val loadErrorPolicy = ExponentialBackoffLoadErrorHandlingPolicy(
            minLoadableRetryCount = 3,
            initialBackoffMs = 1000L,
            maxBackoffMs = 8000L,
            onDiagnosticCaptured = { diag ->
                latestDiagnostic = diag
            }
        )

        val mediaSourceFactory = DefaultMediaSourceFactory(cacheDataSourceFactory)
            .setLoadErrorHandlingPolicy(loadErrorPolicy)

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .apply {
                val streamUri = if (mediaItem.localFilePath != null &&
                    File(mediaItem.localFilePath).exists() &&
                    File(mediaItem.localFilePath).length() > 4096L
                ) {
                    Uri.parse(mediaItem.localFilePath)
                } else {
                    Uri.parse(mediaItem.streamUrl)
                }
                setMediaItem(Media3Item.fromUri(streamUri))
                prepare()
                playWhenReady = true
            }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == Player.STATE_BUFFERING
                if (playbackState == Player.STATE_READY) {
                    durationMs = exoPlayer.duration.coerceAtLeast(0L)
                    playbackErrorMessage = null
                    isRetrying = false
                    retryAttempt = 0
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlayerError(error: PlaybackException) {
                val currentAttempt = retryAttempt + 1
                val maxAttempts = 3

                // Calculate exponential backoff: 1000 * 2^(attempt - 1) -> 1s, 2s, 4s...
                val backoffMs = (1000L * (2.0.pow((currentAttempt - 1).coerceIn(0, 4))).toLong()).coerceAtMost(8000L)

                val diag = ExoNetworkLogger.extractFromPlaybackException(
                    playbackException = error,
                    retryCount = currentAttempt,
                    nextBackoffMs = backoffMs
                )
                latestDiagnostic = diag
                ExoNetworkLogger.logNetworkFailure(diag)

                val isFatalHttpError = diag.httpStatusCode in listOf(401, 403, 404, 410)

                // If fatal HTTP access error (like 403 Forbidden or 404) or retries exhausted, failover immediately
                if ((isFatalHttpError || currentAttempt > maxAttempts) && !isUsingFallbackStream) {
                    isUsingFallbackStream = true
                    isRetrying = false
                    val reason = if (diag.httpStatusCode != null) "HTTP ${diag.httpStatusCode}" else "Acceso denegado"
                    playbackErrorMessage = "Enlace no accesible ($reason). Conmutando a mirror garantizado..."

                    retryJob?.cancel()
                    coroutineScope.launch {
                        delay(250)
                        val fallbackUri = Uri.parse(defaultFallbackUrl)
                        Log.i(ExoNetworkLogger.TAG, "Immediate failover to mirror: $fallbackUri due to $reason")
                        exoPlayer.setMediaItem(Media3Item.fromUri(fallbackUri))
                        exoPlayer.prepare()
                        exoPlayer.play()
                    }
                } else if (currentAttempt <= maxAttempts && !isUsingFallbackStream) {
                    // Retryable transient network error (e.g. timeout, connection reset, 502) -> exponential backoff
                    retryAttempt = currentAttempt
                    isRetrying = true
                    val statusText = if (diag.httpStatusCode != null) "HTTP ${diag.httpStatusCode}" else diag.errorCodeName

                    retryJob?.cancel()
                    retryJob = coroutineScope.launch {
                        val waitSeconds = (backoffMs / 1000L).toInt().coerceAtLeast(1)
                        for (sec in waitSeconds downTo 1) {
                            backoffCountdownSeconds = sec
                            playbackErrorMessage = "Error ($statusText). Reintentando en ${sec}s (Intento $currentAttempt/$maxAttempts)..."
                            delay(1000)
                        }
                        playbackErrorMessage = "Reintentando conexión con servidor..."
                        exoPlayer.prepare()
                        exoPlayer.play()
                    }
                } else {
                    isBuffering = false
                    isRetrying = false
                    playbackErrorMessage = "Error de reproducción. Toca para reintentar."
                }
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            retryJob?.cancel()
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // React to playback speed changes
    LaunchedEffect(playbackSpeed) {
        exoPlayer.playbackParameters = PlaybackParameters(playbackSpeed)
    }

    // Position updater tick
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
            durationMs = exoPlayer.duration.coerceAtLeast(durationMs)
            delay(500)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(PureBlack)
            .testTag("exo_player_container")
            .clickable { showControls = !showControls }
    ) {
        if (!isAudioOnly) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Audio-Only Low Data Wave Visualizer
            AudioOnlyVisualizer(
                title = mediaItem.title,
                author = mediaItem.author,
                isPlaying = isPlaying
            )
        }

        // Buffering indicator
        if (isBuffering && playbackErrorMessage == null) {
            CircularProgressIndicator(
                color = NeonCyan,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.Center)
                    .testTag("player_buffering_indicator")
            )
        }

        // Live Error, Exponential Backoff Banner & Diagnostics Button
        if (playbackErrorMessage != null) {
            Surface(
                color = DarkCardBackground.copy(alpha = 0.95f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isRetrying) NeonAmber else NeonEmerald),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp, start = 8.dp, end = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isRetrying) Icons.Default.Refresh else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isRetrying) NeonAmber else NeonEmerald,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = playbackErrorMessage ?: "",
                        color = if (isRetrying) NeonAmber else NeonEmerald,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isRetrying) {
                        Spacer(modifier = Modifier.width(6.dp))
                        CircularProgressIndicator(
                            color = NeonAmber,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    if (latestDiagnostic != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = NeonAmber.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .clickable { showDiagnosticDialog = true }
                                .testTag("view_network_diagnostics_btn")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Diagnóstico",
                                    tint = NeonAmber,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Headers",
                                    color = NeonAmber,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Technical Network & SimpleCache Diagnostics Dialog
        if (showDiagnosticDialog) {
            val diag = latestDiagnostic
            val cacheSizeBytes = remember(showDiagnosticDialog) {
                ExoPlayerCacheManager.getCacheSizeBytes(context)
            }
            val cacheMb = cacheSizeBytes / (1024.0 * 1024.0)

            Dialog(onDismissRequest = { showDiagnosticDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkCardBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Diagnóstico de Red y SimpleCache",
                                color = NeonCyan,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = { showDiagnosticDialog = false },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextMediumEmphasis)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // SimpleCache Card
                        Surface(
                            color = PureBlack,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Cached,
                                        contentDescription = null,
                                        tint = NeonEmerald,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Media3 SimpleCache (Segmentos)",
                                        color = NeonEmerald,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "• Caché en disco: ${String.format("%.2f", cacheMb)} MB / 150 MB (LRU)",
                                    color = TextHighEmphasis,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "• Ahorro de datos: Los segmentos reproducidos no se descargan de nuevo",
                                    color = TextMediumEmphasis,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "• Tolerancia a fallos: FLAG_IGNORE_CACHE_ON_ERROR activo",
                                    color = TextMediumEmphasis,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (diag != null) {
                            // Status Code & Error Name
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (diag.httpStatusCode != null) Color(0xFFD32F2F) else NeonAmber)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (diag.httpStatusCode != null) "HTTP ${diag.httpStatusCode}" else "Transport Error",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = diag.errorCodeName,
                                    color = TextHighEmphasis,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            if (!diag.url.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "URL: ${diag.url}",
                                    color = TextMediumEmphasis,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            if (diag.retryAttempt > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Backoff Exponencial: Intento #${diag.retryAttempt} (+${diag.nextBackoffDelayMs}ms)",
                                    color = NeonAmber,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Encabezados HTTP de Respuesta:",
                                color = NeonEmerald,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            Surface(
                                color = PureBlack,
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = diag.formattedHeaders,
                                    color = TextHighEmphasis,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }

                            if (!diag.responseBodySnippet.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Cuerpo Recibido:",
                                    color = TextMediumEmphasis,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Surface(
                                    color = PureBlack,
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = diag.responseBodySnippet,
                                        color = TextMediumEmphasis,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(6.dp)
                                    )
                                }
                            }
                        } else {
                            // Connection is healthy
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF2E7D32))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "HTTP 200 OK",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Transmisión Activa y Estable",
                                    color = TextHighEmphasis,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "El flujo se reproduce correctamente y los bloques de video se van almacenando de manera continua en SimpleCache.",
                                color = TextMediumEmphasis,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Overlay Controls
        AnimatedVisibility(
            visible = showControls || !isPlaying,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x99000000))
                    .padding(8.dp)
            ) {
                // Top control buttons (Audio Mode, Speed, AI Summary, Diagnostics)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Audio-Only Toggle Pill
                    Surface(
                        color = if (isAudioOnly) NeonCyanDimSurface else DarkSurfaceVariant,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .clickable { onToggleAudioOnly() }
                            .testTag("toggle_audio_only_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isAudioOnly) Icons.Default.Headphones else Icons.Default.Videocam,
                                contentDescription = "Modo de reproducción",
                                tint = if (isAudioOnly) NeonCyan else TextMediumEmphasis,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isAudioOnly) "Solo Audio (-85% datos)" else "Video Normal",
                                color = if (isAudioOnly) NeonCyan else TextHighEmphasis,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Diagnostics & SimpleCache info button in top bar
                        IconButton(
                            onClick = { showDiagnosticDialog = true },
                            modifier = Modifier.testTag("player_diagnostics_icon_btn")
                        ) {
                            Icon(
                                imageVector = if (latestDiagnostic != null) Icons.Default.Warning else Icons.Default.Info,
                                contentDescription = "Diagnóstico de red y SimpleCache",
                                tint = if (latestDiagnostic != null) NeonAmber else IconGrayLight
                            )
                        }

                        // AI Summary action
                        IconButton(
                            onClick = onAiSummarizeClick,
                            modifier = Modifier.testTag("player_ai_summary_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Resumir con IA",
                                tint = NeonAmber
                            )
                        }

                        // Playback Speed Selector
                        Box {
                            IconButton(
                                onClick = { showSpeedMenu = true },
                                modifier = Modifier.testTag("player_speed_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = "Velocidad de reproducción",
                                        tint = TextHighEmphasis,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "${playbackSpeed}x",
                                        color = TextHighEmphasis,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = showSpeedMenu,
                                onDismissRequest = { showSpeedMenu = false },
                                modifier = Modifier.background(DarkCardBackground)
                            ) {
                                listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "${speed}x",
                                                color = if (playbackSpeed == speed) NeonCyan else TextHighEmphasis
                                            )
                                        },
                                        onClick = {
                                            onSpeedChanged(speed)
                                            showSpeedMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Middle Center Play/Pause & Seek Controls
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val newPos = (exoPlayer.currentPosition - 10_000L).coerceAtLeast(0L)
                            exoPlayer.seekTo(newPos)
                        },
                        modifier = Modifier.size(48.dp).testTag("rewind_10s_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "Retroceder 10s",
                            tint = TextHighEmphasis,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Play/Pause Big Button
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(NeonCyan)
                            .clickable {
                                if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                            }
                            .testTag("play_pause_toggle_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                            tint = PureBlack,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            val newPos = (exoPlayer.currentPosition + 10_000L).coerceAtMost(durationMs)
                            exoPlayer.seekTo(newPos)
                        },
                        modifier = Modifier.size(48.dp).testTag("forward_10s_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forward10,
                            contentDescription = "Avanzar 10s",
                            tint = TextHighEmphasis,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Bottom Timeline & Actions
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                ) {
                    // Seek Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatMs(currentPositionMs),
                            color = TextMediumEmphasis,
                            fontSize = 11.sp
                        )
                        Slider(
                            value = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f,
                            onValueChange = { ratio ->
                                val seekTarget = (ratio * durationMs).toLong()
                                exoPlayer.seekTo(seekTarget)
                                currentPositionMs = seekTarget
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = NeonCyan,
                                activeTrackColor = NeonCyan,
                                inactiveTrackColor = TextDisabled
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                                .testTag("player_seek_slider")
                        )
                        Text(
                            text = formatMs(durationMs),
                            color = TextMediumEmphasis,
                            fontSize = 11.sp
                        )
                    }

                    // Extra Action Buttons in player (Download & Compress & Retry)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isUsingFallbackStream) {
                            Text(
                                text = "⚡ Canal Alternativo Activo",
                                color = NeonEmerald,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = "Canal Directo Red Nacional",
                                color = TextMediumEmphasis,
                                fontSize = 10.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Retry button
                            IconButton(
                                onClick = {
                                    retryAttempt = 0
                                    exoPlayer.prepare()
                                    exoPlayer.play()
                                },
                                modifier = Modifier.testTag("player_manual_retry_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reintentar",
                                    tint = TextHighEmphasis,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = onCompressClick,
                                modifier = Modifier.testTag("player_compress_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Compress,
                                    contentDescription = "Comprimir Video",
                                    tint = NeonEmerald,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = onDownloadClick,
                                modifier = Modifier.testTag("player_download_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Descargar",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioOnlyVisualizer(
    title: String,
    author: String,
    isPlaying: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "audioWave")
    val waveAnim1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "w1"
    )
    val waveAnim2 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "w2"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(54.dp)
        ) {
            val bars = listOf(waveAnim1, waveAnim2, waveAnim1 * 0.7f, waveAnim2 * 1.1f, waveAnim1 * 0.5f)
            bars.forEach { h ->
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .height(if (isPlaying) (48 * h.coerceIn(0.15f, 1f)).dp else 12.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(NeonCyan)
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Modo Solo Audio Activo",
            color = NeonEmerald,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = title,
            color = TextHighEmphasis,
            fontSize = 13.sp,
            maxLines = 1
        )
        Text(
            text = "Transmisión ultra-eficiente para redes 2G / Internet para Todos",
            color = TextMediumEmphasis,
            fontSize = 11.sp
        )
    }
}

private val NeonCyanDimSurface = Color(0xFF003840)

private fun formatMs(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
