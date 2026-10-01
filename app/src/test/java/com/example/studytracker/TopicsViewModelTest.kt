package com.example.studytracker

import com.example.studytracker.data.repository.StudyRepository
import com.example.studytracker.domain.model.*
import com.example.studytracker.ui.TopicsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TopicsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun searchFiltersLocalTopics() = runTest(dispatcher) {
        val repo = FakeStudyRepository()
        val vm = TopicsViewModel(repo)
        val collector = backgroundScope.launch { vm.state.collect {} }
        advanceUntilIdle()
        vm.search("kotlin")
        advanceUntilIdle()
        assertEquals(listOf("Kotlin"), vm.state.value.topics.map { it.title })
        collector.cancel()
    }

    private class FakeStudyRepository : StudyRepository {
        override val topics = MutableStateFlow(listOf(Topic(1, "Kotlin", "Programming"), Topic(2, "Biology", "Science")))
        override val sessions = MutableStateFlow(emptyList<StudySession>())
        override val resources = MutableStateFlow(emptyList<StudyResource>())
        override fun topic(id: Long): Flow<Topic?> = MutableStateFlow(topics.value.firstOrNull { it.id == id })
        override suspend fun saveTopic(topic: Topic) { topics.value = topics.value + topic }
        override suspend fun deleteTopic(topic: Topic) { topics.value = topics.value - topic }
        override suspend fun saveSession(session: StudySession) = Unit
        override suspend fun refreshResources(): Result<Unit> = Result.success(Unit)
        override suspend fun seedIfEmpty() = Unit
    }
}
