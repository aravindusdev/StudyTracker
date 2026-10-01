package com.example.studytracker.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.studytracker.domain.model.Topic
import com.example.studytracker.ui.*
import com.example.studytracker.ui.dashboard.DashboardScreen
import com.example.studytracker.ui.topics.TopicDetailsScreen
import com.example.studytracker.ui.topics.TopicEditorScreen
import com.example.studytracker.ui.topics.TopicsScreen
import com.example.studytracker.ui.sessions.FocusScreen
import com.example.studytracker.ui.sessions.HistoryScreen
import com.example.studytracker.ui.settings.SettingsScreen

@Composable fun StudyNav(factory: StudyViewModelFactory) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val dashboard: DashboardViewModel = viewModel(factory = factory)
    val topics: TopicsViewModel = viewModel(factory = factory)
    val sessions: SessionsViewModel = viewModel(factory = factory)
    val settings: SettingsViewModel = viewModel(factory = factory)
    val tabs = listOf(
        Triple("dashboard", "Home", Icons.Default.Home),
        Triple("topics", "Topics", Icons.AutoMirrored.Filled.List),
        Triple("history", "History", Icons.Default.History),
        Triple("settings", "Settings", Icons.Default.Tune)
    )
    val showTabs = route in tabs.map { it.first }
    fun start(topic: Topic) { sessions.start(topic); nav.navigate("focus") }
    Scaffold(bottomBar = {
        if (showTabs) NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
            tabs.forEach { (destination, label, icon) ->
                NavigationBarItem(
                    selected = route == destination,
                    onClick = { nav.navigate(destination) { popUpTo("dashboard") { saveState = true }; launchSingleTop = true; restoreState = true } },
                    icon = { Icon(icon, contentDescription = null) },
                    label = { Text(label) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }) { padding ->
        NavHost(nav, startDestination = "dashboard", modifier = Modifier.padding(padding)) {
            composable("dashboard") {
                DashboardScreen(dashboard, openTopics = { nav.navigate("topics") },
                    openHistory = { nav.navigate("history") }, openTopic = { nav.navigate("topic/$it") },
                    startTopic = ::start)
            }
            composable("topics") {
                TopicsScreen(topics, sessions, onAdd = { nav.navigate("new") },
                    onOpen = { nav.navigate("topic/$it") })
            }
            composable("new") { TopicEditorScreen(topics, 0, onBack = { nav.popBackStack() }) }
            composable("topic/{id}") { back ->
                val id = back.arguments?.getString("id")?.toLongOrNull() ?: 0L
                TopicDetailsScreen(topics, sessions, id, onBack = { nav.popBackStack() },
                    onEdit = { nav.navigate("edit/$id") }, onStart = ::start,
                    onHistory = { nav.navigate("history") })
            }
            composable("edit/{id}") { back ->
                TopicEditorScreen(topics, back.arguments?.getString("id")?.toLongOrNull() ?: 0L, onBack = { nav.popBackStack() })
            }
            composable("focus") { FocusScreen(sessions, onBack = { nav.popBackStack() }) }
            composable("history") { HistoryScreen(sessions) }
            composable("settings") { SettingsScreen(settings) }
        }
    }
}
