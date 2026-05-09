# Lunus Product Requirements Document

## Summary

Lunus is an offline-first AI study coach for self-directed students preparing for exams from messy study materials. The app turns notes into reviewed flashcards, runs adaptive study sessions, grades free-response answers locally, and keeps core study workflows available without internet connectivity.

## Product Positioning

Lunus is not just a flashcard deck manager. Its differentiator is the AI-assisted study loop: import messy material, generate draft cards, review them for trust, practice through recall and free response, receive feedback, and focus future sessions on weak topics.

## Target User

The initial target user is a self-directed exam learner: college students, serious high school students, certification learners, and independent learners who already have notes, PDFs, screenshots, textbook excerpts, or lecture summaries.

## MVP Scope

### In Scope

- Android-first native app built with Kotlin and Jetpack Compose.
- Account-free local onboarding.
- Subject/exam spaces.
- Pasted text/manual note source creation.
- Mocked AI draft flashcard generation.
- Draft review queue before cards enter the deck.
- Classic flashcard practice.
- Free-response practice with mocked local grading.
- Review Mode and Exam Mode entry points.
- Local-first domain model.
- Model/settings screen stub for future on-device AI setup.
- Technical architecture notes for Google AI Edge/LiteRT integration.

### Out of Scope for MVP

- iOS app.
- Desktop app.
- Cloud sync.
- User accounts.
- Classroom sharing.
- Public deck marketplace.
- PDF/image/OCR import.
- Voice mode.
- General-purpose chatbot.
- Real on-device model inference.
- Subscriptions/payments.

## Offline Boundary

Core study must work offline. Internet may be required for initial install, downloading model files, optional backup/sync later, and future enrichment features.

If no AI model is available, Lunus should still support manual cards, imported notes, accepted cards, and spaced review.

## Core User Journey

1. User creates a subject or exam space.
2. User pastes messy study material.
3. Lunus generates draft cards from the source.
4. User accepts, edits, deletes, or regenerates draft cards.
5. Accepted cards enter the subject deck.
6. User starts Review Mode or Exam Mode.
7. Lunus prompts classic recall and free-response questions.
8. Lunus grades answers, explains gaps, and updates review state.
9. User sees weak topics and the next recommended focus.

## MVP Data Model

- Subject: a course, exam, or learning area.
- Source: pasted notes or manually entered material.
- DraftCard: generated card awaiting review.
- Card: accepted study item.
- ReviewState: scheduling metadata for a card.
- StudySession: one review or cram session.
- AnswerAttempt: user answer, grade, feedback, timestamp.
- Topic: lightweight tags for weak-area tracking.
- ModelProfile: installed/local model metadata and performance notes.

## AI Capabilities

MVP uses mocked AI behavior while preserving the interfaces needed for real on-device inference.

Future real AI capabilities:

- Generate draft flashcards from pasted study material.
- Generate concise explanations.
- Grade free-response answers against expected answers and source context.
- Identify weak topics.
- Suggest next study focus.
- Regenerate or improve selected cards.

AI should appear inside the study workflow, not as a blank chat surface.

## Success Metrics

Initial metrics should be stored locally and visible to the user where useful.

- Cards generated per source.
- Draft cards accepted, edited, or deleted.
- Time from import to first study session.
- First-session completion rate.
- Review streaks.
- Sessions completed.
- Free-response correctness over time.
- Weak topics improving or not improving.
- Model generation time and failure rate.
- User trust signal: AI feedback marked useful or wrong.

## Platform Strategy

Start Android-native using the same technology family as Google AI Edge Gallery: Kotlin, Jetpack Compose, Google AI Edge, and LiteRT. Keep the domain and data model portable enough for later iOS and desktop exploration.

## Nice-to-Have Roadmap

- PDF import.
- Image import and OCR.
- Visual/multimodal cards.
- Exam-specific cram plans.
- Language-learning decks.
- Gamified learning systems.
- Classroom sharing.
- Optional account, backup, and sync.
- iOS app.
- Desktop companion.
