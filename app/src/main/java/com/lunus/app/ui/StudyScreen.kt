package com.lunus.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StudyScreen(vm: LunusViewModel, modifier: Modifier = Modifier) {
    val card = vm.currentStudyCard
    var answer by remember(card?.id) { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Study session", style = MaterialTheme.typography.titleLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { vm.startReviewMode() }, enabled = vm.selectedSubject != null) {
                Text("Review Mode")
            }
            OutlinedButton(onClick = { vm.startExamMode() }, enabled = vm.selectedSubject != null) {
                Text("Exam Mode")
            }
        }

        if (card == null) {
            Text("Select a subject and accept cards to start studying.")
            return
        }

        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(card.front, style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it },
                    label = { Text("Answer in your own words") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        vm.gradeCurrentAnswer(answer)
                        answer = ""
                    },
                    enabled = answer.isNotBlank()
                ) {
                    Text("Grade Answer")
                }
                vm.latestFeedback?.let {
                    Text(it, color = MaterialTheme.colorScheme.primary)
                    TextButton(onClick = { vm.nextCard() }) {
                        Text("Next Card")
                    }
                }
            }
        }
    }
}
