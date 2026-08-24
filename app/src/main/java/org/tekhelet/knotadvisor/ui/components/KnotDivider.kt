package org.tekhelet.knotadvisor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * מוטיב עיצובי בהשראת הליפופים הכחולים-לבנים של פתיל תכלת - שורת מקטעים קצרים
 * לסירוגין (כחול/כחול בהיר), משמשת כקישוט/מפריד במקום קו רגיל באזורים נבחרים.
 */
@Composable
fun KnotDivider(modifier: Modifier = Modifier, segments: Int = 18) {
    val primary = MaterialTheme.colorScheme.primary
    val light = MaterialTheme.colorScheme.tertiary
    Row(
        modifier = modifier.fillMaxWidth().height(5.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        repeat(segments) { index ->
            val color = if (index % 3 == 0) primary else light
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(color)
            )
        }
    }
}
