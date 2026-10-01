package com.example.studytracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studytracker.ui.StudyViewModelFactory
import com.example.studytracker.ui.navigation.StudyNav
import com.example.studytracker.ui.theme.StudyTheme
import com.example.studytracker.ui.theme.Canvas
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = StudyViewModelFactory((application as StudyTrackerApplication).container)
        setContent {
            val mode by (application as StudyTrackerApplication).container.goals.themeMode.collectAsStateWithLifecycle(initialValue = "system")
            val dark = mode == "dark" || (mode == "system" && isSystemInDarkTheme())
            SideEffect {
                window.statusBarColor = (if (dark) Color(0xFF0B0F17) else Canvas).toArgb()
                window.navigationBarColor = (if (dark) Color(0xFF0B0F17) else Canvas).toArgb()
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !dark
                    isAppearanceLightNavigationBars = !dark
                }
            }
            StudyTheme(darkTheme = dark) { StudyNav(factory) }
        }
    }
}
