package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Query("SELECT * FROM downloaded_media ORDER BY downloadedAtEpochMs DESC")
    fun getAllDownloadedMedia(): Flow<List<DownloadedMediaEntity>>

    @Query("SELECT * FROM downloaded_media WHERE id = :id LIMIT 1")
    suspend fun getDownloadedMediaById(id: String): DownloadedMediaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownloadedMedia(media: DownloadedMediaEntity)

    @Update
    suspend fun updateDownloadedMedia(media: DownloadedMediaEntity)

    @Query("DELETE FROM downloaded_media WHERE id = :id")
    suspend fun deleteDownloadedMediaById(id: String)

    @Query("SELECT * FROM playback_history ORDER BY watchedAtEpochMs DESC")
    fun getAllHistory(): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateHistory(history: HistoryEntity)

    @Query("DELETE FROM playback_history WHERE id = :id")
    suspend fun deleteHistoryById(id: String)

    @androidx.room.Query("SELECT * FROM playlists ORDER BY createdAtEpochMs DESC")
    fun getAllPlaylistsFlow(): kotlinx.coroutines.flow.Flow<List<PlaylistEntity>>

    @androidx.room.Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    @androidx.room.Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylistById(id: String)

    @androidx.room.Query("DELETE FROM playback_history")
    suspend fun clearAllHistory()
}
