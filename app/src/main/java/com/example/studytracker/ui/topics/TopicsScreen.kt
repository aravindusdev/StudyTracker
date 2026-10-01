package com.example.studytracker.ui.topics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studytracker.domain.model.Topic
import com.example.studytracker.ui.TopicsViewModel
import com.example.studytracker.ui.SessionsViewModel
import com.example.studytracker.ui.components.*
import kotlinx.coroutines.flow.map

private data class LoadedTopic(val value: Topic?)

@Composable fun TopicsScreen(vm: TopicsViewModel, sessionsVm: SessionsViewModel, onAdd: () -> Unit, onOpen: (Long) -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val sessionsState by sessionsVm.state.collectAsStateWithLifecycle()
    var selectedSubject by remember { mutableStateOf("All") }
    var deleteTarget by remember { mutableStateOf<Topic?>(null) }
    val subjects = listOf("All") + state.topics.map { it.subject }.distinct().sorted()
    val shown = state.topics.filter { selectedSubject == "All" || it.subject == selectedSubject }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { BrandHeader() }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Saved on this device", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(1f))
                Button(onClick = onAdd) { Icon(Icons.Default.Add, contentDescription = null); Text("New Topic") }
            }
        }
        item { OutlinedTextField(state.query, vm::search, label = { Text("Search topics or subjects") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(subjects) { subject -> FilterChip(selected = selectedSubject == subject, onClick = { selectedSubject = subject },
                    label = { Text(subject) }, colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.primary)) }
            }
        }
        item { SectionHeader("Focus Topics") }
        if (state.loading) item { CircularProgressIndicator() }
        else if (shown.isEmpty()) item {
            EmptyPanel(if (state.query.isBlank() && selectedSubject == "All") "No topics created yet" else "No matching topics",
                if (state.query.isBlank() && selectedSubject == "All") "Organize your subjects into study topics." else "Try another search or category.",
                if (state.query.isBlank() && selectedSubject == "All") "Create your first topic" else null, if (state.query.isBlank() && selectedSubject == "All") onAdd else null)
        }
        items(shown, key = { it.id }) { topic ->
            var expanded by remember(topic.id) { mutableStateOf(false) }
            val topicSessions = sessionsState.sessions.filter { it.topicId == topic.id }
            StudyCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SubjectBadge(topic.subject)
                    Spacer(Modifier.weight(1f))
                    Box {
                        IconButton(onClick = { expanded = true }) { Icon(Icons.Default.MoreVert, contentDescription = "Options for " + topic.title) }
                        DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(text = { Text("Open") }, onClick = { expanded = false; onOpen(topic.id) })
                            DropdownMenuItem(text = { Text("Delete") }, onClick = { expanded = false; deleteTarget = topic })
                        }
                    }
                }
                Text(topic.title, style = MaterialTheme.typography.titleLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (topic.description.isNotBlank()) Text(topic.description, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = .25f))
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(minutesLabel(topicSessions.sumOf { it.durationMillis }.div(60_000).toInt()) + " logged",
                        style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    TextButton(onClick = { onOpen(topic.id) }) { Text("Details") }
                }
            }
        }
    }
    deleteTarget?.let { topic ->
        AlertDialog(onDismissRequest = { deleteTarget = null }, title = { Text("Delete " + topic.title + "?") },
            text = { Text("Completed sessions will stay in your history.") },
            confirmButton = { TextButton(onClick = { vm.delete(topic); deleteTarget = null }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancel") } })
    }
}

@Composable fun TopicDetailsScreen(
    vm: TopicsViewModel, sessionsVm: SessionsViewModel, id: Long,
    onBack: () -> Unit, onEdit: () -> Unit, onStart: (Topic) -> Unit, onHistory: () -> Unit
) {
    val loaded by remember(id) { vm.topic(id).map(::LoadedTopic) }.collectAsStateWithLifecycle(initialValue = null)
    val sessionsState by sessionsVm.state.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }
    val item = loaded?.value
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                Text("Topic Details", style = MaterialTheme.typography.titleLarge)
            }
        }
        if (loaded == null) { item { CircularProgressIndicator() }; return@LazyColumn }
        if (item == null) { item { Text("Topic unavailable or deleted.") }; return@LazyColumn }
        item {
            StudyCard(Modifier.fillMaxWidth()) {
                SubjectBadge(item.subject)
                Spacer(Modifier.height(14.dp))
                Text(item.title, style = MaterialTheme.typography.headlineLarge)
                if (item.description.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Text(item.description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        val related = sessionsState.sessions.filter { it.topicId == id }
        val minutes = related.sumOf { it.durationMillis }.div(60_000).toInt()
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricTile("Total time", durationLabel(related.sumOf { it.durationMillis }), Modifier.weight(1f))
                MetricTile("Sessions", related.size.toString(), Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricTile("Avg session", if (related.isEmpty()) "—" else durationLabel(related.sumOf { it.durationMillis } / related.size), Modifier.weight(1f))
                MetricTile("Latest", if (related.isEmpty()) "—" else dateLabel(related.first().startedAt).substringBefore(","), Modifier.weight(1f))
            }
        }
        item {
            StudyCard(Modifier.fillMaxWidth()) {
                Text("Ready to focus?", style = MaterialTheme.typography.titleLarge)
                Text("Start a session for this topic. Time is saved when you finish.", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))
                Button(onClick = { onStart(item) }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null); Text("Start Study Session")
                }
            }
        }
        item { SectionHeader("Recent Sessions", "View all", onHistory) }
        if (related.isEmpty()) item { EmptyPanel("No sessions yet", "Your completed sessions will appear here.") }
        items(related.take(3), key = { it.id }) { session ->
            StudyCard(Modifier.fillMaxWidth()) {
                Text(durationLabel(session.durationMillis) + " focus session", style = MaterialTheme.typography.titleMedium)
                Text(dateLabel(session.startedAt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f)) { Text("Edit Topic") }
                TextButton(onClick = { confirmDelete = true }, modifier = Modifier.weight(1f)) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            }
        }
    }
    if (confirmDelete) AlertDialog(onDismissRequest = { confirmDelete = false }, title = { Text("Delete topic?") },
        text = { Text("Completed sessions remain in history.") },
        confirmButton = { TextButton(onClick = { vm.delete(item!!); confirmDelete = false; onBack() }) { Text("Delete") } },
        dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } })
}

@Composable fun TopicEditorScreen(vm: TopicsViewModel, id: Long, onBack: () -> Unit) {
    val existing by remember(id) { if (id == 0L) kotlinx.coroutines.flow.flowOf(null) else vm.topic(id) }.collectAsStateWithLifecycle(initialValue = null)
    var title by remember(id) { mutableStateOf("") }
    var subject by remember(id) { mutableStateOf("") }
    var description by remember(id) { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(existing?.id) { existing?.let { title = it.title; subject = it.subject; description = it.description } }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
            Text(if (id == 0L) "New Topic" else "Edit Topic", style = MaterialTheme.typography.titleLarge)
        }
        StudyCard(Modifier.fillMaxWidth()) {
            Text("Topic details", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(title, { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(subject, { subject = it }, label = { Text("Subject or category") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(description, { description = it }, label = { Text("Description (optional)") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(16.dp))
            Button(onClick = {
                if (title.isBlank() || subject.isBlank()) error = "Title and subject are required"
                else vm.save(id, title, subject, description, onBack)
            }, enabled = id == 0L || existing != null, modifier = Modifier.fillMaxWidth()) { Text("Save Topic") }
        }
    }
}
