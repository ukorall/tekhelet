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

    /**
     * רווח בסיס בין **כל שני אלמנטים**, בלי קשר לשאלה איזה היכר חוליות נעשה שם.
     *
     * זה נפרד מ-[SMALL_GAP_UNITS] ומ-[CHULYA_GAP_UNITS]: אלה מייצגים החלטה
     * הלכתית (איך מפרידים בין חוליה לחוליה), וזה כאן הוא סתם אוויר בציור, כדי
     * שאפשר יהיה לספור בעין. חוליה שנוגעת בקשר שאחריה נראית כמו גוש אחד.
     */
    const val ELEMENT_GAP_UNITS = 0.35f

    /**
     * עובי החוט ביחס לגובה יחידת כריכה. ירד ב-15% מ-0.82, כי בעובי הקודם
     * הכריכות כמעט נגעו זו בזו והגדיל נראה כמו גוש רציף.
     */
    const val THREAD_RATIO = 0.70f
}

/**
 * קו המתאר של חוט: הגוון של החוט עצמו, כהה יותר.
 *
 * נגזר מהצבע ולא נלקח מהפלטה, כדי שגם חוט אדום או כתום בטביעת האצבע יקבל
 * מתאר שנצמד אליו במקום קו כחלחל שנראה כמו הערה. הוא מצויר תמיד, גם סביב
 * התכלת, ובעובי המינימלי האפשרי - [Stroke.HairlineWidth] הוא פיקסל אחד
 * במסך, בלי קשר לצפיפות.
 */
fun edgeOf(c: Color): Color = Color(c.red * 0.5f, c.green * 0.5f, c.blue * 0.5f, 0.9f)

private val Hairline get() = Stroke(width = Stroke.HairlineWidth)

/** רדיוס פינה לכריכה: חצי מעובי החוט, כך שהקצה מעוגל כמו חוט אמיתי. */
private fun corner(threadW: Float) = CornerRadius(threadW * 0.5f, threadW * 0.5f)

/** כריכה אחת: מלבן אופקי לרוחב הגדיל. */
fun DrawScope.drawWindBar(
    cx: Float, top: Float, w: Float, thickness: Float, colour: Color
) {
    val r = corner(thickness)
    drawRoundRect(colour, Offset(cx - w / 2f, top), Size(w, thickness), r)
    drawRoundRect(edgeOf(colour), Offset(cx - w / 2f, top), Size(w, thickness), r, style = Hairline)
}

/** כריכות פשוטות, כולן באותו צבע. */
fun DrawScope.drawPlainWinds(
    cx: Float, y: Float, w: Float, h: Float, count: Int,
    colour: Color, threadW: Float
) {
    val each = h / count
    repeat(count) { k ->
        drawWindBar(cx, y + each * k + (each - threadW) / 2f, w, threadW, colour)
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
    left: Color, right: Color, threadW: Float
) {
    val each = h / count
    val r = corner(threadW)
    repeat(count) { k ->
        val top = y + each * k + (each - threadW) / 2f
        drawRoundRect(left, Offset(cx - w / 2f, top), Size(w / 2f, threadW), r)
        drawRoundRect(right, Offset(cx, top), Size(w / 2f, threadW), r)
        drawRoundRect(edgeOf(left), Offset(cx - w / 2f, top), Size(w, threadW), r, style = Hairline)
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
    colours: List<Color>, threadW: Float
) {
    val half = w / 2f
    val unit = h / count
    val r = corner(threadW)
    fun colourAt(i: Int) = colours.getOrElse(i) { colours.lastOrNull() ?: Color.Gray }

    fun edge(top: Float, right: Boolean, colour: Color) {
        val x = if (right) cx else cx - half
        drawRoundRect(colour, Offset(x, top), Size(half, threadW), r)
        drawRoundRect(edgeOf(colour), Offset(x, top), Size(half, threadW), r, style = Hairline)
    }

    fun diagonal(top: Float, span: Float, colour: Color) {
        val a = Offset(cx - half * 0.9f, top)
        val b = Offset(cx + half * 0.9f, top + span)
        drawLine(colour, a, b, strokeWidth = threadW, cap = StrokeCap.Round)
        drawLine(edgeOf(colour), a, b, strokeWidth = threadW * 0.12f, cap = StrokeCap.Round)
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
    colour: Color, threadW: Float
) {
    drawPlainWinds(cx, y, w, h, count, colour, threadW)
    val a = Offset(cx - w * 0.44f, y + h * 0.88f)
    val b = Offset(cx + w * 0.44f, y + h * 0.12f)
    drawLine(colour, a, b, strokeWidth = threadW, cap = StrokeCap.Round)
    drawLine(edgeOf(colour), a, b, strokeWidth = threadW * 0.12f, cap = StrokeCap.Round)
}

/**
 * קשר כפול: שני איקסים שמטפסים קצת אחד על השני. כל קו של כל איקס הוא **זוג**
 * חוטים צמודים - אחד תכלת ואחד לבן - כי זה בדיוק מה שקורה בקשר: שני החוטים
 * נקשרים זה בזה. כל חוט בעובי החוט הרגיל.
 */
fun DrawScope.drawDoubleKnot(
    cx: Float, y: Float, w: Float, h: Float, threadW: Float,
    tekhelet: Color, white: Color
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
            listOf(tekhelet to -gap, white to gap).forEach { (colour, dy) ->
                val p1 = Offset(a.x, a.y + dy)
                val p2 = Offset(b.x, b.y + dy)
                drawLine(colour, p1, p2, strokeWidth = threadW, cap = StrokeCap.Round)
                drawLine(edgeOf(colour), p1, p2, strokeWidth = threadW * 0.12f, cap = StrokeCap.Round)
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
