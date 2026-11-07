# Self-Growth Learning Academy — Product Specification

## Product identity
- Persian/Dari name: آکادمی خودسازی Learning
- English name: Self-Growth Learning Academy
- Default display name: زهرا (stored as user data; never hard-coded in UI)
- Android application; offline-first; multiple independent local learner profiles with profile-scoped settings and learning records.

## Supported locales
- fa-AF: Afghan Dari (default, RTL)
- ps-AF: Afghan Pashto (RTL)
- ar: Arabic (RTL)
- en-US: English (LTR)
All numeric UI output uses ASCII digits 0–9 in every locale.

## Non-negotiable product rules
- No AI, chatbot, AI tutor, model downloads, AI APIs, or AI SDKs.
- Profile name is a single source of truth; changing it updates all UI labels without resetting progress.
- Locale, theme, and font changes must not erase learning data.
- Persistent local data; offline-first operation.
- All interactive controls must have real behavior; no placeholder buttons.
- Preserve existing main branch and previous PR; development occurs on a dedicated feature branch.
- Never merge without explicit approval.

## Product modules
1. Home dashboard and navigation
2. Learning path: Pre-A1, A1, A2, B1, B2, C1, C2 and Specialized
3. Lessons with objectives, explanations, vocabulary, localized Dari/Pashto/Arabic meanings, pronunciation, examples, practice and completion rewards
4. Quiz engine: multiple choice, true/false, fill blank, translation, ordering, sentence building, listening where bundled audio exists
5. Educational games with difficulty, score, rules and persisted results
6. XP, coins, levels, badges and anti-duplicate reward ledger
7. Vocabulary notebook, favourites, difficult words and review queue
8. Study timer that counts only active lesson time
9. Calendar, daily goals, challenges and history
10. Reminder and alarm scheduling using Android APIs with permission-aware fallbacks
11. Multiple learner profiles, avatar, language, theme and font preferences
12. Progress analytics derived only from persisted activity records
13. Backup, restore and guarded reset

## Visual design
Soft, polished, playful/puffy cards; consistent spacing and component sizes; responsive layouts; coordinated light/dark and named theme palettes; readable Dari/Pashto typography; safe-area-aware navigation; subtle motion; accessible touch targets.

## Technical direction
- Kotlin + Jetpack Compose
- MVVM / unidirectional UI state
- Navigation Compose
- Room for structured learning and activity records
- DataStore for per-profile preferences and appearance settings
- Room profileId scoping for progress, wallets, attempts, reminders and vocabulary; profile-aware migration for composite vocabulary keys
- WorkManager / AlarmManager only where appropriate
- Repository layer and versioned backup format
- Unit tests for progression, rewards, localization digits, study timer and persistence.

## Delivery gates
A feature is complete only after implementation, compilation, automated tests and a practical UI/device check where possible. APK/AAB and release claims must match actual CI artifacts. No claim of real-device testing unless performed.


## Recent progression and visual updates
- 71 lessons across eight learning bands.
- Sequential unlock: the next lesson unlocks after the immediately preceding lesson is completed; the next band unlocks after the final lesson in the preceding band.
- Game selection uses visual tiles and each game round can display its vocabulary emoji clue.
- Adaptive launcher artwork is scaled into the Android icon safe zone.
