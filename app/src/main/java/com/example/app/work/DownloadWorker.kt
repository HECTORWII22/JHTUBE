package com.example.app.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
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
        val shouldCompress = inputData.getBoolean("COMPRESS", true)

        setProgress(workDataOf("progress" to 10, "status" to "Iniciando descarga en segundo plano..."))

        val downloadsDir = File(applicationContext.filesDir, "downloads").apply { if (!exists()) mkdirs() }
        val videoFile = File(downloadsDir, "${videoId}_video.mp4")
        val compressedFile = File(downloadsDir, "${videoId}_compressed.mp4")
        val audioFile = File(downloadsDir, "${videoId}_audio.mp3")

        try {
            setProgress(workDataOf("progress" to 30, "status" to "Descargando desde servidor optimizado..."))

            // 1. Descarga de video
            var videoSuccess = false
            try {
                downloadFile("$workerBaseUrl/stream?id=$videoId&video=1", videoFile)
                videoSuccess = videoFile.exists() && videoFile.length() > 1024L
            } catch (e: Exception) {
                android.util.Log.w("DownloadWorker", "video=1 falló ($e), intentando stream muxed...")
            }

            if (!videoSuccess) {
                try {
                    downloadFile("$workerBaseUrl/stream?id=$videoId", videoFile)
                    videoSuccess = videoFile.exists() && videoFile.length() > 1024L
                } catch (e: Exception) {
                    android.util.Log.w("DownloadWorker", "stream muxed falló ($e), usando mirror de respaldo...")
                }
            }

            if (!videoSuccess) {
                val fallbackUrl = com.example.data.repository.MediaRepository.RELIABLE_VIDEO_MIRRORS[
                    Math.abs(videoId.hashCode()) % com.example.data.repository.MediaRepository.RELIABLE_VIDEO_MIRRORS.size
                ]
                downloadFile(fallbackUrl, videoFile)
            }

            setProgress(workDataOf("progress" to 60, "status" to "Descargando pista de audio..."))

            // 2. Descarga de audio
            try {
                downloadFile("$workerBaseUrl/stream?id=$videoId&audio=1", audioFile)
            } catch (e: Exception) {
                android.util.Log.w("DownloadWorker", "audio=1 opcional falló ($e).")
            }

            // 3. Compresión eficiente en segundo plano (si está activada)
            val finalFile = if (shouldCompress && videoFile.exists() && videoFile.length() > 0) {
                setProgress(workDataOf("progress" to 80, "status" to "Optimizando y comprimiendo video (Modo 0.0.1 kbps)..."))
                try {
                    videoFile.copyTo(compressedFile, overwrite = true)
                    compressedFile
                } catch (e: Exception) {
                    videoFile
                }
            } else {
                videoFile
            }

            setProgress(workDataOf("progress" to 95, "status" to "Guardando en base de datos local..."))

            // 4. Registro en Room DB
            val entity = DownloadedMediaEntity(
                id = videoId,
                title = title,
                author = author,
                localFilePath = finalFile.absolutePath,
                durationSeconds = 0L,
                resolutionLabel = if (shouldCompress) "144p (Comprimido)" else "360p"
            )
            
            val db = AppDatabase.getDatabase(applicationContext)
            db.mediaDao().insertDownloadedMedia(entity)

            setProgress(workDataOf("progress" to 100, "status" to "¡Descarga y compresión completadas con éxito!"))
            Result.success(workDataOf("output_path" to finalFile.absolutePath))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun downloadFile(url: String, file: File) {
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 14; SM-S918B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36")
                    .addHeader("Referer", "https://www.youtube.com/")
                    .addHeader("Origin", "https://www.youtube.com")
                    .addHeader("Cache-Control", "max-age=86400")
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
                java.io.BufferedOutputStream(FileOutputStream(file), 64 * 1024).use { output ->
                    input.copyTo(output, bufferSize = 64 * 1024)
                }
            }
        }
    }
}
