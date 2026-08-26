@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package org.tekhelet.knotadvisor.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import org.tekhelet.knotadvisor.R

/**
 * טיפוגרפיה עברית.
 *
 * ## למה בכלל להחליף את ברירת המחדל
 *
 * ברירת המחדל של Material היא Roboto, שמעולם לא תוכננה לעברית - האותיות
 * העבריות בה נלקחות מפונט תחליפי של המערכת, והתוצאה משתנה בין מכשיר למכשיר.
 * חוץ מזה, Material מוסיפה `letterSpacing` חיובי כמעט לכל סגנון (0.25sp בגוף
 * הטקסט, 0.5sp בתוויות). ריווח אותיות הוא כלי לטיניות; בעברית הוא **מפרק את
 * המילה** ופוגע בקריאות. לכן כאן הוא אפס בכל מקום.
 *
 * ## הזיווג
 *
 * - **פרנק-רוהל** לכותרות. זה הפונט של ספרי הקודש - אותו שלד אותיות שרואים
 *   בגמרא ובשולחן ערוך. הוא נותן לאפליקציה נוכחות של דף ספר במקום של לוח בקרה,
 *   וזה בדיוק מה שהיא צריכה: זו שיחה על סוגיה, לא דשבורד.
 * - **Heebo** לגוף הטקסט ולתוויות. סאנס עברי נקי שנקרא היטב במסך קטן.
 *
 * שניהם פונטים משתנים (variable), מוטמעים באפליקציה - כך שהמראה זהה בכל מכשיר
 * ולא צריך רשת. הרישיונות נמצאים ב-assets/licenses/.
 *
 * ## גדלים
 *
 * גוף הטקסט הועלה ל-17sp/16sp. ברירת המחדל (14sp ב-bodyMedium) קטנה מדי
 * לעברית - אין באותיות העבריות אורכי עולה ויורד שעוזרים לזהות מילה, ולכן הן
 * צריכות יותר גובה ויותר מרווח שורות. גובה השורה כאן הוא בערך פי 1.6 מהגודל.
 */

private val FrankRuhl = FontFamily(
    Font(R.font.frank_ruhl_libre_variable, FontWeight.Normal),
    Font(R.font.frank_ruhl_libre_variable, FontWeight.Medium),
    Font(R.font.frank_ruhl_libre_variable, FontWeight.SemiBold),
    Font(R.font.frank_ruhl_libre_variable, FontWeight.Bold)
)

private val Heebo = FontFamily(
    Font(R.font.heebo_variable, FontWeight.Light),
    Font(R.font.heebo_variable, FontWeight.Normal),
    Font(R.font.heebo_variable, FontWeight.Medium),
    Font(R.font.heebo_variable, FontWeight.Bold)
)

val DisplayFamily = FrankRuhl
val BodyFamily = Heebo

// גובה שורה בעברית: לא לחתוך את החלק העליון של האותיות, ולמרכז את העודף.
private val HebrewLineHeight = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None
)

private fun display(size: Int, line: Int, weight: FontWeight = FontWeight.Medium) = TextStyle(
    fontFamily = FrankRuhl,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = 0.sp,
    lineHeightStyle = HebrewLineHeight
)

private fun body(size: Int, line: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(
    fontFamily = Heebo,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = 0.sp,
    lineHeightStyle = HebrewLineHeight
)

val HebrewTypography = Typography(
    displayLarge = display(40, 52, FontWeight.Bold),
    displayMedium = display(34, 44, FontWeight.Bold),
    displaySmall = display(29, 38, FontWeight.SemiBold),

    headlineLarge = display(27, 36, FontWeight.SemiBold),
    headlineMedium = display(24, 32, FontWeight.SemiBold),
    headlineSmall = display(21, 29, FontWeight.SemiBold),

    // הכותרות הקטנות עוברות ל-Heebo: בגדלים האלה הסריפים של פרנק-רוהל
    // מתחילים להיסתם במסך, וסאנס פשוט קריא יותר.
    titleLarge = display(20, 28, FontWeight.SemiBold),
    titleMedium = body(17, 24, FontWeight.Medium),
    titleSmall = body(15, 21, FontWeight.Medium),

    bodyLarge = body(17, 28),
    bodyMedium = body(16, 26),
    bodySmall = body(14, 22),

    labelLarge = body(15, 20, FontWeight.Medium),
    labelMedium = body(13, 18, FontWeight.Medium),
    labelSmall = body(12, 16, FontWeight.Medium)
)
