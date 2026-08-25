package org.tekhelet.knotadvisor.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.tekhelet.knotadvisor.model.*

/**
 * המימוש האינטראקטיבי של התרשים.
 *
 * למה כך ולא כתמונה שאפשר להגדיל: התרשים המקורי הוא עץ החלטות, והערך שלו הוא
 * בלראות **איפה אתה עומד** בתוכו - לא בלראות את כולו בבת אחת. תרשים ענק על מסך
 * טלפון הוא בלתי קריא ממילא. לכן כאן יש שני מצבים משלימים:
 *
 *  - **מבט על**: כל חמשת הדיונים זה מתחת לזה, עם מספר השיטות שנשארו בכל שלב.
 *  - **ניווט**: בוחרים ערך בדיון, והרשימה למטה מצטמצמת בזמן אמת.
 *
 * חשוב: הבחירה כאן **מסננת תצוגה בלבד**, היא לא פוסלת. אף שיטת קשירה לא נהיית
 * לא רלוונטית בגלל בחירת מספר חוטים - ולכן תמיד מוצג גם מה נשאר בחוץ ולמה.
 */
@Composable
fun MethodTreeScreen(
    methods: List<KnotMethod>,
    onOpenMethod: (String) -> Unit
) {
    var threadCount by remember { mutableStateOf<ThreadCount?>(null) }
    var windingColor by remember { mutableStateOf<WindingColor?>(null) }
    var chulyotCount by remember { mutableStateOf<ChulyotCount?>(null) }
    var chulyaForm by remember { mutableStateOf<ChulyaForm?>(null) }
    var knotScheme by remember { mutableStateOf<KnotScheme?>(null) }

    fun matches(m: KnotMethod): Boolean {
        val c = m.composition
        if (threadCount != null && c.threadCount != threadCount) return false
        if (windingColor != null && c.windingColor != windingColor) return false
        if (chulyotCount != null && c.chulyotCount != chulyotCount) return false
        if (chulyaForm != null && c.chulyaForm != chulyaForm) return false
        if (knotScheme != null && c.knotScheme != knotScheme) return false
        return true
    }

    val matching = methods.filter { matches(it) }
    val excluded = methods.filterNot { matches(it) }
    val anyFilter = listOfNotNull(threadCount, windingColor, chulyotCount, chulyaForm, knotScheme).isNotEmpty()

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text("מפת השיטות", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(6.dp))
        Text(
            "זה התרשים שלי, רק שאפשר לנווט בו. בוחרים ערך בכל דיון והרשימה למטה " +
                "מצטמצמת. שים לב שזה סינון תצוגה בלבד - שום שיטה לא נפסלת באמת.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${matching.size} מתוך ${methods.size} שיטות", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.weight(1f))
            AnimatedVisibility(anyFilter) {
                TextButton(onClick = {
                    threadCount = null; windingColor = null; chulyotCount = null
                    chulyaForm = null; knotScheme = null
                }) { Text("נקה הכול") }
            }
        }

        Spacer(Modifier.height(10.dp))
        TreeLevel("א. מספר חוטי התכלת", ThreadCount.entries.map { it to it.label }, threadCount,
            counter = { v -> methods.count { it.composition.threadCount == v } }) { threadCount = it }
        TreeLevel("ב. צבע הכריכות", WindingColor.entries.map { it to it.label }, windingColor,
            counter = { v -> methods.count { it.composition.windingColor == v } }) { windingColor = it }
        TreeLevel("ג. מספר החוליות", ChulyotCount.entries.map { it to it.label }, chulyotCount,
            counter = { v -> methods.count { it.composition.chulyotCount == v } }) { chulyotCount = it }
        TreeLevel("ד. צורת החוליה", ChulyaForm.entries.map { it to it.label }, chulyaForm,
            counter = { v -> methods.count { it.composition.chulyaForm == v } }) { chulyaForm = it }
        TreeLevel("ה. שילוב הקשרים", KnotScheme.entries.map { it to it.label }, knotScheme,
            counter = { v -> methods.count { it.composition.knotScheme == v } }) { knotScheme = it }

        Spacer(Modifier.height(18.dp))
        Text("מתאימות", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        if (matching.isEmpty()) {
            Text(
                "אין שיטה מוכרת שעונה בדיוק על הצירוף הזה - וזה בסדר גמור. " +
                    "זה בדיוק המצב שבו כדאי לגשת לבונה ההרכב האישי.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        matching.forEach { m ->
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), onClick = { onOpenMethod(m.id) }) {
                Column(Modifier.padding(14.dp)) {
                    Text(m.name, style = MaterialTheme.typography.titleSmall)
                    Text(m.shortSummary, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        AnimatedVisibility(anyFilter && excluded.isNotEmpty()) {
            Column {
                Spacer(Modifier.height(14.dp))
                Text("לא תואמות לסינון הנוכחי", style = MaterialTheme.typography.titleSmall)
                Text(
                    "מוצגות כאן בכוונה: הן לא פסולות, הן פשוט מכריעות אחרת.",
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(Modifier.height(8.dp))
                excluded.forEach { m ->
                    TextButton(onClick = { onOpenMethod(m.id) }) { Text("• ${m.name}") }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun <T> TreeLevel(
    title: String,
    options: List<Pair<T, String>>,
    selected: T?,
    counter: (T) -> Int,
    onSelect: (T?) -> Unit
) {
    Column(Modifier.padding(bottom = 12.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(6.dp))
        FlowRowSimple {
            options.forEach { (value, label) ->
                val n = counter(value)
                FilterChip(
                    selected = value == selected,
                    onClick = { onSelect(if (value == selected) null else value) },
                    label = { Text("$label ($n)", style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.padding(end = 6.dp, bottom = 6.dp)
                )
            }
        }
    }
}

/**
 * עטיפה לגלישת שורות. FlowRow עדיין מסומן כניסיוני, ולכן ה-OptIn מרוכז כאן בלבד -
 * והחתימה מקבלת lambda רגילה ולא FlowRowScope, כדי לא לדלוף את הטיפוס הניסיוני
 * הלאה לקוראים ולחייב גם אותם ב-OptIn.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRowSimple(content: @Composable () -> Unit) {
    FlowRow { content() }
}
