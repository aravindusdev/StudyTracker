package com.example.studytracker.domain.usecase

import com.example.studytracker.domain.model.StudySession
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class StudySummary {
    fun minutesToday(sessions: List<StudySession>, now: LocalDate = LocalDate.now(), zone: ZoneId = ZoneId.systemDefault()): Int =
        sessions.filter { Instant.ofEpochMilli(it.startedAt).atZone(zone).toLocalDate() == now }.sumOf { it.durationMillis }.div(60_000).toInt()

    fun minutesThisWeek(sessions: List<StudySession>, now: LocalDate = LocalDate.now(), zone: ZoneId = ZoneId.systemDefault()): Int {
        val first = now.minusDays((now.dayOfWeek.value - 1).toLong())
        return sessions.filter { Instant.ofEpochMilli(it.startedAt).atZone(zone).toLocalDate() in first..now }.sumOf { it.durationMillis }.div(60_000).toInt()
    }
}
