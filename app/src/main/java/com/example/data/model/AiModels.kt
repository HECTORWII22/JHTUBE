package com.example.data.model

data class AiMediaAnalysis(
    val mediaId: String,
    val title: String,
    val summary: String,
    val keyPoints: List<String>,
    val topicTags: List<String>,
    val dataSavingAdvice: String,
    val estimatedStreamingDataSavedMb: Float,
    val generatedAtEpochMs: Long = System.currentTimeMillis()
)

data class AiChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageSender {
    USER,
    GEMINI
}
