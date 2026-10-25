package com.kampplus.hava.ui.style

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kampplus.hava.model.StyleMessage
import com.kampplus.hava.ui.LoadState

@Composable
fun StyleAssistantRoute(onBack: () -> Unit, modifier: Modifier = Modifier, viewModel: StyleAssistantViewModel = viewModel()) {
    val forecast by viewModel.forecast.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    StyleAssistantScreen(
        cityName = viewModel.cityName,
        forecastState = forecast,
        messages = messages,
        onSend = viewModel::send,
        onBack = onBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StyleAssistantScreen(
    cityName: String,
    forecastState: LoadState<*>,
    messages: List<StyleMessage>,
    onSend: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var input by remember { mutableStateOf("") }
    fun submit(text: String = input) {
        onSend(text)
        input = ""
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Stil Asistanı")
                        Text(cityName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        },
        bottomBar = {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                placeholder = { Text("Bugün şık olmam lazım…") },
                trailingIcon = {
                    IconButton(onClick = { submit() }, enabled = input.isNotBlank()) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Gönder")
                    }
                },
                shape = RoundedCornerShape(24.dp),
                maxLines = 3,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { submit() })
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("✨", style = MaterialTheme.typography.headlineMedium)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Havayı kombine dönüştür", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "Planını ve tarzını yaz; sıcaklık, yağış, rüzgâr ve UV birlikte değerlendirilsin.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (forecastState is LoadState.Loading) CircularProgressIndicator(modifier = Modifier.widthIn(max = 22.dp))
                }
            }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(QUICK_PROMPTS) { prompt ->
                    AssistChip(onClick = { submit(prompt) }, label = { Text(prompt) })
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages) { message -> MessageBubble(message) }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: StyleMessage) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (message.fromUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Text(
            text = message.text,
            modifier = Modifier
                .widthIn(max = 320.dp)
                .background(
                    if (message.fromUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                    RoundedCornerShape(20.dp)
                )
                .padding(14.dp),
            color = if (message.fromUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private val QUICK_PROMPTS = listOf("Şık bir kombin", "Ofis için", "Spor ve rahat", "Yağmura uygun")
