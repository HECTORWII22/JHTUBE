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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.data.model.MediaItem
import com.example.ui.components.NetworkStatusHeader
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
fun ExploreScreen(
    catalogItems: List<MediaItem>,
    isLoading: Boolean,
    isLowDataMode: Boolean,
    searchQuery: String,
    selectedCategory: String,
    activeDownloadingId: String?,
    downloadProgress: Float = 0f,
    currentCatalogVersionTitle: String = "En Vivo (Servidor)",
    isOfflineCachedMode: Boolean = false,
    onToggleLowDataMode: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onRefresh: () -> Unit,
    onLoadPreviousVersion: () -> Unit = {},
    onLoadOfflineCatalog: () -> Unit = {},
    onOpenDownloads: () -> Unit = {},
    onOpenHistory: () -> Unit = {},
    onPlayMedia: (MediaItem, Boolean) -> Unit,
    onDownloadMedia: (MediaItem) -> Unit,
    onCompressMedia: (MediaItem) -> Unit,
    onAiSummarize: (MediaItem) -> Unit,
    onPlayDirectUrl: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val categories = listOf("Todo", "Noticias", "Música", "Tecnología", "Documentales", "Educación")
    var showDirectUrlDialog by remember { mutableStateOf(false) }
    var directUrlInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PureWhite)
            .padding(horizontal = 14.dp)
            .testTag("explore_screen")
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Network Mode Header
        NetworkStatusHeader(
            isLowDataMode = isLowDataMode,
            onToggleLowDataMode = onToggleLowDataMode
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Direct Link Action Button
        Surface(
            color = DarkSurfaceVariant,
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDirectUrlDialog = true }
                .testTag("open_direct_url_dialog_btn")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pegar Enlace Directo (Video / Audio)",
                        color = TextHighEmphasis,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(NeonCyan.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Abrir URL",
                        color = NeonCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (showDirectUrlDialog) {
            Dialog(onDismissRequest = { showDirectUrlDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DarkCardBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Reproducir Enlace Directo",
                                color = TextHighEmphasis,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { showDirectUrlDialog = false }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextMediumEmphasis)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Introduce cualquier URL directa de video o audio (MP4, MP3, HLS, etc.):",
                            color = TextMediumEmphasis,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = directUrlInput,
                            onValueChange = { directUrlInput = it },
                            placeholder = { Text("https://ejemplo.com/video.mp4", color = TextDisabled, fontSize = 12.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkSurfaceVariant,
                                unfocusedContainerColor = DarkSurfaceVariant,
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = DarkCardBorder,
                                focusedTextColor = TextHighEmphasis,
                                unfocusedTextColor = TextHighEmphasis
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("direct_url_input")
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                if (directUrlInput.isNotBlank()) {
                                    val url = directUrlInput.trim()
                                    showDirectUrlDialog = false
                                    directUrlInput = ""
                                    onPlayDirectUrl(url)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = JhTubeRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("play_direct_url_confirm_btn")
                        ) {
                            Icon(Icons.Default.PlayCircle, contentDescription = null, tint = PureWhite, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Reproducir Ahora", color = PureWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Search Bar with Clear Button
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = {
                Text(
                    text = "Buscar en Invidious Tiekoetter (WIIMMFI, videos)...",
                    color = TextMediumEmphasis,
                    fontSize = 12.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = JhTubeRed
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChanged("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar", tint = TextMediumEmphasis)
                    }
                } else {
                    IconButton(onClick = onRefresh, modifier = Modifier.testTag("explore_refresh_button")) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recargar", tint = TextMediumEmphasis)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkSurfaceVariant,
                unfocusedContainerColor = DarkSurfaceVariant,
                focusedBorderColor = JhTubeRed,
                unfocusedBorderColor = DarkCardBorder,
                focusedTextColor = TextHighEmphasis,
                unfocusedTextColor = TextHighEmphasis
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("explore_search_field")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category Pills
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            items(categories) { cat ->
                val isSelected = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) JhTubeRed else DarkSurfaceVariant)
                        .clickable { onCategorySelected(cat) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("category_chip_$cat")
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) PureWhite else TextHighEmphasis,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Version & Previous Content Quick Access Bar
        Surface(
            color = if (isOfflineCachedMode) NeonAmber.copy(alpha = 0.12f) else DarkSurfaceVariant,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isOfflineCachedMode) NeonAmber.copy(alpha = 0.5f) else DarkCardBorder
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isOfflineCachedMode) Icons.Default.CloudOff else Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = if (isOfflineCachedMode) NeonAmber else NeonEmerald,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = currentCatalogVersionTitle,
                            color = if (isOfflineCachedMode) NeonAmber else TextHighEmphasis,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "${catalogItems.size} videos listados",
                        color = TextMediumEmphasis,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Action buttons for previous versions and offline access
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = DarkCardBackground,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onLoadPreviousVersion() }
                            .testTag("load_previous_version_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 5.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Versión Previa",
                                color = TextHighEmphasis,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Surface(
                        color = DarkCardBackground,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onLoadOfflineCatalog() }
                            .testTag("load_offline_catalog_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 5.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = NeonAmber,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Catálogo 2G/3G",
                                color = TextHighEmphasis,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Surface(
                        color = DarkCardBackground,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenDownloads() }
                            .testTag("open_downloads_quick_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 5.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = NeonEmerald,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Descargas",
                                color = TextHighEmphasis,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Catalog Content
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = NeonCyan)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Conectando con Servidor Multimedia...",
                        color = TextMediumEmphasis,
                        fontSize = 12.sp
                    )
                }
            }
        } else if (catalogItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (searchQuery.isNotBlank()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .background(DarkSurfaceVariant, RoundedCornerShape(16.dp))
                            .padding(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = IconGray,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Buscando videos para \"$searchQuery\"...",
                            color = TextHighEmphasis,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Si la conexión es lenta (0.0.1 kbps), se cargarán automáticamente en breve.",
                            color = TextMediumEmphasis,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { onSearchQueryChanged("") },
                            colors = ButtonDefaults.buttonColors(containerColor = JhTubeRed),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Limpiar Búsqueda", fontSize = 12.sp, color = PureWhite)
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .background(DarkSurfaceVariant, RoundedCornerShape(16.dp))
                            .padding(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = NeonAmber,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No se pudo conectar con el servidor",
                            color = TextHighEmphasis,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Puedes acceder a las versiones guardadas en tu última sesión o al catálogo offline para no consumir datos.",
                            color = TextMediumEmphasis,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onLoadPreviousVersion() },
                                colors = ButtonDefaults.buttonColors(containerColor = JhTubeRed),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("🕒 Ver Sesión Anterior", fontSize = 12.sp)
                            }
                            Button(
                                onClick = { onLoadOfflineCatalog() },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkCardBackground),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("📦 Catálogo 2G", fontSize = 12.sp, color = TextHighEmphasis)
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier.testTag("catalog_list")
            ) {
                if (searchQuery.isBlank()) {
                    item(key = "shorts_shelf_carousel") {
                        ShortsShelfSection(
                            onPlayShort = { shortItem -> onPlayMedia(shortItem, false) },
                            onDownloadShort = { shortItem -> onDownloadMedia(shortItem) }
                        )
                    }
                }

                items(catalogItems, key = { it.id }) { item ->
                    MediaFeedCard(
                        item = item,
                        isDownloading = activeDownloadingId == item.id,
                        downloadProgress = if (activeDownloadingId == item.id) downloadProgress else 0f,
                        onPlayNormal = { onPlayMedia(item, false) },
                        onPlayAudioOnly = { onPlayMedia(item, true) },
                        onDownload = { onDownloadMedia(item) },
                        onCompress = { onCompressMedia(item) },
                        onSummarize = { onAiSummarize(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun MediaFeedCard(
    item: MediaItem,
    isDownloading: Boolean,
    downloadProgress: Float = 0f,
    onPlayNormal: () -> Unit,
    onPlayAudioOnly: () -> Unit,
    onDownload: () -> Unit,
    onCompress: () -> Unit,
    onSummarize: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = DarkCardBackground,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("media_card_${item.id}")
    ) {
        Column {
            // 16:9 Video Thumbnail (Miniatura)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                    .background(DarkSurfaceVariant)
                    .clickable { onPlayNormal() }
                    .testTag("thumbnail_card_${item.id}")
            ) {
                if (!item.thumbnailUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = item.thumbnailUrl,
                        contentDescription = "Miniatura de ${item.title}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFFE0E0E0), Color(0xFFF5F5F5))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = null,
                            tint = JhTubeRed,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                // Play Button Overlay on Thumbnail
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0x99000000))
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Reproducir",
                        tint = PureWhite,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Duration badge (bottom-right)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xCC000000))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.formattedDuration,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Category & Resolution badge (top-start)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xCC000000))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.category,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (item.isAudioOnly) Color(0xCC1B5E20) else Color(0xCC000000))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (item.isAudioOnly) "Audio 2G" else item.resolutionLabel,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Info & Metadata below thumbnail
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    // Author / Channel Initial Avatar
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(JhTubeRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.author.take(1).uppercase(),
                            color = PureWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            color = TextHighEmphasis,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${item.author} • ${item.formattedSize}",
                            color = TextMediumEmphasis,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Play actions
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Play Video Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(JhTubeRed)
                                .clickable { onPlayNormal() }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("play_video_button_${item.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Reproducir",
                                    tint = PureWhite,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Ver",
                                    color = PureWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Play Audio Only (2G / Data saver)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceVariant)
                                .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp))
                                .clickable { onPlayAudioOnly() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("play_audio_button_${item.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Headphones,
                                    contentDescription = "Solo Audio",
                                    tint = IconGray,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Solo Audio",
                                    color = TextHighEmphasis,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Tool actions (AI Summary, Compress, Download)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onSummarize,
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("summarize_item_button_${item.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Resumir con IA",
                                tint = NeonAmber,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onCompress,
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("compress_item_button_${item.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Compress,
                                contentDescription = "Comprimir Video",
                                tint = IconGray,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        if (isDownloading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = JhTubeRed,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${(downloadProgress * 100).toInt()}%",
                                    color = JhTubeRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            IconButton(
                                onClick = onDownload,
                                modifier = Modifier
                                    .size(34.dp)
                                    .testTag("download_item_button_${item.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Descargar",
                                    tint = JhTubeRed,
                                    modifier = Modifier.size(18.dp)
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
fun ShortsShelfSection(
    onPlayShort: (MediaItem) -> Unit,
    onDownloadShort: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val trendingShorts = remember {
        listOf(
            MediaItem(
                id = "short_wiimmfi_1",
                title = "Atajo Secreto Mario Kart Wiimmfi #Shorts",
                author = "MKWii Master",
                streamUrl = com.example.data.repository.MediaRepository.RELIABLE_VIDEO_MIRRORS[0],
                downloadUrl = com.example.data.repository.MediaRepository.RELIABLE_VIDEO_MIRRORS[0],
                fallbackUrl = com.example.data.repository.MediaRepository.RELIABLE_VIDEO_MIRRORS[0],
                durationSeconds = 45,
                resolutionLabel = "Vertical HD",
                thumbnailUrl = "https://invidious.tiekoetter.com/vi/6SfRqmXDa0A/hqdefault.jpg",
                category = "Shorts"
            ),
            MediaItem(
                id = "short_dolphin_2",
                title = "Dolphin 60FPS en Android en 30 Segundos",
                author = "EmuladorPro",
                streamUrl = com.example.data.repository.MediaRepository.RELIABLE_VIDEO_MIRRORS[1],
                downloadUrl = com.example.data.repository.MediaRepository.RELIABLE_VIDEO_MIRRORS[1],
                fallbackUrl = com.example.data.repository.MediaRepository.RELIABLE_VIDEO_MIRRORS[1],
                durationSeconds = 30,
                resolutionLabel = "Vertical HD",
                thumbnailUrl = "https://invidious.tiekoetter.com/vi/nX_POoe48BY/hqdefault.jpg",
                category = "Shorts"
            ),
            MediaItem(
                id = "short_revival_3",
                title = "Cómo revivir tu Nintendo Wii en 2026 #Shorts",
                author = "RetroCraft",
                streamUrl = com.example.data.repository.MediaRepository.RELIABLE_VIDEO_MIRRORS[2],
                downloadUrl = com.example.data.repository.MediaRepository.RELIABLE_VIDEO_MIRRORS[2],
                fallbackUrl = com.example.data.repository.MediaRepository.RELIABLE_VIDEO_MIRRORS[2],
                durationSeconds = 50,
                resolutionLabel = "Vertical HD",
                thumbnailUrl = "https://invidious.tiekoetter.com/vi/ZutIh8kCK1I/hqdefault.jpg",
                category = "Shorts"
            ),
            MediaItem(
                id = "short_speedrun_4",
                title = "Speedrun Épico de Super Mario Galaxy",
                author = "SpeedyRuns",
                streamUrl = com.example.data.repository.MediaRepository.RELIABLE_VIDEO_MIRRORS[3],
                downloadUrl = com.example.data.repository.MediaRepository.RELIABLE_VIDEO_MIRRORS[3],
                fallbackUrl = com.example.data.repository.MediaRepository.RELIABLE_VIDEO_MIRRORS[3],
                durationSeconds = 38,
                resolutionLabel = "Vertical HD",
                thumbnailUrl = "https://invidious.tiekoetter.com/vi/2j6KzWZveQ0/hqdefault.jpg",
                category = "Shorts"
            )
        )
    }

    Column(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = JhTubeRed,
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Shorts Destacados",
                    color = PureBlack,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Surface(
                color = JhTubeRed.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "⚡ Descarga Rápida",
                    color = JhTubeRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp),
            modifier = Modifier.fillMaxWidth().testTag("shorts_carousel_row")
        ) {
            items(trendingShorts, key = { it.id }) { shortItem ->
                Surface(
                    color = DarkCardBackground,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier
                        .width(135.dp)
                        .height(210.dp)
                        .clickable { onPlayShort(shortItem) }
                        .testTag("short_card_${shortItem.id}")
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = shortItem.thumbnailUrl,
                            contentDescription = shortItem.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Gradient overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Transparent,
                                            Color(0x66000000),
                                            Color(0xDD000000)
                                        )
                                    )
                                )
                        )
                        // Top Shorts Tag
                        Surface(
                            color = JhTubeRed,
                            shape = RoundedCornerShape(bottomEnd = 8.dp),
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Text(
                                text = "SHORTS",
                                color = PureWhite,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        // Play Icon Center
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x88000000))
                                .align(Alignment.Center),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Reproducir Short",
                                tint = PureWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Bottom Title and Fast Download Button
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = shortItem.title,
                                color = PureWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "${shortItem.durationSeconds}s",
                                    color = TextMediumEmphasis,
                                    fontSize = 10.sp
                                )
                                Surface(
                                    color = JhTubeRed,
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable { onDownloadShort(shortItem) }
                                        .testTag("download_short_${shortItem.id}")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Download,
                                            contentDescription = "Descarga rápida",
                                            tint = PureWhite,
                                            modifier = Modifier.size(14.dp)
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
