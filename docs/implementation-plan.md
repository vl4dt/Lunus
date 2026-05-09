# Lunus Implementation Plan

## Phase 1: Product Skeleton

- Android project scaffold.
- Compose navigation for Subjects, Study, and Models.
- In-memory domain model.
- Mock `AiCoach`.
- Pasted source to draft cards.
- Draft acceptance to deck.
- Review Mode and Exam Mode entry points.
- Free-response answer grading placeholder.

## Phase 2: Production Local App

- Move domain, AI, repository, and UI into separate packages.
- Add Room entities and migrations.
- Persist subjects, sources, drafts, cards, review state, and attempts.
- Add manual card creation.
- Add draft edit, merge, split, and regenerate actions.
- Add basic weak-topic dashboard.

## Phase 3: On-Device AI

- Research compatible Google AI Edge/LiteRT text models.
- Add model download/import screen.
- Implement `EdgeAiCoach`.
- Add prompt contracts for card generation and grading.
- Add confidence handling and fallback states.
- Track local model latency and failures.

## Phase 4: Rich Input

- Add PDF text extraction.
- Add image import and OCR.
- Add source chunking.
- Preserve source snippets for every generated card.

## Phase 5: Expansion

- iOS feasibility spike.
- Optional account and sync.
- Classroom sharing.
- Gamification.
- Language-learning-specific workflows.
- Desktop companion evaluation.
