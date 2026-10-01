package com.example.studytracker.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "topics")
data class TopicEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subject: String,
    val description: String
)

// Keep session history even if its topic is later deleted; topicTitle is a snapshot.
@Entity(tableName = "sessions", indices = [Index("topicId")])
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val topicId: Long,
    val topicTitle: String,
    val startedAt: Long,
    val endedAt: Long
)

@Entity(tableName = "resources")
data class ResourceEntity(@PrimaryKey val key: String, val title: String, val author: String)

@Entity(tableName = "app_metadata")
data class MetadataEntity(@PrimaryKey val key: String)

@Dao
interface MetadataDao {
    @Query("SELECT COUNT(*) FROM app_metadata WHERE `key` = :key")
    suspend fun count(key: String): Int

    @Insert
    suspend fun insert(value: MetadataEntity)
}

@Dao
interface TopicDao {
    @Query("SELECT * FROM topics ORDER BY title COLLATE NOCASE")
    fun observeAll(): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE id = :id")
    fun observe(id: Long): Flow<TopicEntity?>

    @Query("SELECT COUNT(*) FROM topics")
    suspend fun count(): Int

    @Insert
    suspend fun insert(topic: TopicEntity): Long

    @Update
    suspend fun update(topic: TopicEntity)

    @Delete
    suspend fun delete(topic: TopicEntity)
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions ORDER BY startedAt DESC")
    fun observeAll(): Flow<List<SessionEntity>>

    @Insert
    suspend fun insert(session: SessionEntity)
}

@Dao
interface ResourceDao {
    @Query("SELECT * FROM resources ORDER BY title")
    fun observeAll(): Flow<List<ResourceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(resources: List<ResourceEntity>)
}

@Database(
    entities = [TopicEntity::class, SessionEntity::class, ResourceEntity::class, MetadataEntity::class],
    version = 1,
    exportSchema = false
)
abstract class StudyDatabase : RoomDatabase() {
    abstract fun topics(): TopicDao
    abstract fun sessions(): SessionDao
    abstract fun resources(): ResourceDao
    abstract fun metadata(): MetadataDao
}
