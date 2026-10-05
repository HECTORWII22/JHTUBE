package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Serv1Item(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String? = null,
    @Json(name = "author") val author: String? = null
)

@JsonClass(generateAdapter = true)
data class VideoStreamResponse(
    @Json(name = "id") val id: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "author") val author: String? = null,
    @Json(name = "mediaUrl") val mediaUrl: String? = null,
    @Json(name = "streamUrl") val streamUrl: String? = null,
    @Json(name = "url") val url: String? = null,
    @Json(name = "muxed") val muxed: Boolean = true,
    @Json(name = "kind") val kind: String? = null,
    @Json(name = "quality") val quality: String? = null,
    @Json(name = "error") val error: String? = null
)

data class MediaItem(
    val id: String,
    val title: String,
    val author: String,
    val streamUrl: String,
    val downloadUrl: String,
    val durationSeconds: Long = 0L,
    val resolutionLabel: String = "480p",
    val fileSizeBytes: Long = 0L,
    val isAudioOnly: Boolean = false,
    val isDownloaded: Boolean = false,
    val isCompressed: Boolean = false,
    val localFilePath: String? = null,
    val originalSizeBytes: Long = 0L,
    val thumbnailUrl: String? = null,
    val category: String = "General",
    val fallbackUrl: String? = null
) {
    val formattedSize: String
        get() {
            if (fileSizeBytes <= 0) return "En línea"
            val mb = fileSizeBytes / (1024.0 * 1024.0)
            return if (mb >= 1.0) String.format("%.1f MB", mb) else String.format("%.0f KB", fileSizeBytes / 1024.0)
        }

    val formattedDuration: String
        get() {
            if (durationSeconds <= 0) return "En vivo / Streaming"
            val mins = durationSeconds / 60
            val secs = durationSeconds % 60
            return String.format("%02d:%02d", mins, secs)
        }
}
