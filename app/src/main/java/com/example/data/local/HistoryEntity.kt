package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playback_history")
data class HistoryEntity(
    @PrimaryKey val id: String,
    val mediaId: String,
    val title: String,
    val author: String,
    val playbackPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val watchedAtEpochMs: Long = System.currentTimeMillis(),
    val isSyncedWithServer: Boolean = false
)
