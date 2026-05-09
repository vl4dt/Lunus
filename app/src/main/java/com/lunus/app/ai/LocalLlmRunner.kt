package com.lunus.app.ai

interface LocalLlmRunner {
    suspend fun generate(prompt: String, options: GenerationOptions = GenerationOptions()): GenerationResult
}

data class GenerationOptions(
    val maxTokens: Int = 512,
    val temperature: Float = 0.2f,
    val topK: Int = 40
)

data class GenerationResult(
    val text: String,
    val latencyMs: Long? = null
)

class MissingLocalModelRunner(
    private val modelPathProvider: () -> String?
) : LocalLlmRunner {
    override suspend fun generate(prompt: String, options: GenerationOptions): GenerationResult {
        val modelPath = modelPathProvider()
        val reason = if (modelPath.isNullOrBlank()) {
            "No local model is configured."
        } else {
            "Local model runtime is not connected yet for $modelPath."
        }
        throw LocalModelUnavailableException(reason)
    }
}

class LocalModelUnavailableException(message: String) : RuntimeException(message)
