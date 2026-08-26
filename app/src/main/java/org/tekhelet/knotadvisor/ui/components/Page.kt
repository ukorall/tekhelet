package org.tekhelet.knotadvisor.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.tekhelet.knotadvisor.ui.theme.DisplayFamily

/**
 * אוצר המילים העיצובי של האפליקציה.
 *
 * ## מה השתנה ולמה
 *
 * הגרסה הקודמת בנתה כמעט הכל מ-`Card` של Material: כל פסקה, כל פריט ניווט וכל
 * הערה ישבו בקופסה מוגבהת עם פינות מעוגלות מאוד. התוצאה היתה ערימה של קופסאות
 * צפות שכולן צועקות באותה עוצמה, בלי היררכיה - וזה בדיוק מה שהופך מסך לעייף.
 *
 * הכיוון החדש הוא **דף של ספר**, לא לוח בקרה. זה מתאים לתוכן: זו שיחה על
 * סוגיה, ואנשים כבר יודעים לקרוא דף. בפועל:
 *
 *  - **טקסט יושב על הנייר.** רוב התוכן כבר לא בקופסה בכלל - רק רווח וטיפוגרפיה.
 *  - **קווי שיער במקום צללים.** מה שכן צריך תיחום מקבל [Leaf]: מסגרת של 1dp,
 *    בלי הגבהה.
 *  - **רשימות הן רשימות.** ניווט עובר ל-[NavRow] - שורות מופרדות בקו דק,
 *    במקום ערימת כרטיסים.
 *  - **לציטוט יש צורה משלו.** [SourceQuote] - פס תכלת בצד ההתחלה וטקסט בגופן
 *    הכותרות. האפליקציה מצטטת מקורות כל הזמן ולא היה לזה שום ייצוג.
 *  - **כותרת לכל מסך.** [PageHeader] מוציא את שם המסך מסרגל העליון אל תוך
 *    הדף, איפה שיש לו מקום לנשום בעברית.
 */

/** מרווח הצד הקבוע של כל מסך. */
val PageGutter: Dp = 22.dp

/**
 * כותרת דף: כותרת בגופן הספר, עם פס תכלת אנכי בצד ההתחלה.
 *
 * הפס האנכי הוא הסימן המזהה של המסכים כאן - הוא חוזר גם ב-[SourceQuote],
 * ומקשר ויזואלית בין "זה נושא" לבין "זה מקור".
 */
@Composable
fun PageHeader(
    title: String,
    kicker: String? = null,
    lead: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth()) {
        kicker?.let {
            Text(
                it,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.height(6.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .width(4.dp)
                    .height(30.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(Modifier.width(12.dp))
            Text(title, style = MaterialTheme.typography.headlineMedium)
        }
        lead?.let {
            Spacer(Modifier.height(12.dp))
            Text(
                it,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * כותרת פרק בתוך הדף: קו שיער שנמשך לרוחב, והכותרת יושבת עליו בצד ההתחלה.
 * זה מה שנותן למסך ארוך מבנה שאפשר לסרוק בעין.
 */
@Composable
fun SectionHeading(title: String, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().padding(top = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(12.dp))
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

/**
 * משטח מתוחם - מחליף את `Card`. שטוח, עם מסגרת של קו שיער ובלי הגבהה, כדי
 * שהוא יתחום בלי לצוף.
 */
@Composable
fun Leaf(
    modifier: Modifier = Modifier,
    tinted: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val base = modifier
        .fillMaxWidth()
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }
    Surface(
        modifier = base,
        shape = MaterialTheme.shapes.medium,
        color = if (tinted) MaterialTheme.colorScheme.secondaryContainer
        else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

/**
 * שורת ניווט. מחליפה את הכרטיסים הלחיצים: שורה ברוחב מלא, עם צ'ברון שמצביע
 * לכיוון ההתקדמות (שמאלה, כי הכל כאן מימין לשמאל), ומפריד דק מתחת.
 */
@Composable
fun NavRow(
    title: String,
    subtitle: String? = null,
    badge: String? = null,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val contentColor =
        if (enabled) MaterialTheme.colorScheme.onSurface
        else MaterialTheme.colorScheme.onSurfaceVariant
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled, onClick = onClick)
                .padding(vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        color = contentColor
                    )
                    badge?.let {
                        Spacer(Modifier.width(8.dp))
                        Pill(it)
                    }
                }
                subtitle?.let {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Chevron(MaterialTheme.colorScheme.outline)
        }
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}

/** צ'ברון מצויר ידנית ומצביע שמאלה - כיוון ההתקדמות בממשק עברי. */
@Composable
private fun Chevron(color: androidx.compose.ui.graphics.Color) {
    Canvas(Modifier.size(width = 9.dp, height = 16.dp)) {
        val w = size.width
        val h = size.height
        drawLine(
            color = color,
            start = Offset(w, 0f),
            end = Offset(0f, h / 2),
            strokeWidth = 1.8.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(0f, h / 2),
            end = Offset(w, h),
            strokeWidth = 1.8.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

/** תווית קטנה - "בבנייה", "מומלץ", מספר. */
@Composable
fun Pill(text: String, emphasised: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (emphasised) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (emphasised) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
        )
    }
}

/**
 * ציטוט ממקור. פס תכלת בצד ההתחלה, טקסט בגופן הספר, וייחוס מתחת.
 *
 * הייחוס הוא לא קישוט: אחרי שהתברר שהצגתי המלצה מעשית בלי לומר של מי היא,
 * הכלל כאן הוא שכל דבר שהוא ציטוט או הנחיה של מישהו מקבל שם.
 */
@Composable
fun SourceQuote(text: String, source: String? = null, modifier: Modifier = Modifier) {
    // IntrinsicSize.Min נותן ל-Row גובה מוגדר, ורק אז fillMaxHeight על הפס
    // באמת מותח אותו לאורך כל הציטוט. בלעדיו הפס היה נשאר בגובה המינימלי.
    Row(modifier.fillMaxWidth().height(IntrinsicSize.Min), verticalAlignment = Alignment.Top) {
        Box(
            Modifier
                .width(3.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.tertiary)
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = DisplayFamily,
                    fontStyle = FontStyle.Italic
                )
            )
            source?.let {
                Spacer(Modifier.height(6.dp))
                Text(
                    it,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

/** הערת שוליים - קטנה, שקטה, לא נכנסת לקופסה. */
@Composable
fun Aside(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

/**
 * מפריד בדמות גדיל: מקטעי כריכה בתכלת, רווחי היכר-חוליות, ונקודת קשר במרכז.
 * מחליף את המפריד הישן, שהיה שורת מקטעים אחידה בלי שום קשר לגדיל האמיתי.
 */
@Composable
fun GadilRule(modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val light = MaterialTheme.colorScheme.tertiary
    Canvas(modifier.fillMaxWidth().height(9.dp)) {
        val y = size.height / 2
        val stroke = 3.dp.toPx()
        val gap = 5.dp.toPx()
        // שלוש חוליות של שלוש כריכות, קשר במרכז - הקצוות בהירים, כמו שהכריכה
        // הראשונה והאחרונה תמיד בלבן.
        val chulyot = 4
        val windsPer = 3
        val knotR = 3.dp.toPx()
        val totalGaps = (chulyot - 1) * gap * 2 + (chulyot * (windsPer - 1)) * gap
        val windW = (size.width - totalGaps - knotR * 2) / (chulyot * windsPer)
        var x = 0f
        for (c in 0 until chulyot) {
            for (w in 0 until windsPer) {
                val edge = (c == 0 && w == 0) || (c == chulyot - 1 && w == windsPer - 1)
                drawLine(
                    color = if (edge) light else primary,
                    start = Offset(x, y),
                    end = Offset(x + windW, y),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
                x += windW
                if (w < windsPer - 1) x += gap
            }
            if (c == chulyot / 2 - 1) {
                x += gap
                drawCircle(primary, radius = knotR, center = Offset(x + knotR, y))
                x += knotR * 2 + gap
            } else if (c < chulyot - 1) {
                x += gap * 2
            }
        }
    }
}

/**
 * עוטף מחרוזת ב"בידוד משמאל לימין".
 *
 * זה נחוץ כי מספרים וסימנים כמו % ו-₪ הם ניטרליים מבחינת כיוון, ובתוך הקשר
 * עברי הם נוטים לקפוץ לצד הלא נכון - "85%" מוצג כ-"%85", ו-"115₪" כ-"₪115".
 * התווים U+2066/U+2069 מבודדים את הקטע ומכריחים אותו להישאר משמאל לימין.
 */
fun ltr(text: String): String = "⁦$text⁩"
