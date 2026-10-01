# Architecture and code tour

The app is one Android module, `app`, with a small explicit `AppContainer` in `StudyTrackerApplication.kt`. It creates one Room database, a Retrofit service, repositories, and a summary use case. `MainActivity` passes a ViewModel factory to the Navigation Compose graph.

## Layers

- `domain/model`: plain Kotlin types used outside persistence and HTTP.
- `domain/usecase`: `StudySummary` calculates daily and weekly minutes. It has no Android dependencies and has a unit test.
- `data/local`: Room entities, DAO queries, and the database. `Flow` queries emit new lists after writes.
- `data/remote`: Retrofit endpoint and JSON response types for Open Library.
- `data/repository`: interfaces and implementations. The study repository maps Room rows into domain models; a successful refresh writes books to Room. `DataStoreGoalRepository` owns goal and theme preferences.
- `ui/StudyViewModels.kt`: feature ViewModels combine repository flows into immutable `StateFlow` screen states and handle user events in `viewModelScope`.
- `ui/dashboard`, `topics`, `sessions`, `settings`: Compose renders states and forwards clicks/text changes. `topics` contains the list, editor, and details. `sessions` contains History and Focus. No composable accesses Room or the network.
- `ui/navigation`: route graph and bottom navigation.
- `ui/components`: shared brand header, cards, badges, metrics, and empty states.
- `ui/theme`: paired bundled fonts and Material 3 light/dark colors.

## Data flow example

```text
Tap Stop and save → SessionsViewModel.stop()
  → StudyRepository.saveSession()
  → Room SessionDao.insert()
  → SessionDao.observeAll() Flow emits
  → SessionsViewModel.state emits
  → HistoryScreen recomposes
```

`DashboardViewModel.refresh()` follows the same path: remote HTTP result → repository → Room resource rows → Room Flow → dashboard state. A failed request changes only an error message; it does not clear the database.

## Tests

- `StudySummaryTest` checks date filtering in a deterministic UTC example.
- `TopicsViewModelTest` checks filtering of repository data through screen state.
- `TopicDaoTest` uses an in memory Room database on an emulator or device to check insert and observation.

The package name is `com.example.studytracker`; change the namespace and application ID together if you want a personal publisher ID.
