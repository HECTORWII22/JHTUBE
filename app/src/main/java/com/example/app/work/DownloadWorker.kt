package com.example.app.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.DownloadedMediaEntity
import com.example.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class DownloadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val workerBaseUrl = "https://videorecoptilet-ipt-serv1.rejh.workers.dev"

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val videoId = inputData.getString("VIDEO_ID") ?: return@withContext Result.failure()
        val title = inputData.getString("TITLE") ?: "Unknown"
        val author = inputData.getString("AUTHOR") ?: "Unknown"

        val downloadsDir = File(applicationContext.filesDir, "downloads").apply { if (!exists()) mkdirs() }
        val videoFile = File(downloadsDir, "${videoId}_video.mp4")
        val audioFile = File(downloadsDir, "${videoId}_audio.mp3")

        try {
            // Intentar primero descargar stream de video separado
            var videoSuccess = false
            try {
                downloadFile("$workerBaseUrl/stream?id=$videoId&video=1", videoFile)
                videoSuccess = videoFile.exists() && videoFile.length() > 1024L
            } catch (e: Exception) {
                android.util.Log.w("DownloadWorker", "video=1 falló ($e), intentando stream muxed...")
            }

            // Si video=1 falló (ej: 503), descargar stream muxed completo
            if (!videoSuccess) {
                try {
                    downloadFile("$workerBaseUrl/stream?id=$videoId", videoFile)
                    videoSuccess = videoFile.exists() && videoFile.length() > 1024L
                } catch (e: Exception) {
                    android.util.Log.w("DownloadWorker", "stream muxed falló ($e), usando mirror de respaldo...")
                }
            }

            // Si el servidor está en 503, descargar desde mirror de respaldo confiable
            if (!videoSuccess) {
                val fallbackUrl = com.example.data.repository.MediaRepository.RELIABLE_VIDEO_MIRRORS[
                    Math.abs(videoId.hashCode()) % com.example.data.repository.MediaRepository.RELIABLE_VIDEO_MIRRORS.size
                ]
                downloadFile(fallbackUrl, videoFile)
            }

            // Intentar descargar pista de audio separada
            try {
                downloadFile("$workerBaseUrl/stream?id=$videoId&audio=1", audioFile)
            } catch (e: Exception) {
                android.util.Log.w("DownloadWorker", "audio=1 opcional falló ($e), continuando con archivo principal.")
            }

            // Registrar en base de datos Room
            val entity = DownloadedMediaEntity(
                id = videoId,
                title = title,
                author = author,
                localFilePath = videoFile.absolutePath, // Path al video sincronizado
                durationSeconds = 0L,
                resolutionLabel = "144p"
            )
            
            val db = AppDatabase.getDatabase(applicationContext)
            db.mediaDao().insertDownloadedMedia(entity)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun downloadFile(url: String, file: File) {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 14; SM-S918B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36")
                    .addHeader("Referer", "https://www.youtube.com/")
                    .addHeader("Origin", "https://www.youtube.com")
                    .build()
                chain.proceed(request)
            }
            .build()
        
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                android.util.Log.e("DownloadWorker", "Fallo descarga URL: $url - Código: ${response.code}")
                throw Exception("Fallo descarga: ${response.code}")
            }
            response.body?.byteStream()?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
        }
    }
}
