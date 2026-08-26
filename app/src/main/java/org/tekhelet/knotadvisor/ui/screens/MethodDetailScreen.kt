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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.tekhelet.knotadvisor.R
import org.tekhelet.knotadvisor.data.AssetImages
import org.tekhelet.knotadvisor.model.KnotMethod
import org.tekhelet.knotadvisor.logic.CompositionCoherence
import org.tekhelet.knotadvisor.ui.components.*

@Composable
fun MethodDetailScreen(method: KnotMethod, onUseInGuide: (() -> Unit)? = null) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = PageGutter),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(Modifier.height(20.dp))
            PageHeader(title = method.name, kicker = eraLabel(method.era))
        }
        item { MethodImages(method) }
        item {
            Text(method.fullDescription, style = MaterialTheme.typography.bodyLarge)
        }
        item { CompositionCard(method.composition) }
        if (method.editorialNote != null) {
            item {
                Leaf(tinted = true) {
                    Text("מה אני חושב על זה", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(method.editorialNote, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text("איך זה ייראה", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    TzitzitVisual(method.composition, height = 260.dp)
                }
            }
        }
        val remarks = CompositionCoherence.review(method.composition)
        if (remarks.isNotEmpty()) {
            item { Text("שווה לשים לב", style = MaterialTheme.typography.titleMedium) }
            items(remarks) { r ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text(r.title, style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(4.dp))
                        Text(r.body, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        if (method.variants.isNotEmpty()) {
            item {
                Column {
                    Text("מה עוד אפשר לעשות עם זה", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "הרבה ממה שאנשים באמת קושרים הוא לא השיטה ה\"נקייה\" של הראשון, " +
                            "אלא צירוף שנוצר בפועל - בדרך כלל הוספה של משהו מבוסס. " +
                            "אלה הצירופים הנפוצים על הבסיס הזה.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            items(method.variants) { variant ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                variant.name,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.weight(1f)
                            )
                            if (variant.commonness >= 7) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        "נפוץ",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(variant.rationale, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(10.dp))
                        TzitzitVisual(
                            variant.applyTo(method.composition),
                            height = 220.dp,
                            showLegend = false
                        )
                    }
                }
            }
        }
        if (method.sources.isNotEmpty()) {
            item { Text("מקורות", style = MaterialTheme.typography.titleMedium) }
            items(method.sources) { source ->
                val uriHandler = LocalUriHandler.current
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("${source.title} (${source.author})", style = MaterialTheme.typography.bodyMedium)
                        source.excerpt?.let {
                            Spacer(Modifier.height(4.dp))
                            Text("\"$it\"", style = MaterialTheme.typography.bodySmall)
                        }
                        source.note?.let {
                            Spacer(Modifier.height(4.dp))
                            Text(it, style = MaterialTheme.typography.labelSmall)
                        }
                        source.link?.let { url ->
                            TextButton(onClick = { uriHandler.openUri(url) }) {
                                Text("לפתוח בספריא")
                            }
                        }
                    }
                }
            }
        }
        if (onUseInGuide != null) {
            item {
                Spacer(Modifier.height(8.dp))
                Button(onClick = onUseInGuide, modifier = Modifier.fillMaxWidth()) {
                    Text("זו הבחירה שלי - איך קושרים את זה")
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
