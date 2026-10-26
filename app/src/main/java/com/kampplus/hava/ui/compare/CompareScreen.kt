package com.kampplus.hava.ui.compare

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kampplus.hava.model.TemperatureUnit
import com.kampplus.hava.model.WeatherCondition
import com.kampplus.hava.model.comfortScore
import kotlin.math.roundToInt

@Composable
fun CompareRoute(unit: TemperatureUnit, onBack: () -> Unit, modifier: Modifier = Modifier, viewModel: CompareViewModel = viewModel()) {
    val firstQuery by viewModel.firstQuery.collectAsStateWithLifecycle()
    val secondQuery by viewModel.secondQuery.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CompareScreen(
        firstQuery = firstQuery,
        secondQuery = secondQuery,
        uiState = uiState,
        unit = unit,
        onFirstQueryChange = viewModel::setFirstQuery,
        onSecondQueryChange = viewModel::setSecondQuery,
        onCompare = viewModel::compare,
        onBack = onBack,
        modifier = modifier
    )
}

@Composable
private fun CompareScreen(
    firstQuery: String,
    secondQuery: String,
    uiState: CompareUiState,
    unit: TemperatureUnit,
    onFirstQueryChange: (String) -> Unit,
    onSecondQueryChange: (String) -> Unit,
    onCompare: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier, containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri") }
                Column {
                    Text("Şehir karşılaştır", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Gerçek verilerle planına en uygun şehri seç.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            OutlinedTextField(
                value = firstQuery,
                onValueChange = onFirstQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Birinci şehir") },
                singleLine = true
            )
            OutlinedTextField(
                value = secondQuery,
                onValueChange = onSecondQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("İkinci şehir") },
                singleLine = true
            )
            Button(onClick = onCompare, modifier = Modifier.fillMaxWidth()) { Text("Şimdi karşılaştır") }

            when (val state = uiState) {
                CompareUiState.Idle -> Unit
                CompareUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                is CompareUiState.Error -> Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(state.message, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                }
                is CompareUiState.Success -> {
                    val firstScore = state.first.forecast.comfortScore()
                    val secondScore = state.second.forecast.comfortScore()
                    Surface(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = if (firstScore.value == secondScore.value) {
                                "İki şehir de bugün benzer konfor sunuyor."
                            } else {
                                "Bugünün avantajı: ${if (firstScore.value > secondScore.value) state.first.city.name else state.second.city.name}"
                            },
                            modifier = Modifier.padding(18.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    ComparisonCard(state.first, unit)
                    ComparisonCard(state.second, unit)
                }
            }
        }
    }
}

@Composable
private fun ComparisonCard(item: ComparedCity, unit: TemperatureUnit) {
    val current = item.forecast.current
    val condition = WeatherCondition.fromWeatherCode(current.weatherCode)
    val score = item.forecast.comfortScore()
    val today = item.forecast.daily.firstOrNull()
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(item.city.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(item.city.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(condition.emoji, style = MaterialTheme.typography.headlineMedium)
            }
            Text(temperature(current.temperatureC, unit), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            Text(condition.label)
            Text("Wearly Comfort Score  ${score.value}/100 · ${score.label}", color = MaterialTheme.colorScheme.primary)
            Text(
                "Hissedilen ${temperature(current.apparentTemperatureC ?: current.temperatureC, unit)}  ·  " +
                    "Yağış %${today?.precipitationProbability ?: 0}  ·  Rüzgâr ${(current.windSpeedKmh ?: 0.0).roundToInt()} km/sa",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun temperature(celsius: Double, unit: TemperatureUnit): String = when (unit) {
    TemperatureUnit.CELSIUS -> "${celsius.roundToInt()}°C"
    TemperatureUnit.FAHRENHEIT -> "${(celsius * 9 / 5 + 32).roundToInt()}°F"
}
