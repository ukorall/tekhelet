package org.tekhelet.knotadvisor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.tekhelet.knotadvisor.logic.GadilBuilder
import org.tekhelet.knotadvisor.logic.ThreadLength
import org.tekhelet.knotadvisor.logic.TyingInstructions
import org.tekhelet.knotadvisor.model.KnotComposition
import org.tekhelet.knotadvisor.model.KnotMethod
import org.tekhelet.knotadvisor.model.ThreadCount
import org.tekhelet.knotadvisor.ui.components.KnotDivider
import org.tekhelet.knotadvisor.ui.components.TzitzitVisual

/**
 * "איך בפועל" - מה לקנות ואיך לקשור. הכל נגזר מההרכב שנבחר, כך שההוראות תמיד
 * תואמות בדיוק לשיטה ולא לשיטה כללית כלשהי.
 */
@Composable
fun TyingGuideScreen(
    methods: List<KnotMethod>,
    initialComposition: KnotComposition,
    onOpenBuilder: () -> Unit
) {
    var selected by remember {
        mutableStateOf(
            methods.firstOrNull { it.composition == initialComposition }
                ?: methods.firstOrNull()
        )
    }
    var useCustom by remember { mutableStateOf(initialComposition.threadCount != null) }
    val composition = if (useCustom) initialComposition else (selected?.composition ?: KnotComposition())

    val steps = remember(composition) { TyingInstructions.generate(composition) }
    val estimate = remember(composition) { ThreadLength.estimate(composition) }
    val summary = remember(composition) { GadilBuilder.summary(composition) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text("איך בפועל", style = MaterialTheme.typography.headlineSmall)
        KnotDivider(modifier = Modifier.padding(top = 10.dp, bottom = 12.dp))
        Text(
            "אחרי שהחלטת - כאן נמצא מה לקנות ואיך לקשור את זה בפועל. ההוראות למטה " +
                "נגזרות מההרכב שבחרת, אז הן מדויקות לשיטה שלך ולא כלליות.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(18.dp))
        Text("לפי איזה הרכב", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        if (initialComposition.threadCount != null) {
            FilterChip(
                selected = useCustom,
                onClick = { useCustom = true },
                label = { Text("ההרכב שבניתי") },
                modifier = Modifier.padding(end = 6.dp)
            )
        }
        methods.forEach { m ->
            FilterChip(
                selected = !useCustom && selected?.id == m.id,
                onClick = { selected = m; useCustom = false },
                label = { Text(m.name, style = MaterialTheme.typography.labelSmall) },
                modifier = Modifier.padding(end = 6.dp, bottom = 4.dp)
            )
        }
        TextButton(onClick = onOpenBuilder) { Text("או לבנות הרכב משלי") }

        // ---- מה לקנות ----
        Spacer(Modifier.height(20.dp))
        SectionTitle("מה לקנות")
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(productLine(composition.threadCount), style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(10.dp))
                Text("אורך החוט", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                estimate.notes.forEach {
                    Text("• $it", style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 4.dp))
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "המספרים כאן הם קירוב שימושי לקנייה, לא פסק. גם שיעור \"אגודל\" עצמו " +
                        "שנוי במחלוקת, ולכן זה טווח ולא מספר יחיד.",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        Card(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(14.dp)) {
                Text("להשלמה", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                Text(
                    "כאן צריך להיכנס מידע על היצרנים והמוכרים בפועל, מה כל אחד מהם " +
                        "מתכוון כשהוא כותב \"ראב\"ד\" סתם, ומה ההבדל בין המוצרים. " +
                        "גם ההנחות והמבצעים שייכים לכאן.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // ---- הציור ----
        Spacer(Modifier.height(22.dp))
        SectionTitle("איך זה ייראה")
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                TzitzitVisual(composition, height = 340.dp)
                Spacer(Modifier.height(10.dp))
                Text(
                    "${summary.chulyot} חוליות · ${summary.totalWinds} כריכות · " +
                        "${summary.doubleKnots} קשרים כפולים",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // ---- ההוראות ----
        Spacer(Modifier.height(22.dp))
        SectionTitle("צעד אחר צעד")
        steps.forEach { step ->
            Row(Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        "${step.number}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(step.text, style = MaterialTheme.typography.bodyMedium)
                    step.note?.let {
                        Spacer(Modifier.height(3.dp))
                        Text(it, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Card(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(14.dp)) {
                Text("להשלמה", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                Text(
                    "כאן צריכות להיכנס הדרכות ספציפיות לכל סוג חוליה וקשירה - " +
                        "תמונות שלב-אחר-שלב או סרטונים קצרים, עם תפריט פנימי לפי סוג. " +
                        "בינתיים יש את הציור ואת ההוראות המילוליות.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
}

private fun productLine(tc: ThreadCount?): String = when (tc) {
    ThreadCount.RAMBAM_1_OF_8 ->
        "אתה צריך ערכה שבה חוט אחד צבוע בחציו בתכלת - זה מה שנמכר בדרך כלל כ\"רמב\"ם\". " +
            "שים לב שאסור לחבר חוט תכלת לחוט לבן כדי לייצר חוט כזה בעצמך, כי יש בזה חשש בל תוסיף."
    ThreadCount.RAAVAD_2_OF_8 ->
        "אתה צריך חוט תכלת אחד שלם ושלושה חוטי לבן - זה מה שנמכר כ\"ראב\"ד\". " +
            "כדאי לוודא מול המוכר מה בדיוק הוא מתכוון: יש \"ראב\"ד קצר\" ו\"ראב\"ד ארוך\", והם נבדלים באורך."
    ThreadCount.TOSAFOT_4_OF_8 ->
        "אתה צריך שני חוטי תכלת שלמים ושני חוטי לבן - נמכר כ\"תוספות\". " +
            "זו האפשרות היקרה ביותר, בדרך כלל בערך פי שניים מראב\"ד."
    null -> "קודם צריך להכריע כמה חוטים יהיו תכלת - זה מה שקובע איזה מוצר לקנות."
}
