package com.example.studytracker.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studytracker.domain.model.StudySession
import com.example.studytracker.domain.model.Topic
import com.example.studytracker.ui.DashboardViewModel
import com.example.studytracker.ui.components.*
import com.example.studytracker.ui.theme.Indigo
import com.example.studytracker.ui.theme.Mint
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable fun DashboardScreen(
    vm: DashboardViewModel,
    openTopics: () -> Unit,
    openHistory: () -> Unit,
    openTopic: (Long) -> Unit,
    startTopic: (Topic) -> Unit
) {
    val state by vm.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.seed() }
    val today = vm.minutesToday(state.sessions)
    val todayDate = LocalDate.now()
    val dark = MaterialTheme.colorScheme.background == Color(0xFF0B0F17)
    val heroText = if (dark) MaterialTheme.colorScheme.onSurface else Color.White
    val heroMuted = if (dark) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFFD8D4FF)
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item { BrandHeader() }
        item {
            Column {
                Text(todayDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d")).uppercase(),
                    style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(when (LocalTime.now().hour) { in 5..11 -> "Good morning"; in 12..17 -> "Good afternoon"; else -> "Good evening" },
                    style = MaterialTheme.typography.headlineLarge)
            }
        }
        if (state.loading) { item { CircularProgressIndicator() }; return@LazyColumn }
        item {
            Surface(shape = RoundedCornerShape(26.dp), color = if (dark) MaterialTheme.colorScheme.surface else Indigo, shadowElevation = if (dark) 0.dp else 5.dp) {
                Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(8.dp).background(Mint, CircleShape))
                        Spacer(Modifier.width(8.dp))
                        Text("DAILY OBJECTIVE", style = MaterialTheme.typography.labelMedium, color = heroMuted, modifier = Modifier.weight(1f))
                        Text("Target: " + minutesLabel(state.goal), style = MaterialTheme.typography.labelMedium, color = heroText)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(minutesLabel(today), style = MaterialTheme.typography.headlineLarge, color = heroText)
                            Text(if (today >= state.goal) "Daily goal reached" else minutesLabel(state.goal - today) + " left to reach today's goal",
                                style = MaterialTheme.typography.bodyMedium, color = heroMuted)
                        }
                        Box(Modifier.size(82.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(progress = { (today.toFloat() / state.goal).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxSize(), color = if (dark) MaterialTheme.colorScheme.primary else Color(0xFF6CF8BB), trackColor = heroText.copy(alpha = .18f), strokeWidth = 8.dp)
                            Text((today * 100 / state.goal).coerceAtMost(100).toString() + "%", style = MaterialTheme.typography.titleMedium, color = heroText)
                        }
                    }
                    Button(onClick = { state.topics.firstOrNull()?.let(startTopic) ?: openTopics() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (dark) MaterialTheme.colorScheme.primary else Color.White, contentColor = if (dark) MaterialTheme.colorScheme.onPrimary else Indigo),
                        shape = RoundedCornerShape(14.dp)) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Start Quick Session")
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricTile("Studied today", minutesLabel(today), Modifier.weight(1f))
                MetricTile("Sessions today", state.sessions.count { sessionDate(it) == todayDate }.toString(), Modifier.weight(1f))
                MetricTile("This week", minutesLabel(weekMinutes(state.sessions)), Modifier.weight(1f))
            }
        }
        item { WeeklyRhythm(state.sessions, openHistory) }
        item { SectionHeader("Recent Topics", "See all", openTopics) }
        val recent = (state.sessions.mapNotNull { session -> state.topics.firstOrNull { it.id == session.topicId } } + state.topics).distinctBy { it.id }.take(3)
        if (recent.isEmpty()) item {
            EmptyPanel("Your study plan starts here", "Create a topic to organize your next focus session.", "Create a topic", openTopics)
        }
        items(recent, key = { it.id }) { topic ->
            StudyCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SubjectBadge(topic.subject)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { startTopic(topic) }) { Icon(Icons.Default.PlayArrow, contentDescription = "Study " + topic.title) }
                }
                Spacer(Modifier.height(8.dp))
                Text(topic.title, style = MaterialTheme.typography.titleLarge)
                val minutes = state.sessions.filter { it.topicId == topic.id }.sumOf { it.durationMillis }.div(60_000).toInt()
                Text(if (minutes == 0) "Ready for your first session" else minutesLabel(minutes) + " studied",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = { openTopic(topic.id) }, contentPadding = PaddingValues(0.dp)) { Text("View topic") }
            }
        }
        item { SectionHeader("Study Reading") }
        if (state.error != null) item { Text(state.error!!, color = MaterialTheme.colorScheme.error) }
        if (state.resources.isEmpty()) item {
            StudyCard {
                Text("No books cached yet", style = MaterialTheme.typography.titleMedium)
                Text("Refresh when online to save study skills books for later.", style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = vm::refresh, enabled = !state.refreshing) { Text(if (state.refreshing) "Refreshing…" else "Refresh books") }
            }
        } else {
            items(state.resources.take(3), key = { it.key }) { book ->
                StudyCard(Modifier.fillMaxWidth()) {
                    Text(book.title, style = MaterialTheme.typography.titleMedium)
                    Text(book.author, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            item { TextButton(onClick = vm::refresh, enabled = !state.refreshing) { Text(if (state.refreshing) "Refreshing…" else "Refresh books") } }
        }
        item { Text("Study data saved locally on this device", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable private fun WeeklyRhythm(sessions: List<StudySession>, openHistory: () -> Unit) {
    val now = LocalDate.now()
    val first = now.minusDays((now.dayOfWeek.value - 1).toLong())
    val daily = (0..6).map { offset -> sessions.filter { sessionDate(it) == first.plusDays(offset.toLong()) }.sumOf { it.durationMillis }.div(60_000).toInt() }
    val max = (daily.maxOrNull() ?: 0).coerceAtLeast(60)
    StudyCard(Modifier.fillMaxWidth()) {
        SectionHeader("Weekly Rhythm", "Details", openHistory)
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth().height(110.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
            daily.forEachIndexed { index, minutes ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(if (minutes == 0) "—" else minutesLabel(minutes), style = MaterialTheme.typography.labelSmall)
                    Box(Modifier.width(22.dp).height((8 + 60 * minutes / max).dp)
                        .background(if (first.plusDays(index.toLong()) == now) MaterialTheme.colorScheme.primary else if (minutes > 0) Mint else MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp)))
                    Text(first.plusDays(index.toLong()).dayOfWeek.name.take(1), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

private fun sessionDate(session: StudySession): LocalDate = Instant.ofEpochMilli(session.startedAt).atZone(ZoneId.systemDefault()).toLocalDate()
private fun weekMinutes(sessions: List<StudySession>): Int {
    val now = LocalDate.now()
    val first = now.minusDays((now.dayOfWeek.value - 1).toLong())
    return sessions.filter { sessionDate(it) in first..now }.sumOf { it.durationMillis }.div(60_000).toInt()
}
