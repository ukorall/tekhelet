package org.tekhelet.knotadvisor.ui.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * פרימיטיבי הציור של הגדיל, במקום אחד.
 *
 * למה זה קובץ נפרד: יש עכשיו שני מקומות שמציירים גדיל - [TzitzitVisual] שמצייר
 * הרכב הלכתי אמיתי, ו-[FreeformVisual] של טביעת האצבע שמצייר רצף מקטעים חופשי
 * (כולל מקטעים שאין להם שום משמעות הלכתית). אם כל אחד היה מצייר בעצמו, קשר כפול
 * היה נראה אחרת בשני המסכים בתוך שבוע.
 *
 * ## עובי החוט
 *
 * כל דבר שמייצג חוט מצויר ב**אותו עובי**: כריכה, אלכסון של חוליה תימנית, וכל קו
 * באיקס של הקשר. העובי נגזר מגובה יחידת הכריכה ([THREAD_RATIO]), כך שהוא גדל
 * וקטן יחד עם הציור. קודם לכן לקשר ולאלכסונים היו עוביים משלהם, והתוצאה נראתה
 * כאילו הגדיל עשוי משלושה חוטים שונים.
 */
object GadilMetrics {
    /** גובה הקשר הכפול, ביחידות כריכה. */
    const val KNOT_UNITS = 1.8f
    /** רווח היכר-חוליות הרחב (הרמב"ם, תימן). */
    const val CHULYA_GAP_UNITS = 2.4f
    /** רווח קטן בין חוליה לחוליה. */
    const val SMALL_GAP_UNITS = 0.8f
    /** עובי החוט ביחס לגובה יחידת כריכה. */
    const val THREAD_RATIO = 0.82f
}

/** כריכה אחת: מלבן אופקי לרוחב הגדיל. */
fun DrawScope.drawWindBar(
    cx: Float, top: Float, w: Float, thickness: Float,
    colour: Color, outline: Color?
) {
    drawRoundRect(colour, Offset(cx - w / 2f, top), Size(w, thickness), CornerRadius(2.5f, 2.5f))
    outline?.let {
        drawRoundRect(
            it, Offset(cx - w / 2f, top), Size(w, thickness),
            CornerRadius(2.5f, 2.5f), style = Stroke(width = 1f)
        )
    }
}

/** כריכות פשוטות, כולן באותו צבע. */
fun DrawScope.drawPlainWinds(
    cx: Float, y: Float, w: Float, h: Float, count: Int,
    colour: Color, threadW: Float, outline: Color?
) {
    val each = h / count
    repeat(count) { k ->
        drawWindBar(cx, y + each * k + (each - threadW) / 2f, w, threadW, colour, outline)
    }
}

/**
 * כריכות שכל אחת מהן חצי בצבע אחד וחצי בשני.
 *
 * זה **חסר משמעות הלכתית לחלוטין** וקיים רק בבונה של טביעת האצבע, שם המטרה היא
 * לשחק. הוא נמצא כאן ולא שם רק כדי שכל הציור יישאר במקום אחד.
 */
fun DrawScope.drawSplitWinds(
    cx: Float, y: Float, w: Float, h: Float, count: Int,
    left: Color, right: Color, threadW: Float, outline: Color?
) {
    val each = h / count
    repeat(count) { k ->
        val top = y + each * k + (each - threadW) / 2f
        drawRoundRect(left, Offset(cx - w / 2f, top), Size(w / 2f, threadW), CornerRadius(2.5f, 2.5f))
        drawRoundRect(right, Offset(cx, top), Size(w / 2f, threadW), CornerRadius(2.5f, 2.5f))
        outline?.let {
            drawRoundRect(
                it, Offset(cx - w / 2f, top), Size(w, threadW),
                CornerRadius(2.5f, 2.5f), style = Stroke(width = 1f)
            )
        }
    }
}

/**
 * חוליה תימנית: חצי כריכה ימנית, **שני** אלכסונים היורדים משמאל לימין, וחצי
 * כריכה שמאלית. המבנה מחזיק את עצמו, ולכן אין צורך בקשר בין החוליות.
 *
 * `colours` הוא צבע לכל אחת משלוש הכריכות, כדי לאפשר חוליה משולבת (לבן בקצה
 * אחד ותכלת בשאר). האלכסונים נצבעים בצבע החוליה שהם עושים - לא בצבע ניטרלי.
 */
fun DrawScope.drawYemenite(
    cx: Float, y: Float, w: Float, h: Float, count: Int,
    colours: List<Color>, threadW: Float, outlineFor: (Color) -> Color?
) {
    val half = w / 2f
    val unit = h / count
    fun colourAt(i: Int) = colours.getOrElse(i) { colours.lastOrNull() ?: Color.Gray }

    fun edge(top: Float, right: Boolean, colour: Color) {
        val x = if (right) cx else cx - half
        drawRoundRect(colour, Offset(x, top), Size(half, threadW), CornerRadius(2f, 2f))
        outlineFor(colour)?.let {
            drawRoundRect(
                it, Offset(x, top), Size(half, threadW),
                CornerRadius(2f, 2f), style = Stroke(width = 1f)
            )
        }
    }

    fun diagonal(top: Float, span: Float, colour: Color) {
        drawLine(
            colour,
            Offset(cx - half * 0.9f, top),
            Offset(cx + half * 0.9f, top + span),
            strokeWidth = threadW, cap = StrokeCap.Round
        )
        outlineFor(colour)?.let {
            drawLine(
                it,
                Offset(cx - half * 0.9f, top),
                Offset(cx + half * 0.9f, top + span),
                strokeWidth = threadW * 0.16f, cap = StrokeCap.Round
            )
        }
    }

    when {
        count >= 3 -> {
            edge(y + unit * 0.1f, right = true, colour = colourAt(0))
            diagonal(y + unit * 0.95f, unit * 0.55f, colourAt(1))
            diagonal(y + unit * 1.75f, unit * 0.55f, colourAt(1))
            edge(y + h - unit * 0.72f, right = false, colour = colourAt(2))
        }
        count == 2 -> {
            edge(y + unit * 0.1f, right = true, colour = colourAt(0))
            diagonal(y + unit * 1.0f, unit * 0.5f, colourAt(1))
        }
        else -> edge(y + unit * 0.15f, right = true, colour = colourAt(0))
    }
}

/** חוליה תימנית הפוכה: הכריכות, ומעליהן אלכסון בכיוון ההפוך - בצבע החוליה. */
fun DrawScope.drawInvertedYemenite(
    cx: Float, y: Float, w: Float, h: Float, count: Int,
    colour: Color, threadW: Float, outline: Color?
) {
    drawPlainWinds(cx, y, w, h, count, colour, threadW, outline)
    drawLine(
        color = colour,
        start = Offset(cx - w * 0.44f, y + h * 0.88f),
        end = Offset(cx + w * 0.44f, y + h * 0.12f),
        strokeWidth = threadW,
        cap = StrokeCap.Round
    )
    outline?.let {
        drawLine(
            it,
            Offset(cx - w * 0.44f, y + h * 0.88f),
            Offset(cx + w * 0.44f, y + h * 0.12f),
            strokeWidth = threadW * 0.16f, cap = StrokeCap.Round
        )
    }
}

/**
 * קשר כפול: שני איקסים שמטפסים קצת אחד על השני. כל קו של כל איקס הוא **זוג**
 * חוטים צמודים - אחד תכלת ואחד לבן - כי זה בדיוק מה שקורה בקשר: שני החוטים
 * נקשרים זה בזה. כל חוט בעובי החוט הרגיל.
 */
fun DrawScope.drawDoubleKnot(
    cx: Float, y: Float, w: Float, h: Float, threadW: Float,
    tekhelet: Color, white: Color, outline: Color?
) {
    val half = w * 0.46f
    val each = h * 0.62f            // גדול מ-h/2, ולכן האיקסים חופפים מעט
    val gap = threadW * 0.55f       // הזחה בין החוט התכלת ללבן, כך שהם צמודים

    repeat(2) { k ->
        val top = y + (h - each) * k + each * 0.1f
        val bot = top + each * 0.8f
        listOf(
            Offset(cx - half, top) to Offset(cx + half, bot),
            Offset(cx + half, top) to Offset(cx - half, bot)
        ).forEach { (a, b) ->
            drawLine(
                tekhelet, Offset(a.x, a.y - gap), Offset(b.x, b.y - gap),
                strokeWidth = threadW, cap = StrokeCap.Round
            )
            drawLine(
                white, Offset(a.x, a.y + gap), Offset(b.x, b.y + gap),
                strokeWidth = threadW, cap = StrokeCap.Round
            )
            outline?.let {
                drawLine(
                    it, Offset(a.x, a.y + gap), Offset(b.x, b.y + gap),
                    strokeWidth = threadW * 0.16f, cap = StrokeCap.Round
                )
            }
        }
    }
}

/** קשר שטוח - קיים רק בטביעת האצבע. לא רלוונטי לציצית. */
fun DrawScope.drawFlatKnot(
    cx: Float, y: Float, w: Float, h: Float, threadW: Float,
    a: Color, b: Color
) {
    val half = w * 0.44f
    val mid = y + h / 2f
    drawLine(a, Offset(cx - half, mid - threadW), Offset(cx + half, mid - threadW),
        strokeWidth = threadW, cap = StrokeCap.Round)
    drawLine(b, Offset(cx - half, mid + threadW), Offset(cx + half, mid + threadW),
        strokeWidth = threadW, cap = StrokeCap.Round)
    drawArc(
        color = a, startAngle = 200f, sweepAngle = 140f, useCenter = false,
        topLeft = Offset(cx - half * 0.5f, y + h * 0.12f),
        size = Size(half, h * 0.5f),
        style = Stroke(width = threadW, cap = StrokeCap.Round)
    )
}

/** פפיון. קיים רק בטביעת האצבע, ואין לו שום קשר לציצית. */
fun DrawScope.drawBow(
    cx: Float, y: Float, w: Float, h: Float, threadW: Float, colour: Color
) {
    val half = w * 0.42f
    val mid = y + h / 2f
    listOf(-1f, 1f).forEach { side ->
        drawArc(
            color = colour, startAngle = if (side < 0) 90f else 270f,
            sweepAngle = 180f, useCenter = false,
            topLeft = Offset(cx + (if (side < 0) -half else 0f), mid - h * 0.3f),
            size = Size(half, h * 0.6f),
            style = Stroke(width = threadW, cap = StrokeCap.Round)
        )
    }
    drawCircle(colour, radius = threadW * 0.9f, center = Offset(cx, mid))
}

/** לולאה מסולסלת. גם היא בידור בלבד. */
fun DrawScope.drawSpiral(
    cx: Float, y: Float, w: Float, h: Float, threadW: Float, colour: Color
) {
    val turns = 5
    val step = h / turns
    repeat(turns) { i ->
        val top = y + step * i
        drawArc(
            color = colour,
            startAngle = if (i % 2 == 0) 180f else 0f,
            sweepAngle = 180f, useCenter = false,
            topLeft = Offset(cx - w * 0.38f, top),
            size = Size(w * 0.76f, step * 1.15f),
            style = Stroke(width = threadW, cap = StrokeCap.Round)
        )
    }
}

/** החוטים המאונכים שסביבם כורכים, מצוירים מאחורי הגדיל. */
fun DrawScope.drawCoreThreads(cx: Float, top: Float, bottom: Float, w: Float, colour: Color) {
    listOf(-0.5f, -0.17f, 0.17f, 0.5f).forEach { t ->
        drawLine(
            color = colour,
            start = Offset(cx + t * w * 0.62f, top),
            end = Offset(cx + t * w * 0.62f, bottom),
            strokeWidth = 2.4f,
            cap = StrokeCap.Round
        )
    }
}
