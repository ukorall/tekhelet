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
import org.tekhelet.knotadvisor.ui.components.*

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
    val summary = remember(composition) { GadilBuilder.plan(composition) }
    val recommendation = remember(composition) { Products.recommend(composition) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = PageGutter)
    ) {
        Spacer(Modifier.height(20.dp))
        PageHeader(
            title = "איך בפועל",
            lead = "אחרי שהחלטת - כאן נמצא מה לקנות ואיך לקשור. ההוראות נגזרות מההרכב " +
                "שבחרת, אז הן מדויקות לשיטה שלך ולא כלליות."
        )

        Spacer(Modifier.height(24.dp))
        SectionHeading("לפי איזה הרכב")
        Spacer(Modifier.height(12.dp))
        if (initialComposition.threadCount != null) {
            FilterChip(
                selected = useCustom,
                onClick = { useCustom = true },
                label = { Text("ההרכב שבניתי") },
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.padding(end = 6.dp, bottom = 4.dp)
            )
        }
        methods.forEach { m ->
            FilterChip(
                selected = !useCustom && selected?.id == m.id,
                onClick = { selected = m; useCustom = false },
                label = { Text(m.name, style = MaterialTheme.typography.labelSmall) },
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.padding(end = 6.dp, bottom = 4.dp)
            )
        }
        TextButton(onClick = onOpenBuilder) { Text("או לבנות הרכב משלי") }

        // ================= מה לקנות =================
        Spacer(Modifier.height(24.dp))
        SectionHeading("מה לקנות")
        Spacer(Modifier.height(14.dp))

        Leaf(tinted = true) {
            Text("כלל האצבע", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(
                "סופרים את הכריכות בתכלת בלבד. קרוב ל-40 - צריך ארוך. קרוב ל-20 - " +
                    "מספיק חינוך/גר\"א (או רמב\"ם 7, שמקביל לו מבחינה פרקטית). " +
                    "קרוב ל-0 - מספיק \"7\", אבל זה כמעט אף פעם לא באמת רלוונטי.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(12.dp))
            Text(recommendation.reasoning, style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(Modifier.height(14.dp))
        recommendation.matches.forEach { p ->
            Leaf(modifier = Modifier.padding(bottom = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(p.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    Text(
                        ltr("${p.priceThin}₪ דק · ${p.priceThick}₪ עבה"),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(3.dp))
                Aside(p.tier.label)
                p.note?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        recommendation.cheaperIfSeven?.let {
            Spacer(Modifier.height(4.dp))
            Leaf(tinted = true) {
                Text("אפשר גם בזול יותר", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(12.dp))
        }

        if (recommendation.lishmahOptions.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Text("ואם רוצים להדר: ניפוץ לשמה", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))
            Aside(
                "זו לא מדרגת אורך אלא מדרגת הידור - הצמר נופץ לשמה. המחיר קופץ " +
                    "משמעותית, ואין בו פיצול לדק ועבה."
            )
            Spacer(Modifier.height(8.dp))
            recommendation.lishmahOptions.forEach { p ->
                Row(Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                    Text(p.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Text(
                        ltr("${p.price}₪"),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Aside(
            "המחירים נלקחו מהחנות של ארגון פתיל תכלת (chanut.tekhelet.com) ונכונים " +
                "לאוגוסט 2026. הם משתנים בין מוכרים ולאורך זמן, אז תתייחס אליהם כסדר " +
                "גודל. השמות כאן הם השמות המדויקים שבחנות, כדי שיהיה אפשר למצוא אותם."
        )

        // ================= מחשבון שאריות =================
        Spacer(Modifier.height(22.dp))
        LeftoverCalculator(composition)

        // ================= הציור =================
        Spacer(Modifier.height(26.dp))
        SectionHeading("איך זה ייראה")
        Spacer(Modifier.height(14.dp))
        Leaf {
            TzitzitVisual(composition, height = 720.dp)
            Spacer(Modifier.height(12.dp))
            Aside(
                ltr("${summary.chulyot}") + " חוליות · " +
                    ltr("${summary.totalWinds}") + " כריכות (" +
                    ltr("${summary.tekheletWinds}") + " בתכלת) · " +
                    ltr("${summary.doubleKnots}") + " קשרים כפולים"
            )
        }

        // ================= ההוראות =================
        Spacer(Modifier.height(26.dp))
        SectionHeading("צעד אחר צעד")
        Spacer(Modifier.height(14.dp))
        steps.forEach { step ->
            Row(Modifier.fillMaxWidth().padding(bottom = 14.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(
                        ltr("${step.number}"),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(step.text, style = MaterialTheme.typography.bodyLarge)
                    step.note?.let {
                        Spacer(Modifier.height(5.dp))
                        Aside(it)
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Leaf(tinted = true) {
            Text("להשלמה", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(
                "כאן צריך להיכנס תפריט פנימי של סוגי קשירות וחוליות, עם הדרכה ספציפית " +
                    "לכל אחת - תמונות שלב-אחר-שלב או סרטונים קצרים. " +
                    "גם רשימת האורכים המדויקים בסנטימטרים תיכנס לכאן ותחליף את האומדן שבמחשבון.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(Modifier.height(32.dp))
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

    Leaf {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("יש לי כבר חוט - זה יספיק?", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(2.dp))
                Aside("למי שמכין משאריות ולא רוצה לקנות סט חדש")
            }
            TextButton(onClick = { open = !open }) { Text(if (open) "סגור" else "פתח") }
        }

        AnimatedVisibility(open) {
            Column {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("אורך חוט התכלת שיש לך, בס\"מ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                )
                verdict?.let { v ->
                    Spacer(Modifier.height(12.dp))
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
                Spacer(Modifier.height(12.dp))
                ThreadLength.estimate(composition).notes.forEach {
                    Text(
                        "• $it",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Aside(
                    "החישוב מבוסס על אומדן גס של כמה חוט \"מבזבז\" כל ליפוף וכל קשר כפול. " +
                        "כשיגיעו המספרים המדויקים אחליף אותם וזה יהיה אמין הרבה יותר."
                )
            }
        }
    }
}
