# Lunus

Lunus is an Android-first prototype for an offline-first AI study coach. The initial app proves the core study loop with mocked AI before adding Google AI Edge/LiteRT inference.

## Current Prototype

- Create subject/exam spaces.
- Paste source material.
- Generate draft flashcards through a mocked `AiCoach`.
- Review and accept draft cards.
- Study accepted cards in Review Mode or Exam Mode.
- Grade free-response answers with mocked local feedback.
- View a local model setup stub.

## Planned Next Steps

1. Add Gradle wrapper and verify the Android build.
2. Split `MainActivity.kt` into UI, domain, and AI packages.
3. Add Room persistence.
4. Add edit/merge/split actions in the draft review queue.
5. Replace `MockAiCoach` with a LiteRT-backed `EdgeAiCoach`.
6. Add PDF/image import after the text-based study loop is stable.

## Documents

- [PRD](PRD.md)
- [Architecture](docs/architecture.md)
