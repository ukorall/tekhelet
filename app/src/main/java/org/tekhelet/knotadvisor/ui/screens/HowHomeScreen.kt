package org.tekhelet.knotadvisor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** מסך הבית של אזור "איך" (בחירת שיטת קשירה) - מגיעים אליו מ-TopicsHomeScreen. */
@Composable
fun HowHomeScreen(
    onStartQuestionnaire: () -> Unit,
    onBrowseLibrary: () -> Unit,
    onOpenHistory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))
        Text("איך? - שיטת קשירה", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text(
            "כלי עזר לבחירת שיטת קשירה בתכלת, לאחר שכבר הוחלט לקשור תכלת בציצית. " +
                "עונים על כמה שאלות על מה חשוב לכם, והאפליקציה מציעה שיטות מתאימות עם הסבר קצר.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        Button(onClick = onStartQuestionnaire, modifier = Modifier.fillMaxWidth()) {
            Text("התחל שאלון")
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onBrowseLibrary, modifier = Modifier.fillMaxWidth()) {
            Text("עיון חופשי בכל השיטות")
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onOpenHistory, modifier = Modifier.fillMaxWidth()) {
            Text("ייעוצים קודמים ששמרתי")
        }
    }
}
