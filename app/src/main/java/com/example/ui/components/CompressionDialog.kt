package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.example.data.model.CompressionConfig
import com.example.data.model.MediaItem
import com.example.data.model.ResolutionPreset
import com.example.data.model.VideoCodec
import com.example.ui.theme.DarkCardBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.PureBlack
import com.example.ui.theme.TextDisabled
import com.example.ui.theme.TextHighEmphasis
import com.example.ui.theme.TextMediumEmphasis

@Composable
fun CompressionDialog(
    mediaItem: MediaItem,
    currentConfig: CompressionConfig,
    onConfigChanged: (CompressionConfig) -> Unit,
    onStartCompression: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedRes by remember { mutableStateOf(currentConfig.targetResolution) }
    var selectedCodec by remember { mutableStateOf(currentConfig.codec) }
    var bitrateKbps by remember { mutableStateOf(currentConfig.videoBitrateKbps) }
    var muteAudio by remember { mutableStateOf(currentConfig.muteAudio) }

    val duration = if (mediaItem.durationSeconds > 0) mediaItem.durationSeconds else 180L
    val originalBytes = if (mediaItem.fileSizeBytes > 0) mediaItem.fileSizeBytes else 35 * 1024 * 1024L

    val tempConfig = remember(selectedRes, selectedCodec, bitrateKbps, muteAudio) {
        CompressionConfig(
            targetResolution = selectedRes,
            codec = selectedCodec,
            videoBitrateKbps = bitrateKbps,
            muteAudio = muteAudio
        )
    }

    val estimatedBytes = tempConfig.calculateEstimatedSizeBytes(duration)
    val estimatedMb = estimatedBytes / (1024.0 * 1024.0)
    val originalMb = originalBytes / (1024.0 * 1024.0)
    val savingsPct = tempConfig.calculateSavingsPercentage(originalBytes, duration)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkCardBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("compression_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Compress,
                            contentDescription = null,
                            tint = NeonEmerald,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Compresión Local de Video",
                            color = TextHighEmphasis,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextMediumEmphasis)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = mediaItem.title,
                    color = TextHighEmphasis,
                    fontSize = 13.sp,
                    maxLines = 1,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Tamaño actual: ${mediaItem.formattedSize} • Duración: ${mediaItem.formattedDuration}",
                    color = TextMediumEmphasis,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Estimated Savings Banner Card
                Surface(
                    color = DarkSurfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Ahorro de espacio estimado",
                                color = TextMediumEmphasis,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "~$savingsPct% de memoria guardada",
                                color = NeonEmerald,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "De ${String.format("%.1f", originalMb)} MB ➔ ~${String.format("%.1f", estimatedMb)} MB",
                                color = TextHighEmphasis,
                                fontSize = 11.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonEmerald.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "-$savingsPct%",
                                color = NeonEmerald,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Resolution Preset Selection
                Text(
                    text = "Resolución Destino:",
                    color = TextHighEmphasis,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ResolutionPreset.entries.forEach { res ->
                        val isSelected = selectedRes == res
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) NeonCyan.copy(alpha = 0.12f) else DarkSurfaceVariant)
                                .border(
                                    1.dp,
                                    if (isSelected) NeonCyan else DarkCardBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    selectedRes = res
                                    bitrateKbps = res.suggestedBitrateKbps
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = res.label,
                                    color = if (isSelected) NeonCyan else TextHighEmphasis,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = "${res.width}x${res.height}",
                                    color = TextMediumEmphasis,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Codec Selection: H.264 vs H.265
                Text(
                    text = "Códec de Compresión:",
                    color = TextHighEmphasis,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VideoCodec.entries.forEach { codec ->
                        val isSelected = selectedCodec == codec
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) NeonEmerald.copy(alpha = 0.15f) else DarkSurfaceVariant)
                                .border(
                                    1.dp,
                                    if (isSelected) NeonEmerald else DarkCardBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedCodec = codec }
                                .padding(vertical = 8.dp, horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = codec.displayName,
                                color = if (isSelected) NeonEmerald else TextHighEmphasis,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Video Bitrate Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Tasa de Bits (Bitrate):", color = TextHighEmphasis, fontSize = 12.sp)
                    Text(text = "$bitrateKbps kbps", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = bitrateKbps.toFloat(),
                    onValueChange = { bitrateKbps = it.toInt() },
                    valueRange = 250f..3000f,
                    steps = 11,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonCyan,
                        inactiveTrackColor = TextDisabled
                    ),
                    modifier = Modifier.testTag("compression_bitrate_slider")
                )

                // Mute Audio Option
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { muteAudio = !muteAudio }
                ) {
                    Checkbox(
                        checked = muteAudio,
                        onCheckedChange = { muteAudio = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = NeonCyan,
                            checkmarkColor = PureBlack
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Eliminar pista de audio (ahorro extra para clips mudos)",
                        color = TextHighEmphasis,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Start Button
                Button(
                    onClick = {
                        onConfigChanged(tempConfig)
                        onStartCompression()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("start_compression_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        tint = PureBlack,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Comprimir y Guardar en Memoria",
                        color = PureBlack,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
