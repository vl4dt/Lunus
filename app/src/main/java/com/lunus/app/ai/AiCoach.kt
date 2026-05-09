package com.lunus.app.ai

import com.lunus.app.domain.AnswerFeedback
import com.lunus.app.domain.CardModel
import com.lunus.app.domain.DraftCard
import com.lunus.app.domain.Source

interface AiCoach {
    suspend fun generateDraftCards(subjectId: String, source: Source): List<DraftCard>
    suspend fun gradeAnswer(card: CardModel, answer: String): AnswerFeedback
}
