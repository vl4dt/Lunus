# Lunus Architecture

## Direction

Lunus starts as a clean Android app inspired by Google AI Edge Gallery rather than a fork. Gallery is useful as a reference for model discovery, local model management, and LiteRT inference, but Lunus needs a study-centered information architecture.

## Initial Stack

- Kotlin
- Jetpack Compose
- Material 3
- AndroidX Lifecycle ViewModel
- Future persistence: Room/SQLite
- Future inference: Google AI Edge and LiteRT

## Layers

### UI

Compose screens for:

- Subject list
- Subject detail
- Source creation
- Draft review
- Study session
- Model/settings

### Domain

Domain models describe learning concepts independently of Android UI:

- Subject
- Source
- DraftCard
- Card
- ReviewState
- StudySession
- AnswerAttempt
- ModelProfile

### AI Boundary

The UI talks to an `AiCoach` interface. The current implementation is `MockAiCoach`, which creates deterministic draft cards and feedback. A later `EdgeAiCoach` can use LiteRT without changing the product flow.

### Persistence Boundary

The current implementation keeps state in memory for a fast prototype. The next production step is a Room-backed repository.

## On-Device AI Plan

1. Keep the `AiCoach` interface stable.
2. Add a model registry inspired by AI Edge Gallery.
3. Let the user download or import a compatible local model.
4. Store model metadata in `ModelProfile`.
5. Run flashcard generation and grading through a LiteRT-backed implementation.
6. Add fallback states for missing, slow, or low-confidence models.

## Offline Behavior

The app should never require connectivity for accepted cards, manual study, review scheduling, or already-imported sources. Model download and optional sync are online features, but studying is not.
