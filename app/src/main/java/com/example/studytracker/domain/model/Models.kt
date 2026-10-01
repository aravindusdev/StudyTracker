package com.example.studytracker.domain.model

data class Topic(val id: Long = 0, val title: String, val subject: String, val description: String = "")
data class StudySession(val id: Long = 0, val topicId: Long, val topicTitle: String, val startedAt: Long, val endedAt: Long) {
    val durationMillis: Long get() = (endedAt - startedAt).coerceAtLeast(0)
}
data class StudyResource(val key: String, val title: String, val author: String)
