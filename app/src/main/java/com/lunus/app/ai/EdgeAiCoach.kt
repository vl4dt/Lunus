package com.lunus.app.ai

import com.lunus.app.domain.AnswerFeedback
import com.lunus.app.domain.CardModel
import com.lunus.app.domain.DraftCard
import com.lunus.app.domain.Source

class EdgeAiCoach(
    private val runner: LocalLlmRunner,
    private val fallback: AiCoach = MockAiCoach(),
    private val onRuntimeError: (String) -> Unit = {}
) : AiCoach {
    override suspend fun generateDraftCards(subjectId: String, source: Source): List<DraftCard> {
        return runCatching {
            runner.generate(buildCardGenerationPrompt(source))
            // Real JSON parsing will be added when the runtime dependency is connected.
            fallback.generateDraftCards(subjectId, source)
        }.getOrElse { error ->
            onRuntimeError(error.message ?: "Local model generation failed.")
            fallback.generateDraftCards(subjectId, source)
        }
    }

    override suspend fun gradeAnswer(card: CardModel, answer: String): AnswerFeedback {
        return runCatching {
            runner.generate(buildGradingPrompt(card, answer))
            // Real JSON parsing will be added when the runtime dependency is connected.
            fallback.gradeAnswer(card, answer)
        }.getOrElse { error ->
            onRuntimeError(error.message ?: "Local model grading failed.")
            fallback.gradeAnswer(card, answer)
        }
    }

    private fun buildCardGenerationPrompt(source: Source): String {
        return """
            Create flashcards from this study source.
            Return strict JSON with cards containing front, back, topic, and sourceSnippet.

            Source:
            ${source.body}
        """.trimIndent()
    }

    private fun buildGradingPrompt(card: CardModel, answer: String): String {
        return """
            Grade the user's answer against the expected answer.
            Return strict JSON with correct, score, feedback, and missing.

            Prompt: ${card.front}
            Expected answer: ${card.back}
            User answer: $answer
        """.trimIndent()
    }
}
