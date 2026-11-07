# خودسازی و انگلیسی — Self-Growth & English

Offline-first Android app for English learning and personal growth.

## App identity

- Display name: خودسازی و انگلیسی
- English name: Self-Growth & English
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
- Profile preferences are stored locally with Preferences DataStore.
- Learning activity, rewards, vocabulary, reminders and history are stored in Room.

## Implemented app areas

- Compose app shell with home, learning, games, progress and profile navigation, plus study tools, vocabulary and calendar pages.
- Editable profile details, avatar selection, locale, font and sixteen theme choices.
- Six learning bands from Pre-A1 through C1; current lesson catalog contains 50 lesson entries.
- Lesson completion, level unlock rules and one-time reward ledger.
- 26 educational game modes in the current game catalog; individual gameplay still requires full manual QA.
- Mixed quiz formats, saved quiz attempts and incorrect-question history.
- Searchable vocabulary notebook with persisted favourites and practice tracking.
- Lesson study timer and daily/weekly/monthly study statistics.
- Calendar and activity history.
- Reminder scheduling, notification permission handling, exact-alarm fallback and reboot restoration.
- JSON backup and merge-restore for profile and learning records.
- Two-step reset flow.

## Architecture

- Kotlin and Jetpack Compose
- MVVM and repository boundary
- Room database with explicit migrations
- Preferences DataStore
- Android AlarmManager and Notification APIs
- Localized lesson and game catalogs
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

GitHub Actions uploads the debug APK as the `bh-personal-academy-debug-apk` artifact when the build succeeds.

## Current validation

- GitHub Actions run #213 completed successfully for commit `39bd8030d976ad67ca7f4b3aca924dadbb60397c`.
- Unit tests, debug APK build and artifact upload all passed in that run.
- The uploaded artifact contains `app-debug.apk` (20,878,505 bytes).
- Automated CI does not prove physical-device behavior or release readiness.

## Known incomplete work before release

- Independent multi-profile support is not complete: the current repository and Room queries still default to the single `primary` profile. Do not create a second profile until profile-scoped storage and a data-preserving migration are implemented.
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
