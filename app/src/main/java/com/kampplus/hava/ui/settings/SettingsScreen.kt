package com.kampplus.hava.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import com.kampplus.hava.model.OutfitPriority
import com.kampplus.hava.model.StylePreference
import com.kampplus.hava.model.TemperatureSensitivity
import com.kampplus.hava.model.TemperatureUnit
import com.kampplus.hava.model.ThemeMode
import com.kampplus.hava.model.UserSettings

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    settings: UserSettings,
    onBack: () -> Unit,
    onTemperatureUnitChange: (TemperatureUnit) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onDailyAdviceChange: (Boolean) -> Unit,
    onStylePreferenceChange: (StylePreference) -> Unit,
    onTemperatureSensitivityChange: (TemperatureSensitivity) -> Unit,
    onOutfitPriorityChange: (OutfitPriority) -> Unit,
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

            PreferenceCard(
                title = "Stil profilim",
                subtitle = "Wearly Look, planında özel bir stil belirtmezsen bu çizgiyi kullanır."
            ) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StylePreference.entries.forEach { preference ->
                        FilterChip(
                            selected = settings.stylePreference == preference,
                            onClick = { onStylePreferenceChange(preference) },
                            label = { Text(preference.label) }
                        )
                    }
                }
            }

            PreferenceCard(
                title = "Sıcaklığı nasıl hissederim?",
                subtitle = "Önerilen katmanlar yalnızca termometreye değil, sana göre de ayarlanır."
            ) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TemperatureSensitivity.entries.forEach { sensitivity ->
                        FilterChip(
                            selected = settings.temperatureSensitivity == sensitivity,
                            onClick = { onTemperatureSensitivityChange(sensitivity) },
                            label = { Text(sensitivity.label) }
                        )
                    }
                }
            }

            PreferenceCard(
                title = "Kombin önceliğim",
                subtitle = "Konfor ve şıklık arasındaki dengeyi belirle."
            ) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutfitPriority.entries.forEach { priority ->
                        FilterChip(
                            selected = settings.outfitPriority == priority,
                            onClick = { onOutfitPriorityChange(priority) },
                            label = { Text(priority.label) }
                        )
                    }
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
                        "Hava ve şehir sonuçları Open-Meteo’dan, Türkiye ilçe koordinatları açık coğrafya servisinden alınır. wearly. hesap açtırmaz; arama ve tercihlerini kendi sunucusuna kaydetmez.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("wearly. · Sürüm 1.1.0", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
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

private val StylePreference.label: String
    get() = when (this) {
        StylePreference.CASUAL -> "Günlük"
        StylePreference.ELEGANT -> "Zarif"
        StylePreference.SPORTY -> "Sportif"
        StylePreference.MINIMAL -> "Minimal"
        StylePreference.BUSINESS -> "Ofis"
    }

private val TemperatureSensitivity.label: String
    get() = when (this) {
        TemperatureSensitivity.COLD_SENSITIVE -> "Çabuk üşürüm"
        TemperatureSensitivity.BALANCED -> "Dengeli"
        TemperatureSensitivity.HEAT_SENSITIVE -> "Çabuk ısınırım"
    }

private val OutfitPriority.label: String
    get() = when (this) {
        OutfitPriority.COMFORT -> "Konfor"
        OutfitPriority.BALANCED -> "Dengeli"
        OutfitPriority.ELEGANCE -> "Şıklık"
    }
