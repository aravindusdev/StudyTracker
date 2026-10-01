package com.example.studytracker.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.room.withTransaction
import com.example.studytracker.data.local.*
import com.example.studytracker.data.remote.OpenLibraryDataSource
import com.example.studytracker.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.CancellationException

interface StudyRepository {
    val topics: Flow<List<Topic>>
    val sessions: Flow<List<StudySession>>
    val resources: Flow<List<StudyResource>>
    fun topic(id: Long): Flow<Topic?>
    suspend fun saveTopic(topic: Topic)
    suspend fun deleteTopic(topic: Topic)
    suspend fun saveSession(session: StudySession)
    suspend fun refreshResources(): Result<Unit>
    suspend fun seedIfEmpty()
}

class RoomStudyRepository(
    private val db: StudyDatabase,
    private val remote: OpenLibraryDataSource
) : StudyRepository {
    override val topics = db.topics().observeAll().map { rows -> rows.map { it.toModel() } }
    override val sessions = db.sessions().observeAll().map { rows ->
        rows.map {
            StudySession(
                it.id,
                it.topicId,
                it.topicTitle,
                it.startedAt,
                it.endedAt
            )
        }
    }
    override val resources = db.resources().observeAll()
        .map { rows -> rows.map { StudyResource(it.key, it.title, it.author) } }

    override fun topic(id: Long) = db.topics().observe(id).map { it?.toModel() }
    override suspend fun saveTopic(topic: Topic) {
        val entity = TopicEntity(
            topic.id,
            topic.title.trim(),
            topic.subject.trim(),
            topic.description.trim()
        )
        if (topic.id == 0L) db.topics().insert(entity) else db.topics().update(entity)
    }

    override suspend fun deleteTopic(topic: Topic) =
        db.topics().delete(TopicEntity(topic.id, topic.title, topic.subject, topic.description))

    override suspend fun saveSession(session: StudySession) = db.sessions().insert(
        SessionEntity(
            session.id,
            session.topicId,
            session.topicTitle,
            session.startedAt,
            session.endedAt
        )
    )

    override suspend fun refreshResources(): Result<Unit> = try {
        val books = remote.fetchBooks().mapNotNull { dto ->
            val key = dto.key ?: return@mapNotNull null
            val title = dto.title ?: return@mapNotNull null
            ResourceEntity(key, title, dto.author_name?.firstOrNull() ?: "Unknown author")
        }
        if (books.isEmpty()) error("No books were returned")
        db.resources().insertAll(books)
        Result.success(Unit)
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (failure: Exception) {
        Result.failure(failure)
    }

    override suspend fun seedIfEmpty() {
        db.withTransaction {
            if (db.metadata().count("seeded") == 0) {
                for (topic in listOf(
                    Topic(
                        title = "Kotlin fundamentals",
                        subject = "Programming",
                        description = "Syntax, functions, and collections"
                    ),
                    Topic(
                        title = "Biology revision",
                        subject = "Science",
                        description = "Cells and genetics"
                    ),
                    Topic(
                        title = "Spanish vocabulary",
                        subject = "Languages",
                        description = "Practice 20 new words"
                    )
                )) saveTopic(topic)
                db.metadata().insert(MetadataEntity("seeded"))
            }
        }
    }

    private fun TopicEntity.toModel() = Topic(id, title, subject, description)
}

interface GoalRepository {
    val dailyGoalMinutes: Flow<Int>
    val themeMode: Flow<String>
    suspend fun setDailyGoal(minutes: Int)
    suspend fun setThemeMode(mode: String)
}

class DataStoreGoalRepository(private val store: DataStore<Preferences>) : GoalRepository {
    private val key = intPreferencesKey("daily_goal_minutes")
    private val themeKey = stringPreferencesKey("theme_mode")
    override val dailyGoalMinutes = store.data.map { it[key] ?: 60 }
    override val themeMode = store.data.map { it[themeKey] ?: "system" }
    override suspend fun setDailyGoal(minutes: Int) {
        store.edit { it[key] = minutes.coerceIn(1, 1440) }
    }
    override suspend fun setThemeMode(mode: String) {
        if (mode in setOf("system", "light", "dark")) store.edit { it[themeKey] = mode }
    }
}
