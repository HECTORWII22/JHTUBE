package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.PlayArrow
import com.example.data.model.MediaItem
import com.example.ui.components.ExoPlayerView
import com.example.ui.theme.DarkCardBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.IconGray
import com.example.ui.theme.JhTubeRed
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextDisabled
import com.example.ui.theme.TextHighEmphasis
import com.example.ui.theme.TextMediumEmphasis

@Composable
fun PlayerScreen(
    currentMedia: MediaItem?,
    isAudioOnly: Boolean,
    playbackSpeed: Float,
    selectedQuality: String,
    downloadedItems: List<MediaItem> = emptyList(),
    onPlayMedia: (MediaItem) -> Unit = {},
    onToggleAudioOnly: () -> Unit,
    onSpeedChanged: (Float) -> Unit,
    onQualitySelected: (String) -> Unit,
    onDownload: (MediaItem) -> Unit,
    onCompress: (MediaItem) -> Unit,
    onAiSummarize: (MediaItem) -> Unit,
    onNavigateExplore: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (currentMedia == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(PureWhite)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircleOutline,
                    contentDescription = null,
                    tint = JhTubeRed,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Ningún medio en reproducción",
                    color = TextHighEmphasis,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Explora el catálogo o abre un archivo descargado de tu biblioteca.",
                    color = TextMediumEmphasis,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onNavigateExplore,
                    colors = ButtonDefaults.buttonColors(containerColor = JhTubeRed),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("player_go_explore_button")
                ) {
                    Text(text = "Explorar Contenido", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    var showQualityMenu by remember { mutableStateOf(false) }
    val qualityOptions = listOf("240p (Ultra 2G)", "360p (Ahorro)", "480p Estándar", "720p HD", "Solo Audio (2G)")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PureWhite)
            .verticalScroll(rememberScrollState())
            .testTag("player_screen")
    ) {
        // ExoPlayer View
        ExoPlayerView(
            mediaItem = currentMedia,
            isAudioOnly = isAudioOnly,
            playbackSpeed = playbackSpeed,
            onToggleAudioOnly = onToggleAudioOnly,
            onSpeedChanged = onSpeedChanged,
            onDownloadClick = { onDownload(currentMedia) },
            onCompressClick = { onCompress(currentMedia) },
            onAiSummarizeClick = { onAiSummarize(currentMedia) }
        )

        Column(modifier = Modifier.padding(14.dp)) {
            // Media Title & Author
            Text(
                text = currentMedia.title,
                color = TextHighEmphasis,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentMedia.author,
                    color = TextMediumEmphasis,
                    fontSize = 13.sp
                )
                if (currentMedia.isDownloaded) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.OfflinePin,
                            contentDescription = null,
                            tint = NeonEmerald,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Archivo Local",
                            color = NeonEmerald,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Pills Grid (Quality Selector, Audio Toggle, AI Summarize, Compress)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Quality Selector Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    Surface(
                        color = DarkSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showQualityMenu = true }
                            .testTag("quality_selector_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HighQuality,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = selectedQuality,
                                color = TextHighEmphasis,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showQualityMenu,
                        onDismissRequest = { showQualityMenu = false },
                        modifier = Modifier.background(DarkCardBackground)
                    ) {
                        qualityOptions.forEach { q ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = q,
                                        color = if (selectedQuality == q) NeonCyan else TextHighEmphasis
                                    )
                                },
                                onClick = {
                                    onQualitySelected(q)
                                    showQualityMenu = false
                                }
                            )
                        }
                    }
                }

                // Download Button
                if (!currentMedia.isDownloaded) {
                    Surface(
                        color = DarkSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onDownload(currentMedia) }
                            .testTag("player_download_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = NeonEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Descargar",
                                color = NeonEmerald,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                // AI Summarize Button
                Surface(
                    color = DarkSurfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonAmber.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onAiSummarize(currentMedia) }
                        .testTag("player_ai_summarize_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = NeonAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Resumir IA",
                            color = NeonAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Compression & Download Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Compress Video Button
                Button(
                    onClick = { onCompress(currentMedia) },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("player_compress_action_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Compress,
                        contentDescription = null,
                        tint = IconGray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Comprimir", color = TextHighEmphasis, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // Download Button
                Button(
                    onClick = { onDownload(currentMedia) },
                    colors = ButtonDefaults.buttonColors(containerColor = JhTubeRed),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("player_download_action_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Descargar", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Efficiency & Bandwidth Analytics Card
            Surface(
                color = DarkCardBackground,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Métricas de Ahorro y Conexión (2G/3G)",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Consumo Estimado", color = TextMediumEmphasis, fontSize = 10.sp)
                            Text(
                                text = if (isAudioOnly) "~0.4 MB/minuto" else "~2.5 MB/minuto (360p)",
                                color = NeonEmerald,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text(text = "Códec Recomendado", color = TextMediumEmphasis, fontSize = 10.sp)
                            Text(text = "H.265 / AAC 64k", color = TextHighEmphasis, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Column {
                            Text(text = "Ahorro Activo", color = TextMediumEmphasis, fontSize = 10.sp)
                            Text(
                                text = if (isAudioOnly) "-85%" else "-60%",
                                color = NeonEmerald,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cached,
                            contentDescription = null,
                            tint = NeonEmerald,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Media3 SimpleCache: Segmentos en disco (reproducción sin gastar datos)",
                            color = TextMediumEmphasis,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Quick Offline Playback List: lists other cached videos from Room database for instant play
            val otherOfflineItems = remember(downloadedItems, currentMedia) {
                downloadedItems.filter { it.id != currentMedia?.id }
            }
            if (otherOfflineItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "Más Videos Locales (Descargados)",
                    color = TextHighEmphasis,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    otherOfflineItems.forEach { item ->
                        Surface(
                            color = DarkSurfaceVariant,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPlayMedia(item) }
                                .testTag("player_offline_item_${item.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(72.dp)
                                        .aspectRatio(16f / 9f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(PureBlack),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!item.thumbnailUrl.isNullOrBlank()) {
                                        coil.compose.AsyncImage(
                                            model = item.thumbnailUrl,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(Color(0x99000000)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = PureWhite,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        color = TextHighEmphasis,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = item.author,
                                            color = TextMediumEmphasis,
                                            fontSize = 9.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = item.resolutionLabel,
                                            color = JhTubeRed,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "(${item.formattedSize})",
                                            color = TextMediumEmphasis,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
