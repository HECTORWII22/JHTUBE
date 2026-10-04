package com.example.util

import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy
import java.io.IOException
import kotlin.math.min
import kotlin.math.pow

data class NetworkDiagnosticInfo(
    val timestamp: Long = System.currentTimeMillis(),
    val httpStatusCode: Int? = null,
    val httpStatusMessage: String? = null,
    val url: String? = null,
    val errorCode: Int = 0,
    val errorCodeName: String = "",
    val responseHeaders: Map<String, List<String>> = emptyMap(),
    val responseBodySnippet: String? = null,
    val retryAttempt: Int = 0,
    val nextBackoffDelayMs: Long = 0L
) {
    val formattedHeaders: String
        get() = if (responseHeaders.isEmpty()) {
            "No se recibieron encabezados HTTP"
        } else {
            responseHeaders.entries.joinToString("\n") { (key, values) ->
                "$key: ${values.joinToString(", ")}"
            }
        }
}

@OptIn(UnstableApi::class)
class ExponentialBackoffLoadErrorHandlingPolicy(
    private val minLoadableRetryCount: Int = 3,
    private val initialBackoffMs: Long = 1000L,
    private val maxBackoffMs: Long = 8000L,
    private val onDiagnosticCaptured: ((NetworkDiagnosticInfo) -> Unit)? = null
) : DefaultLoadErrorHandlingPolicy(minLoadableRetryCount) {

    companion object {
        const val TAG = "ExoPlayerNetwork"
    }

    override fun getRetryDelayMsFor(loadErrorInfo: LoadErrorHandlingPolicy.LoadErrorInfo): Long {
        val error = loadErrorInfo.exception
        val errorCount = loadErrorInfo.errorCount
        val uri = loadErrorInfo.loadEventInfo.uri.toString()

        val diagnostic = ExoNetworkLogger.extractDiagnostics(
            error = error,
            url = uri,
            retryCount = errorCount,
            nextBackoffMs = 0L
        )

        // For non-recoverable HTTP errors like 403 Forbidden or 404 Not Found, fail fast
        if (diagnostic.httpStatusCode in listOf(401, 403, 404, 410)) {
            Log.w(TAG, "Non-retryable HTTP ${diagnostic.httpStatusCode} on $uri - aborting loader retries to failover immediately.")
            ExoNetworkLogger.logNetworkFailure(diagnostic)
            onDiagnosticCaptured?.invoke(diagnostic)
            return C.TIME_UNSET
        }

        // Calculate exponential backoff with ceiling: initial * 2^(errorCount - 1)
        val exponent = (errorCount - 1).coerceIn(0, 4)
        val backoffDelayMs = (initialBackoffMs * (2.0.pow(exponent)).toLong()).coerceAtMost(maxBackoffMs)

        val updatedDiagnostic = diagnostic.copy(nextBackoffDelayMs = backoffDelayMs)
        ExoNetworkLogger.logNetworkFailure(updatedDiagnostic)
        onDiagnosticCaptured?.invoke(updatedDiagnostic)

        return backoffDelayMs
    }
}

object ExoNetworkLogger {
    const val TAG = "ExoPlayerNetwork"

    fun extractDiagnostics(
        error: Throwable,
        url: String? = null,
        errorCode: Int = 0,
        errorCodeName: String = "",
        retryCount: Int = 0,
        nextBackoffMs: Long = 0L
    ): NetworkDiagnosticInfo {
        var statusCode: Int? = null
        var statusMsg: String? = null
        var headers: Map<String, List<String>> = emptyMap()
        var bodySnippet: String? = null
        var targetUrl: String? = url

        var current: Throwable? = error
        while (current != null) {
            if (current is HttpDataSource.InvalidResponseCodeException) {
                statusCode = current.responseCode
                statusMsg = current.responseMessage
                headers = current.headerFields
                if (targetUrl == null) {
                    targetUrl = current.dataSpec.uri.toString()
                }
                if (current.responseBody.isNotEmpty()) {
                    val len = min(current.responseBody.size, 512)
                    bodySnippet = String(current.responseBody, 0, len)
                }
                break
            } else if (current is HttpDataSource.HttpDataSourceException) {
                if (targetUrl == null) {
                    targetUrl = current.dataSpec.uri.toString()
                }
            }
            current = current.cause
        }

        return NetworkDiagnosticInfo(
            httpStatusCode = statusCode,
            httpStatusMessage = statusMsg,
            url = targetUrl,
            errorCode = errorCode,
            errorCodeName = errorCodeName.ifBlank { error.javaClass.simpleName },
            responseHeaders = headers,
            responseBodySnippet = bodySnippet,
            retryAttempt = retryCount,
            nextBackoffDelayMs = nextBackoffMs
        )
    }

    fun extractFromPlaybackException(
        playbackException: PlaybackException,
        retryCount: Int,
        nextBackoffMs: Long
    ): NetworkDiagnosticInfo {
        return extractDiagnostics(
            error = playbackException,
            errorCode = playbackException.errorCode,
            errorCodeName = playbackException.errorCodeName,
            retryCount = retryCount,
            nextBackoffMs = nextBackoffMs
        )
    }

    fun logNetworkFailure(diag: NetworkDiagnosticInfo) {
        val sep = "============================================================"
        Log.e(TAG, sep)
        Log.e(TAG, "🚨 [EXOPLAYER NETWORK FAILURE DETECTED]")
        Log.e(TAG, "URL Solicitada: ${diag.url ?: "Desconocida"}")
        if (diag.httpStatusCode != null) {
            Log.e(TAG, "Código de Respuesta HTTP: ${diag.httpStatusCode} (${diag.httpStatusMessage ?: "Sin mensaje de estado"})")
        } else {
            Log.e(TAG, "Error de Transporte: ${diag.errorCodeName} (Código ${diag.errorCode})")
        }

        if (diag.responseHeaders.isNotEmpty()) {
            Log.e(TAG, "--- Encabezados HTTP de Respuesta (${diag.responseHeaders.size}) ---")
            diag.responseHeaders.forEach { (name, values) ->
                Log.e(TAG, "  • $name: ${values.joinToString(", ")}")
            }
        } else {
            Log.w(TAG, "Sin encabezados de respuesta disponibles (falló a nivel TCP/DNS o timeout previo)")
        }

        if (!diag.responseBodySnippet.isNullOrBlank()) {
            Log.e(TAG, "Cuerpo de Respuesta (Fragmento): ${diag.responseBodySnippet}")
        }

        if (diag.nextBackoffDelayMs > 0L) {
            Log.i(TAG, "⏱ Mecanismo de Reintento: Intento #${diag.retryAttempt} programado en ${diag.nextBackoffDelayMs}ms (Backoff Exponencial)")
        }
        Log.e(TAG, sep)
    }
}
