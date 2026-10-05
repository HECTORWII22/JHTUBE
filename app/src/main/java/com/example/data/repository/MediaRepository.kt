package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.DownloadedMediaEntity
import com.example.data.local.HistoryEntity
import com.example.data.model.AddHistoryRequest
import com.example.data.model.HistoryItemRemote
import com.example.data.model.LoginRequest
import com.example.data.model.MediaItem
import com.example.data.model.Serv1Item
import com.example.data.model.UserSession
import com.example.data.remote.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

class MediaRepository(private val context: Context) {
    companion object {
        private const val TAG = "MediaRepository"

        val RELIABLE_VIDEO_MIRRORS = listOf(
            "https://vjs.zencdn.net/v/oceans.mp4",
            "https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4",
            "https://interactive-examples.mdn.mozilla.net/media/cc0-videos/friday.mp4",
            "https://filesamples.com/samples/video/mp4/sample_960x400_ocean_with_audio.mp4",
            "https://filesamples.com/samples/video/mp4/sample_640x360.mp4",
            "https://filesamples.com/samples/video/mp4/sample_1280x720_surfing_with_audio.mp4",
            "https://www.w3schools.com/html/mov_bbb.mp4",
            "https://www.w3schools.com/html/movie.mp4"
        )

        val RELIABLE_AUDIO_MIRRORS = listOf(
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"
        )

        val SAMPLE_THUMBNAILS = listOf(
            "https://img.youtube.com/vi/aqz-KE-bpKQ/mqdefault.jpg",
            "https://img.youtube.com/vi/TLkA0RELQ1g/mqdefault.jpg",
            "https://img.youtube.com/vi/YE7VzlLtp-4/mqdefault.jpg",
            "https://img.youtube.com/vi/uM_hot2rTdY/mqdefault.jpg"
        )
    }

    private val db = AppDatabase.getDatabase(context)
    private val mediaDao = db.mediaDao()
    private val serv1Api = NetworkClient.serv1Api
    private val serv0Api = NetworkClient.serv0Api

    // Curated high-efficiency offline/backup catalog for 2G/3G testing
    private val sampleBackupCatalog = listOf(
        MediaItem(
            id = "sample_nature_eco",
            title = "Oasis Silvestre: Vida y Naturaleza HD",
            author = "EcoDocumentales",
            streamUrl = RELIABLE_VIDEO_MIRRORS[0],
            downloadUrl = RELIABLE_VIDEO_MIRRORS[0],
            fallbackUrl = RELIABLE_VIDEO_MIRRORS[0],
            durationSeconds = 596,
            resolutionLabel = "720p",
            fileSizeBytes = 42 * 1024 * 1024L,
            thumbnailUrl = "https://img.youtube.com/vi/aqz-KE-bpKQ/mqdefault.jpg",
            category = "Documentales"
        ),
        MediaItem(
            id = "sample_tech_future",
            title = "Avances en Conectividad Rural y 3G",
            author = "Telecomunicaciones Abiertas",
            streamUrl = RELIABLE_VIDEO_MIRRORS[1],
            downloadUrl = RELIABLE_VIDEO_MIRRORS[1],
            fallbackUrl = RELIABLE_VIDEO_MIRRORS[1],
            durationSeconds = 653,
            resolutionLabel = "480p",
            fileSizeBytes = 28 * 1024 * 1024L,
            thumbnailUrl = "https://img.youtube.com/vi/TLkA0RELQ1g/mqdefault.jpg",
            category = "Tecnología"
        ),
        MediaItem(
            id = "sample_audio_stream",
            title = "Transmisión Informativa Continua (Audio 2G)",
            author = "Radio Noticias Satelital",
            streamUrl = RELIABLE_AUDIO_MIRRORS[0],
            downloadUrl = RELIABLE_AUDIO_MIRRORS[0],
            fallbackUrl = RELIABLE_AUDIO_MIRRORS[0],
            durationSeconds = 372,
            resolutionLabel = "Audio 48k",
            fileSizeBytes = 5 * 1024 * 1024L,
            thumbnailUrl = "https://img.youtube.com/vi/uM_hot2rTdY/mqdefault.jpg",
            isAudioOnly = true,
            category = "Noticias"
        ),
        MediaItem(
            id = "sample_edu_solar",
            title = "Energías Limpias para Comunidades Aisladas",
            author = "Educación Para Todos",
            streamUrl = RELIABLE_VIDEO_MIRRORS[2],
            downloadUrl = RELIABLE_VIDEO_MIRRORS[2],
            fallbackUrl = RELIABLE_VIDEO_MIRRORS[2],
            durationSeconds = 15,
            resolutionLabel = "360p",
            fileSizeBytes = 8 * 1024 * 1024L,
            thumbnailUrl = "https://img.youtube.com/vi/YE7VzlLtp-4/mqdefault.jpg",
            category = "Educación"
        )
    )

    // In-memory & Persistent Snapshot of previously loaded video versions
    private var lastSuccessfulFeedSnapshot: List<MediaItem> = emptyList()

    suspend fun getTrendingMedia(): List<MediaItem> = withContext(Dispatchers.IO) {
        try {
            val remoteItems = serv1Api.getTrending()
            if (remoteItems.isNotEmpty()) {
                val mapped = remoteItems.mapIndexed { index, item -> mapServ1ToMediaItem(item, index) }
                lastSuccessfulFeedSnapshot = mapped
                mapped
            } else {
                if (lastSuccessfulFeedSnapshot.isNotEmpty()) lastSuccessfulFeedSnapshot else sampleBackupCatalog
            }
        } catch (e: Exception) {
            Log.w(TAG, "serv1 trending failed, using previous saved version: ${e.message}")
            if (lastSuccessfulFeedSnapshot.isNotEmpty()) lastSuccessfulFeedSnapshot else sampleBackupCatalog
        }
    }

    fun getPreviousSavedVersion(): List<MediaItem> {
        return if (lastSuccessfulFeedSnapshot.isNotEmpty()) lastSuccessfulFeedSnapshot else sampleBackupCatalog
    }

    fun getOfflineEmergencyCatalog(): List<MediaItem> {
        return sampleBackupCatalog
    }

    suspend fun searchMedia(query: String): List<MediaItem> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext getTrendingMedia()
        try {
            val remoteResults = serv1Api.search(query)
            if (remoteResults.isNotEmpty()) {
                remoteResults.mapIndexed { index, item -> mapServ1ToMediaItem(item, index) }
            } else {
                sampleBackupCatalog.filter {
                    it.title.contains(query, ignoreCase = true) ||
                            it.author.contains(query, ignoreCase = true)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "serv1 search failed, filtering local catalog: ${e.message}")
            sampleBackupCatalog.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.author.contains(query, ignoreCase = true)
            }
        }
    }

    private fun mapServ1ToMediaItem(item: Serv1Item, index: Int = 0): MediaItem {
        val streamUrl = "https://videorecoptilet-ipt-serv1.rejh.workers.dev/video?id=${item.id}"
        val downloadUrl = "https://videorecoptilet-ipt-serv1.rejh.workers.dev/stream?id=${item.id}"

        val thumbUrl = if (item.id.isNotBlank() && !item.id.startsWith("sample_")) {
            "https://img.youtube.com/vi/${item.id}/mqdefault.jpg"
        } else {
            SAMPLE_THUMBNAILS[Math.abs(item.id.hashCode()) % SAMPLE_THUMBNAILS.size]
        }

        return MediaItem(
            id = item.id,
            title = item.title ?: "Medio (${item.id})",
            author = item.author ?: "JHTUBE Creador",
            streamUrl = streamUrl,
            downloadUrl = downloadUrl,
            fallbackUrl = MediaRepository.RELIABLE_VIDEO_MIRRORS[Math.abs(item.id.hashCode()) % MediaRepository.RELIABLE_VIDEO_MIRRORS.size],
            thumbnailUrl = thumbUrl,
            durationSeconds = 240L,
            resolutionLabel = "144p Ultra-Ahorro",
            fileSizeBytes = 12 * 1024 * 1024L,
            category = "General"
        )
    }

    // Local downloads management
    fun getDownloadedMediaFlow(): Flow<List<MediaItem>> {
        return mediaDao.getAllDownloadedMedia().map { list ->
            list.map { entity ->
                val thumbUrl = if (entity.id.isNotBlank() && !entity.id.startsWith("sample_")) {
                    "https://img.youtube.com/vi/${entity.id}/mqdefault.jpg"
                } else {
                    SAMPLE_THUMBNAILS[Math.abs(entity.id.hashCode()) % SAMPLE_THUMBNAILS.size]
                }
                MediaItem(
                    id = entity.id,
                    title = entity.title,
                    author = entity.author,
                    streamUrl = entity.localFilePath,
                    downloadUrl = entity.localFilePath,
                    fallbackUrl = entity.localFilePath,
                    thumbnailUrl = thumbUrl,
                    durationSeconds = entity.durationSeconds,
                    resolutionLabel = entity.resolutionLabel,
                    fileSizeBytes = entity.fileSizeBytes,
                    originalSizeBytes = entity.originalSizeBytes,
                    isDownloaded = true,
                    isCompressed = entity.isCompressed,
                    localFilePath = entity.localFilePath,
                    isAudioOnly = entity.isAudioOnly
                )
            }
        }
    }

    suspend fun downloadMediaFile(
        item: MediaItem,
        onProgress: (Float, Long) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        val downloadsDir = File(context.filesDir, "downloads").apply { if (!exists()) mkdirs() }
        val cleanId = item.id.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val extension = if (item.isAudioOnly) "mp3" else "mp4"
        val targetFile = File(downloadsDir, "${cleanId}_${System.currentTimeMillis()}.$extension")

        val targetUrl = if (item.id.startsWith("sample_") || item.id.startsWith("direct_")) {
            item.downloadUrl.takeIf { it.isNotBlank() && it.startsWith("http") }
                ?: (item.fallbackUrl ?: RELIABLE_VIDEO_MIRRORS[0])
        } else {
            // Use the comprehensive /stream?id=... endpoint which correctly handles video muxing
            "https://videorecoptilet-ipt-serv1.rejh.workers.dev/stream?id=${item.id}"
        }

        val downloadResult = downloadFromUrl(targetUrl, targetFile, onProgress)
        if (downloadResult.isFailure) {
            val fallbackUrl = item.fallbackUrl ?: RELIABLE_VIDEO_MIRRORS[0]
            val fallbackResult = downloadFromUrl(fallbackUrl, targetFile, onProgress)
            if (fallbackResult.isFailure) {
                return@withContext fallbackResult
            }
        }

        // Register in local Room DB
        val entity = DownloadedMediaEntity(
            id = item.id,
            title = item.title,
            author = item.author,
            localFilePath = targetFile.absolutePath,
            durationSeconds = item.durationSeconds,
            resolutionLabel = item.resolutionLabel,
            fileSizeBytes = targetFile.length(),
            originalSizeBytes = targetFile.length(),
            isCompressed = false,
            isAudioOnly = item.isAudioOnly
        )
        mediaDao.insertDownloadedMedia(entity)

        Result.success(targetFile)
    }

    private suspend fun downloadFromUrl(
        url: String,
        targetFile: File,
        onProgress: (Float, Long) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val okHttpClient = okhttp3.OkHttpClient.Builder()
                .connectTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .protocols(listOf(okhttp3.Protocol.HTTP_1_1))
                .build()

            val request = okhttp3.Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36")
                .addHeader("Referer", "https://www.youtube.com/")
                .addHeader("Origin", "https://www.youtube.com")
                .build()
            val response = okHttpClient.newCall(request).execute()
            val body = response.body ?: return@withContext Result.failure(IllegalStateException("Respuesta vacía"))

            val totalBytes = body.contentLength()
            var downloadedBytes = 0L

            body.byteStream().use { input ->
                FileOutputStream(targetFile).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        downloadedBytes += read
                        val progress = if (totalBytes > 0) downloadedBytes.toFloat() / totalBytes else 0.5f
                        onProgress(progress, downloadedBytes)
                    }
                    output.flush()
                }
            }

            if (targetFile.exists() && targetFile.length() > 2048L) {
                Result.success(targetFile)
            } else {
                targetFile.delete()
                Result.failure(IllegalStateException("Archivo descargado incompleto"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in downloadFromUrl: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun ensureLocalMediaFile(item: MediaItem): File = withContext(Dispatchers.IO) {
        // Check if already on disk and non-empty
        if (item.localFilePath != null) {
            val localFile = File(item.localFilePath)
            if (localFile.exists() && localFile.length() > 4096L) {
                return@withContext localFile
            }
        }

        val cacheDir = File(context.cacheDir, "media_cache").apply { if (!exists()) mkdirs() }
        val cleanId = item.id.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val cacheFile = File(cacheDir, "source_${cleanId}.mp4")

        if (cacheFile.exists() && cacheFile.length() > 4096L) {
            return@withContext cacheFile
        }

        // Download to cache using reliable mirror
        val mirrorUrl = item.fallbackUrl
            ?: if (item.isAudioOnly) RELIABLE_AUDIO_MIRRORS[0] else RELIABLE_VIDEO_MIRRORS[0]

        val result = downloadFromUrl(mirrorUrl, cacheFile) { _, _ -> }
        if (result.isSuccess && cacheFile.exists() && cacheFile.length() > 1024L) {
            return@withContext cacheFile
        }

        cacheFile
    }

    suspend fun saveCompressedMediaItem(
        originalItem: MediaItem,
        compressedFile: File,
        resolutionLabel: String,
        codec: String
    ) = withContext(Dispatchers.IO) {
        val newId = "comp_${originalItem.id}_${System.currentTimeMillis()}"
        val entity = DownloadedMediaEntity(
            id = newId,
            title = "${originalItem.title} [Comprimido]",
            author = originalItem.author,
            localFilePath = compressedFile.absolutePath,
            durationSeconds = originalItem.durationSeconds,
            resolutionLabel = resolutionLabel,
            fileSizeBytes = compressedFile.length(),
            originalSizeBytes = originalItem.fileSizeBytes.takeIf { it > 0 } ?: (compressedFile.length() * 2),
            isCompressed = true,
            isAudioOnly = originalItem.isAudioOnly,
            codec = codec
        )
        mediaDao.insertDownloadedMedia(entity)
    }

    suspend fun deleteDownloadedMedia(id: String) = withContext(Dispatchers.IO) {
        val entity = mediaDao.getDownloadedMediaById(id)
        if (entity != null) {
            val file = File(entity.localFilePath)
            if (file.exists()) {
                file.delete()
            }
            mediaDao.deleteDownloadedMediaById(id)
        }
    }

    // Playback History with Serv0 worker sync
    fun getHistoryFlow(): Flow<List<HistoryEntity>> = mediaDao.getAllHistory()

    suspend fun recordHistory(
        item: MediaItem,
        positionMs: Long,
        durationMs: Long,
        token: String?
    ) = withContext(Dispatchers.IO) {
        val historyId = "hist_${item.id}"
        val entity = HistoryEntity(
            id = historyId,
            mediaId = item.id,
            title = item.title,
            author = item.author,
            playbackPositionMs = positionMs,
            durationMs = durationMs,
            watchedAtEpochMs = System.currentTimeMillis(),
            isSyncedWithServer = false
        )
        mediaDao.insertOrUpdateHistory(entity)

        // Try syncing to Serv0 in background
        if (token != null) {
            try {
                val response = serv0Api.addHistory(
                    AddHistoryRequest(
                        token = token,
                        id = item.id,
                        title = item.title,
                        author = item.author,
                        positionMs = positionMs
                    )
                )
                if (response.isSuccessful) {
                    mediaDao.insertOrUpdateHistory(entity.copy(isSyncedWithServer = true))
                }
            } catch (e: Exception) {
                Log.d(TAG, "serv0 sync deferred: ${e.message}")
            }
        }
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        mediaDao.clearAllHistory()
    }

    suspend fun loginServ0(username: String, pass: String): UserSession = withContext(Dispatchers.IO) {
        try {
            val response = serv0Api.login(LoginRequest(user = username, pass = pass))
            if (response.isSuccessful && response.body()?.ok == true) {
                UserSession(
                    username = username,
                    token = response.body()?.token ?: UUID.randomUUID().toString(),
                    isLoggedIn = true,
                    isOfflineMode = false
                )
            } else {
                // If serv0 returns no_kv or error, enable local offline session
                UserSession(
                    username = username.ifBlank { "Usuario Local" },
                    token = "local_token_${System.currentTimeMillis()}",
                    isLoggedIn = true,
                    isOfflineMode = true
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "serv0 login fallback to offline session: ${e.message}")
            UserSession(
                username = username.ifBlank { "Usuario Offline" },
                token = "offline_token",
                isLoggedIn = true,
                isOfflineMode = true
            )
        }
    }
}
