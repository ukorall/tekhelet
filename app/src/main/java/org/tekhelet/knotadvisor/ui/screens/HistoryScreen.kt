package org.tekhelet.knotadvisor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.tekhelet.knotadvisor.model.KnotMethod
import org.tekhelet.knotadvisor.model.SavedConsultation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(consultations: List<SavedConsultation>, methods: List<KnotMethod>) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("ייעוצים קודמים", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            "שימושי כשאתם עוזרים לכמה חברים להחליט לאורך זמן, ורוצים להיזכר מה הומלץ למי.",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(16.dp))

        if (consultations.isEmpty()) {
            Text("עדיין אין ייעוצים שמורים.", style = MaterialTheme.typography.bodyMedium)
            return@Column
        }

        val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale("he")) }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(consultations.sortedByDescending { it.createdAtEpochMillis }) { consultation ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(consultation.consultingFor, style = MaterialTheme.typography.titleMedium)
                        Text(
                            dateFormat.format(Date(consultation.createdAtEpochMillis)),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(Modifier.height(8.dp))
                        val chosenName = consultation.finalChoiceMethodId
                            ?.let { id -> methods.find { it.id == id }?.name }
                        Text(
                            chosenName?.let { "נבחר: $it" } ?: "טרם נבחרה שיטה סופית",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
