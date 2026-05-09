# Google AI Edge Integration Research

Research date: 2026-05-08

## Sources Checked

- Google AI Edge Gallery GitHub repository: https://github.com/google-ai-edge/gallery
- LiteRT for Android documentation: https://ai.google.dev/edge/litert/android
- MediaPipe LLM Inference Android guide: https://ai.google.dev/edge/mediapipe/solutions/genai/llm_inference/android

## Current Takeaways

Google AI Edge Gallery is a Kotlin-heavy reference app for on-device GenAI. Its current public README positions it around fully offline local inference, model management, Hugging Face model discovery/download, benchmark feedback, custom model import, and Google AI Edge/LiteRT technology.

For Lunus, the useful idea is not to fork Gallery. The useful ideas are:

- A local model library with explicit model state.
- Download/import flows for compatible local models.
- Runtime capability checks before enabling AI features.
- Benchmark/latency feedback so the user understands whether a model is practical on their device.
- A clean fallback path when no model is installed.

## Runtime Choice

There are two related but distinct Google routes:

1. **LiteRT Android `CompiledModel` API**
   - Best fit for lower-level `.tflite` model execution and classical ML style inference.
   - Current docs list `com.google.ai.edge.litert:litert:2.1.0`.
   - This is important for future optimized model execution and Gallery-style model handling.

2. **MediaPipe LLM Inference API for Android**
   - Best fit for Lunus' first real AI tasks: text generation, summarization, flashcard generation, and answer grading.
   - Exposes options such as `modelPath`, `maxTokens`, `topK`, `temperature`, `randomSeed`, LoRA path, async result listener, and error listener.
   - Supports multimodal prompting for compatible models, which maps well to future visual flashcards and image/PDF workflows.

Recommendation: implement Lunus' first real AI path through the MediaPipe LLM Inference API, while keeping the app architecture named generically around `AiCoach` and `ModelProfile` so lower-level LiteRT or LiteRT-LM support can be added later.

Update for Gemma 4: Google now presents **AICore Developer Preview + ML Kit GenAI Prompt API** as the first-party Android path for prototyping Gemma 4 E2B/Fast and E4B/Full directly on supported devices. For Lunus, that means Gemma 4 should be modeled as an AICore-backed provider in `ModelProfile`, separate from user-selected local model files.

## Device Testing Constraint

Google's LLM Inference Android guide warns that the API is optimized for high-end devices and does not reliably support emulators. The emulator should remain our UI/product validation target, but real on-device model inference should be tested on a capable physical Android device.

Gemma 4 through AICore also requires a supported physical Android device, AICore readiness, and the Developer Preview model configuration. The emulator cannot prove Gemma 4 inference.

## Gemma 4 Trial Plan

1. Confirm the phone appears in `adb devices` as `device`.
2. Confirm the phone is on the ML Kit Prompt API/AICore supported-device list.
3. Enroll/configure AICore Developer Preview if required.
4. Select **Gemma 4 Fast E2B** in Lunus first.
5. Run model self-check.
6. If stable, try **Gemma 4 Full E4B**.
7. Replace `MissingLocalModelRunner` with an AICore/Prompt API runner.
8. Validate short flashcard generation and grading prompts before long source imports.

## MVP AI Feature Order

1. Add model profile persistence and UI states.
2. Add local model import path before network download.
3. Add `LocalLlmRunner` abstraction around the Google runtime.
4. Add `EdgeAiCoach` behind the existing `AiCoach` interface.
5. Implement flashcard generation from pasted text.
6. Implement free-response grading.
7. Add latency/failure metrics to the model profile.
8. Add Hugging Face download/discovery later.

## Prompt Contracts

### Draft Card Generation

Input:

- Subject name.
- Source text chunk.
- Existing topics if available.
- Requested number of cards.

Output should be strict JSON:

```json
{
  "cards": [
    {
      "front": "Question or prompt",
      "back": "Expected answer",
      "topic": "Short topic",
      "sourceSnippet": "Relevant supporting excerpt"
    }
  ]
}
```

Rules:

- Prefer recall prompts over trivia.
- Keep answers concise but complete.
- Do not invent facts outside the provided source.
- Preserve a short source snippet for trust and editing.
- If source quality is poor, return fewer cards.

### Free-Response Grading

Input:

- Card front.
- Expected answer.
- User answer.
- Source snippet.

Output should be strict JSON:

```json
{
  "correct": true,
  "score": 0.82,
  "feedback": "Short study-focused feedback",
  "missing": ["important missing point"]
}
```

Rules:

- Grade against meaning, not exact wording.
- Say "Needs review" when confidence is low.
- Do not penalize harmless phrasing differences.
- Keep feedback brief enough to read during a study session.

## Proposed Code Shape

```kotlin
interface LocalLlmRunner {
    suspend fun generate(
        prompt: String,
        options: GenerationOptions
    ): GenerationResult
}

class EdgeAiCoach(
    private val runner: LocalLlmRunner
) : AiCoach {
    override suspend fun generateDraftCards(...)
    override suspend fun gradeAnswer(...)
}
```

The current `AiCoach` interface is synchronous because `MockAiCoach` is simple. Real inference should move this boundary to `suspend` methods before integrating the runtime.

## Risks

- Model files are large and may require device storage UX.
- Device capability varies widely.
- Real inference may not work in emulator.
- JSON parsing from LLM output needs strict validation and fallback behavior.
- Model license and distribution constraints must be checked per model.
- Downloading from Hugging Face likely requires account/token handling for some models.

## Next Engineering Tasks

- Convert `AiCoach` to suspend methods and call it from ViewModel coroutines. Done.
- Add `ModelProfile` to domain and Room. Done.
- Add model availability state to the Models tab. Done.
- Add local model import UI placeholder. Done.
- Add `EdgeAiCoach` skeleton that fails gracefully until runtime dependency and model path are configured. Done.
- Add physical-device validation task for real inference.

## Current Skeleton

The app now has:

- `AiCoach`: suspend boundary used by the ViewModel.
- `MockAiCoach`: deterministic offline fallback.
- `LocalLlmRunner`: runtime-facing generation interface.
- `MissingLocalModelRunner`: placeholder runner that reports unavailable runtime/model state.
- `EdgeAiCoach`: calls the runner, reports runtime errors to `ModelProfile`, and falls back to `MockAiCoach`.
- `ModelProfile.provider`: distinguishes custom local files from AICore Gemma 4.
- `ModelProfile.variant`: records Custom Local, Gemma 4 Fast E2B, or Gemma 4 Full E4B.

This lets Lunus keep the full study loop working while model runtime work is unfinished. The next runtime step is to replace `MissingLocalModelRunner` with a MediaPipe-backed implementation on a physical Android device.
