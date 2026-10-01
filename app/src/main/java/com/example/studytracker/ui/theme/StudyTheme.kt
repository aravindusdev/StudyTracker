package com.example.studytracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.studytracker.R

val Indigo = Color(0xFF2A14B4)
val IndigoBright = Color(0xFF4338CA)
val Mint = Color(0xFF10B981)
val Amber = Color(0xFFF59E0B)
val Ink = Color(0xFF141B2B)
val MutedInk = Color(0xFF464554)
val Canvas = Color(0xFFF9F9FF)
val SoftIndigo = Color(0xFFE9EDFF)

private val jakarta = FontFamily(Font(R.font.plus_jakarta_sans))
private val inter = FontFamily(Font(R.font.inter))

private val studyTypography = Typography(
    displayLarge = TextStyle(fontFamily = jakarta, fontSize = 48.sp, lineHeight = 56.sp, fontWeight = FontWeight.Bold),
    displayMedium = TextStyle(fontFamily = jakarta, fontSize = 40.sp, lineHeight = 48.sp, fontWeight = FontWeight.Bold),
    headlineLarge = TextStyle(fontFamily = jakarta, fontSize = 32.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontFamily = jakarta, fontSize = 26.sp, lineHeight = 34.sp, fontWeight = FontWeight.SemiBold),
    headlineSmall = TextStyle(fontFamily = jakarta, fontSize = 22.sp, lineHeight = 30.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontFamily = jakarta, fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontFamily = jakarta, fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontFamily = inter, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = inter, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = inter, fontSize = 12.sp, lineHeight = 17.sp),
    labelLarge = TextStyle(fontFamily = inter, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontFamily = inter, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold),
    labelSmall = TextStyle(fontFamily = inter, fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium)
)

private val light = lightColorScheme(
    primary = Indigo, onPrimary = Color.White, primaryContainer = Color(0xFFE3DFFF), onPrimaryContainer = Color(0xFF100069),
    secondary = Color(0xFF006C49), onSecondary = Color.White, secondaryContainer = Color(0xFF6CF8BB),
    tertiary = Color(0xFF744800), background = Canvas, onBackground = Ink, surface = Color.White, onSurface = Ink,
    surfaceVariant = SoftIndigo, onSurfaceVariant = MutedInk, outline = Color(0xFFC7C4D7), error = Color(0xFFBA1A1A)
)

private val dark = darkColorScheme(
    primary = Color(0xFF818CF8), onPrimary = Color(0xFF0B0F17), primaryContainer = Color(0xFF31336E),
    secondary = Color(0xFF34D399), tertiary = Color(0xFFFBBF24), background = Color(0xFF0B0F17),
    onBackground = Color(0xFFE2E8F0), surface = Color(0xFF182234), onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF1E293B), onSurfaceVariant = Color(0xFF94A3B8), outline = Color(0xFF334155), error = Color(0xFFF87171)
)

@Composable fun StudyTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) dark else light, typography = studyTypography, content = content)
}
