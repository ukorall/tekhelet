package org.tekhelet.knotadvisor.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.tekhelet.knotadvisor.logic.GadilBuilder
import org.tekhelet.knotadvisor.logic.GadilSegment
import org.tekhelet.knotadvisor.model.KnotComposition

/**
 * מצייר את הגדיל לפי ההרכב, במוסכמות הציור שאוריאל הגדיר:
 * קשר כפול = X כפול, חוליה תימנית = חצי ליפוף + אלכסון + חצי ליפוף,
 * חוליה תימנית הפוכה = קו אלכסוני על הליפופים, וברמב"ם רווח שדרכו רואים
 * את החוטים המאונכים.
 */
@Composable
fun TzitzitVisual(
    composition: KnotComposition,
    modifier: Modifier = Modifier,
    height: Dp = 320.dp,
    showLegend: Boolean = true
) {
    val segments = GadilBuilder.build(composition)
    val tekhelet = MaterialTheme.colorScheme.primary
    val white = Color(0xFFFDFDFD)
    val outline = MaterialTheme.colorScheme.outline
    val cloth = MaterialTheme.colorScheme.surfaceVariant

    Column(modifier) {
        Canvas(Modifier.fillMaxWidth().height(height)) {
            drawGadil(segments, tekhelet, white, outline, cloth)
        }
        if (showLegend) {
            Spacer(Modifier.height(8.dp))
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    LegendDot("תכלת", tekhelet)
                    LegendDot("לבן", white, border = outline)
                }
                Spacer(Modifier.height(3.dp))
                Text("✕✕ = קשר כפול", style = MaterialTheme.typography.labelSmall)
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
    segments: List<GadilSegment>,
    tekhelet: Color,
    white: Color,
    outline: Color,
    cloth: Color
) {
    val cx = size.width / 2f
    val clothH = size.height * 0.08f
    val gadilW = size.width * 0.20f
    val halfW = gadilW / 2f

    drawRoundRect(
        color = cloth,
        topLeft = Offset(cx - size.width * 0.24f, 0f),
        size = Size(size.width * 0.48f, clothH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
    )

    // מחשבים כמה "יחידות ליפוף" יש בסך הכול, כדי לפרוס אותן על גובה הגדיל
    var units = 0f
    segments.forEach {
        units += when (it) {
            is GadilSegment.Wind -> 1f
            is GadilSegment.YemeniteChulya -> 3f
            is GadilSegment.InvertedYemenite -> it.windCount.toFloat()
            is GadilSegment.Knot -> 2.4f
            GadilSegment.ChulyaGap -> 3f
            GadilSegment.SmallGap -> 0.7f
        }
    }
    if (units <= 0f) return

    val gadilH = size.height * 0.60f
    val u = gadilH / units
    var y = clothH + 4f

    segments.forEach { seg ->
        when (seg) {
            is GadilSegment.Wind -> {
                drawWind(cx, y, gadilW, u, if (seg.tekhelet) tekhelet else white, seg.tekhelet, outline)
                y += u
            }

            is GadilSegment.YemeniteChulya -> {
                val h = u * 3f
                val col = if (seg.tekhelet) tekhelet else white
                if (seg.mixedWithWhite) {
                    // חלק לבן = ליפוף "שבור", ואחריו החלק התכלת כמבנה התימני
                    drawBrokenWind(cx, y, gadilW, u, white, outline)
                    drawYemenite(cx, y + u, gadilW, u * 2f, col, seg.tekhelet, outline)
                } else {
                    drawYemenite(cx, y, gadilW, h, col, seg.tekhelet, outline)
                }
                y += h
            }

            is GadilSegment.InvertedYemenite -> {
                val h = u * seg.windCount
                val col = if (seg.tekhelet) tekhelet else white
                repeat(seg.windCount) { k ->
                    drawWind(cx, y + u * k, gadilW, u, col, seg.tekhelet, outline)
                }
                // הסימון של "הפוכה": קו אלכסוני על גבי הליפופים
                drawLine(
                    color = outline,
                    start = Offset(cx - halfW * 0.92f, y + h * 0.92f),
                    end = Offset(cx + halfW * 0.92f, y + h * 0.08f),
                    strokeWidth = 2.6f,
                    cap = StrokeCap.Round
                )
                y += h
            }

            is GadilSegment.Knot -> {
                val h = u * 2.4f
                drawDoubleX(cx, y, gadilW, h, outline)
                y += h
            }

            GadilSegment.ChulyaGap -> {
                // רווח שדרכו רואים את החוטים המאונכים שסביבם מלפפים
                val h = u * 3f
                listOf(-0.55f, -0.18f, 0.18f, 0.55f).forEach { t ->
                    drawLine(
                        color = outline.copy(alpha = 0.5f),
                        start = Offset(cx + t * gadilW, y),
                        end = Offset(cx + t * gadilW, y + h),
                        strokeWidth = 2.2f,
                        cap = StrokeCap.Round
                    )
                }
                y += h
            }

            GadilSegment.SmallGap -> y += u * 0.7f
        }
    }

    // הענף - החוטים החופשיים
    val strandTop = y + 3f
    val strands = 8
    for (i in 0 until strands) {
        val t = (i - (strands - 1) / 2f) / strands
        drawLine(
            color = if (i % 4 == 0) tekhelet else white,
            start = Offset(cx + t * gadilW * 0.9f, strandTop),
            end = Offset(cx + t * size.width * 0.55f, size.height),
            strokeWidth = 3.2f,
            cap = StrokeCap.Round
        )
        if (i % 4 != 0) {
            drawLine(
                color = outline.copy(alpha = 0.3f),
                start = Offset(cx + t * gadilW * 0.9f, strandTop),
                end = Offset(cx + t * size.width * 0.55f, size.height),
                strokeWidth = 0.8f,
                cap = StrokeCap.Round
            )
        }
    }
}

/** ליפוף פשוט - מלבן אופקי לרוחב הגדיל. */
private fun DrawScope.drawWind(
    cx: Float, y: Float, w: Float, h: Float, color: Color, isTekhelet: Boolean, outline: Color
) {
    drawRoundRect(
        color = color,
        topLeft = Offset(cx - w / 2f, y),
        size = Size(w, h * 0.9f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.5f, 2.5f)
    )
    if (!isTekhelet) {
        drawRoundRect(
            color = outline.copy(alpha = 0.45f),
            topLeft = Offset(cx - w / 2f, y),
            size = Size(w, h * 0.9f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.5f, 2.5f),
            style = Stroke(width = 0.9f)
        )
    }
}

/** ליפוף "שבור" - החלק הלבן של חוליה תימנית משולבת. */
private fun DrawScope.drawBrokenWind(
    cx: Float, y: Float, w: Float, h: Float, color: Color, outline: Color
) {
    val half = w / 2f
    val gap = w * 0.14f
    drawRoundRect(
        color = color,
        topLeft = Offset(cx - half, y),
        size = Size(half - gap / 2f, h * 0.9f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
    )
    drawRoundRect(
        color = color,
        topLeft = Offset(cx + gap / 2f, y),
        size = Size(half - gap / 2f, h * 0.9f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
    )
    drawRoundRect(
        color = outline.copy(alpha = 0.45f),
        topLeft = Offset(cx - half, y),
        size = Size(half - gap / 2f, h * 0.9f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f),
        style = Stroke(width = 0.9f)
    )
    drawRoundRect(
        color = outline.copy(alpha = 0.45f),
        topLeft = Offset(cx + gap / 2f, y),
        size = Size(half - gap / 2f, h * 0.9f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f),
        style = Stroke(width = 0.9f)
    )
}

/**
 * חוליה תימנית: חצי ליפוף ימני, שני ליפופים באלכסון שמתחיל משמאל ויורד לימין,
 * וחצי ליפוף שמאלי. הכול כמבנה אחד.
 */
private fun DrawScope.drawYemenite(
    cx: Float, y: Float, w: Float, h: Float, color: Color, isTekhelet: Boolean, outline: Color
) {
    val half = w / 2f
    val unit = h / 3f
    val stroke = unit * 0.72f

    // חצי ליפוף ימני (בתחילת המבנה)
    drawRoundRect(
        color = color,
        topLeft = Offset(cx, y),
        size = Size(half, stroke),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
    )
    // שני ליפופים באלכסון - משמאל למעלה לימין למטה
    drawLine(
        color = color,
        start = Offset(cx - half * 0.95f, y + unit * 0.5f),
        end = Offset(cx + half * 0.95f, y + unit * 2.4f),
        strokeWidth = stroke,
        cap = StrokeCap.Round
    )
    // חצי ליפוף שמאלי (בסוף המבנה)
    drawRoundRect(
        color = color,
        topLeft = Offset(cx - half, y + h - stroke),
        size = Size(half, stroke),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
    )

    if (!isTekhelet) {
        drawRoundRect(
            color = outline.copy(alpha = 0.5f),
            topLeft = Offset(cx, y),
            size = Size(half, stroke),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f),
            style = Stroke(width = 0.9f)
        )
        drawRoundRect(
            color = outline.copy(alpha = 0.5f),
            topLeft = Offset(cx - half, y + h - stroke),
            size = Size(half, stroke),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f),
            style = Stroke(width = 0.9f)
        )
    }
}

/** קשר כפול - שני X זה מתחת לזה. */
private fun DrawScope.drawDoubleX(cx: Float, y: Float, w: Float, h: Float, color: Color) {
    val half = w * 0.42f
    val each = h / 2f
    repeat(2) { k ->
        val top = y + each * k + each * 0.12f
        val bot = y + each * (k + 1) - each * 0.12f
        drawLine(color, Offset(cx - half, top), Offset(cx + half, bot), strokeWidth = 3.4f, cap = StrokeCap.Round)
        drawLine(color, Offset(cx + half, top), Offset(cx - half, bot), strokeWidth = 3.4f, cap = StrokeCap.Round)
    }
}
