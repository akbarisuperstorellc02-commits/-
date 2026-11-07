# BH Personal Academy — QA checklist

## Automated checks
- [x] `gradle testDebugUnitTest` passes (CI run 42395604417).
- [x] `gradle assembleDebug` succeeds (CI run 42395604417).
- [x] APK artifact is uploaded by GitHub Actions (artifact 13583512344).
- [x] No AI/chatbot/model/API dependencies or source files are included in the active app build; no AI tutor/chat screen is wired into the app navigation.
- [ ] Manifest has no INTERNET permission.

## Profile and preferences
- [ ] Create a second learner profile and verify it appears in the profile selector.
- [ ] Switch between profiles and verify each profile has an independent wallet, lesson progress, quizzes, games, vocabulary, study sessions, goals and reminders.
- [ ] Restart the app and verify the selected profile is restored.
- [ ] Verify duplicate usernames are rejected when creating a profile.
- [ ] Reset one profile and verify all other profiles remain unchanged.
- [ ] Upgrade a database from schema version 3 to 4 and verify existing vocabulary is preserved for the primary profile.
- [ ] Export one profile, switch profiles, restore the backup and verify records merge only into the selected profile.
- [ ] Fresh install shows the default profile name زهرا.
- [ ] Changing display name updates the dashboard immediately.
- [ ] Username, avatar URI, locale, theme, font and daily targets survive app restart.
- [ ] Locale direction changes correctly between RTL and LTR.
- [ ] All visible numeric values use ASCII digits 0-9.

## Learning and rewards
- [ ] Pre-A1 lessons are available on first launch.
- [ ] Completing all lessons in a band unlocks the next band.
- [ ] Re-completing a lesson does not duplicate its reward.
- [ ] Completing a band awards its one-time level reward.
- [ ] Lesson timer pauses when the app moves to the background.
- [ ] Daily, weekly and monthly study totals reflect saved sessions.

## Quizzes and games
- [ ] Mixed quiz supports all five question formats.
- [ ] Answer selection locks after one response.
- [ ] Quiz results and incorrect question IDs persist.
- [ ] All game modes can be opened and completed.
- [ ] One hint deducts exactly 6 coins.
- [ ] Double-tapping the hint control cannot charge twice for one request.
- [ ] Insufficient balance prevents hint use.

## Vocabulary and progress
- [ ] Vocabulary seed is inserted separately for each profile without overwriting favourites.
- [ ] Search works for English words and Dari meanings.
- [ ] Favourite changes survive restart.
- [ ] Progress totals are calculated from Room records.
- [ ] Achievement badges reflect real recorded activity.

## Reminders and calendar
- [ ] Notification permission is requested on Android 13+.
- [ ] Exact-alarm permission guidance is shown when needed.
- [ ] Daily and selected-weekday reminders trigger as configured.
- [ ] Editing, disabling and deleting reminders works.
- [ ] Enabled reminders are restored after reboot and app replacement.
- [ ] Calendar marks days with recorded study sessions.

## Backup and reset
- [ ] JSON backup exports only the selected profile and its learning records.
- [ ] Restore validates backup format and version.
- [ ] Restore merges by stable IDs and restores enabled reminders.
- [ ] Reset requires two separate confirmations.
- [ ] Reset cancels alarms, clears Room/DataStore and recreates the default wallet.

## Source review completed (not a substitute for device QA)
- [x] Study calendar is wired to saved Room study sessions and shows per-day activity.
- [x] Calendar animal companion changes according to saved study minutes (0, 15, 30 and 60 minute thresholds).
- [x] Progress badges are based on saved lesson, quiz, game, study-time and XP records.
- [x] Four supported app locales are used for the new companion and achievement text.

## Physical-device checks
- [ ] Install and launch on a real Android device.
- [ ] Verify small and large screen layouts.
- [ ] Verify Android back navigation and system insets.
- [ ] Verify image picker permission persists after restart.
- [ ] Verify reminder delivery with app closed and after reboot.
- [ ] Verify backup export/import using actual files.

- [ ] Verify lesson and level unlock progression on a clean profile and an upgraded profile.
- [ ] Verify illustrated game tiles and visual vocabulary clues on physical Android devices.
- [ ] Verify launcher icon is fully visible across Android launcher masks and legacy launcher icons.
