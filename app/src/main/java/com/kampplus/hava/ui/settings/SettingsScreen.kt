package com.kampplus.hava.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kampplus.hava.model.TemperatureUnit
import com.kampplus.hava.model.ThemeMode
import com.kampplus.hava.model.UserSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: UserSettings,
    onBack: () -> Unit,
    onTemperatureUnitChange: (TemperatureUnit) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onDailyAdviceChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Ayarlar") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PreferenceCard(title = "Sıcaklık birimi", subtitle = "Tüm değerlerde birim açıkça gösterilir.") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = settings.temperatureUnit == TemperatureUnit.CELSIUS,
                        onClick = { onTemperatureUnitChange(TemperatureUnit.CELSIUS) },
                        label = { Text("Santigrat · °C") }
                    )
                    FilterChip(
                        selected = settings.temperatureUnit == TemperatureUnit.FAHRENHEIT,
                        onClick = { onTemperatureUnitChange(TemperatureUnit.FAHRENHEIT) },
                        label = { Text("Fahrenhayt · °F") }
                    )
                }
            }

            PreferenceCard(title = "Görünüm", subtitle = "Sistem tercihini izle veya temayı sabitle.") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = settings.themeMode == mode,
                            onClick = { onThemeModeChange(mode) },
                            label = { Text(mode.label) }
                        )
                    }
                }
            }

            PreferenceCard(title = "Günün Asistanı", subtitle = "Giysi, şemsiye, UV ve rüzgâr önerilerini göster.") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(if (settings.dailyAdviceEnabled) "Açık" else "Kapalı")
                    Switch(checked = settings.dailyAdviceEnabled, onCheckedChange = onDailyAdviceChange)
                }
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Veri ve gizlilik", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Hava ve şehir sonuçları Open-Meteo’dan, Türkiye ilçe koordinatları açık coğrafya servisinden alınır. Lodos hesap açtırmaz; arama ve tercihlerini kendi sunucusuna kaydetmez.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("Lodos · Sürüm 1.1.0", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun PreferenceCard(title: String, subtitle: String, content: @Composable () -> Unit) {
    Card(shape = RoundedCornerShape(20.dp)) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            content()
        }
    }
}

private val ThemeMode.label: String
    get() = when (this) {
        ThemeMode.SYSTEM -> "Sistem"
        ThemeMode.LIGHT -> "Açık"
        ThemeMode.DARK -> "Koyu"
    }
