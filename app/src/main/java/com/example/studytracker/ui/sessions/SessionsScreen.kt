package com.example.studytracker.ui.sessions

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studytracker.domain.model.StudySession
import com.example.studytracker.ui.SessionsViewModel
import com.example.studytracker.ui.components.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable fun HistoryScreen(vm: SessionsViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val now = LocalDate.now()
    val first = now.minusDays((now.dayOfWeek.value - 1).toLong())
    val weekly = state.sessions.filter { sessionDate(it) in first..now }
    val daily = (0..6).map { index -> weekly.filter { sessionDate(it) == first.plusDays(index.toLong()) }.sumOf { it.durationMillis }.div(60_000).toInt() }
    val grouped = state.sessions.groupBy { sessionDate(it) }.toSortedMap(reverseOrder())
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item { BrandHeader() }
        item {
            Column {
                Text("Study History", style = MaterialTheme.typography.headlineLarge)
                Text("Your focus log and weekly rhythm", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (state.loading) { item { CircularProgressIndicator() }; return@LazyColumn }
        item {
            StudyCard(Modifier.fillMaxWidth()) {
                Text("WEEKLY VOLUME", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Text(minutesLabel(vm.weekMinutes(state.sessions)), style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricTile("Daily average", minutesLabel(daily.sum() / 7), Modifier.weight(1f))
                    MetricTile("Sessions", weekly.size.toString(), Modifier.weight(1f))
                }
            }
        }
        item {
            StudyCard(Modifier.fillMaxWidth()) {
                Text("Daily Distribution", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(18.dp))
                val max = (daily.maxOrNull() ?: 0).coerceAtLeast(60)
                Row(Modifier.fillMaxWidth().height(142.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
                    daily.forEachIndexed { index, value ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(if (value == 0) "—" else minutesLabel(value), style = MaterialTheme.typography.labelSmall)
                            Box(Modifier.width(27.dp).height((9 + 80 * value / max).dp)
                                .background(if (value == 0) MaterialTheme.colorScheme.surfaceVariant else if (first.plusDays(index.toLong()) == now) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                    RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp)))
                            Text(first.plusDays(index.toLong()).dayOfWeek.name.take(1), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
        if (grouped.isEmpty()) item { EmptyPanel("No sessions yet", "Finish a study timer to build your focus history.") }
        grouped.forEach { (date, sessions) ->
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(date.format(DateTimeFormatter.ofPattern("EEEE, MMM d")), style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    Text(minutesLabel(sessions.sumOf { it.durationMillis }.div(60_000).toInt()),
                        style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
            items(sessions, key = { it.id }) { session ->
                StudyCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                        Spacer(Modifier.width(10.dp))
                        Text(session.topicTitle, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f),
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(durationLabel(session.durationMillis),
                            style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(dateLabel(session.startedAt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun FocusScreen(vm: SessionsViewModel, onBack: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    var showEndSheet by remember { mutableStateOf(false) }
    BackHandler(state.activeTopic != null) { showEndSheet = true }
    val topic = state.activeTopic
    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { if (topic == null) onBack() else showEndSheet = true }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Active Focus Session", style = MaterialTheme.typography.titleLarge)
        }
        Spacer(Modifier.height(30.dp))
        if (topic == null) {
            EmptyPanel("No timer running", "Choose a topic to begin a focus session.", "Back to topics", onBack)
        } else {
            SubjectBadge(topic.subject)
            Spacer(Modifier.height(14.dp))
            Text(topic.title, style = MaterialTheme.typography.headlineMedium)
            Text("Count-up timer • 50 min suggested block", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.weight(.5f))
            Box(Modifier.size(280.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(progress = { (state.elapsedMillis / 3_000_000f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxSize(), strokeWidth = 14.dp,
                    color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceVariant)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("DEEP FOCUS ACTIVE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                    Text("%02d:%02d".format(state.elapsedMillis / 60_000, state.elapsedMillis / 1_000 % 60),
                        style = MaterialTheme.typography.displayLarge)
                    Text("elapsed", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.weight(.5f))
            StudyCard(Modifier.fillMaxWidth()) {
                Text("Stay with one thing", style = MaterialTheme.typography.titleMedium)
                Text("Your time is saved only when you end and log this session.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(20.dp))
            Button(onClick = { showEndSheet = true }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Icon(Icons.Default.Stop, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("End Session")
            }
            Spacer(Modifier.height(24.dp))
        }
    }
    if (showEndSheet && topic != null) {
        ModalBottomSheet(onDismissRequest = { showEndSheet = false }) {
            Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("End session?", style = MaterialTheme.typography.headlineMedium)
                Text("You have focused for " + durationLabel(state.elapsedMillis) + ". Save this time to your study history, resume, or discard it.",
                    style = MaterialTheme.typography.bodyLarge)
                Button(onClick = { vm.stop(); showEndSheet = false; onBack() },
                    enabled = state.elapsedMillis >= 1_000, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Save and log session")
                }
                OutlinedButton(onClick = { showEndSheet = false }, modifier = Modifier.fillMaxWidth()) { Text("Resume session") }
                TextButton(onClick = { vm.discard(); showEndSheet = false; onBack() }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Text("Discard session", color = MaterialTheme.colorScheme.error)
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

private fun sessionDate(session: StudySession): LocalDate = Instant.ofEpochMilli(session.startedAt).atZone(ZoneId.systemDefault()).toLocalDate()
