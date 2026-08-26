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
import org.tekhelet.knotadvisor.ui.components.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    consultations: List<SavedConsultation>,
    methods: List<KnotMethod>,
    onDelete: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = PageGutter)) {
        Spacer(Modifier.height(20.dp))
        PageHeader(
            title = "ייעוצים קודמים",
            lead = "כל ייעוץ נשמר בנפרד, כדי שאפשר יהיה לחזור ולראות מה יצא למי ומתי."
        )
        Spacer(Modifier.height(24.dp))

        if (consultations.isEmpty()) {
            Text("עדיין אין ייעוצים שמורים.", style = MaterialTheme.typography.bodyMedium)
            return@Column
        }

        val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale("he")) }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(consultations.sortedByDescending { it.createdAtEpochMillis }) { consultation ->
                Leaf {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Text(
                            consultation.consultingFor,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { onDelete(consultation.id) }) { Text("מחיקה") }
                    }
                    Aside(ltr(dateFormat.format(Date(consultation.createdAtEpochMillis))))
                    Spacer(Modifier.height(10.dp))
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
