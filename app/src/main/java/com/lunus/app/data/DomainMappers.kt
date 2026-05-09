package com.lunus.app.data

import com.lunus.app.domain.CardModel
import com.lunus.app.domain.DraftCard
import com.lunus.app.domain.ModelProfile
import com.lunus.app.domain.ModelProvider
import com.lunus.app.domain.ModelRuntimeStatus
import com.lunus.app.domain.ModelVariant
import com.lunus.app.domain.ReviewState
import com.lunus.app.domain.Source
import com.lunus.app.domain.Subject
import java.time.Instant

fun Subject.toEntity(createdAt: String = Instant.now().toString()) = SubjectEntity(
    id = id,
    name = name,
    createdAt = createdAt
)

fun SubjectEntity.toDomain() = Subject(
    id = id,
    name = name
)

fun Source.toEntity() = SourceEntity(
    id = id,
    subjectId = subjectId,
    body = body,
    createdAt = createdAt
)

fun SourceEntity.toDomain() = Source(
    id = id,
    subjectId = subjectId,
    body = body,
    createdAt = createdAt
)

fun DraftCard.toEntity(createdAt: String = Instant.now().toString()) = DraftCardEntity(
    id = id,
    subjectId = subjectId,
    sourceId = sourceId,
    front = front,
    back = back,
    topic = topic,
    sourceSnippet = sourceSnippet,
    createdAt = createdAt
)

fun DraftCardEntity.toDomain() = DraftCard(
    id = id,
    subjectId = subjectId,
    sourceId = sourceId,
    front = front,
    back = back,
    topic = topic,
    sourceSnippet = sourceSnippet
)

fun CardModel.toEntity(createdAt: String = Instant.now().toString()) = CardEntity(
    id = id,
    subjectId = subjectId,
    front = front,
    back = back,
    topic = topic,
    attempts = reviewState.attempts,
    correctStreak = reviewState.correctStreak,
    lastReviewedAt = reviewState.lastReviewedAt,
    createdAt = createdAt
)

fun CardEntity.toDomain() = CardModel(
    id = id,
    subjectId = subjectId,
    front = front,
    back = back,
    topic = topic,
    reviewState = ReviewState(
        attempts = attempts,
        correctStreak = correctStreak,
        lastReviewedAt = lastReviewedAt
    )
)

fun ModelProfile.toEntity() = ModelProfileEntity(
    id = id,
    displayName = displayName,
    provider = provider.name,
    variant = variant.name,
    modelPath = modelPath,
    runtimeStatus = runtimeStatus.name,
    lastLatencyMs = lastLatencyMs,
    lastError = lastError,
    updatedAt = updatedAt
)

fun ModelProfileEntity.toDomain() = ModelProfile(
    id = id,
    displayName = displayName,
    provider = runCatching { ModelProvider.valueOf(provider) }
        .getOrDefault(ModelProvider.LocalFile),
    variant = runCatching { ModelVariant.valueOf(variant) }
        .getOrDefault(ModelVariant.CustomLocal),
    modelPath = modelPath,
    runtimeStatus = runCatching { ModelRuntimeStatus.valueOf(runtimeStatus) }
        .getOrDefault(ModelRuntimeStatus.Error),
    lastLatencyMs = lastLatencyMs,
    lastError = lastError,
    updatedAt = updatedAt
)
