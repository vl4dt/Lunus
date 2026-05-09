package com.lunus.app.ai

import com.lunus.app.domain.AnswerFeedback
import com.lunus.app.domain.CardModel
import com.lunus.app.domain.DraftCard
import com.lunus.app.domain.Source

class MockAiCoach : AiCoach {
    override suspend fun generateDraftCards(subjectId: String, source: Source): List<DraftCard> {
        val sentences = source.body
            .split('.', '\n')
            .map { it.trim() }
            .filter { it.length > 24 }
            .take(4)

        return sentences.map { sentence ->
            val topic = sentence.split(' ').take(3).joinToString(" ")
            DraftCard(
                subjectId = subjectId,
                sourceId = source.id,
                front = "Explain: $topic",
                back = sentence,
                topic = topic,
                sourceSnippet = sentence.take(96)
            )
        }.ifEmpty {
            listOf(
                DraftCard(
                    subjectId = subjectId,
                    sourceId = source.id,
                    front = "What is the key idea in this source?",
                    back = source.body.take(180),
                    topic = "Key idea",
                    sourceSnippet = source.body.take(96)
                )
            )
        }
    }

    override suspend fun gradeAnswer(card: CardModel, answer: String): AnswerFeedback {
        val expectedTerms = card.back
            .lowercase()
            .split(' ', ',', '.', ';', ':', '(', ')')
            .filter { it.length > 4 }
            .distinct()
            .take(6)
        val matched = expectedTerms.count { answer.lowercase().contains(it) }
        val correct = matched >= maxOf(1, expectedTerms.size / 3)
        val message = if (correct) {
            "Good coverage. You matched $matched important term(s). Next review can be spaced farther out."
        } else {
            "Needs review. Compare your answer with: ${card.back.take(140)}"
        }
        return AnswerFeedback(correct = correct, message = message)
    }
}
