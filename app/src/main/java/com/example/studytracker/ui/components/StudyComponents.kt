package com.example.studytracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.studytracker.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable fun BrandHeader(subtitle: String? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.size(34.dp).background(IndigoBright, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.AutoStories, contentDescription = null, tint = Color.White, modifier = Modifier.size(21.dp))
        }
        Column(Modifier.weight(1f)) {
            Text("StudyFlow", style = MaterialTheme.typography.titleLarge)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Box(Modifier.size(34.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.School, contentDescription = "Study Tracker", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(19.dp))
        }
    }
}

@Composable fun StudyCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier = modifier, shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .24f)), shadowElevation = 1.dp) {
        Column(Modifier.padding(18.dp), content = content)
    }
}

@Composable fun SectionHeader(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        if (action != null && onAction != null) TextButton(onClick = onAction) { Text(action) }
    }
}

@Composable fun SubjectBadge(subject: String) {
    val tint = subjectColor(subject)
    Text(subject, style = MaterialTheme.typography.labelMedium, color = tint,
        modifier = Modifier.background(tint.copy(alpha = .12f), RoundedCornerShape(7.dp)).padding(horizontal = 9.dp, vertical = 5.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
}

@Composable fun EmptyPanel(title: String, body: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .58f)) {
        Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(68.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.AutoStories, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(34.dp))
            }
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (action != null && onAction != null) Button(onClick = onAction) { Text(action) }
        }
    }
}

@Composable fun MetricTile(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .52f)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

fun subjectColor(subject: String): Color = when {
    subject.contains("bio", true) || subject.contains("science", true) -> Color(0xFF00714D)
    subject.contains("math", true) -> Color(0xFF555D7A)
    subject.contains("computer", true) || subject.contains("program", true) -> Color(0xFF965500)
    else -> IndigoBright
}

fun minutesLabel(minutes: Int): String = if (minutes >= 60) "${minutes / 60}h ${minutes % 60}m" else "${minutes}m"
fun durationLabel(millis: Long): String = if (millis < 60_000) "${millis.coerceAtLeast(0) / 1_000}s" else minutesLabel((millis / 60_000).toInt())
fun dateLabel(epochMillis: Long): String = DateTimeFormatter.ofPattern("MMM d, h:mm a").format(Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()))
