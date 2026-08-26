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
import org.tekhelet.knotadvisor.ui.theme.TekheletPalette
import org.tekhelet.knotadvisor.ui.theme.TekheletTheme

/**
 * מצייר את הגדיל מ**רכיבים**, לא כתמונה סגורה לכל שיטה.
 *
 * כל מקטע שה-GadilBuilder מייצר מצויר בנפרד לפי צורת החוליה, ולכן אפשר להרכיב
 * שיטה חדשה ומיד לראות אותה מצוירת - בלי לגעת בקוד הציור. הפרימיטיבים עצמם
 * יושבים ב-[GadilMetrics] ובקובץ GadilDraw.kt, ומשותפים עם בונה טביעת האצבע.
 *
 * ## פרופורציות
 *
 * הגדיל ארוך פי שניים מהרוחב שלו לעומת הגרסה הקודמת: `gadilW` נגזר מרוחב
 * הקנבס ואינו תלוי בגובה, ולכן הכפלת הגובה מאריכה את הגדיל בלי לעבות אותו.
 * זו הסיבה שברירת המחדל של `height` היא 640dp ולא 320.
 *
 * ## צבעים
 *
 * הצבעים נלקחים מ-[TekheletPalette] ולא מ-`colorScheme`. גוון התכלת הוא הדבר
 * היחיד באפליקציה שאסור לו להשתנות בין מצבי התאורה, כי אנשים בוחרים לפיו.
 */
@Composable
fun TzitzitVisual(
    composition: KnotComposition,
    modifier: Modifier = Modifier,
    height: Dp = 640.dp,
    showLegend: Boolean = true
) {
    val plan = GadilBuilder.plan(composition)
    val palette = TekheletTheme.palette

    Column(modifier) {
        Canvas(Modifier.fillMaxWidth().height(height)) {
            drawGadil(plan, palette)
        }
        if (showLegend) {
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LegendDot("תכלת", palette.threadTekhelet)
                LegendDot("לבן", palette.threadWhite, border = palette.threadOutline)
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

private fun DrawScope.drawGadil(plan: GadilBuilder.Plan, palette: TekheletPalette) {
    val cx = size.width / 2f
    val gadilW = (size.width * 0.30f).coerceAtMost(150f)
    val clothH = (size.height * 0.045f).coerceAtMost(26f)

    // קו מתאר מצויר רק סביב חוט הלבן. סביב התכלת הוא מלכלך את הגוון.
    fun outlineFor(c: Color): Color? =
        if (c == palette.threadTekhelet) null else palette.threadOutline.copy(alpha = 0.5f)

    drawRoundRect(
        color = palette.gadilBackdrop,
        topLeft = Offset(cx - gadilW * 0.78f, clothH),
        size = Size(gadilW * 1.56f, size.height * 0.74f),
        cornerRadius = CornerRadius(8f, 8f)
    )
    drawRoundRect(
        color = palette.threadOutline.copy(alpha = 0.35f),
        topLeft = Offset(cx - size.width * 0.22f, 0f),
        size = Size(size.width * 0.44f, clothH),
        cornerRadius = CornerRadius(5f, 5f)
    )

    var units = 0f
    plan.elements.forEach {
        units += when (it) {
            is GadilBuilder.Element.Winds -> it.piece.length.toFloat()
            GadilBuilder.Element.Knot -> GadilMetrics.KNOT_UNITS
            GadilBuilder.Element.ChulyaGap -> GadilMetrics.CHULYA_GAP_UNITS
            GadilBuilder.Element.SmallGap -> GadilMetrics.SMALL_GAP_UNITS
        }
    }
    if (units <= 0f) return

    val gadilH = size.height * 0.70f
    val u = gadilH / units
    val threadW = u * GadilMetrics.THREAD_RATIO
    var y = clothH + 3f

    drawCoreThreads(cx, clothH, clothH + gadilH + 6f, gadilW, palette.coreThread)

    plan.elements.forEach { el ->
        when (el) {
            is GadilBuilder.Element.Winds -> {
                val h = u * el.piece.length
                val colour =
                    if (el.piece.tekhelet) palette.threadTekhelet else palette.threadWhite
                when (plan.chulyaForm) {
                    ChulyaForm.YEMENITE_SELF_HOLDING -> drawYemenite(
                        cx, y, gadilW, h, el.piece.length,
                        colours = List(el.piece.length) { colour },
                        threadW = threadW,
                        outlineFor = { c -> outlineFor(c) }
                    )
                    ChulyaForm.YEMENITE_INVERTED ->
                        drawInvertedYemenite(cx, y, gadilW, h, el.piece.length, colour, threadW, outlineFor(colour))
                    else ->
                        drawPlainWinds(cx, y, gadilW, h, el.piece.length, colour, threadW, outlineFor(colour))
                }
                y += h
            }
            GadilBuilder.Element.Knot -> {
                val h = u * GadilMetrics.KNOT_UNITS
                drawDoubleKnot(
                    cx, y, gadilW, h, threadW,
                    palette.threadTekhelet, palette.threadWhite,
                    palette.threadOutline.copy(alpha = 0.5f)
                )
                y += h
            }
            GadilBuilder.Element.ChulyaGap -> y += u * GadilMetrics.CHULYA_GAP_UNITS
            GadilBuilder.Element.SmallGap -> y += u * GadilMetrics.SMALL_GAP_UNITS
        }
    }

    // הענף
    val strandTop = y + 4f
    for (i in 0 until 8) {
        val t = (i - 3.5f) / 8f
        val colour = if (i % 4 == 0) palette.threadTekhelet else palette.threadWhite
        drawLine(
            color = colour,
            start = Offset(cx + t * gadilW * 0.7f, strandTop),
            end = Offset(cx + t * size.width * 0.5f, size.height),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
        outlineFor(colour)?.let {
            drawLine(
                color = it.copy(alpha = 0.3f),
                start = Offset(cx + t * gadilW * 0.7f, strandTop),
                end = Offset(cx + t * size.width * 0.5f, size.height),
                strokeWidth = 0.7f, cap = StrokeCap.Round
            )
        }
    }
}
