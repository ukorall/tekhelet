package org.tekhelet.knotadvisor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.tekhelet.knotadvisor.model.KnotMethod
import org.tekhelet.knotadvisor.ui.components.*

@Composable
fun MethodsLibraryScreen(methods: List<KnotMethod>, onSelect: (String) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = PageGutter)) {
        item {
            Spacer(Modifier.height(20.dp))
            PageHeader(
                title = "כל שיטות הקשירה",
                lead = "הרשימה המלאה, בלי דירוג ובלי שאלון."
            )
            Spacer(Modifier.height(20.dp))
        }
        items(methods) { method ->
            NavRow(method.name, method.shortSummary) { onSelect(method.id) }
        }
        item { Spacer(Modifier.height(32.dp)) }
    }
}
