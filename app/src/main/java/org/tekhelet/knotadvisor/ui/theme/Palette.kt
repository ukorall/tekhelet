package org.tekhelet.knotadvisor.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import org.tekhelet.knotadvisor.model.AppMode


/**
 * צבעי **החוט עצמו**, בנפרד מצבעי הממשק.
 *
 * ההפרדה הזו היא העיקר: הציור לא קורא מ-`colorScheme` אלא מכאן. כך אפשר להחליף
 * את כל ערכת הממשק בלי לגעת בגוון התכלת - שהוא הדבר האחד באפליקציה שאסור לו
 * לזוז, כי אנשים בוחרים לפיו איך הציצית שלהם תיראה. במצב המבצעי הממשק משתנה
 * לגמרי, והתכלת נשארת אותו `#2F5C88` בדיוק.
 */
data class TekheletPalette(
    /** גוון החוט האמיתי. זהה בשני המצבים. */
    val threadTekhelet: Color,
    /** חוט הלבן. לבן במנהלתי, ירוק זית כהה במבצעי. */
    val threadWhite: Color,
    /** רקע הגדיל, כדי שחוט הלבן ייראה על גביו. */
    val gadilBackdrop: Color,
    /** החוטים המאונכים שסביבם כורכים. */
    val coreThread: Color,
    /** קו מתאר דק סביב חוט לבן. */
    val threadOutline: Color,
    /** צבעים ל"מקטעים המוזרים" בטביעת האצבע - לא הלכה, בידור. */
    val odd: List<Color>
)

private val TekheletThread = Color(0xFF2F5C88)

// ירוק זית - המצב המבצעי. הכהה הוא ה"לבן", הבהיר הוא ה"תכלת הבהירה".
internal val OliveDeep = Color(0xFF1E2216)   // רקע
internal val OliveSurface = Color(0xFF262B1B) // משטח
internal val OliveDark = Color(0xFF3D4522)   // "הלבן" - חוטים וליבה
internal val OliveMid = Color(0xFF5C6733)
internal val OliveLight = Color(0xFF9AA863)  // "התכלת הבהירה"
internal val OliveText = Color(0xFFF1F3E6)

val AdminPalette = TekheletPalette(
    threadTekhelet = TekheletThread,
    threadWhite = Color(0xFFFCFCFD),
    gadilBackdrop = Color(0xFFD2E1F0).copy(alpha = 0.55f),
    coreThread = Color(0xFF2F5C88).copy(alpha = 0.45f),
    threadOutline = Color(0xFF8AACCE),
    odd = listOf(
        Color(0xFFC0392B), // אדום
        Color(0xFFD98324), // כתום
        Color(0xFF7D3C98), // סגול
        Color(0xFF16A085)  // טורקיז
    )
)

val OperationalPalette = TekheletPalette(
    // אותו גוון בדיוק. זה לא בטעות ואסור "לתקן" את זה לגוון בהיר יותר.
    threadTekhelet = TekheletThread,
    threadWhite = OliveDark,
    // הרקע בהיר יותר מ"הלבן" שעליו, אחרת חוטי הלבן נבלעים בו
    gadilBackdrop = OliveMid.copy(alpha = 0.55f),
    coreThread = OliveLight.copy(alpha = 0.5f),
    threadOutline = OliveLight,
    odd = listOf(
        Color(0xFFD64545),
        Color(0xFFE08A2E),
        Color(0xFFA569BD),
        Color(0xFF20C0A0)
    )
)

val LocalTekheletPalette = compositionLocalOf { AdminPalette }

/** קיצור: `TekheletTheme.palette` בתוך composable. */
object TekheletTheme {
    val palette: TekheletPalette
        @Composable @ReadOnlyComposable get() = LocalTekheletPalette.current
}
