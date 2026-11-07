# Implementation Roadmap — Self-Growth Learning Academy

## Phase 0 — Safe project foundation
- Create isolated feature branch from main.
- Inspect and remove all AI-related dependencies, source files, routes and UI entry points.
- Establish package structure, app identity, dependency baseline and CI.
- Add product specification and architecture records.

## Phase 1 — Core architecture
- Domain models and repository contracts.
- Room entities/DAOs/database and migrations.
- DataStore profile/preferences, multiple local profiles and profile switching.
- Profile-scoped progress, vocabulary, reminders, backups and reward ledger, with Room migrations.
- Localization resource strategy and ASCII-digit formatter.
- Theme tokens and typography system.

## Phase 2 — App shell and profile
- Responsive Compose navigation and dashboard.
- Profile editing, avatar, locale, font and theme selection.
- Ensure one profile source of truth and no hard-coded display name.

## Phase 3 — Learning content
- Content schema and bundled curriculum for Pre-A1 through C1.
- Lesson reader, vocabulary, pronunciation/audio assets, practice and completion.
- Unlock rules and level assessments.

## Phase 4 — Assessment and games
- Reusable quiz engine and varied question types.
- Game framework and game content.
- Atomic reward transactions; hint cost exactly 6 coins and once per hint action.

## Phase 5 — Progress and routines
- Analytics from real activity records.
- Active-lesson study timer.
- Goals, streaks, calendar, history and vocabulary review.
- Notifications, reminders and alarms with Android-version permission handling.

## Phase 6 — Quality and delivery
- Unit, database, UI and navigation tests.
- Offline checks, accessibility and small/large screen checks.
- CI debug APK; release signing/AAB only with proper credentials.
- Install and verify on a real device before declaring device validation complete.

## Rules
- Work only on feature/bh-personal-academy-rebuild.
- Do not merge to main without explicit approval.
- Do not mark unfinished modules as complete.

- [x] Add 8 new lessons, for 71 total.
- [x] Add unit coverage for sequential lesson and band unlock rules.
- [x] Add illustrated game selection tiles and vocabulary visual clues.
- [x] Scale adaptive launcher foreground into Android safe zone.
