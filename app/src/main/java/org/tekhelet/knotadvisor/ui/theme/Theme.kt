package org.tekhelet.knotadvisor.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// פלטת צבעים בהשראת תכלת: כל הגוונים השולטים נעים על סקאלה אחת בין כחול ללבן,
// בלי גוונים "זרים" לסקאלה (לא חום/קרם/אפור ניטרלי) - גם בערכת הכהה. הגוון הראשי
// כוון לפי תצלומי חוט תכלת אמיתי (פתיל תכלת) - כחול-פלדה בינוני, לא נייבי כהה מדי.
private val TekhletBlue900 = Color(0xFF0D2A48) // הכי כהה - עוגן ניגודיות
private val TekhletBlue700 = Color(0xFF2F5C88) // ראשי - קרוב לגוון החוט בתצלומים
private val TekhletBlue500 = Color(0xFF4C7BAA)
private val TekhletBlue300 = Color(0xFF8AACCE)
private val TekhletBlue100 = Color(0xFFD2E1F0)
private val TekhletBlue50 = Color(0xFFEFF5FB) // כמעט לבן, עדיין עם גוון כחול עדין
private val TekhletWhite = Color(0xFFFFFFFF)

private val LightColors = lightColorScheme(
    primary = TekhletBlue700,
    onPrimary = TekhletWhite,
    primaryContainer = TekhletBlue100,
    onPrimaryContainer = TekhletBlue900,
    secondary = TekhletBlue500,
    onSecondary = TekhletWhite,
    secondaryContainer = TekhletBlue50,
    onSecondaryContainer = TekhletBlue900,
    tertiary = TekhletBlue300,
    onTertiary = TekhletBlue900,
    tertiaryContainer = TekhletBlue50,
    onTertiaryContainer = TekhletBlue900,
    background = TekhletBlue50,
    onBackground = TekhletBlue900,
    surface = TekhletWhite,
    onSurface = TekhletBlue900,
    surfaceVariant = TekhletBlue100,
    onSurfaceVariant = TekhletBlue700,
    outline = TekhletBlue300
)

private val DarkColors = darkColorScheme(
    primary = TekhletBlue300,
    onPrimary = TekhletBlue900,
    primaryContainer = TekhletBlue700,
    onPrimaryContainer = TekhletBlue50,
    secondary = TekhletBlue100,
    onSecondary = TekhletBlue900,
    secondaryContainer = TekhletBlue500,
    onSecondaryContainer = TekhletBlue50,
    tertiary = TekhletBlue100,
    onTertiary = TekhletBlue900,
    background = Color(0xFF0A1830),
    onBackground = TekhletBlue50,
    surface = Color(0xFF0F2140),
    onSurface = TekhletBlue50,
    surfaceVariant = TekhletBlue700,
    onSurfaceVariant = TekhletBlue100,
    outline = TekhletBlue500
)

// פינות מעוגלות רכות יותר מברירת המחדל - בהשראת המרקם הרך של הבד והחוט בתצלומים.
private val KnotShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun KnotAdvisorTheme(useDarkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (useDarkTheme) DarkColors else LightColors,
        shapes = KnotShapes,
        content = content
    )
}
