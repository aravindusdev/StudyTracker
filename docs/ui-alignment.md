# UI alignment with the supplied StudyFlow reference

The Android app translates the supplied HTML and PNG mockups into native Jetpack Compose. The screenshots in the reference folder are design input, not Android runtime screens.

| Reference screen | Android route and implementation |
|---|---|
| Home dashboard | `dashboard` in `ui/dashboard/DashboardScreen.kt` |
| Topics | `topics` in `ui/topics/TopicsScreen.kt` |
| Topic details | `topic/{id}` in `ui/topics/TopicsScreen.kt` |
| Active focus session | `focus` in `ui/sessions/SessionsScreen.kt` |
| Session history | `history` in `ui/sessions/SessionsScreen.kt` |
| Settings | `settings` in `ui/settings/SettingsScreen.kt` |
| Dark dashboard | System/Light/Dark selection in Settings, applied by `ui/theme/StudyTheme.kt` |

## Shared visual language

- Plus Jakarta Sans for headings and focal numbers; Inter for body text and labels. Both are bundled under the Open Font License in `docs/licenses`.
- Indigo primary actions, mint progress accents, pale canvas, rounded white cards, and a four-tab bottom navigation. Dark mode uses layered slate surfaces and periwinkle actions.
- Native Compose controls retain touch targets, text scaling, and screen-reader descriptions where actions need them.
- The launcher icon uses a vector interpretation of the reference's stacked-line mark.

## Data fidelity

The mockups show account identity, cloud sync, streaks, focus quality, mastery percentages, reminders, and export actions. Those are absent because this app does not have the data sources or workflows to support them. The new screens instead show real Room session totals, the saved DataStore goal and theme, locally cached Open Library books, and honest empty states. Topic deletion retains session history.
