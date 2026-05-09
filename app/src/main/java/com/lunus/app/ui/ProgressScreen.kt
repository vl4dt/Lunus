package com.lunus.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun ProgressScreen(vm: LunusViewModel, modifier: Modifier = Modifier) {
    val subject = vm.selectedSubject
    val topicProgress = vm.topicProgress
    val cardCount = subject?.let { vm.cardsFor(it.id).size } ?: 0
    val reviewedCount = topicProgress.sumOf { it.reviewedCount }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Weak topics", style = MaterialTheme.typography.titleLarge)
            Text(
                subject?.name ?: "Select a subject to see progress.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            ProgressSummaryCard(
                cardCount = cardCount,
                reviewedCount = reviewedCount,
                topicCount = topicProgress.size
            )
        }

        if (topicProgress.isEmpty()) {
            item {
                Text("Add or accept cards to start tracking weak topics.")
            }
        } else {
            items(topicProgress) { topic ->
                TopicProgressCard(topic)
            }
        }
    }
}

@Composable
private fun ProgressSummaryCard(cardCount: Int, reviewedCount: Int, topicCount: Int) {
    val progress = if (cardCount == 0) 0f else reviewedCount.toFloat() / cardCount.toFloat()

    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Study coverage", fontWeight = FontWeight.SemiBold)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("$reviewedCount/$cardCount reviewed")
                Text("$topicCount topics")
            }
        }
    }
}

@Composable
private fun TopicProgressCard(topic: TopicProgress) {
    val reviewedProgress = if (topic.cardCount == 0) {
        0f
    } else {
        topic.reviewedCount.toFloat() / topic.cardCount.toFloat()
    }
    val averageStreak = (topic.averageStreak * 10).roundToInt() / 10.0

    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(topic.topic, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Text(topic.focusLabel, color = MaterialTheme.colorScheme.primary)
            }
            LinearProgressIndicator(
                progress = { reviewedProgress },
                modifier = Modifier.fillMaxWidth()
            )
            Text("${topic.reviewedLabel} · ${topic.totalAttempts} attempts · $averageStreak streak avg")
        }
    }
}
