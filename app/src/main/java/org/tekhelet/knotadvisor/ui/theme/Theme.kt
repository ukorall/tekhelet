package org.tekhelet.knotadvisor.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// פלטת צבעים בהשראת תכלת - כחול עמוק כצבע ראשי, לבן/קרם כרקע.
private val TekhletBlue = Color(0xFF1B3A6B)
private val TekhletBlueLight = Color(0xFF3E5C8A)
private val SandBackground = Color(0xFFFBF8F2)
private val SandBackgroundDark = Color(0xFF14161A)

private val LightColors = lightColorScheme(
    primary = TekhletBlue,
    secondary = TekhletBlueLight,
    background = SandBackground,
    surface = SandBackground
)

private val DarkColors = darkColorScheme(
    primary = TekhletBlueLight,
    secondary = TekhletBlue,
    background = SandBackgroundDark,
    surface = SandBackgroundDark
)

@Composable
fun KnotAdvisorTheme(useDarkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (useDarkTheme) DarkColors else LightColors,
        content = content
    )
}
