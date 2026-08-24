package org.tekhelet.knotadvisor.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.tekhelet.knotadvisor.R
import org.tekhelet.knotadvisor.data.AssetImages
import org.tekhelet.knotadvisor.model.KnotMethod

@Composable
fun MethodDetailScreen(method: KnotMethod, onFinalize: (() -> Unit)? = null) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text(method.name, style = MaterialTheme.typography.headlineSmall)
            Text(eraLabel(method.era), style = MaterialTheme.typography.labelMedium)
        }
        item { MethodImages(method) }
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

/** מציג את כל תמונות השיטה שנמצאו ב-assets (ראו AssetImages), או placeholder אם אין אף אחת. */
@Composable
fun MethodImages(method: KnotMethod, imageHeight: Dp = 160.dp) {
    val context = LocalContext.current
    val images = remember(method.id) { AssetImages.imagesFor(context, method.id) }

    if (images.isEmpty()) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Box(Modifier.fillMaxWidth().height(imageHeight), contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(R.drawable.ic_knot_placeholder),
                    contentDescription = "אין עדיין תמונה לשיטה זו",
                    modifier = Modifier.height(imageHeight * 0.6f)
                )
            }
        }
    } else {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(images) { fileName ->
                AsyncImage(
                    model = AssetImages.assetUri(method.id, fileName),
                    contentDescription = "תמונה של קשירת ${method.name}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .height(imageHeight)
                        .width(imageHeight)
                        .clip(RoundedCornerShape(16.dp))
                )
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
