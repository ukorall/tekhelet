package org.tekhelet.knotadvisor.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.tekhelet.knotadvisor.model.*
import org.tekhelet.knotadvisor.ui.components.KnotDivider
import org.tekhelet.knotadvisor.ui.components.TzitzitVisual

/**
 * "מה יפה בעיניי" - הכרעה חזותית ישירה בין שלוש המראות האפשריים, עם ציור לכל
 * אחד. זו שאלה שונה מהסליידר "עד כמה חשוב לך שיהיה יפה": שם קובעים כמה משקל
 * לתת ליופי, וכאן קובעים מה נחשב יפה. לכן זה מקבל משקל גדול יותר בשקלול.
 */
@Composable
fun LookPreferenceScreen(
    selected: LookPreference?,
    knotPreference: KnotLookPreference?,
    onSelect: (LookPreference) -> Unit,
    onSelectKnot: (KnotLookPreference) -> Unit,
    onContinue: () -> Unit
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text("מה יפה בעיניך?", style = MaterialTheme.typography.headlineSmall)
        KnotDivider(modifier = Modifier.padding(top = 10.dp, bottom = 12.dp))
        Text(
            "אמרת לי כמה חשוב לך שהקשירה תהיה יפה. עכשיו אני רוצה לדעת מה בעצם " +
                "יפה בעיניך - כי על זה אין ויכוח, וזה שוקל אצלי יותר מכל ניחוש " +
                "שאני יכול לנחש במקומך.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(18.dp))
        LookPreference.entries.forEach { option ->
            val isSelected = option == selected
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .selectable(isSelected) { onSelect(option) }
                    .then(
                        if (isSelected) Modifier.border(
                            2.dp,
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.shapes.medium
                        ) else Modifier
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surface
                )
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = isSelected, onClick = { onSelect(option) })
                            Text(option.label, style = MaterialTheme.typography.titleSmall)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(option.blurb, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.width(10.dp))
                    Box(Modifier.width(96.dp)) {
                        TzitzitVisual(
                            composition = sampleFor(option),
                            height = 170.dp,
                            showLegend = false
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Text("ומה לגבי הקשרים הכפולים?", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(4.dp))
        Text(
            "יש שיטות שבהן רואים קשרים כפולים לאורך הגדיל, ויש שבהן כמעט לא. " +
                "יש אנשים שזה בדיוק מה שעושה להם את זה יפה, ויש שזה מפריע להם.",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(10.dp))
        KnotLookPreference.entries.forEach { k ->
            Row(
                Modifier.fillMaxWidth().selectable(k == knotPreference) { onSelectKnot(k) }
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = k == knotPreference, onClick = { onSelectKnot(k) })
                Spacer(Modifier.width(6.dp))
                Text(k.label, style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(Modifier.height(20.dp))
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
            Text("להמשיך לתוצאות")
        }
        Spacer(Modifier.height(24.dp))
    }
}

/** הרכב לדוגמה לכל אפשרות, רק לצורך הציור. */
private fun sampleFor(p: LookPreference): KnotComposition = when (p) {
    LookPreference.ALTERNATING_WINDS -> KnotComposition(
        threadCount = ThreadCount.RAAVAD_2_OF_8,
        windingColor = WindingColor.ALTERNATING_WINDS,
        chulyotCount = ChulyotCount.FOUR,
        chulyaForm = ChulyaForm.STANDARD_DOUBLE_KNOT,
        knotScheme = KnotScheme.DOUBLE_EVERY_CHULYA,
        windsPerChulya = listOf(7, 9, 11, 13)
    )
    LookPreference.ALTERNATING_CHULYOT -> KnotComposition(
        threadCount = ThreadCount.RAAVAD_2_OF_8,
        windingColor = WindingColor.ALTERNATING_CHULYOT,
        chulyotCount = ChulyotCount.THIRTEEN,
        chulyaForm = ChulyaForm.STANDARD_DOUBLE_KNOT,
        knotScheme = KnotScheme.FIVE_GROUPS_CHINUCH
    )
    LookPreference.ALL_TEKHELET -> KnotComposition(
        threadCount = ThreadCount.RAAVAD_2_OF_8,
        windingColor = WindingColor.MOSTLY_TEKHELET_SINGLE_WIND,
        chulyotCount = ChulyotCount.SEVEN,
        chulyaForm = ChulyaForm.STANDARD_DOUBLE_KNOT,
        knotScheme = KnotScheme.DOUBLE_EVERY_CHULYA
    )
}
