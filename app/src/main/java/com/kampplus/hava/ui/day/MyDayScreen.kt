package com.kampplus.hava.ui.day

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
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
import com.kampplus.hava.data.DayPlanStore
import com.kampplus.hava.model.DayPlan
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun MyDayRoute(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val store = remember(context) { DayPlanStore(context.applicationContext) }
    val plans by store.plans.collectAsStateWithLifecycle()
    MyDayScreen(
        plans = plans,
        onSave = store::upsert,
        onDelete = store::delete,
        onAddToCalendar = { addToCalendar(context, it) },
        onBack = onBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyDayScreen(
    plans: List<DayPlan>,
    onSave: (DayPlan) -> Unit,
    onDelete: (String) -> Unit,
    onAddToCalendar: (DayPlan) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var editedPlan by remember { mutableStateOf<DayPlan?>(null) }
    var showEditor by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("My Day")
                        Text("Planını hava için hazırla", style = MaterialTheme.typography.labelMedium)
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
            FloatingActionButton(
                onClick = {
                    editedPlan = null
                    showEditor = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Plan ekle")
            }
        }
    ) { innerPadding ->
        if (plans.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(28.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🗓️", style = MaterialTheme.typography.displayMedium)
                Text("Günün henüz boş", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    "Üniversite, toplantı veya buluşma ekle; Wearly planını hava bağlamıyla birlikte düzenlesin.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("Yaklaşan planların", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
                items(plans, key = DayPlan::id) { plan ->
                    PlanCard(
                        plan = plan,
                        onClick = {
                            editedPlan = plan
                            showEditor = true
                        },
                        onAddToCalendar = { onAddToCalendar(plan) },
                        onDelete = { onDelete(plan.id) }
                    )
                }
            }
        }
    }

    if (showEditor) {
        PlanEditorDialog(
            plan = editedPlan,
            onDismiss = { showEditor = false },
            onSave = {
                onSave(it)
                showEditor = false
            }
        )
    }
}

@Composable
private fun PlanCard(plan: DayPlan, onClick: () -> Unit, onAddToCalendar: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(plan.time, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(plan.date.takeLast(5), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(plan.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    listOf(plan.eventType, plan.location).filter(String::isNotBlank).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (plan.dressExpectation.isNotBlank()) {
                    Text("Stil: ${plan.dressExpectation}", style = MaterialTheme.typography.labelMedium)
                }
            }
            TextButton(onClick = onAddToCalendar) { Text("Takvim") }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Planı sil")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlanEditorDialog(plan: DayPlan?, onDismiss: () -> Unit, onSave: (DayPlan) -> Unit) {
    var title by remember(plan) { mutableStateOf(plan?.title.orEmpty()) }
    var date by remember(plan) { mutableStateOf(plan?.date ?: LocalDate.now().toString()) }
    var time by remember(plan) {
        mutableStateOf(plan?.time ?: LocalTime.now().plusHours(1).format(DateTimeFormatter.ofPattern("HH:mm")))
    }
    var location by remember(plan) { mutableStateOf(plan?.location.orEmpty()) }
    var eventType by remember(plan) { mutableStateOf(plan?.eventType ?: EVENT_TYPES.first()) }
    var dressExpectation by remember(plan) { mutableStateOf(plan?.dressExpectation.orEmpty()) }
    var note by remember(plan) { mutableStateOf(plan?.note.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (plan == null) "Plan ekle" else "Planı düzenle") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item { OutlinedTextField(title, { title = it }, label = { Text("Başlık") }, modifier = Modifier.fillMaxWidth()) }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(date, { date = it }, label = { Text("Tarih · YYYY-AA-GG") }, modifier = Modifier.weight(1.5f))
                        OutlinedTextField(time, { time = it }, label = { Text("Saat") }, modifier = Modifier.weight(1f))
                    }
                }
                item {
                    OutlinedTextField(location, {
                        location = it
                    }, label = { Text("Konum · isteğe bağlı") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        EVENT_TYPES.forEach { type ->
                            AssistChip(onClick = { eventType = type }, label = { Text(if (eventType == type) "✓ $type" else type) })
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        dressExpectation,
                        { dressExpectation = it },
                        label = { Text("Giyim beklentisi") },
                        placeholder = { Text("Şık ama rahat") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item { OutlinedTextField(note, { note = it }, label = { Text("Not") }, modifier = Modifier.fillMaxWidth(), maxLines = 3) }
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank() && DATE_PATTERN.matches(date) && TIME_PATTERN.matches(time),
                onClick = {
                    onSave(
                        DayPlan(
                            id = plan?.id.orEmpty(),
                            title = title.trim(),
                            date = date,
                            time = time,
                            location = location.trim(),
                            eventType = eventType,
                            dressExpectation = dressExpectation.trim(),
                            note = note.trim()
                        )
                    )
                }
            ) { Text("Kaydet") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Vazgeç") } }
    )
}

private val EVENT_TYPES = listOf("Günlük", "Okul", "İş", "Buluşma")
private val DATE_PATTERN = Regex("\\d{4}-\\d{2}-\\d{2}")
private val TIME_PATTERN = Regex("\\d{2}:\\d{2}")

private fun addToCalendar(context: Context, plan: DayPlan) {
    val start = runCatching {
        LocalDate.parse(plan.date)
            .atTime(LocalTime.parse(plan.time))
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }.getOrNull() ?: return
    val description = buildList {
        if (plan.eventType.isNotBlank()) add("Tür: ${plan.eventType}")
        if (plan.dressExpectation.isNotBlank()) add("Giyim: ${plan.dressExpectation}")
        if (plan.note.isNotBlank()) add(plan.note)
        add("Wearly ile planlandı.")
    }.joinToString("\n")
    val intent = Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI).apply {
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start)
        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, start + 60 * 60 * 1000L)
        putExtra(CalendarContract.Events.TITLE, plan.title)
        putExtra(CalendarContract.Events.EVENT_LOCATION, plan.location)
        putExtra(CalendarContract.Events.DESCRIPTION, description)
    }
    runCatching { context.startActivity(intent) }
}
