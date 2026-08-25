package org.tekhelet.knotadvisor.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import org.tekhelet.knotadvisor.logic.GadilBuilder
import org.tekhelet.knotadvisor.logic.GadilSegment
import org.tekhelet.knotadvisor.model.KnotComposition

/**
 * מצייר את הגדיל בפועל לפי ההרכב: כל כריכה, כל קשר, בצבע הנכון ובסדר הנכון.
 * זו התשובה המיידית לשאלה "איך זה ייראה", בלי להמתין לתצלומים.
 */
@Composable
fun TzitzitVisual(
    composition: KnotComposition,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 300.dp
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
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            LegendDot("תכלת", tekhelet)
            LegendDot("לבן", white, border = outline)
            Text("| קו רוחב = קשר כפול", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun LegendDot(label: String, color: Color, border: Color? = null) {
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
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
    val clothH = size.height * 0.09f

    // פינת הבגד
    drawRoundRect(
        color = cloth,
        topLeft = Offset(cx - size.width * 0.24f, 0f),
        size = Size(size.width * 0.48f, clothH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
    )

    val windUnits = segments.count { it is GadilSegment.Wind }
    val knotUnits = segments.count { it is GadilSegment.Knot }
    // הגדיל תופס כשני שליש מהגובה, והענף (החוטים החופשיים) את השאר
    val gadilH = size.height * 0.62f
    val totalUnits = windUnits + knotUnits * 2.1f
    if (totalUnits <= 0f) return
    val unit = gadilH / totalUnits

    val gadilW = size.width * 0.19f
    var y = clothH + 4f

    segments.forEach { seg ->
        when (seg) {
            is GadilSegment.Wind -> {
                val h = unit
                drawRoundRect(
                    color = if (seg.tekhelet) tekhelet else white,
                    topLeft = Offset(cx - gadilW / 2f, y),
                    size = Size(gadilW, h * 0.94f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.5f, 2.5f)
                )
                if (!seg.tekhelet) {
                    drawRoundRect(
                        color = outline.copy(alpha = 0.45f),
                        topLeft = Offset(cx - gadilW / 2f, y),
                        size = Size(gadilW, h * 0.94f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.5f, 2.5f),
                        style = Stroke(width = 0.9f)
                    )
                }
                y += h
            }
            is GadilSegment.Knot -> {
                val h = unit * 2.1f
                // קשר כפול - מצויר כמעט-אליפסה רחבה יותר מהגדיל
                drawRoundRect(
                    color = outline,
                    topLeft = Offset(cx - gadilW * 0.78f, y + h * 0.12f),
                    size = Size(gadilW * 1.56f, h * 0.72f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(h * 0.36f, h * 0.36f)
                )
                y += h
            }
            is GadilSegment.Gap -> {
                y += unit * 0.55f
            }
        }
    }

    // הענף - החוטים החופשיים
    val strandTop = y + 2f
    val strandBottom = size.height
    val strands = 8
    for (i in 0 until strands) {
        val t = (i - (strands - 1) / 2f) / strands
        val xTop = cx + t * gadilW * 0.9f
        val xBottom = cx + t * size.width * 0.55f
        drawLine(
            color = if (i % 4 == 0) tekhelet else white,
            start = Offset(xTop, strandTop),
            end = Offset(xBottom, strandBottom),
            strokeWidth = 3.2f,
            cap = StrokeCap.Round
        )
        if (i % 4 != 0) {
            drawLine(
                color = outline.copy(alpha = 0.35f),
                start = Offset(xTop, strandTop),
                end = Offset(xBottom, strandBottom),
                strokeWidth = 0.8f,
                cap = StrokeCap.Round
            )
        }
    }
}
