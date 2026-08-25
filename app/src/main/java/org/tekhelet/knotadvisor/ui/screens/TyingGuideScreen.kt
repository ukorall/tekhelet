package org.tekhelet.knotadvisor.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.tekhelet.knotadvisor.logic.GadilBuilder
import org.tekhelet.knotadvisor.logic.Products
import org.tekhelet.knotadvisor.logic.ThreadLength
import org.tekhelet.knotadvisor.logic.TyingInstructions
import org.tekhelet.knotadvisor.model.KnotComposition
import org.tekhelet.knotadvisor.model.KnotMethod
import org.tekhelet.knotadvisor.ui.components.KnotDivider
import org.tekhelet.knotadvisor.ui.components.TzitzitVisual

/**
 * "איך בפועל" - מה לקנות ואיך לקשור. הכול נגזר מההרכב שנבחר, כולל המלצת המוצר
 * ומחשבון "יש לי מספיק חוט".
 */
@Composable
fun TyingGuideScreen(
    methods: List<KnotMethod>,
    initialComposition: KnotComposition,
    onOpenBuilder: () -> Unit
) {
    var selected by remember {
        mutableStateOf(methods.firstOrNull { it.composition == initialComposition } ?: methods.firstOrNull())
    }
    var useCustom by remember { mutableStateOf(initialComposition.threadCount != null) }
    val composition = if (useCustom) initialComposition else (selected?.composition ?: KnotComposition())

    val steps = remember(composition) { TyingInstructions.generate(composition) }
    val summary = remember(composition) { GadilBuilder.summary(composition) }
    val recommendation = remember(composition) { Products.recommend(composition) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text("איך בפועל", style = MaterialTheme.typography.headlineSmall)
        KnotDivider(modifier = Modifier.padding(top = 10.dp, bottom = 12.dp))
        Text(
            "אחרי שהחלטת - כאן נמצא מה לקנות ואיך לקשור. ההוראות נגזרות מההרכב " +
                "שבחרת, אז הן מדויקות לשיטה שלך ולא כלליות.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(16.dp))
        Text("לפי איזה הרכב", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        if (initialComposition.threadCount != null) {
            FilterChip(
                selected = useCustom,
                onClick = { useCustom = true },
                label = { Text("ההרכב שבניתי") },
                modifier = Modifier.padding(end = 6.dp, bottom = 4.dp)
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

        // ================= מה לקנות =================
        Spacer(Modifier.height(20.dp))
        SectionTitle("מה לקנות")

        Card(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("כלל האצבע", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                Text(
                    "סופרים את הכריכות בתכלת בלבד. קרוב ל-40 - צריך ארוך. קרוב ל-20 - " +
                        "מספיק חינוך/גר\"א (או רמב\"ם 7, שמקביל לו מבחינה פרקטית). " +
                        "קרוב ל-0 - מספיק \"7\", אבל זה כמעט אף פעם לא באמת רלוונטי.",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(10.dp))
                Text(recommendation.reasoning, style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(Modifier.height(10.dp))
        recommendation.matches.forEach { p ->
            Card(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(p.name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                        Text("${p.priceThin}₪ דק · ${p.priceThick}₪ עבה",
                            style = MaterialTheme.typography.labelMedium)
                    }
                    Text(p.tier.label, style = MaterialTheme.typography.labelSmall)
                    p.note?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(it, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        Text(
            "המחירים כאן הם קנה מידה בלבד, לא מחירון מדויק - הם משתנים בין מוכרים ולאורך זמן. " +
                "בפועל המחיר נופל על מספר סגור מאוד של אפשרויות.",
            style = MaterialTheme.typography.labelSmall
        )

        // ================= מחשבון שאריות =================
        Spacer(Modifier.height(18.dp))
        LeftoverCalculator(composition)

        // ================= הציור =================
        Spacer(Modifier.height(22.dp))
        SectionTitle("איך זה ייראה")
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                TzitzitVisual(composition, height = 360.dp)
                Spacer(Modifier.height(10.dp))
                Text(
                    "${summary.chulyot} חוליות · ${summary.totalWinds} כריכות " +
                        "(${summary.tekheletWinds} בתכלת) · ${summary.doubleKnots} קשרים כפולים",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // ================= ההוראות =================
        Spacer(Modifier.height(22.dp))
        SectionTitle("צעד אחר צעד")
        steps.forEach { step ->
            Row(Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                Surface(color = MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small) {
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
                    "כאן צריך להיכנס תפריט פנימי של סוגי קשירות וחוליות, עם הדרכה ספציפית " +
                        "לכל אחת - תמונות שלב-אחר-שלב או סרטונים קצרים. " +
                        "גם רשימת האורכים המדויקים בסנטימטרים תיכנס לכאן ותחליף את האומדן שבמחשבון.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

/**
 * "יש לי מספיק חוט?" - למי שרוצה להכין ציצית משאריות ולא לקנות סט חדש.
 */
@Composable
private fun LeftoverCalculator(composition: KnotComposition) {
    var open by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }
    val cm = input.toDoubleOrNull()
    val verdict = remember(composition, cm) { cm?.let { ThreadLength.check(composition, it) } }

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("יש לי כבר חוט - זה יספיק?", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "למי שמכין משאריות ולא רוצה לקנות סט חדש",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                TextButton(onClick = { open = !open }) { Text(if (open) "סגור" else "פתח") }
            }

            AnimatedVisibility(open) {
                Column {
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("אורך חוט התכלת שיש לך, בס\"מ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    verdict?.let { v ->
                        Spacer(Modifier.height(10.dp))
                        Surface(
                            color = if (v.enough) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.secondaryContainer,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                v.message,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    ThreadLength.estimate(composition).notes.forEach {
                        Text("• $it", style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 4.dp))
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "החישוב מבוסס על אומדן גס של כמה חוט \"מבזבז\" כל ליפוף וכל קשר כפול. " +
                            "כשיגיעו המספרים המדויקים אחליף אותם וזה יהיה אמין הרבה יותר.",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
}
