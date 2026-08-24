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
fun MethodDetailScreen(method: KnotMethod, onFinalize: (() -> Unit)? = null) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text(method.name, style = MaterialTheme.typography.headlineSmall)
            Text(eraLabel(method.era), style = MaterialTheme.typography.labelMedium)
        }
        item {
            if (method.imageAssets.isEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box(Modifier.padding(32.dp)) {
                        Text("[כאן תוצג תמונה של הקשירה - ממתין להעלאת תמונות]")
                    }
                }
            }
        }
        item {
            Text(method.fullDescription, style = MaterialTheme.typography.bodyLarge)
        }
        if (method.editorialNote != null) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("הערת עריכה", style = MaterialTheme.typography.labelLarge)
                        Text(method.editorialNote, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        if (method.variants.isNotEmpty()) {
            item { Text("גרסאות אורך", style = MaterialTheme.typography.titleMedium) }
            items(method.variants) { variant ->
                Text("• ${variant.name}" + (variant.note?.let { " - $it" } ?: ""))
            }
        }
        if (method.sources.isNotEmpty()) {
            item { Text("מקורות", style = MaterialTheme.typography.titleMedium) }
            items(method.sources) { source ->
                Column {
                    Text("${source.title} (${source.author})", style = MaterialTheme.typography.bodyMedium)
                    source.excerpt?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                    source.note?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                }
            }
        }
        if (onFinalize != null) {
            item {
                Spacer(Modifier.height(8.dp))
                Button(onClick = onFinalize, modifier = Modifier.fillMaxWidth()) {
                    Text("זו הבחירה שלי")
                }
            }
        }
    }
}

private fun eraLabel(era: org.tekhelet.knotadvisor.model.Era): String = when (era) {
    org.tekhelet.knotadvisor.model.Era.GEONIM -> "תקופת הגאונים"
    org.tekhelet.knotadvisor.model.Era.RISHONIM -> "תקופת הראשונים"
    org.tekhelet.knotadvisor.model.Era.ACHRONIM -> "תקופת האחרונים"
    org.tekhelet.knotadvisor.model.Era.CONTEMPORARY -> "פוסק בן זמננו"
}
