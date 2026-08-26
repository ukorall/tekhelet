package org.tekhelet.knotadvisor.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.tekhelet.knotadvisor.model.AppMode

// פלטת צבעים בהשראת תכלת: כל הגוונים השולטים נעים על סקאלה אחת בין כחול ללבן,
// בלי גוונים "זרים" לסקאלה (לא חום/קרם/אפור ניטרלי) - גם בערכת הכהה. הגוון הראשי
// כוון לפי תצלומי חוט תכלת אמיתי (פתיל תכלת) - כחול-פלדה בינוני, לא נייבי כהה מדי.
//
// הפלטה עצמה לא השתנתה ברענון העיצובי. מה שהשתנה הוא הטיפוגרפיה, הצורות,
// ואוצר המילים של הרכיבים (ראו ui/components/Page.kt).
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
    // הרקע הוא ה"נייר" - לבן עם גוון כחול עדין; המשטחים עצמם לבנים.
    background = TekhletBlue50,
    onBackground = TekhletBlue900,
    surface = TekhletWhite,
    onSurface = TekhletBlue900,
    surfaceVariant = TekhletBlue100,
    onSurfaceVariant = TekhletBlue700,
    outline = TekhletBlue300,
    outlineVariant = TekhletBlue100
)

/**
 * המצב המבצעי.
 *
 * זו לא "ערכה כהה" גנרית: הלבן הופך לירוק זית כהה, התכלת הבהירה לירוק זית
 * בהיר, והטקסטים ללבן. התכלת עצמה נשארת - אבל בממשק היא מוצגת בגוון בהיר יותר
 * (TekhletBlue300), אחרת טקסט כחול כהה על זית כהה לא נקרא. גוון **החוט**
 * נשמר במדויק ב-OperationalPalette ולא כאן. ראו Palette.kt.
 */
private val OperationalColors = darkColorScheme(
    primary = TekhletBlue300,
    onPrimary = OliveDeep,
    primaryContainer = TekhletBlue700,
    onPrimaryContainer = OliveText,
    secondary = OliveLight,
    onSecondary = OliveDeep,
    secondaryContainer = OliveDark,
    onSecondaryContainer = OliveText,
    tertiary = OliveLight,
    onTertiary = OliveDeep,
    tertiaryContainer = OliveDark,
    onTertiaryContainer = OliveText,
    background = OliveDeep,
    onBackground = OliveText,
    surface = OliveSurface,
    onSurface = OliveText,
    surfaceVariant = OliveDark,
    onSurfaceVariant = OliveLight,
    outline = OliveMid,
    outlineVariant = OliveDark,
    error = Color(0xFFE08A8A),
    onError = OliveDeep
)

/**
 * צורות חדות יותר מהקודמות (היו 6/10/16/22/28).
 *
 * זו החלטה עיצובית ולא קוסמטית: הפינות המעוגלות מאוד נתנו מראה של "אפליקציה
 * עם בועות", וכל פסקת טקסט נראתה כמו כרטיס צף. הכיוון החדש הוא **דף מודפס** -
 * הטקסט יושב על הנייר, והמסגרות הן קווי שיער דקים ולא קופסאות מרחפות. פינות
 * חדות יותר הן חצי מהעבודה; החצי השני הוא הוויתור על הצללות (ראו Leaf).
 */
private val KnotShapes = Shapes(
    extraSmall = RoundedCornerShape(3.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(10.dp),
    large = RoundedCornerShape(14.dp),
    extraLarge = RoundedCornerShape(20.dp)
)

@Composable
fun KnotAdvisorTheme(mode: AppMode = AppMode.ADMIN, content: @Composable () -> Unit) {
    val operational = mode == AppMode.OPERATIONAL
    CompositionLocalProvider(
        LocalTekheletPalette provides if (operational) OperationalPalette else AdminPalette
    ) {
        MaterialTheme(
            colorScheme = if (operational) OperationalColors else LightColors,
            shapes = KnotShapes,
            typography = HebrewTypography,
            content = content
        )
    }
}
