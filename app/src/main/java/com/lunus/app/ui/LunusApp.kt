package com.lunus.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LunusApp(vm: LunusViewModel = viewModel()) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            var tab by remember { mutableStateOf(AppTab.Subjects) }
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text("Lunus", fontWeight = FontWeight.Bold)
                                Text("Offline-first AI study coach", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    )
                },
                bottomBar = {
                    NavigationBar {
                        AppTab.entries.forEach { item ->
                            NavigationBarItem(
                                selected = tab == item,
                                onClick = { tab = item },
                                label = { Text(item.label) },
                                icon = {}
                            )
                        }
                    }
                }
            ) { padding ->
                when (tab) {
                    AppTab.Subjects -> SubjectScreen(vm, Modifier.padding(padding))
                    AppTab.Study -> StudyScreen(vm, Modifier.padding(padding))
                    AppTab.Progress -> ProgressScreen(vm, Modifier.padding(padding))
                    AppTab.Models -> ModelScreen(vm, Modifier.padding(padding))
                }
            }
        }
    }
}

enum class AppTab(val label: String) {
    Subjects("Subjects"),
    Study("Study"),
    Progress("Progress"),
    Models("Models")
}
