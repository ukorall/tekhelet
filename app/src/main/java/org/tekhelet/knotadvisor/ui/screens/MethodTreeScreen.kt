package org.tekhelet.knotadvisor.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.tekhelet.knotadvisor.model.*
import org.tekhelet.knotadvisor.ui.components.KnotDivider
import org.tekhelet.knotadvisor.ui.components.TzitzitVisual

/**
 * מפת השיטות - הגרסה הניתנת לניווט של התרשים.
 *
 * המבנה מחקה את התרשים המקורי: שאלה, מתחתיה הענפים שיוצאים ממנה, וקווים
 * שמחברים ביניהם. יורדים שלב-שלב, ובכל שלב רואים כמה שיטות עדיין "חיות"
 * בענף הזה, ואת הגדילים עצמם מצוירים.
 *
 * הבדל מכוון מבונה ההרכב: כאן **לא בונים** שיטה, אלא **מנווטים** בין שיטות
 * קיימות. ולכן הבחירה מסננת תצוגה בלבד - שום שיטה לא נפסלת, ומה שיצא מהענף
 * הנוכחי עדיין מוצג למטה.
 */
@Composable
fun MethodTreeScreen(
    methods: List<KnotMethod>,
    onOpenMethod: (String) -> Unit
) {
    var threadCount by remember { mutableStateOf<ThreadCount?>(null) }
    var windingColor by remember { mutableStateOf<WindingColor?>(null) }
    var chulyotCount by remember { mutableStateOf<ChulyotCount?>(null) }
    var knotScheme by remember { mutableStateOf<KnotScheme?>(null) }

    fun survives(m: KnotMethod, upTo: Int): Boolean {
        val c = m.composition
        if (upTo >= 1 && threadCount != null && c.threadCount != threadCount) return false
        if (upTo >= 2 && windingColor != null && c.windingColor != windingColor) return false
        if (upTo >= 3 && chulyotCount != null && c.chulyotCount != chulyotCount) return false
        if (upTo >= 4 && knotScheme != null && c.knotScheme != knotScheme) return false
        return true
    }

    val matching = methods.filter { survives(it, 4) }
    val excluded = methods.filterNot { survives(it, 4) }
    val anyFilter = listOfNotNull(threadCount, windingColor, chulyotCount, knotScheme).isNotEmpty()

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(12.dp))
        Text("מפת השיטות", style = MaterialTheme.typography.headlineSmall)
        KnotDivider(modifier = Modifier.padding(top = 10.dp, bottom = 10.dp))
        Text(
            "זה התרשים שלי, רק שאפשר לרדת בו שלב-שלב. בכל צומת בוחרים ענף, " +
                "והמפה מצטמצמת. שום שיטה לא נפסלת - מה שיוצא מהענף פשוט יורד למטה. " +
                "מוצגים כאן רק ענפים שיש להם שיטה בפועל; אפשרויות תיאורטיות נוספות " +
                "קיימות בבונה ההרכב האישי.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    "${matching.size} מתוך ${methods.size} שיטות",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Spacer(Modifier.weight(1f))
            AnimatedVisibility(anyFilter) {
                TextButton(onClick = {
                    threadCount = null; windingColor = null; chulyotCount = null; knotScheme = null
                }) { Text("לנקות הכול") }
            }
        }

        Spacer(Modifier.height(6.dp))
        RootNode()

        TreeNode(
            question = "כמה חוטי תכלת?",
            branches = ThreadCount.entries.mapNotNull { v ->
                val n = methods.count { it.composition.threadCount == v && survives(it, 0) }
                val exists = methods.any { it.composition.threadCount == v }
                if (exists) Branch(v, v.label, n) else null
            },
            selected = threadCount,
            onSelect = { threadCount = it }
        )
        TreeNode(
            question = "איך נראות הכריכות?",
            branches = WindingColor.entries.mapNotNull { v ->
                val n = methods.count { it.composition.windingColor == v && survives(it, 1) }
                if (methods.any { it.composition.windingColor == v }) Branch(v, v.label, n) else null
            },
            selected = windingColor,
            onSelect = { windingColor = it }
        )
        TreeNode(
            question = "כמה חוליות?",
            branches = ChulyotCount.entries.mapNotNull { v ->
                val n = methods.count { it.composition.chulyotCount == v && survives(it, 2) }
                if (methods.any { it.composition.chulyotCount == v }) Branch(v, v.label, n) else null
            },
            selected = chulyotCount,
            onSelect = { chulyotCount = it }
        )
        TreeNode(
            question = "ואיפה הקשרים?",
            branches = KnotScheme.entries.mapNotNull { v ->
                val n = methods.count { it.composition.knotScheme == v && survives(it, 3) }
                if (methods.any { it.composition.knotScheme == v }) Branch(v, v.label, n) else null
            },
            selected = knotScheme,
            onSelect = { knotScheme = it },
            isLast = true
        )

        Spacer(Modifier.height(14.dp))
        Text("השיטות שנשארו", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        if (matching.isEmpty()) {
            Card(Modifier.fillMaxWidth()) {
                Text(
                    "אין שיטה מוכרת שעונה בדיוק על הצירוף הזה - וזה בסדר גמור. " +
                        "בדיוק בשביל זה יש את בונה ההרכב האישי.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(14.dp)
                )
            }
        } else {
            Row(Modifier.horizontalScroll(rememberScrollState())) {
                matching.forEach { m ->
                    MethodLeaf(m) { onOpenMethod(m.id) }
                    Spacer(Modifier.width(10.dp))
                }
            }
        }

        AnimatedVisibility(anyFilter && excluded.isNotEmpty()) {
            Column {
                Spacer(Modifier.height(16.dp))
                Text("יצאו מהענף הזה", style = MaterialTheme.typography.titleSmall)
                Text(
                    "מוצגות כאן בכוונה - הן לא פסולות, הן פשוט מכריעות אחרת.",
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(Modifier.height(6.dp))
                excluded.forEach { m ->
                    TextButton(onClick = { onOpenMethod(m.id) }) { Text("• ${m.name}") }
                }
            }
        }
        Spacer(Modifier.height(28.dp))
    }
}

private data class Branch<T>(val value: T, val label: String, val count: Int)

@Composable
private fun RootNode() {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            color = MaterialTheme.colorScheme.primary,
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                "קשירת ציצית בתכלת",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

/** צומת אחד בעץ: שאלה, קו יורד, ושורת ענפים. */
@Composable
private fun <T> TreeNode(
    question: String,
    branches: List<Branch<T>>,
    selected: T?,
    onSelect: (T?) -> Unit,
    isLast: Boolean = false
) {
    Connector()
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            question,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center
        )
    }
    Spacer(Modifier.height(6.dp))
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
        branches.forEach { b ->
            val isSelected = b.value == selected
            val dimmed = b.count == 0
            Surface(
                color = when {
                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                    dimmed -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    else -> MaterialTheme.colorScheme.surface
                },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .width(150.dp)
                    .padding(end = 8.dp)
                    .then(
                        if (isSelected) Modifier.border(
                            2.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium
                        ) else Modifier.border(
                            1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            MaterialTheme.shapes.medium
                        )
                    ),
                onClick = { onSelect(if (isSelected) null else b.value) }
            ) {
                Column(Modifier.padding(10.dp)) {
                    Text(
                        b.label,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 3
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        if (b.count == 0) "אין שיטה כזאת" else "${b.count} שיטות",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
    if (!isLast) Spacer(Modifier.height(2.dp))
}

/** קו אנכי שמחבר בין צמתים, כמו בתרשים המקורי. */
@Composable
private fun Connector() {
    val color = MaterialTheme.colorScheme.outline
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(Modifier.height(18.dp).width(2.dp)) {
            drawLine(
                color = color,
                start = Offset(size.width / 2f, 0f),
                end = Offset(size.width / 2f, size.height),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
        }
    }
}

/** "עלה" בעץ - שיטה, עם הגדיל שלה מצויר, כמו התצלומים בתחתית התרשים. */
@Composable
private fun MethodLeaf(method: KnotMethod, onClick: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .width(128.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), MaterialTheme.shapes.medium),
        onClick = onClick
    ) {
        Column(
            Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                method.name,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Spacer(Modifier.height(6.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                TzitzitVisual(method.composition, height = 190.dp, showLegend = false)
            }
        }
    }
}
