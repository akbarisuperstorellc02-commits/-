# آکادمی اختصاصی BH — BH Personal Academy

Offline-first Android learning and personal-development app.

## App identity

- App label: آکادمی اختصاصی BH
- English name: BH Personal Academy
- Application ID: `com.bh.personalacademy`
- Default profile name: زهرا
- Default locale: Afghan Dari (`fa-AF`)
- Supported locales: `fa-AF`, `ps-AF`, `ar`, `en-US`
- Minimum Android: API 24
- Target Android: API 35
- Compile SDK: 36

## Product rules

- No AI, chatbot, AI tutor, AI models, AI APIs or AI SDKs.
- No internet permission is declared; core learning is local and offline.
- User profile settings are stored in DataStore.
- Learning activity, rewards, vocabulary, reminders and history are stored in Room.
- UI number formatting normalizes Persian and Arabic-Indic numerals to ASCII digits.
- The default display name is profile data, not a hard-coded UI label.

## Current implemented modules

- Compose app shell with five main navigation destinations and additional tool pages.
- Profile editing, persistent avatar selection, locale, font and sixteen theme choices.
- Six learning bands from Pre-A1 through C1 with a structured 38-lesson catalog.
- Lesson completion, level unlock rules and one-time reward ledger.
- Eighteen playable educational game modes with recorded results.
- Mixed quiz with vocabulary, true/false, fill-in-the-blank, translation and sentence-building questions.
- Searchable vocabulary notebook with persisted favourites and practice tracking.
- Lesson study timer, independent timer and daily/weekly/monthly study statistics.
- Calendar and activity history.
- Reminder scheduling, notification permission handling, exact-alarm fallback and reboot restoration.
- JSON backup and merge-restore for profile and learning records.
- Two-step reset with reminder cancellation and default wallet recreation.

## Architecture

- Kotlin and Jetpack Compose
- MVVM / repository boundary
- Room database with explicit migrations
- Preferences DataStore
- Android AlarmManager and Notification APIs
- Localized lesson and game catalogs
- Unit tests for domain rules, content catalogs, digit formatting and reminder scheduling

## Build and test

The repository CI installs Gradle 9.3.1 and JDK 17. The project currently uses a system Gradle invocation rather than a committed Gradle Wrapper.

From the repository root, with Android SDK 36 installed:

```bash
gradle testDebugUnitTest
gradle assembleDebug
```

Debug APK output:

`app/build/outputs/apk/debug/app-debug.apk`

GitHub Actions uploads the debug APK as the `bh-personal-academy-debug-apk` artifact when the build succeeds.

## Reminder permissions

- Android 13+ requires notification permission for visible reminders.
- Android 12+ may require the user to allow exact alarms. If exact-alarm access is unavailable, the app falls back to an inexact alarm and informs the user.
- Saved enabled reminders are restored after device reboot and app replacement.

## Backup and restore

The profile page can create a versioned JSON backup and restore it. Restore merges records by their stable IDs; it does not silently erase local records. Full reset is a separate two-confirmation action.

## Validation status

A successful automated build is required before an APK is described as ready. Automated CI does not replace real-device checks. Verify profile persistence, theme and locale changes, lesson progression, reward idempotency, hint cost, timer behavior, reminder delivery, reboot restoration, backup/restore and reset on a physical Android device before release.

The development branch is `feature/bh-personal-academy-rebuild`. The default `main` branch is intentionally not modified by this work.
