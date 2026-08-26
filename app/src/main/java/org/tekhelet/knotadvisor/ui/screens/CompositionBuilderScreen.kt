package org.tekhelet.knotadvisor.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.tekhelet.knotadvisor.logic.CompositionCoherence
import org.tekhelet.knotadvisor.logic.GadilBuilder
import org.tekhelet.knotadvisor.model.*
import org.tekhelet.knotadvisor.ui.AppViewModel
import org.tekhelet.knotadvisor.ui.components.KnotDivider
import org.tekhelet.knotadvisor.ui.components.TzitzitVisual

/**
 * בונה ההרכב האישי: במקום לבחור שיטה מוכנה, מכריעים בכל אחד מחמשת הדיונים
 * בנפרד ורואים מיד מה יוצא. זה בדיוק מה שהרב שמואל אריאל עשה בפועל - הכריע
 * בכל דיון לגופו והגיע להרכב שאינו זהה לאף שיטה קיימת.
 *
 * העיקרון: **שום צירוף לא נחסם.** אם יש מתח פנימי בהרכב - אני אומר לך מה
 * המתח ולמה, ואתה מחליט. יש הרבה הרכבים מנומקים, לא רק אחד.
 */
@Composable
fun CompositionBuilderScreen(viewModel: AppViewModel) {
    val c = viewModel.customComposition
    val remarks = remember(c) { CompositionCoherence.review(c) }
    val summary = remember(c) { GadilBuilder.plan(c) }
    val closest = remember(c) { viewModel.closestMethod(c) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text("בונה ההרכב האישי", style = MaterialTheme.typography.headlineSmall)
        KnotDivider(modifier = Modifier.padding(top = 10.dp, bottom = 10.dp))
        Text(
            "כל שיטה מוכרת היא בסך הכול צירוף של הכרעות בחמישה דיונים. כאן אתה מכריע " +
                "בכל אחד בעצמו ורואה מיד מה יצא. אין כאן צירוף \"אסור\" - יש צירופים " +
                "שמושכים לשני כיוונים, ועל אלה אעיר לך.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(18.dp))
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("איך זה ייראה", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(10.dp))
                TzitzitVisual(c, height = 320.dp)
                Spacer(Modifier.height(12.dp))
                Text(
                    "${summary.chulyot} חוליות · ${summary.totalWinds} כריכות " +
                        "(${summary.tekheletWinds} תכלת, ${summary.whiteWinds} לבן) · " +
                        "${summary.doubleKnots} קשרים כפולים",
                    style = MaterialTheme.typography.bodySmall
                )
                closest?.let { (method, overlap) ->
                    Spacer(Modifier.height(6.dp))
                    Text(
                        if (overlap == 5) "זה בדיוק ${method.name}."
                        else "הכי קרוב ל${method.name} ($overlap מתוך 5 דיונים).",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        DecisionGroup(
            title = "א. מספר חוטי התכלת",
            binding = DecisionBinding.threadCount,
            options = ThreadCount.entries.map { it to "${it.label} — ${it.attribution}" },
            selected = c.threadCount,
            onSelect = { v -> viewModel.updateComposition { it.copy(threadCount = v) } }
        )
        DecisionGroup(
            title = "ב. צבע הכריכות",
            binding = DecisionBinding.windingColor,
            options = WindingColor.entries.map { it to "${it.label} — ${it.attribution}" },
            selected = c.windingColor,
            onSelect = { v -> viewModel.updateComposition { it.copy(windingColor = v) } }
        )
        DecisionGroup(
            title = "ג. מספר החוליות",
            binding = DecisionBinding.chulyotCount,
            options = ChulyotCount.entries.map { it to it.label },
            selected = c.chulyotCount,
            onSelect = { v -> viewModel.updateComposition { it.copy(chulyotCount = v) } }
        )
        DecisionGroup(
            title = "ד. צורת החוליה",
            binding = DecisionBinding.chulyaForm,
            options = ChulyaForm.entries.map { it to "${it.label} — ${it.attribution}" },
            selected = c.chulyaForm,
            onSelect = { v -> viewModel.updateComposition { it.copy(chulyaForm = v) } }
        )
        DecisionGroup(
            title = "ה. שילוב הקשרים",
            binding = DecisionBinding.knotScheme,
            options = KnotScheme.entries.map { it to "${it.label} — ${it.attribution}" },
            selected = c.knotScheme,
            onSelect = { v -> viewModel.updateComposition { it.copy(knotScheme = v) } }
        )

        Spacer(Modifier.height(16.dp))

        // ההערה שתמיד מוצגת, בכל הרכב
        val always = CompositionCoherence.alwaysRemember()
        Card(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(Modifier.padding(14.dp)) {
                Text(always.title, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                Text(always.body, style = MaterialTheme.typography.bodySmall)
            }
        }

        AnimatedVisibility(remarks.isNotEmpty()) {
            Column {
                Spacer(Modifier.height(14.dp))
                Text("כמה דברים ששווה שתדע על הצירוף הזה", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                remarks.forEach { r ->
                    Card(
                        Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (r.severity == CompositionCoherence.Severity.TENSION)
                                MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text(r.title, style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(4.dp))
                            Text(r.body, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        Text("להתחיל מהרכב מוכר", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        viewModel.allMethods.forEach { m ->
            OutlinedButton(
                onClick = { viewModel.loadCompositionFrom(m) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
            ) { Text(m.name) }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun <T> DecisionGroup(
    title: String,
    binding: BindingLevel,
    options: List<Pair<T, String>>,
    selected: T?,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Card(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleSmall)
                    Text(binding.explanation, style = MaterialTheme.typography.labelSmall)
                }
                Surface(
                    color = when (binding) {
                        BindingLevel.BINDING -> MaterialTheme.colorScheme.primary
                        BindingLevel.PREFERRED -> MaterialTheme.colorScheme.secondaryContainer
                        BindingLevel.ADORNMENT -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        binding.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (binding == BindingLevel.BINDING)
                            MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { expanded = !expanded }) {
                Text(
                    options.firstOrNull { it.first == selected }?.second
                        ?.let { "נבחר: ${it.substringBefore(" — ")}" }
                        ?: "עדיין לא בחרת"
                )
            }
            AnimatedVisibility(expanded) {
                Column {
                    options.forEach { (value, label) ->
                        Row(
                            Modifier.fillMaxWidth()
                                .selectable(selected = value == selected) { onSelect(value); expanded = false }
                                .padding(vertical = 7.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            RadioButton(selected = value == selected, onClick = {
                                onSelect(value); expanded = false
                            })
                            Spacer(Modifier.width(6.dp))
                            Text(label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
