package org.tekhelet.knotadvisor.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.tekhelet.knotadvisor.model.*

/**
 * מציג את "ההרכב ההלכתי" של שיטה - חמשת הדיונים הנפרדים ומה השיטה מכריעה בכל אחד,
 * יחד עם רמת המחייבות של כל דיון. ראו Composition.kt לרקע המלא.
 *
 * המטרה החינוכית: שהמשתמש יראה ששיטה אינה "חבילה" אטומית, ושרוב ההבדלים בין
 * השיטות אינם מעכבים - כדי שלא ייתקע על החלטות שאין להן משקל הלכתי אמיתי.
 */
@Composable
fun CompositionCard(composition: KnotComposition, modifier: Modifier = Modifier) {
    val rows = buildList {
        composition.threadCount?.let {
            add(Triple("מספר חוטי התכלת", "${it.label} (${it.attribution})", DecisionBinding.threadCount))
        }
        composition.windingColor?.let {
            add(Triple("צבע הכריכות", "${it.label} (${it.attribution})", DecisionBinding.windingColor))
        }
        composition.chulyotCount?.let {
            add(Triple("מספר החוליות", it.label, DecisionBinding.chulyotCount))
        }
        composition.chulyaForm?.let {
            add(Triple("צורת החוליה", "${it.label} (${it.attribution})", DecisionBinding.chulyaForm))
        }
        composition.knotScheme?.let {
            add(Triple("שילוב הקשרים", "${it.label} (${it.attribution})", DecisionBinding.knotScheme))
        }
    }
    if (rows.isEmpty() && composition.note == null) return

    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("ההרכב ההלכתי", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(2.dp))
            Text(
                "כל שיטה היא צירוף של הכרעות בחמישה דיונים נפרדים",
                style = MaterialTheme.typography.labelSmall
            )
            GadilRule(modifier = Modifier.padding(vertical = 10.dp))

            rows.forEach { (title, value, binding) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                    BindingChip(binding)
                }
                Text(value, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))
            }

            if (composition.windGroups.isNotEmpty()) {
                Text("כריכות בין קשר לקשר", style = MaterialTheme.typography.labelLarge)
                Text(
                    composition.windGroups.joinToString(", "),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(12.dp))
            }

            composition.note?.let {
                Text(it, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun BindingChip(level: BindingLevel) {
    val colors = when (level) {
        BindingLevel.BINDING -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        BindingLevel.PREFERRED -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        BindingLevel.ADORNMENT -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(color = colors.first, shape = MaterialTheme.shapes.small) {
        Text(
            level.label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.second,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
