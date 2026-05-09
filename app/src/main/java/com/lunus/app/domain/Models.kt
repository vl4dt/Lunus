package com.lunus.app.domain

import java.time.Instant
import java.util.UUID

data class Subject(
    val id: String = UUID.randomUUID().toString(),
    val name: String
)

data class Source(
    val id: String = UUID.randomUUID().toString(),
    val subjectId: String,
    val body: String,
    val createdAt: String = Instant.now().toString()
)

data class DraftCard(
    val id: String = UUID.randomUUID().toString(),
    val subjectId: String,
    val sourceId: String,
    val front: String,
    val back: String,
    val topic: String,
    val sourceSnippet: String
)

data class CardModel(
    val id: String = UUID.randomUUID().toString(),
    val subjectId: String,
    val front: String,
    val back: String,
    val topic: String,
    val reviewState: ReviewState
)

data class ReviewState(
    val attempts: Int = 0,
    val correctStreak: Int = 0,
    val lastReviewedAt: String? = null
)

data class AnswerFeedback(
    val correct: Boolean,
    val message: String
)

data class ModelProfile(
    val id: String = "local-llm",
    val displayName: String = "Local study model",
    val provider: ModelProvider = ModelProvider.LocalFile,
    val variant: ModelVariant = ModelVariant.CustomLocal,
    val modelPath: String? = null,
    val runtimeStatus: ModelRuntimeStatus = ModelRuntimeStatus.NotConfigured,
    val lastLatencyMs: Long? = null,
    val lastError: String? = null,
    val updatedAt: String = Instant.now().toString()
)

enum class ModelRuntimeStatus {
    NotConfigured,
    Ready,
    Error
}

enum class ModelProvider {
    LocalFile,
    AicoreGemma4
}

enum class ModelVariant {
    CustomLocal,
    Gemma4FastE2B,
    Gemma4FullE4B
}
