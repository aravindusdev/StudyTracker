package com.example.studytracker.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.content.Context
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TopicDaoTest {
    @Test fun insertAndObserveTopic() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(), StudyDatabase::class.java).build()
        try {
            db.topics().insert(TopicEntity(title = "Math", subject = "Science", description = "Algebra"))
            assertEquals("Math", db.topics().observeAll().first().single().title)
        } finally { db.close() }
    }
}
