package com.lunus.app.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lunus.app.ai.AiCoach
import com.lunus.app.ai.EdgeAiCoach
import com.lunus.app.ai.LocalLlmRunner
import com.lunus.app.ai.MissingLocalModelRunner
import com.lunus.app.ai.MockAiCoach
import com.lunus.app.data.LunusDatabase
import com.lunus.app.domain.CardModel
import com.lunus.app.domain.DraftCard
import com.lunus.app.domain.ModelProfile
import com.lunus.app.domain.ModelProvider
import com.lunus.app.domain.ModelRuntimeStatus
import com.lunus.app.domain.ModelVariant
import com.lunus.app.domain.ReviewState
import com.lunus.app.domain.Source
import com.lunus.app.domain.Subject
import com.lunus.app.data.toDomain
import com.lunus.app.data.toEntity
import java.time.Instant
import kotlinx.coroutines.launch

class LunusViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = LunusDatabase.get(application).lunusDao()
    private val mockAiCoach: AiCoach = MockAiCoach()
    private val localLlmRunner: LocalLlmRunner = MissingLocalModelRunner { modelProfile.modelPath }
    private val edgeAiCoach: AiCoach = EdgeAiCoach(
        runner = localLlmRunner,
        fallback = mockAiCoach,
        onRuntimeError = { message -> markModelError(message) }
    )
    private val activeAiCoach: AiCoach
        get() = if (modelProfile.modelPath.isNullOrBlank()) {
            mockAiCoach
        } else {
            edgeAiCoach
        }

    var subjects by mutableStateOf(emptyList<Subject>())
        private set
    var sources by mutableStateOf(emptyList<Source>())
        private set
    var drafts by mutableStateOf(emptyList<DraftCard>())
        private set
    var cards by mutableStateOf(emptyList<CardModel>())
        private set
    var selectedSubject by mutableStateOf(subjects.firstOrNull())
        private set
    var currentStudyCard by mutableStateOf<CardModel?>(null)
        private set
    var latestFeedback by mutableStateOf<String?>(null)
        private set
    var modelProfile by mutableStateOf(ModelProfile())
        private set

    val topicProgress: List<TopicProgress>
        get() {
            val subjectId = selectedSubject?.id ?: return emptyList()
            return cardsFor(subjectId)
                .groupBy { it.topic.ifBlank { "Untitled" } }
                .map { (topic, topicCards) ->
                    val reviewed = topicCards.count { it.reviewState.attempts > 0 }
                    val needsReview = topicCards.count { it.reviewState.correctStreak == 0 }
                    val totalAttempts = topicCards.sumOf { it.reviewState.attempts }
                    val averageStreak = topicCards
                        .map { it.reviewState.correctStreak }
                        .average()
                        .takeUnless { it.isNaN() }
                        ?: 0.0
                    TopicProgress(
                        topic = topic,
                        cardCount = topicCards.size,
                        reviewedCount = reviewed,
                        needsReviewCount = needsReview,
                        totalAttempts = totalAttempts,
                        averageStreak = averageStreak
                    )
                }
                .sortedWith(
                    compareByDescending<TopicProgress> { it.needsReviewCount }
                        .thenBy { it.averageStreak }
                        .thenBy { it.topic.lowercase() }
                )
        }

    init {
        viewModelScope.launch {
            if (dao.subjectCount() == 0) {
                dao.insertSubject(Subject(name = "Biology Midterm").toEntity())
            }
        }
        viewModelScope.launch {
            dao.observeSubjects().collect { entities ->
                subjects = entities.map { it.toDomain() }
                if (selectedSubject == null || subjects.none { it.id == selectedSubject?.id }) {
                    selectedSubject = subjects.firstOrNull()
                }
            }
        }
        viewModelScope.launch {
            dao.observeSources().collect { entities ->
                sources = entities.map { it.toDomain() }
            }
        }
        viewModelScope.launch {
            dao.observeDrafts().collect { entities ->
                drafts = entities.map { it.toDomain() }
            }
        }
        viewModelScope.launch {
            dao.observeCards().collect { entities ->
                cards = entities.map { it.toDomain() }
                val currentId = currentStudyCard?.id
                currentStudyCard = cards.firstOrNull { it.id == currentId }
                    ?: selectedSubject?.let { cardsFor(it.id).firstOrNull() }
            }
        }
        viewModelScope.launch {
            dao.observeModelProfile().collect { entity ->
                modelProfile = entity?.toDomain() ?: ModelProfile()
            }
        }
    }

    fun addSubject(name: String) {
        val subject = Subject(name = name.trim())
        viewModelScope.launch {
            dao.insertSubject(subject.toEntity())
            selectedSubject = subject
        }
    }

    fun selectSubject(id: String) {
        selectedSubject = subjects.firstOrNull { it.id == id }
        latestFeedback = null
        currentStudyCard = cardsFor(id).firstOrNull()
    }

    fun generateDraftsFromSource(text: String) {
        val subject = selectedSubject ?: return
        val source = Source(subjectId = subject.id, body = text.trim())
        viewModelScope.launch {
            val generatedDrafts = activeAiCoach.generateDraftCards(subject.id, source)
            dao.insertSource(source.toEntity())
            dao.insertDrafts(generatedDrafts.map { it.toEntity() })
        }
    }

    fun acceptDraft(id: String) {
        val draft = drafts.firstOrNull { it.id == id } ?: return
        val card = CardModel(
            subjectId = draft.subjectId,
            front = draft.front,
            back = draft.back,
            topic = draft.topic,
            reviewState = ReviewState()
        )
        viewModelScope.launch {
            dao.insertCard(card.toEntity())
            dao.deleteDraft(draft.toEntity())
            currentStudyCard = currentStudyCard ?: card
        }
    }

    fun deleteDraft(id: String) {
        val draft = drafts.firstOrNull { it.id == id } ?: return
        viewModelScope.launch {
            dao.deleteDraft(draft.toEntity())
        }
    }

    fun updateDraft(id: String, front: String, back: String, topic: String) {
        val cleanFront = front.trim()
        val cleanBack = back.trim()
        val cleanTopic = topic.trim()
        if (cleanFront.isBlank() || cleanBack.isBlank() || cleanTopic.isBlank()) {
            return
        }
        viewModelScope.launch {
            dao.updateDraft(
                id = id,
                front = cleanFront,
                back = cleanBack,
                topic = cleanTopic
            )
        }
    }

    fun addManualCard(front: String, back: String, topic: String) {
        val subject = selectedSubject ?: return
        val cleanFront = front.trim()
        val cleanBack = back.trim()
        val cleanTopic = topic.trim().ifBlank { "Manual" }
        if (cleanFront.isBlank() || cleanBack.isBlank()) {
            return
        }
        val card = CardModel(
            subjectId = subject.id,
            front = cleanFront,
            back = cleanBack,
            topic = cleanTopic,
            reviewState = ReviewState()
        )
        viewModelScope.launch {
            dao.insertCard(card.toEntity())
            currentStudyCard = currentStudyCard ?: card
        }
    }

    fun saveModelPath(path: String) {
        val cleanPath = path.trim()
        val updatedProfile = modelProfile.copy(
            provider = ModelProvider.LocalFile,
            variant = ModelVariant.CustomLocal,
            modelPath = cleanPath.ifBlank { null },
            runtimeStatus = if (cleanPath.isBlank()) {
                ModelRuntimeStatus.NotConfigured
            } else {
                ModelRuntimeStatus.Ready
            },
            lastError = null,
            updatedAt = Instant.now().toString()
        )
        viewModelScope.launch {
            dao.upsertModelProfile(updatedProfile.toEntity())
        }
    }

    fun selectGemma4Variant(variant: ModelVariant) {
        val updatedProfile = modelProfile.copy(
            provider = ModelProvider.AicoreGemma4,
            variant = variant,
            modelPath = null,
            runtimeStatus = ModelRuntimeStatus.NotConfigured,
            lastLatencyMs = null,
            lastError = "AICore Gemma 4 runtime is not connected in this build yet.",
            updatedAt = Instant.now().toString()
        )
        viewModelScope.launch {
            dao.upsertModelProfile(updatedProfile.toEntity())
        }
    }

    fun markModelError(message: String) {
        val updatedProfile = modelProfile.copy(
            runtimeStatus = ModelRuntimeStatus.Error,
            lastError = message.trim().ifBlank { "Unknown model error" },
            updatedAt = Instant.now().toString()
        )
        viewModelScope.launch {
            dao.upsertModelProfile(updatedProfile.toEntity())
        }
    }

    fun runModelSelfCheck() {
        viewModelScope.launch {
            runCatching {
                localLlmRunner.generate("Return the word ready.")
            }.onSuccess { result ->
                val updatedProfile = modelProfile.copy(
                    runtimeStatus = ModelRuntimeStatus.Ready,
                    lastLatencyMs = result.latencyMs,
                    lastError = null,
                    updatedAt = Instant.now().toString()
                )
                dao.upsertModelProfile(updatedProfile.toEntity())
            }.onFailure { error ->
                markModelError(error.message ?: "Local model self-check failed.")
            }
        }
    }

    fun startReviewMode() {
        latestFeedback = null
        currentStudyCard = selectedSubject?.let { cardsFor(it.id).firstOrNull() }
    }

    fun startExamMode() {
        latestFeedback = null
        currentStudyCard = selectedSubject?.let { subject ->
            cardsFor(subject.id).sortedBy { it.reviewState.correctStreak }.firstOrNull()
        }
    }

    fun gradeCurrentAnswer(answer: String) {
        val card = currentStudyCard ?: return
        viewModelScope.launch {
            val feedback = activeAiCoach.gradeAnswer(card, answer)
            latestFeedback = feedback.message
            val updatedCards = cards.map {
                if (it.id == card.id) {
                    it.copy(
                        reviewState = it.reviewState.copy(
                            attempts = it.reviewState.attempts + 1,
                            correctStreak = if (feedback.correct) {
                                it.reviewState.correctStreak + 1
                            } else {
                                0
                            },
                            lastReviewedAt = Instant.now().toString()
                        )
                    )
                } else {
                    it
                }
            }
            cards = updatedCards
            updatedCards.firstOrNull { it.id == card.id }?.let { updatedCard ->
                dao.updateCard(updatedCard.toEntity())
            }
        }
    }

    fun nextCard() {
        val subject = selectedSubject ?: return
        val subjectCards = cardsFor(subject.id)
        val currentIndex = subjectCards.indexOfFirst { it.id == currentStudyCard?.id }
        currentStudyCard = subjectCards.getOrNull((currentIndex + 1).coerceAtMost(subjectCards.lastIndex))
            ?: subjectCards.firstOrNull()
        latestFeedback = null
    }

    fun draftsFor(subjectId: String) = drafts.filter { it.subjectId == subjectId }

    fun cardsFor(subjectId: String) = cards.filter { it.subjectId == subjectId }
}

data class TopicProgress(
    val topic: String,
    val cardCount: Int,
    val reviewedCount: Int,
    val needsReviewCount: Int,
    val totalAttempts: Int,
    val averageStreak: Double
) {
    val reviewedLabel: String = "$reviewedCount/$cardCount reviewed"
    val focusLabel: String = when {
        cardCount == 0 -> "No cards"
        needsReviewCount == 0 -> "Stable"
        reviewedCount == 0 -> "Not studied"
        else -> "$needsReviewCount need review"
    }
}
