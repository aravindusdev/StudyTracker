package com.example.studytracker

import com.example.studytracker.domain.model.StudySession
import com.example.studytracker.domain.usecase.StudySummary
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class StudySummaryTest {
    @Test fun countsOnlyCurrentDay() {
        val zone = ZoneId.of("UTC")
        val today = LocalDate.of(2026, 9, 28)
        val start = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val sessions = listOf(StudySession(1, 1, "Kotlin", start, start + 30 * 60_000), StudySession(2, 1, "Kotlin", start - 86_400_000, start - 56_400_000))
        assertEquals(30, StudySummary().minutesToday(sessions, today, zone))
        assertEquals(30, StudySummary().minutesThisWeek(sessions, today, zone))
    }
}
