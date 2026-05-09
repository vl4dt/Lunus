# Lunus Development Tracker

Status values: `todo`, `in_progress`, `done`, `blocked`.

## Active Work

| ID | Status | Priority | Task | Notes |
| --- | --- | --- | --- | --- |
| LUN-001 | done | P0 | Define MVP PRD | Captured offline-first AI study coach direction. |
| LUN-002 | done | P0 | Create Android scaffold | Compose app builds with `assembleDebug`. |
| LUN-003 | done | P0 | Separate prototype code into packages | Split UI, domain, AI, and state management. |
| LUN-004 | done | P0 | Add Room persistence | Persist subjects, sources, drafts, cards, review state, and attempts. |
| LUN-005 | done | P1 | Add draft edit flow | Users can edit prompt, expected answer, and topic before accepting. |
| LUN-006 | done | P1 | Add manual card creation | Required fallback when AI model is unavailable. |
| LUN-007 | done | P1 | Add basic weak-topic dashboard | Progress tab ranks weak topics from local review state. |
| LUN-008 | done | P1 | Add emulator/device UI verification | Emulator `emulator-5554` connected; debug APK installed and launched. |
| LUN-009 | done | P2 | Research AI Edge Gallery model integration | Recommended MediaPipe LLM Inference first, with LiteRT model management later. |
| LUN-010 | todo | P2 | Upgrade Android Gradle Plugin later | Defer until MVP skeleton stabilizes. |
| LUN-011 | done | P1 | Make AI coach boundary asynchronous | Required for real on-device inference. |
| LUN-012 | done | P1 | Add model profile persistence | Stores model path, runtime status, latency, and errors through Room v2 migration. |
| LUN-013 | done | P1 | Add local model import placeholder | Models tab can choose a local file URI and persist it. |
| LUN-014 | done | P2 | Add EdgeAiCoach skeleton | Graceful fallback until runtime and model are configured. |
| LUN-015 | blocked | P1 | Test real inference on physical Android device | Blocked until a capable physical Android device is connected; only `emulator-5554` is available. |
| LUN-016 | done | P1 | Add model self-check action | Exercises configured model path and surfaces graceful runtime failure. |
| LUN-017 | done | P1 | Add Gemma 4 model target settings | Supports AICore Gemma 4 E2B/Fast and E4B/Full selection. |

## Session Log

### 2026-05-08

- Resolved product direction through grill-me session.
- Created PRD, architecture notes, implementation plan, and Android scaffold.
- Android Studio wrapper works; `.\gradlew.bat assembleDebug` builds successfully.
- No connected emulator/device currently visible through `adb devices`.
- Beads CLI is installed, but project tracking with `bd` is blocked because Dolt is not installed.
- Added repo-local development tracker as the active tracking source.
- Split prototype code into `domain`, `ai`, and `ui` packages.
- Verified package split with `.\gradlew.bat assembleDebug`.
- Added Room/KSP persistence for subjects, sources, draft cards, accepted cards, and review state.
- Verified Room-backed prototype with `.\gradlew.bat assembleDebug`.
- Confirmed emulator `emulator-5554` through `adb devices`.
- Installed and launched `app-debug.apk` on the emulator.
- Added draft card edit/save/cancel flow and verified build/install/launch.
- Added manual card creation as a no-AI fallback and verified build/install/launch.
- Added Progress tab with study coverage and weak-topic ranking, then verified build/install/launch.
- Researched Google AI Edge Gallery, LiteRT Android, and MediaPipe LLM Inference API.
- Added `docs/research/google-ai-edge-integration.md` with recommended integration path.
- Converted `AiCoach` to a suspend boundary and verified build/install/launch.
- Added persisted `ModelProfile` state with Room migration 1 -> 2 and Models tab controls.
- Added local model file picker placeholder and persisted selected URI through `ModelProfile`.
- Added `LocalLlmRunner`, `MissingLocalModelRunner`, and `EdgeAiCoach` skeleton with mock fallback.
- Checked `adb devices`; real MediaPipe LLM validation is blocked because only emulator `emulator-5554` is connected.
- Added model self-check action in Models tab and verified build/install/launch.
- Added Gemma 4 Fast E2B and Full E4B model target settings with Room migration 2 -> 3.
- Verified current Gemma 4 Android path: AICore Developer Preview with ML Kit GenAI Prompt API for E2B/Fast and E4B/Full on supported devices.
