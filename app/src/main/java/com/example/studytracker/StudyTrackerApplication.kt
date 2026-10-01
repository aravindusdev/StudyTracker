package com.example.studytracker

import android.app.Application
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.example.studytracker.data.local.StudyDatabase
import com.example.studytracker.data.remote.OpenLibraryDataSource
import com.example.studytracker.data.remote.OpenLibraryService
import com.example.studytracker.data.repository.DataStoreGoalRepository
import com.example.studytracker.data.repository.RoomStudyRepository
import com.example.studytracker.domain.usecase.StudySummary
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

private val Application.goalStore by preferencesDataStore(name = "settings")

class StudyTrackerApplication : Application() {
    val container by lazy {
        val db = Room.databaseBuilder(this, StudyDatabase::class.java, "study-tracker.db").build()
        val client = OkHttpClient.Builder().callTimeout(10, TimeUnit.SECONDS).build()
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val api = Retrofit.Builder().baseUrl("https://openlibrary.org/").client(client).addConverterFactory(MoshiConverterFactory.create(moshi)).build().create(OpenLibraryService::class.java)
        AppContainer(RoomStudyRepository(db, OpenLibraryDataSource(api)), DataStoreGoalRepository(goalStore), StudySummary())
    }
}

data class AppContainer(
    val study: RoomStudyRepository,
    val goals: DataStoreGoalRepository,
    val summary: StudySummary
)
