package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.AiMediaAnalysis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiService {
    companion object {
        private const val TAG = "GeminiService"
        private const val MODEL_NAME = "gemini-2.5-flash"
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun analyzeMedia(title: String, author: String, durationSec: Long): AiMediaAnalysis = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Local AI intelligent heuristic summary when API key is not yet set in Secrets
            return@withContext generateLocalSummary(title, author, durationSec)
        }

        val prompt = """
            Eres el Asistente IA de StreamLite, una app para redes 2G/3G y bajo consumo.
            Analiza el siguiente contenido multimedia y genera un resumen estructurado para que el usuario conozca el contenido SIN tener que gastar datos reproduciendo el video completo.
            
            Título: "$title"
            Canal/Autor: "$author"
            Duración estimada: $durationSec segundos
            
            Responde en formato JSON válido con las siguientes claves:
            {
              "summary": "Resumen conciso y claro de 2 a 3 oraciones sobre qué trata este contenido.",
              "keyPoints": ["Punto clave 1", "Punto clave 2", "Punto clave 3"],
              "topicTags": ["Tag1", "Tag2", "Tag3"],
              "dataSavingAdvice": "Consejo técnico para ahorrar datos móviles al reproducir este contenido (ej: usar 360p o solo audio)."
            }
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            val contents = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val parts = JSONArray().apply {
                        val partObj = JSONObject().apply {
                            put("text", prompt)
                        }
                        put(partObj)
                    }
                    put("parts", parts)
                }
                put(contentObj)
            }
            put("contents", contents)

            val genConfig = JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.3)
            }
            put("generationConfig", genConfig)
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent?key=$apiKey"
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toString().toRequestBody(mediaType))
            .build()

        try {
            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                Log.w(TAG, "Gemini call unsuccessful: ${response.code} $responseBody")
                return@withContext generateLocalSummary(title, author, durationSec)
            }

            val rootJson = JSONObject(responseBody)
            val candidates = rootJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val rawText = parts?.optJSONObject(0)?.optString("text") ?: ""

            parseAnalysisJson(title, author, rawText, durationSec)
        } catch (e: Exception) {
            Log.e(TAG, "Error invoking Gemini: ${e.message}", e)
            generateLocalSummary(title, author, durationSec)
        }
    }

    suspend fun askAssistant(question: String, currentMediaTitle: String?): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Para interactuar en vivo con Gemini 2.5 Flash, configura tu GEMINI_API_KEY en el panel de Secrets de AI Studio. Mientras tanto, StreamLite está optimizado para funcionar sin conexión con algoritmos locales de compresión y ahorro de datos."
        }

        val contextInfo = if (!currentMediaTitle.isNullOrBlank()) "El usuario está explorando/reproduciendo: \"$currentMediaTitle\"." else ""
        val prompt = "Eres el asistente inteligente de StreamLite, una app de streaming de bajo consumo y compresión de video local para redes lentas (2G/3G / Internet para Todos). $contextInfo Responde de forma concisa, útil y en español a la siguiente consulta del usuario: $question"

        val jsonBody = JSONObject().apply {
            val contents = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val parts = JSONArray().apply {
                        val partObj = JSONObject().apply {
                            put("text", prompt)
                        }
                        put(partObj)
                    }
                    put("parts", parts)
                }
                put(contentObj)
            }
            put("contents", contents)
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent?key=$apiKey"
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toString().toRequestBody(mediaType))
            .build()

        try {
            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                return@withContext "No se pudo conectar con el servicio de IA (Código ${response.code}). Verifica tu conexión o clave de API."
            }
            val rootJson = JSONObject(responseBody)
            val candidates = rootJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val rawText = parts?.optJSONObject(0)?.optString("text") ?: "Sin respuesta."
            rawText.trim()
        } catch (e: Exception) {
            "Error de comunicación con Gemini: ${e.localizedMessage}"
        }
    }

    private fun parseAnalysisJson(title: String, author: String, jsonString: String, durationSec: Long): AiMediaAnalysis {
        return try {
            val cleanJson = jsonString.trim().removeSurrounding("```json", "```").trim()
            val obj = JSONObject(cleanJson)
            val summary = obj.optString("summary", "Resumen generado para $title por $author.")
            val keyPoints = mutableListOf<String>()
            obj.optJSONArray("keyPoints")?.let { arr ->
                for (i in 0 until arr.length()) {
                    keyPoints.add(arr.optString(i))
                }
            }
            val tags = mutableListOf<String>()
            obj.optJSONArray("topicTags")?.let { arr ->
                for (i in 0 until arr.length()) {
                    tags.add(arr.optString(i))
                }
            }
            val advice = obj.optString("dataSavingAdvice", "Utiliza calidad 360p o el modo Solo Audio para ahorrar hasta 85% de datos en redes lentas.")
            val savedMb = calculateEstimatedDataSavings(durationSec)

            AiMediaAnalysis(
                mediaId = title.hashCode().toString(),
                title = title,
                summary = summary,
                keyPoints = if (keyPoints.isNotEmpty()) keyPoints else listOf("Puntos clave optimizados para lectura rápida.", "Ahorra ancho de banda sin reproducir todo el contenido."),
                topicTags = if (tags.isNotEmpty()) tags else listOf("Streaming", "Eficiencia", "Audio/Video"),
                dataSavingAdvice = advice,
                estimatedStreamingDataSavedMb = savedMb
            )
        } catch (e: Exception) {
            generateLocalSummary(title, author, durationSec)
        }
    }

    private fun generateLocalSummary(title: String, author: String, durationSec: Long): AiMediaAnalysis {
        val savedMb = calculateEstimatedDataSavings(durationSec)
        return AiMediaAnalysis(
            mediaId = title.hashCode().toString(),
            title = title,
            summary = "Contenido multimedia \"$title\" creado por $author. Resumen optimizado para redes de bajo consumo 2G/3G sin requerir descarga previa de video completo.",
            keyPoints = listOf(
                "Análisis de bajo consumo: Transmisión recomendada en modo Audio-Only o 360p.",
                "Compresión local disponible: reduce hasta un 70% de espacio al guardar.",
                "Compatible con reproducción sin conexión."
            ),
            topicTags = listOf("Multimedia", "Ahorro de Datos", "Local"),
            dataSavingAdvice = "En redes 2G/3G activa 'Solo Audio' para reducir el consumo de ~15MB/minuto a menos de 1MB/minuto.",
            estimatedStreamingDataSavedMb = savedMb
        )
    }

    private fun calculateEstimatedDataSavings(durationSec: Long): Float {
        val duration = if (durationSec > 0) durationSec else 300L
        // 1080p stream ~ 5 Mbps = ~0.625 MB/s. Summary reading uses ~0.005 MB.
        val streamMb = (5000.0f / 8.0f / 1024.0f) * duration
        return (streamMb - 0.05f).coerceAtLeast(4.5f)
    }
}
