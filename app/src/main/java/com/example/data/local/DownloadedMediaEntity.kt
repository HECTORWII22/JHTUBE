package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_media")
data class DownloadedMediaEntity(
    @PrimaryKey val id: String,
    val title: String,
    val author: String,
    val localFilePath: String,
    val durationSeconds: Long = 0L,
    val resolutionLabel: String = "480p",
    val fileSizeBytes: Long = 0L,
    val originalSizeBytes: Long = 0L,
    val isCompressed: Boolean = false,
    val isAudioOnly: Boolean = false,
    val codec: String = "H.264",
    val downloadedAtEpochMs: Long = System.currentTimeMillis()
)
