package com.example.studytracker.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studytracker.ui.SettingsViewModel
import com.example.studytracker.ui.components.*
import java.util.Locale

@Composable fun SettingsScreen(vm: SettingsViewModel) {
    val goal by vm.goal.collectAsStateWithLifecycle()
    val mode by vm.themeMode.collectAsStateWithLifecycle()
    val message by vm.message.collectAsStateWithLifecycle()
    var input by remember { mutableStateOf(goal.toString()) }
    LaunchedEffect(goal) { input = goal.toString() }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item { BrandHeader() }
        item {
            Column {
                Text("PREFERENCES", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Settings", style = MaterialTheme.typography.headlineLarge)
            }
        }
        item { Text("STUDY GOALS", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) }
        item {
            StudyCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Daily Study Target", style = MaterialTheme.typography.titleMedium)
                        Text("A pace that fits your day", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(minutesLabel(goal), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(18.dp))
                LinearProgressIndicator(progress = { (goal / 240f).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceVariant)
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = { vm.save((goal - 15).coerceAtLeast(1).toString()) }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Remove, contentDescription = null); Text("15m")
                    }
                    OutlinedButton(onClick = { vm.save((goal + 15).coerceAtMost(1440).toString()) }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Add, contentDescription = null); Text("15m")
                    }
                }
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(input, { input = it }, label = { Text("Exact minutes") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                Button(onClick = { vm.save(input) }, modifier = Modifier.fillMaxWidth()) { Text("Save daily goal") }
                if (message != null) Text(message!!, style = MaterialTheme.typography.bodySmall,
                    color = if (message!!.contains("saved", true)) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error)
            }
        }
        item { Text("APPEARANCE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) }
        item {
            StudyCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.BrightnessAuto, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(10.dp))
                    Text("Theme preference", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("system" to "System", "light" to "Light", "dark" to "Dark").forEach { (value, label) ->
                        FilterChip(selected = mode == value, onClick = { vm.setThemeMode(value) },
                            label = { Text(label) }, modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.primary))
                    }
                }
            }
        }
        item { Text("DATA & PRIVACY", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) }
        item {
            StudyCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(10.dp))
                    Text("Offline-first storage", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(8.dp))
                Text("Topics, sessions, and your daily goal are stored on this device. Optional study book suggestions are cached after a successful refresh.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item { Text("ABOUT", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) }
        item {
            StudyCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(10.dp))
                    Text("StudyFlow for Android", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(8.dp))
                Text("Native Kotlin • Jetpack Compose • Room • DataStore",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("A local study planner with optional Open Library reading suggestions.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
