# آکادمی خودسازی Learning — Self-Growth Learning Academy

Offline-first Android app for English learning and personal growth.

## App identity

- Display name: آکادمی خودسازی Learning
- English name: Self-Growth Learning Academy
- Application ID: `com.bh.personalacademy`
- Default profile: زهرا
- Default locale: Afghan Dari (`fa-AF`)
- Supported locales: `fa-AF`, `ps-AF`, `ar`, `en-US`
- Minimum Android: API 24
- Target Android: API 35
- Compile SDK: 36
- Version: 1.0.0

## Product rules

- No AI, chatbot, private AI tutor, AI models, AI APIs or AI SDKs.
- No internet permission is declared; core learning is local and offline.
- All displayed numbers must use ASCII digits (0-9), regardless of language.
- Multiple learner profiles are stored locally with profile-specific Preferences DataStore settings and profile-scoped Room records.
- Switching profiles switches the active wallet, lessons, quizzes, games, study sessions, vocabulary, goals, reminders and statistics. Vocabulary uses a profile-aware composite key with a Room 3→4 migration.
- Learning activity, rewards, vocabulary, reminders and history are stored in Room.

## Implemented app areas

- Compose app shell with home, learning, games, progress and profile navigation, plus study tools, vocabulary and calendar pages.
- Multiple local profiles with creation and switching; each profile has independent settings, avatar, locale, font, themes, wallet and learning records.
- Eight learning stages from Pre-A1 through C2, followed by a Specialized English track; current lesson catalog contains 71 lesson entries across general and professional English, including a multilingual 15-animal vocabulary lesson; 8 graded stories in the story library.
- Lesson completion, level unlock rules and one-time reward ledger.
- 26 educational game modes in the current game catalog. Regression checks now cover hidden missing-word answers and valid letter scrambling; several named modes still need dedicated mechanics and full manual QA.
- Mixed quiz formats, saved quiz attempts and incorrect-question history.
- Searchable vocabulary notebook with persisted favourites and practice tracking.
- Multilingual story library with graded short stories from beginner to specialized level, English reading text, localized translations, vocabulary cards and comprehension questions.
- Lesson study timer and daily/weekly/monthly study statistics.
- Calendar and activity history.
- Reminder scheduling, notification permission handling, exact-alarm fallback and reboot restoration.
- JSON backup and merge-restore scoped to the currently selected profile.
- Two-step reset flow.

## Architecture

- Kotlin and Jetpack Compose
- MVVM and repository boundary
- Room database with explicit migrations
- Preferences DataStore
- Android AlarmManager and Notification APIs
- Localized lesson and game catalogs, including specialized business, technology, academic, healthcare, job-interview and travel English
- Unit tests for domain rules, content catalogs, digit formatting and reminder scheduling

## Build and test

The repository CI uses JDK 17 and Gradle 9.3.1.

From the repository root, with Android SDK 36 installed:

```bash
gradle testDebugUnitTest
gradle assembleDebug
```

Debug APK output:

`app/build/outputs/apk/debug/app-debug.apk`

GitHub Actions uploads the debug APK as the `self-growth-academy-debug-apk` artifact when the build succeeds.

## Current validation

- The latest CI validation for each change is recorded in GitHub Actions; only successful runs are treated as build validation.
- Speaking practice includes Android speech recognition, Android system speech-input fallback, text-to-speech prompts, word-level transcript similarity scoring, saved attempts and one-time rewards. Physical-device validation is still required. It is not phoneme-level pronunciation assessment, and recognition availability/offline support depends on the Android speech service and installed language pack. Automated CI does not prove physical-device behavior or release readiness.

## Known incomplete work before release

- Multi-profile support has now been implemented in DataStore and profile-scoped Room queries, including vocabulary composite keys and migration 3→4; it still requires automated and physical-device regression testing before release.
- The final user-provided launcher icon has not yet been integrated; the current launcher resources must be replaced with the approved source image.
- Physical-device QA remains required for layouts, all translations, each game mode, timer lifecycle, notifications, reboot restoration, backup/restore and reset.
- A signed release APK/AAB has not yet been produced; the available APK is a debug build.

## Reminder permissions

- Android 13+ requires notification permission for visible reminders.
- Android 12+ may require exact-alarm access. If unavailable, the app falls back to an inexact alarm and informs the user.
- Saved enabled reminders are restored after device reboot and app replacement.

## Backup and restore

The profile page can create a versioned JSON backup and restore it. Restore merges records by stable IDs; it does not silently erase local records. Full reset is a separate confirmation flow.

The development branch is `feature/bh-personal-academy-rebuild`. The default `main` branch is intentionally not modified.


Recent update: the learning catalog now contains 71 lessons across all eight bands. Game selection cards use illustrated emoji artwork and each vocabulary round includes a visual clue. Lesson and level locks now follow the immediately preceding lesson / final lesson of the previous band. The adaptive launcher foreground is scaled into Android's safe zone.
