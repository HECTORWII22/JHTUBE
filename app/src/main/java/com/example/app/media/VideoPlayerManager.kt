package com.example.app.media

import android.content.Context
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource

class VideoPlayerManager(private val context: Context) {

    private var player: ExoPlayer? = null
    private val workerBaseUrl = "https://videorecoptilet-ipt-serv1.rejh.workers.dev"
    private val userAgent = "Mozilla/5.0 (Linux; Android 14; SM-S918B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"

    // Helper para crear cliente con headers de navegador
    private fun createHttpClientBuilder(): okhttp3.OkHttpClient.Builder {
        return okhttp3.OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("User-Agent", userAgent)
                    .addHeader("Referer", "https://www.youtube.com/")
                    .addHeader("Origin", "https://www.youtube.com")
                    .build()
                chain.proceed(request)
            }
    }

    // 2. Reproducir Vídeo usando MergingMediaSource para sincronizar audio y video
    fun playVideo(videoId: String) {
        val videoUrl = "$workerBaseUrl/stream?id=$videoId&video=1"
        val audioUrl = "$workerBaseUrl/stream?id=$videoId&audio=1"
        
        // Configurar la fábrica HTTP para soportar las peticiones por Rangos del Worker
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Linux; Android 13; Mobile)")
            .setAllowCrossProtocolRedirects(true)

        val videoSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(Uri.parse(videoUrl)))

        val audioSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(Uri.parse(audioUrl)))

        // MergingMediaSource combina audio y video y los sincroniza
        val mergedSource = androidx.media3.exoplayer.source.MergingMediaSource(true, videoSource, audioSource)

        player?.apply {
            setMediaSource(mergedSource)
            prepare()
            playWhenReady = true
        }
    }

    // 3. Reproducir Solo Audio (Modo Fondo)
    fun playAudioOnly(videoId: String) {
        val audioUrl = "$workerBaseUrl/stream?id=$videoId&audio=1"
        
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Linux; Android 13; Mobile)")

        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(Uri.parse(audioUrl)))

        player?.apply {
            setMediaSource(mediaSource)
            prepare()
            playWhenReady = true
        }
    }

    // 4. Liberar recursos
    fun releasePlayer() {
        player?.release()
        player = null
    }
}
