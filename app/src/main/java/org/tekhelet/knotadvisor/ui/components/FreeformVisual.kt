package org.tekhelet.knotadvisor.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.tekhelet.knotadvisor.logic.FreeformGadil.SegmentKind
import org.tekhelet.knotadvisor.ui.theme.TekheletPalette
import org.tekhelet.knotadvisor.ui.theme.TekheletTheme

/**
 * מצייר רצף מקטעים חופשי - הציור של "טביעת אצבע".
 *
 * משתמש **באותם פרימיטיבים** של הציור ההלכתי (GadilDraw.kt), ולכן קשר כפול
 * נראה כאן בדיוק כמו במסך של שיטה אמיתית. ההבדל היחיד הוא מאיפה מגיע רצף
 * המקטעים: שם הוא נגזר מהרכב הלכתי, כאן הוא נבחר ידנית.
 */
@Composable
fun FreeformVisual(
    segments: List<SegmentKind>,
    modifier: Modifier = Modifier,
    height: Dp = 640.dp
) {
    val palette = TekheletTheme.palette
    Canvas(modifier.fillMaxWidth().height(height)) {
        drawFreeform(segments, palette)
    }
}

private fun DrawScope.drawFreeform(segments: List<SegmentKind>, palette: TekheletPalette) {
    if (segments.isEmpty()) return
    val cx = size.width / 2f
    val gadilW = (size.width * 0.30f).coerceAtMost(150f)
    val clothH = (size.height * 0.045f).coerceAtMost(26f)

    fun outlineFor(c: Color): Color? =
        if (c == palette.threadWhite) palette.threadOutline.copy(alpha = 0.5f) else null

    drawRoundRect(
        color = palette.gadilBackdrop,
        topLeft = Offset(cx - gadilW * 0.78f, clothH),
        size = Size(gadilW * 1.56f, size.height * 0.82f),
        cornerRadius = CornerRadius(8f, 8f)
    )

    val units = segments.sumOf { it.units.toDouble() }.toFloat()
    if (units <= 0f) return
    val gadilH = size.height * 0.80f
    val u = gadilH / units
    val threadW = u * GadilMetrics.THREAD_RATIO

    drawCoreThreads(cx, clothH, clothH + gadilH + 6f, gadilW, palette.coreThread)

    val tek = palette.threadTekhelet
    val wht = palette.threadWhite
    val red = palette.odd.first()

    var y = clothH + 3f
    segments.forEach { seg ->
        val h = u * seg.units
        when (seg) {
            SegmentKind.CHULYA_TEKHELET ->
                drawPlainWinds(cx, y, gadilW, h, 3, tek, threadW, outlineFor(tek))
            SegmentKind.CHULYA_WHITE ->
                drawPlainWinds(cx, y, gadilW, h, 3, wht, threadW, outlineFor(wht))
            SegmentKind.CHULYA_ALTERNATING -> {
                val each = h / 3f
                listOf(wht, tek, wht).forEachIndexed { i, c ->
                    drawWindBar(cx, y + each * i + (each - threadW) / 2f, gadilW, threadW, c, outlineFor(c))
                }
            }
            SegmentKind.YEMENITE_TEKHELET ->
                drawYemenite(cx, y, gadilW, h, 3, List(3) { tek }, threadW) { c -> outlineFor(c) }
            SegmentKind.YEMENITE_WHITE ->
                drawYemenite(cx, y, gadilW, h, 3, List(3) { wht }, threadW) { c -> outlineFor(c) }
            SegmentKind.YEMENITE_MIXED_FIRST ->
                drawYemenite(cx, y, gadilW, h, 3, listOf(wht, tek, tek), threadW) { c -> outlineFor(c) }
            SegmentKind.YEMENITE_MIXED_LAST ->
                drawYemenite(cx, y, gadilW, h, 3, listOf(tek, tek, wht), threadW) { c -> outlineFor(c) }
            SegmentKind.YEMENITE_INVERTED ->
                drawInvertedYemenite(cx, y, gadilW, h, 3, tek, threadW, outlineFor(tek))

            SegmentKind.WIND_TEKHELET ->
                drawPlainWinds(cx, y, gadilW, h, 1, tek, threadW, outlineFor(tek))
            SegmentKind.WIND_WHITE ->
                drawPlainWinds(cx, y, gadilW, h, 1, wht, threadW, outlineFor(wht))
            SegmentKind.WINDS_TEKHELET_2 ->
                drawPlainWinds(cx, y, gadilW, h, 2, tek, threadW, outlineFor(tek))
            SegmentKind.WINDS_WHITE_2 ->
                drawPlainWinds(cx, y, gadilW, h, 2, wht, threadW, outlineFor(wht))

            SegmentKind.KNOT_DOUBLE ->
                drawDoubleKnot(cx, y, gadilW, h, threadW, tek, wht, outlineFor(wht))

            SegmentKind.GAP_CHULYA, SegmentKind.GAP_SMALL -> Unit

            SegmentKind.ODD_SPLIT_3 ->
                drawSplitWinds(cx, y, gadilW, h, 3, wht, tek, threadW, outlineFor(wht))
            SegmentKind.ODD_RED_CHULYA ->
                drawPlainWinds(cx, y, gadilW, h, 3, red, threadW, null)
            SegmentKind.ODD_RED_WIND ->
                drawPlainWinds(cx, y, gadilW, h, 1, red, threadW, null)
            SegmentKind.ODD_YEMENITE_RED ->
                drawYemenite(cx, y, gadilW, h, 3, List(3) { red }, threadW) { null }
            SegmentKind.ODD_RAINBOW_3 -> {
                val each = h / 3f
                palette.odd.take(3).forEachIndexed { i, c ->
                    drawWindBar(cx, y + each * i + (each - threadW) / 2f, gadilW, threadW, c, null)
                }
            }
            SegmentKind.ODD_FLAT_KNOT ->
                drawFlatKnot(cx, y, gadilW, h, threadW, palette.odd[1], tek)
            SegmentKind.ODD_BOW ->
                drawBow(cx, y, gadilW, h, threadW, palette.odd[2])
            SegmentKind.ODD_SPIRAL ->
                drawSpiral(cx, y, gadilW, h, threadW, palette.odd[3])
        }
        y += h
    }

    // הענף
    val strandTop = y + 4f
    for (i in 0 until 8) {
        val t = (i - 3.5f) / 8f
        drawLine(
            color = if (i % 4 == 0) tek else wht,
            start = Offset(cx + t * gadilW * 0.7f, strandTop),
            end = Offset(cx + t * size.width * 0.5f, size.height),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
    }
}
