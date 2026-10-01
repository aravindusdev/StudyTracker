package com.example.studytracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.studytracker.AppContainer
import com.example.studytracker.domain.model.*
import com.example.studytracker.data.repository.GoalRepository
import com.example.studytracker.data.repository.StudyRepository
import com.example.studytracker.domain.usecase.StudySummary
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DashboardState(val loading: Boolean = true, val topics: List<Topic> = emptyList(), val sessions: List<StudySession> = emptyList(), val resources: List<StudyResource> = emptyList(), val goal: Int = 60, val error: String? = null, val refreshing: Boolean = false)
class DashboardViewModel(private val repo: StudyRepository, goals: GoalRepository, private val summary: StudySummary) : ViewModel() {
    private val error = MutableStateFlow<String?>(null)
    private val refreshing = MutableStateFlow(false)
    val state: StateFlow<DashboardState> = combine(repo.topics, repo.sessions, repo.resources, goals.dailyGoalMinutes) { topics, sessions, resources, goal ->
        DashboardState(false, topics, sessions, resources, goal)
    }.combine(error) { state, message -> state.copy(error = message) }
        .combine(refreshing) { state, busy -> state.copy(refreshing = busy) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardState())
    fun minutesToday(sessions: List<StudySession>) = summary.minutesToday(sessions)
    fun refresh() { viewModelScope.launch { refreshing.value = true; error.value = repo.refreshResources().exceptionOrNull()?.let { "Could not refresh books. Your saved study data is still available." }; refreshing.value = false } }
    fun seed() { viewModelScope.launch { repo.seedIfEmpty() } }
}

data class TopicsState(val loading: Boolean = true, val topics: List<Topic> = emptyList(), val query: String = "", val error: String? = null)
class TopicsViewModel(private val repo: StudyRepository) : ViewModel() {
    private val query = MutableStateFlow("")
    private val error = MutableStateFlow<String?>(null)
    val state = combine(repo.topics, query, error) { topics, q, e -> TopicsState(false, topics.filter { it.title.contains(q, true) || it.subject.contains(q, true) }, q, e) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TopicsState())
    fun search(value: String) { query.value = value }
    fun save(id: Long, title: String, subject: String, description: String, onSaved: () -> Unit) {
        if (title.isBlank() || subject.isBlank()) { error.value = "Title and subject are required"; return }
        viewModelScope.launch { repo.saveTopic(Topic(id, title, subject, description)); error.value = null; onSaved() }
    }
    fun delete(topic: Topic) { viewModelScope.launch { repo.deleteTopic(topic) } }
    fun topic(id: Long) = repo.topic(id)
}

data class SessionsState(val loading: Boolean = true, val topics: List<Topic> = emptyList(), val sessions: List<StudySession> = emptyList(), val activeTopic: Topic? = null, val startedAt: Long? = null, val elapsedMillis: Long = 0, val error: String? = null)
class SessionsViewModel(private val repo: StudyRepository, private val summary: StudySummary) : ViewModel() {
    private val active = MutableStateFlow<Pair<Topic, Long>?>(null)
    private val elapsed = MutableStateFlow(0L)
    private val error = MutableStateFlow<String?>(null)
    private var ticker: Job? = null
    val state = combine(repo.topics, repo.sessions, active, elapsed, error) { topics, sessions, current, elapsedMs, e -> SessionsState(false, topics, sessions, current?.first, current?.second, elapsedMs, e) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SessionsState())
    fun start(topic: Topic) {
        if (active.value != null) return
        val start = System.currentTimeMillis()
        active.value = topic to start
        ticker = viewModelScope.launch { while (true) { elapsed.value = System.currentTimeMillis() - start; delay(1_000) } }
    }
    fun stop() {
        val current = active.value ?: return
        val end = System.currentTimeMillis()
        ticker?.cancel(); active.value = null; elapsed.value = 0
        if (end - current.second < 1_000) { error.value = "Study for at least one second to save a session"; return }
        viewModelScope.launch { repo.saveSession(StudySession(topicId = current.first.id, topicTitle = current.first.title, startedAt = current.second, endedAt = end)); error.value = null }
    }
    fun discard() { ticker?.cancel(); active.value = null; elapsed.value = 0; error.value = null }
    fun weekMinutes(sessions: List<StudySession>) = summary.minutesThisWeek(sessions)
}

class SettingsViewModel(private val goals: GoalRepository) : ViewModel() {
    val goal = goals.dailyGoalMinutes.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 60)
    val themeMode = goals.themeMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "system")
    val message = MutableStateFlow<String?>(null)
    fun setThemeMode(mode: String) { viewModelScope.launch { goals.setThemeMode(mode) } }
    fun save(value: String) {
        val minutes = value.toIntOrNull()
        if (minutes == null || minutes !in 1..1440) { message.value = "Enter 1 to 1440 minutes"; return }
        viewModelScope.launch { goals.setDailyGoal(minutes); message.value = "Daily goal saved" }
    }
}

class StudyViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when (modelClass) {
        DashboardViewModel::class.java -> DashboardViewModel(container.study, container.goals, container.summary)
        TopicsViewModel::class.java -> TopicsViewModel(container.study)
        SessionsViewModel::class.java -> SessionsViewModel(container.study, container.summary)
        SettingsViewModel::class.java -> SettingsViewModel(container.goals)
        else -> error("Unknown ViewModel: $modelClass")
    } as T
}
