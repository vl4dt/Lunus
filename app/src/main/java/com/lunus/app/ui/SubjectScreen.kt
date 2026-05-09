package com.lunus.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lunus.app.domain.DraftCard

@Composable
fun SubjectScreen(vm: LunusViewModel, modifier: Modifier = Modifier) {
    var subjectName by remember { mutableStateOf("") }
    var sourceText by remember { mutableStateOf("") }
    var manualFront by remember { mutableStateOf("") }
    var manualBack by remember { mutableStateOf("") }
    var manualTopic by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Create an exam space", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = subjectName,
                    onValueChange = { subjectName = it },
                    label = { Text("Subject or exam") },
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        vm.addSubject(subjectName)
                        subjectName = ""
                    },
                    enabled = subjectName.isNotBlank()
                ) {
                    Text("Add")
                }
            }
        }

        item {
            Text("Paste source material", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = sourceText,
                onValueChange = { sourceText = it },
                label = { Text("Notes, excerpts, or study guide text") },
                minLines = 5,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    vm.generateDraftsFromSource(sourceText)
                    sourceText = ""
                },
                enabled = sourceText.isNotBlank() && vm.selectedSubject != null
            ) {
                Text("Generate Draft Cards")
            }
        }

        item {
            Text("Create manual card", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = manualFront,
                onValueChange = { manualFront = it },
                label = { Text("Prompt") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = manualBack,
                onValueChange = { manualBack = it },
                label = { Text("Expected answer") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = manualTopic,
                onValueChange = { manualTopic = it },
                label = { Text("Topic, optional") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    vm.addManualCard(manualFront, manualBack, manualTopic)
                    manualFront = ""
                    manualBack = ""
                    manualTopic = ""
                },
                enabled = manualFront.isNotBlank() && manualBack.isNotBlank() && vm.selectedSubject != null
            ) {
                Text("Add Manual Card")
            }
        }

        items(vm.subjects) { subject ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (subject.id == vm.selectedSubject?.id) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
                onClick = { vm.selectSubject(subject.id) }
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(subject.name, style = MaterialTheme.typography.titleMedium)
                    Text("${vm.cardsFor(subject.id).size} accepted cards")
                    Text("${vm.draftsFor(subject.id).size} drafts awaiting review")
                }
            }
        }

        val selectedSubject = vm.selectedSubject
        if (selectedSubject != null) {
            item {
                Text("Draft review", style = MaterialTheme.typography.titleMedium)
            }
            items(vm.draftsFor(selectedSubject.id)) { draft ->
                DraftCardItem(
                    draft = draft,
                    onAccept = { vm.acceptDraft(draft.id) },
                    onDelete = { vm.deleteDraft(draft.id) },
                    onSave = { front, back, topic -> vm.updateDraft(draft.id, front, back, topic) }
                )
            }
        }
    }
}

@Composable
private fun DraftCardItem(
    draft: DraftCard,
    onAccept: () -> Unit,
    onDelete: () -> Unit,
    onSave: (front: String, back: String, topic: String) -> Unit
) {
    var editing by remember(draft.id) { mutableStateOf(false) }
    var front by remember(draft.id, draft.front) { mutableStateOf(draft.front) }
    var back by remember(draft.id, draft.back) { mutableStateOf(draft.back) }
    var topic by remember(draft.id, draft.topic) { mutableStateOf(draft.topic) }

    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (editing) {
                OutlinedTextField(
                    value = front,
                    onValueChange = { front = it },
                    label = { Text("Prompt") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = back,
                    onValueChange = { back = it },
                    label = { Text("Expected answer") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Topic") },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(draft.front, fontWeight = FontWeight.SemiBold)
                Text(draft.back)
                Text("Topic: ${draft.topic}", style = MaterialTheme.typography.labelMedium)
            }
            Text("Source: ${draft.sourceSnippet}", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (editing) {
                    Button(
                        onClick = {
                            onSave(front, back, topic)
                            editing = false
                        },
                        enabled = front.isNotBlank() && back.isNotBlank() && topic.isNotBlank()
                    ) {
                        Text("Save")
                    }
                    OutlinedButton(
                        onClick = {
                            front = draft.front
                            back = draft.back
                            topic = draft.topic
                            editing = false
                        }
                    ) {
                        Text("Cancel")
                    }
                } else {
                    Button(onClick = onAccept) {
                        Text("Accept")
                    }
                    OutlinedButton(onClick = { editing = true }) {
                        Text("Edit")
                    }
                    OutlinedButton(onClick = onDelete) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}
