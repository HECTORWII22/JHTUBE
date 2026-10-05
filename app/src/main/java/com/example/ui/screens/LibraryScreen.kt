package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CompressionProgress
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Cached
import com.example.data.model.MediaItem
import com.example.data.repository.StorageSummary
import com.example.ui.components.CompressionActiveBanner
import com.example.ui.components.StorageSummaryCard
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
fun LibraryScreen(
    downloadedItems: List<MediaItem>,
    storageSummary: StorageSummary,
    compressionProgress: CompressionProgress,
    isCompressing: Boolean,
    cacheSizeBytes: Long = 0L,
    onClearCache: () -> Unit = {},
    onCancelCompression: () -> Unit,
    onPlayMedia: (MediaItem) -> Unit,
    onCompressMedia: (MediaItem) -> Unit,
    onDeleteMedia: (String) -> Unit,
    onNavigateExplore: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("Todos") }
    val filters = listOf("Todos", "Comprimidos", "Solo Audio")

    val filteredList = remember(downloadedItems, selectedFilter) {
        when (selectedFilter) {
            "Comprimidos" -> downloadedItems.filter { it.isCompressed }
            "Solo Audio" -> downloadedItems.filter { it.isAudioOnly }
            else -> downloadedItems
        }
    }

    val compressedCount = downloadedItems.count { it.isCompressed }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PureWhite)
            .padding(horizontal = 14.dp)
            .testTag("library_screen")
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Storage Footprint Summary
        StorageSummaryCard(
            summary = storageSummary,
            downloadCount = downloadedItems.size,
            compressedCount = compressedCount
        )

        // Media3 SimpleCache Local Segment Cache Status
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            color = DarkCardBackground,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
            modifier = Modifier.fillMaxWidth().testTag("simple_cache_status_card")
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Cached,
                        contentDescription = "Caché de segmentos",
                        tint = IconGray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Caché ExoPlayer (SimpleCache)",
                            color = PureWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        val mb = cacheSizeBytes / (1024.0 * 1024.0)
                        Text(
                            text = if (mb >= 0.1) String.format("%.1f MB en caché local (sin datos)", mb) else "Caché activo (LRU 150 MB)",
                            color = TextMediumEmphasis,
                            fontSize = 11.sp
                        )
                    }
                }

                if (cacheSizeBytes > 0) {
                    IconButton(
                        onClick = onClearCache,
                        modifier = Modifier.testTag("clear_simple_cache_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Limpiar Caché",
                            tint = IconGray
                        )
                    }
                }
            }
        }

        // Active Compression Banner
        if (isCompressing) {
            Spacer(modifier = Modifier.height(10.dp))
            CompressionActiveBanner(
                progress = compressionProgress,
                onCancel = onCancelCompression
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Pills
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            items(filters) { filter ->
                val isSelected = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) JhTubeRed else DarkSurfaceVariant)
                        .clickable { selectedFilter = filter }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("library_filter_$filter")
                ) {
                    Text(
                        text = filter,
                        color = if (isSelected) PureWhite else TextHighEmphasis,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (downloadedItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        tint = JhTubeRed,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Tu biblioteca local está vacía",
                        color = TextHighEmphasis,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Descarga videos desde el catálogo para reproducirlos sin gastar datos y comprimirlos localmente.",
                        color = TextMediumEmphasis,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onNavigateExplore,
                        colors = ButtonDefaults.buttonColors(containerColor = JhTubeRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("library_explore_button")
                    ) {
                        Text(text = "Ir al Catálogo", color = PureWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier.testTag("downloaded_media_list")
            ) {
                items(filteredList, key = { it.id }) { item ->
                    DownloadedMediaItemCard(
                        item = item,
                        onPlay = { onPlayMedia(item) },
                        onCompress = { onCompressMedia(item) },
                        onDelete = { onDeleteMedia(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun DownloadedMediaItemCard(
    item: MediaItem,
    onPlay: () -> Unit,
    onCompress: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = DarkCardBackground,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (item.isCompressed) JhTubeRed.copy(alpha = 0.5f) else DarkCardBorder
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("downloaded_item_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 16:9 Thumbnail
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceVariant)
                    .clickable { onPlay() }
            ) {
                if (!item.thumbnailUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = item.thumbnailUrl,
                        contentDescription = "Miniatura",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = JhTubeRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Color(0x99000000))
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Badges row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.isCompressed) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(JhTubeRed)
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "COMPRIMIDO",
                                color = PureWhite,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    if (item.isAudioOnly) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(DarkSurfaceVariant)
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "AUDIO",
                                color = TextHighEmphasis,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    Text(
                        text = item.resolutionLabel,
                        color = JhTubeRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.formattedSize,
                        color = TextMediumEmphasis,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.title,
                    color = TextHighEmphasis,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = item.author,
                    color = TextMediumEmphasis,
                    fontSize = 10.sp
                )
            }

            // Action Buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onCompress, modifier = Modifier.size(32.dp).testTag("compress_local_${item.id}")) {
                    Icon(
                        imageVector = Icons.Default.Compress,
                        contentDescription = "Comprimir Video",
                        tint = IconGray,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp).testTag("delete_local_${item.id}")) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Eliminar",
                        tint = IconGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
