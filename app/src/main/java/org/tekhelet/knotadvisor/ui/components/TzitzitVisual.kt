package org.tekhelet.knotadvisor.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.tekhelet.knotadvisor.logic.GadilBuilder
import org.tekhelet.knotadvisor.model.ChulyaForm
import org.tekhelet.knotadvisor.model.KnotComposition

/**
 * מצייר את הגדיל מ**רכיבים**, לא כתמונה סגורה לכל שיטה.
 *
 * כל מקטע שה-GadilBuilder מייצר מצויר בנפרד לפי צורת החוליה, ולכן אפשר להרכיב
 * שיטה חדשה ומיד לראות אותה מצוירת - בלי לגעת בקוד הציור.
 *
 * מוסכמות הציור, לפי מה שאוריאל הגדיר:
 *  - קשר כפול: שני איקסים עבים שמטפסים קצת אחד על השני, וכל קו באיקס עשוי
 *    זוג קווים - אחד כחול ואחד לבן (שני החוטים שנקשרים זה בזה).
 *  - חוליה תימנית: חצי כריכה ימנית, **שני** קווים אלכסוניים היורדים משמאל
 *    לימין, וחצי כריכה שמאלית.
 *  - חוליה תימנית משולבת: בתחילת הגדיל הלבן קודם, בסופו הלבן אחרון.
 *  - חוליה תימנית הפוכה: אלכסון בכיוון ההפוך על גבי הכריכות.
 *  - רווח חוליה: רואים דרכו את החוטים המאונכים שסביבם כורכים.
 */
@Composable
fun TzitzitVisual(
    composition: KnotComposition,
    modifier: Modifier = Modifier,
    height: Dp = 320.dp,
    showLegend: Boolean = true
) {
    val plan = GadilBuilder.plan(composition)
    val tekhelet = MaterialTheme.colorScheme.primary
    val white = Color(0xFFFCFCFD)
    val outline = MaterialTheme.colorScheme.outline
    // רקע מעט כהה מהלבן, כדי שכריכות לבנות ייראו על גביו
    val backdrop = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    val core = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

    Column(modifier) {
        Canvas(Modifier.fillMaxWidth().height(height)) {
            drawGadil(plan, tekhelet, white, outline, backdrop, core)
        }
        if (showLegend) {
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LegendDot("תכלת", tekhelet)
                LegendDot("לבן", white, border = outline)
                Text("✕ קשר כפול", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun LegendDot(label: String, color: Color, border: Color? = null) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.size(11.dp)) {
            drawCircle(color)
            border?.let { drawCircle(it, style = Stroke(width = 1.2f)) }
        }
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

private fun DrawScope.drawGadil(
    plan: GadilBuilder.Plan,
    tekhelet: Color,
    white: Color,
    outline: Color,
    backdrop: Color,
    core: Color
) {
    val cx = size.width / 2f
    val gadilW = (size.width * 0.30f).coerceAtMost(150f)
    val clothH = size.height * 0.07f

    // רקע לאורך הגדיל, כדי שכריכות לבנות לא ייעלמו
    drawRoundRect(
        color = backdrop,
        topLeft = Offset(cx - gadilW * 0.78f, clothH),
        size = Size(gadilW * 1.56f, size.height * 0.70f),
        cornerRadius = CornerRadius(8f, 8f)
    )

    // פינת הבגד
    drawRoundRect(
        color = outline.copy(alpha = 0.35f),
        topLeft = Offset(cx - size.width * 0.22f, 0f),
        size = Size(size.width * 0.44f, clothH),
        cornerRadius = CornerRadius(5f, 5f)
    )

    // יחידות אנכיות: כריכה = 1, קשר = 1.8, רווח חוליה = 2
    var units = 0f
    plan.elements.forEach {
        units += when (it) {
            is GadilBuilder.Element.Winds -> it.piece.length.toFloat()
            GadilBuilder.Element.Knot -> 1.8f
            GadilBuilder.Element.ChulyaGap -> 2f
            GadilBuilder.Element.SmallGap -> 0.45f
        }
    }
    if (units <= 0f) return

    val gadilH = size.height * 0.66f
    val u = gadilH / units
    var y = clothH + 3f

    // החוטים המאונכים שסביבם כורכים - מצוירים לכל אורך הגדיל, מתחת לכריכות
    val coreXs = listOf(-0.5f, -0.17f, 0.17f, 0.5f)
    coreXs.forEach { t ->
        drawLine(
            color = core,
            start = Offset(cx + t * gadilW * 0.62f, clothH),
            end = Offset(cx + t * gadilW * 0.62f, clothH + gadilH + 6f),
            strokeWidth = 2.4f,
            cap = StrokeCap.Round
        )
    }

    plan.elements.forEach { el ->
        when (el) {
            is GadilBuilder.Element.Winds -> {
                val h = u * el.piece.length
                val colour = if (el.piece.tekhelet) tekhelet else white
                when (plan.chulyaForm) {
                    ChulyaForm.YEMENITE_SELF_HOLDING ->
                        drawYemenite(cx, y, gadilW, h, el.piece.length, colour, el.piece.tekhelet, outline)
                    ChulyaForm.YEMENITE_INVERTED ->
                        drawInvertedYemenite(cx, y, gadilW, h, el.piece.length, colour, el.piece.tekhelet, outline)
                    else ->
                        drawPlainWinds(cx, y, gadilW, h, el.piece.length, colour, el.piece.tekhelet, outline)
                }
                y += h
            }
            GadilBuilder.Element.Knot -> {
                val h = u * 1.8f
                drawDoubleKnot(cx, y, gadilW, h, tekhelet, white, outline)
                y += h
            }
            GadilBuilder.Element.ChulyaGap -> y += u * 2f
            GadilBuilder.Element.SmallGap -> y += u * 0.45f
        }
    }

    // הענף
    val strandTop = y + 4f
    for (i in 0 until 8) {
        val t = (i - 3.5f) / 8f
        drawLine(
            color = if (i % 4 == 0) tekhelet else white,
            start = Offset(cx + t * gadilW * 0.7f, strandTop),
            end = Offset(cx + t * size.width * 0.5f, size.height),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
        if (i % 4 != 0) {
            drawLine(
                color = outline.copy(alpha = 0.3f),
                start = Offset(cx + t * gadilW * 0.7f, strandTop),
                end = Offset(cx + t * size.width * 0.5f, size.height),
                strokeWidth = 0.7f, cap = StrokeCap.Round
            )
        }
    }
}

/** כריכות פשוטות - מלבן אופקי לכל כריכה. */
private fun DrawScope.drawPlainWinds(
    cx: Float, y: Float, w: Float, h: Float, count: Int,
    colour: Color, isTekhelet: Boolean, outline: Color
) {
    val each = h / count
    repeat(count) { k ->
        val top = y + each * k + each * 0.08f
        val hh = each * 0.84f
        drawRoundRect(colour, Offset(cx - w / 2f, top), Size(w, hh), CornerRadius(2.5f, 2.5f))
        if (!isTekhelet) {
            drawRoundRect(
                outline.copy(alpha = 0.5f), Offset(cx - w / 2f, top), Size(w, hh),
                CornerRadius(2.5f, 2.5f), style = Stroke(width = 1f)
            )
        }
    }
}

/**
 * חוליה תימנית: חצי כריכה ימנית, שני קווים אלכסוניים היורדים משמאל לימין,
 * וחצי כריכה שמאלית. מקטע קצר מ-3 מצויר כחלק מהמבנה.
 */
private fun DrawScope.drawYemenite(
    cx: Float, y: Float, w: Float, h: Float, count: Int,
    colour: Color, isTekhelet: Boolean, outline: Color
) {
    val half = w / 2f
    val unit = h / count
    val t = unit * 0.62f

    fun edge(top: Float, right: Boolean) {
        val x = if (right) cx else cx - half
        drawRoundRect(colour, Offset(x, top), Size(half, t), CornerRadius(2f, 2f))
        if (!isTekhelet) {
            drawRoundRect(
                outline.copy(alpha = 0.5f), Offset(x, top), Size(half, t),
                CornerRadius(2f, 2f), style = Stroke(width = 1f)
            )
        }
    }

    fun diagonal(top: Float, span: Float) {
        drawLine(
            colour,
            Offset(cx - half * 0.92f, top),
            Offset(cx + half * 0.92f, top + span),
            strokeWidth = t, cap = StrokeCap.Round
        )
        if (!isTekhelet) {
            drawLine(
                outline.copy(alpha = 0.45f),
                Offset(cx - half * 0.92f, top),
                Offset(cx + half * 0.92f, top + span),
                strokeWidth = t * 0.18f, cap = StrokeCap.Round
            )
        }
    }

    when (count) {
        3 -> {
            edge(y + unit * 0.1f, right = true)
            // שני קווים אלכסוניים, לא אחד
            diagonal(y + unit * 0.95f, unit * 0.55f)
            diagonal(y + unit * 1.75f, unit * 0.55f)
            edge(y + h - unit * 0.72f, right = false)
        }
        2 -> {
            edge(y + unit * 0.1f, right = true)
            diagonal(y + unit * 1.0f, unit * 0.5f)
        }
        else -> edge(y + unit * 0.15f, right = true)
    }
}

/** חוליה תימנית הפוכה: הכריכות, ומעליהן אלכסון בכיוון ההפוך. */
private fun DrawScope.drawInvertedYemenite(
    cx: Float, y: Float, w: Float, h: Float, count: Int,
    colour: Color, isTekhelet: Boolean, outline: Color
) {
    drawPlainWinds(cx, y, w, h, count, colour, isTekhelet, outline)
    drawLine(
        color = outline.copy(alpha = 0.85f),
        start = Offset(cx - w * 0.44f, y + h * 0.9f),
        end = Offset(cx + w * 0.44f, y + h * 0.1f),
        strokeWidth = 2.6f,
        cap = StrokeCap.Round
    )
}

/**
 * קשר כפול: שני איקסים עבים שמטפסים קצת אחד על השני. כל קו של כל איקס עשוי
 * זוג קווים - כחול ולבן - כי זה בדיוק מה שקורה בקשר: שני החוטים נקשרים זה בזה.
 */
private fun DrawScope.drawDoubleKnot(
    cx: Float, y: Float, w: Float, h: Float,
    tekhelet: Color, white: Color, outline: Color
) {
    val half = w * 0.46f
    val each = h * 0.62f          // גדול מ-h/2, ולכן האיקסים חופפים מעט
    val stroke = h * 0.17f
    val offset = stroke * 0.42f   // הזחה בין הקו הכחול ללבן

    repeat(2) { k ->
        val top = y + (h - each) * k + each * 0.1f
        val bot = top + each * 0.8f
        listOf(
            Offset(cx - half, top) to Offset(cx + half, bot),
            Offset(cx + half, top) to Offset(cx - half, bot)
        ).forEach { (a, b) ->
            val dx = if (b.x > a.x) offset else -offset
            drawLine(
                tekhelet, Offset(a.x, a.y - offset), Offset(b.x, b.y - offset),
                strokeWidth = stroke, cap = StrokeCap.Round
            )
            drawLine(
                white, Offset(a.x + dx * 0.3f, a.y + offset), Offset(b.x + dx * 0.3f, b.y + offset),
                strokeWidth = stroke, cap = StrokeCap.Round
            )
            drawLine(
                outline.copy(alpha = 0.35f),
                Offset(a.x + dx * 0.3f, a.y + offset), Offset(b.x + dx * 0.3f, b.y + offset),
                strokeWidth = stroke * 0.16f, cap = StrokeCap.Round
            )
        }
    }
}
