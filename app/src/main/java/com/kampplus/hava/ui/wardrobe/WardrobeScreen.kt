package com.kampplus.hava.ui.wardrobe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kampplus.hava.data.WardrobeStore
import com.kampplus.hava.model.WardrobeItem

@Composable
fun WardrobeRoute(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val store = remember(context) { WardrobeStore(context.applicationContext) }
    val items by store.items.collectAsStateWithLifecycle()
    WardrobeScreen(items, store::upsert, store::delete, onBack, modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WardrobeScreen(
    items: List<WardrobeItem>,
    onSave: (WardrobeItem) -> Unit,
    onDelete: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var editedItem by remember { mutableStateOf<WardrobeItem?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("My Wardrobe")
                        Text("Gerçek parçaların, gerçek kombinlerin", style = MaterialTheme.typography.labelMedium)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editedItem = null
                showEditor = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Kıyafet ekle")
            }
        }
    ) { innerPadding ->
        if (items.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(28.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("👚", style = MaterialTheme.typography.displayMedium)
                Text("Dolabın henüz boş", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    "İlk parçanı manuel ekle. Wearly yalnızca gerçekten kaydettiğin kıyafetlerle kombin oluştursun.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Text("${items.size} kayıtlı parça", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
                items(items, key = WardrobeItem::id) { item ->
                    WardrobeCard(
                        item = item,
                        onClick = {
                            editedItem = item
                            showEditor = true
                        },
                        onDelete = { onDelete(item.id) }
                    )
                }
            }
        }
    }
    if (showEditor) {
        WardrobeEditorDialog(
            item = editedItem,
            onDismiss = { showEditor = false },
            onSave = {
                onSave(it)
                showEditor = false
            }
        )
    }
}

@Composable
private fun WardrobeCard(item: WardrobeItem, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(categoryIcon(item.category), style = MaterialTheme.typography.headlineMedium)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "${item.category} · ${item.color} · ${item.season}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(item.style, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Kıyafeti sil") }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WardrobeEditorDialog(item: WardrobeItem?, onDismiss: () -> Unit, onSave: (WardrobeItem) -> Unit) {
    var name by remember(item) { mutableStateOf(item?.name.orEmpty()) }
    var category by remember(item) { mutableStateOf(item?.category ?: CATEGORIES.first()) }
    var color by remember(item) { mutableStateOf(item?.color.orEmpty()) }
    var season by remember(item) { mutableStateOf(item?.season ?: SEASONS.first()) }
    var style by remember(item) { mutableStateOf(item?.style ?: STYLES.first()) }
    var purpose by remember(item) { mutableStateOf(item?.purpose.orEmpty()) }
    var note by remember(item) { mutableStateOf(item?.note.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item == null) "Kıyafet ekle" else "Kıyafeti düzenle") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item { OutlinedTextField(name, { name = it }, label = { Text("Parça adı") }, modifier = Modifier.fillMaxWidth()) }
                item {
                    Text("Kategori", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        CATEGORIES.forEach { value ->
                            AssistChip(onClick = { category = value }, label = { Text(if (category == value) "✓ $value" else value) })
                        }
                    }
                }
                item { OutlinedTextField(color, { color = it }, label = { Text("Renk") }, modifier = Modifier.fillMaxWidth()) }
                item {
                    Text("Mevsim", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SEASONS.forEach { value ->
                            AssistChip(onClick = { season = value }, label = { Text(if (season == value) "✓ $value" else value) })
                        }
                    }
                }
                item {
                    Text("Stil", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        STYLES.forEach { value ->
                            AssistChip(onClick = { style = value }, label = { Text(if (style == value) "✓ $value" else value) })
                        }
                    }
                }
                item {
                    OutlinedTextField(purpose, { purpose = it }, label = { Text("Kullanım amacı") }, modifier = Modifier.fillMaxWidth())
                }
                item { OutlinedTextField(note, { note = it }, label = { Text("Not") }, modifier = Modifier.fillMaxWidth(), maxLines = 3) }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && color.isNotBlank(),
                onClick = {
                    onSave(
                        WardrobeItem(
                            id = item?.id.orEmpty(),
                            name = name.trim(),
                            category = category,
                            color = color.trim(),
                            season = season,
                            style = style,
                            purpose = purpose.trim(),
                            note = note.trim()
                        )
                    )
                }
            ) { Text("Kaydet") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Vazgeç") } }
    )
}

private fun categoryIcon(category: String) = when (category) {
    "Üst" -> "👕"
    "Alt" -> "👖"
    "Dış giyim" -> "🧥"
    "Ayakkabı" -> "👟"
    else -> "👜"
}

private val CATEGORIES = listOf("Üst", "Alt", "Dış giyim", "Ayakkabı", "Aksesuar")
private val SEASONS = listOf("Dört mevsim", "Yaz", "Kış", "Geçiş")
private val STYLES = listOf("Casual", "Elegant", "Sporty", "Business", "Minimal")
