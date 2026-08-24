package org.tekhelet.knotadvisor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.tekhelet.knotadvisor.model.KnotMethod

@Composable
fun MethodsLibraryScreen(methods: List<KnotMethod>, onSelect: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("כל שיטות הקשירה", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(methods) { method ->
                Card(modifier = Modifier.fillMaxWidth(), onClick = { onSelect(method.id) }) {
                    Column(Modifier.padding(16.dp)) {
                        Text(method.name, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(method.shortSummary, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
