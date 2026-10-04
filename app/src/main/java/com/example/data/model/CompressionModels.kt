package com.example.data.model

enum class VideoCodec(val displayName: String, val mimeType: String, val efficiencyMultiplier: Float) {
    H264("H.264 (AVC Universal)", "video/avc", 1.0f),
    H265("H.265 (HEVC Ultra Ahorro)", "video/hevc", 0.65f)
}

enum class ResolutionPreset(val label: String, val width: Int, val height: Int, val suggestedBitrateKbps: Int) {
    RES_240P("240p (Ultra Ligero 2G)", 426, 240, 300),
    RES_360P("360p (Ahorro Red 3G)", 640, 360, 600),
    RES_480P("480p (Estándar Móvil)", 854, 480, 1100),
    RES_720P("720p (HD Balanceado)", 1280, 720, 2200),
    RES_1080P("1080p (Full HD)", 1920, 1080, 4000)
}

data class CompressionConfig(
    val targetResolution: ResolutionPreset = ResolutionPreset.RES_360P,
    val codec: VideoCodec = VideoCodec.H264,
    val videoBitrateKbps: Int = 600,
    val audioBitrateKbps: Int = 64,
    val muteAudio: Boolean = false,
    val fps: Int = 24
) {
    fun calculateEstimatedSizeBytes(durationSeconds: Long): Long {
        if (durationSeconds <= 0) return 0L
        val effectiveBitrate = (videoBitrateKbps * codec.efficiencyMultiplier + if (muteAudio) 0 else audioBitrateKbps) * 1000
        val totalBits = effectiveBitrate * durationSeconds
        return (totalBits / 8).toLong()
    }

    fun calculateSavingsPercentage(originalSizeBytes: Long, durationSeconds: Long): Int {
        if (originalSizeBytes <= 0L || durationSeconds <= 0L) return 50
        val estimated = calculateEstimatedSizeBytes(durationSeconds)
        val saved = originalSizeBytes - estimated
        val pct = ((saved.toDouble() / originalSizeBytes.toDouble()) * 100).toInt()
        return pct.coerceIn(0, 95)
    }
}

enum class CompressionStatus {
    IDLE,
    PREPARING,
    TRANSCODING,
    COMPLETED,
    FAILED,
    CANCELLED
}

data class CompressionProgress(
    val mediaId: String = "",
    val status: CompressionStatus = CompressionStatus.IDLE,
    val progress: Float = 0f,
    val statusMessage: String = "",
    val originalSizeBytes: Long = 0L,
    val outputSizeBytes: Long = 0L,
    val outputPath: String? = null,
    val errorMessage: String? = null
)
