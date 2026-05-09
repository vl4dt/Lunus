package com.lunus.app.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lunus.app.domain.ModelProvider
import com.lunus.app.domain.ModelRuntimeStatus
import com.lunus.app.domain.ModelVariant

@Composable
fun ModelScreen(vm: LunusViewModel, modifier: Modifier = Modifier) {
    val profile = vm.modelProfile
    var modelPath by remember { mutableStateOf(profile.modelPath.orEmpty()) }
    val modelPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val selectedPath = it.toString()
            modelPath = selectedPath
            vm.saveModelPath(selectedPath)
        }
    }

    LaunchedEffect(profile.modelPath) {
        modelPath = profile.modelPath.orEmpty()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Local model setup", style = MaterialTheme.typography.titleLarge)
        Text("Lunus keeps studying available even when no local AI model is configured.")

        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(profile.displayName, fontWeight = FontWeight.SemiBold)
                Text("Provider: ${profile.provider.label}")
                Text("Variant: ${profile.variant.label}")
                Text("Status: ${profile.runtimeStatus.label}")
                Text("Path: ${profile.modelPath ?: "Not configured"}")
                profile.lastLatencyMs?.let { Text("Last latency: ${it}ms") }
                profile.lastError?.let {
                    Text("Last error: $it", color = MaterialTheme.colorScheme.error)
                }
            }
        }

        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Gemma 4 via AICore", fontWeight = FontWeight.SemiBold)
                Text("Use on supported physical devices with AICore Developer Preview.")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { vm.selectGemma4Variant(ModelVariant.Gemma4FastE2B) },
                        enabled = profile.variant != ModelVariant.Gemma4FastE2B
                    ) {
                        Text("Fast E2B")
                    }
                    Button(
                        onClick = { vm.selectGemma4Variant(ModelVariant.Gemma4FullE4B) },
                        enabled = profile.variant != ModelVariant.Gemma4FullE4B
                    ) {
                        Text("Full E4B")
                    }
                }
            }
        }

        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Local model path", fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = modelPath,
                    onValueChange = { modelPath = it },
                    label = { Text("Path to local model file") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { modelPicker.launch(arrayOf("*/*")) }) {
                        Text("Choose File")
                    }
                    Button(onClick = { vm.saveModelPath(modelPath) }) {
                        Text("Save")
                    }
                    OutlinedButton(onClick = { vm.saveModelPath("") }) {
                        Text("Clear")
                    }
                }
                OutlinedButton(
                    onClick = { vm.runModelSelfCheck() },
                    enabled = profile.modelPath != null
                ) {
                    Text("Self Check")
                }
            }
        }

        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Planned AI Edge integration", fontWeight = FontWeight.SemiBold)
                Text("Import a compatible on-device model.")
                Text("Run generation and grading through an EdgeAiCoach.")
                Text("Fall back to manual study when a model is missing or slow.")
            }
        }
    }
}

private val ModelRuntimeStatus.label: String
    get() = when (this) {
        ModelRuntimeStatus.NotConfigured -> "Not configured"
        ModelRuntimeStatus.Ready -> "Configured"
        ModelRuntimeStatus.Error -> "Error"
    }

private val ModelProvider.label: String
    get() = when (this) {
        ModelProvider.LocalFile -> "Local file"
        ModelProvider.AicoreGemma4 -> "AICore Gemma 4"
    }

private val ModelVariant.label: String
    get() = when (this) {
        ModelVariant.CustomLocal -> "Custom local"
        ModelVariant.Gemma4FastE2B -> "Gemma 4 Fast E2B"
        ModelVariant.Gemma4FullE4B -> "Gemma 4 Full E4B"
    }
