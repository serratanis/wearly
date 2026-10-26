package com.kampplus.hava.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kampplus.hava.model.StylePreference
import com.kampplus.hava.model.TemperatureSensitivity
import com.kampplus.hava.model.UserSettings

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    settings: UserSettings,
    onStylePreferenceChange: (StylePreference) -> Unit,
    onTemperatureSensitivityChange: (TemperatureSensitivity) -> Unit,
    onHomeCityChange: (String) -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var homeCity by rememberSaveable(settings.homeCity) { mutableStateOf(settings.homeCity) }
    val titles = listOf("Tarzını tanıyalım", "Konforunu kişiselleştir", "Şehrini seç")
    val descriptions = listOf(
        "Wearly, hava koşullarına uygun önerileri senin stil çizginde hazırlar.",
        "Aynı sıcaklık herkese aynı hissettirmez. Önerileri sana göre ayarlayalım.",
        "Açılış önerilerini yaşadığın şehre göre hazırlayalım. Sonradan değiştirebilirsin."
    )

    Scaffold(modifier = modifier, containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("wearly.", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                TextButton(onClick = onComplete) { Text("Atla") }
            }
            LinearProgressIndicator(
                progress = { (step + 1) / 3f },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.size(4.dp))
            Text(
                text = listOf("✨", "🌡️", "📍")[step],
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Text(titles[step], style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                descriptions[step],
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                when (step) {
                    0 -> FlowRow(
                        modifier = Modifier.padding(18.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StylePreference.entries.forEach { preference ->
                            FilterChip(
                                selected = settings.stylePreference == preference,
                                onClick = { onStylePreferenceChange(preference) },
                                label = { Text(preference.label()) }
                            )
                        }
                    }
                    1 -> Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TemperatureSensitivity.entries.forEach { sensitivity ->
                            FilterChip(
                                modifier = Modifier.fillMaxWidth(),
                                selected = settings.temperatureSensitivity == sensitivity,
                                onClick = { onTemperatureSensitivityChange(sensitivity) },
                                label = { Text(sensitivity.label()) }
                            )
                        }
                    }
                    else -> Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = homeCity,
                            onValueChange = { homeCity = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Şehir") },
                            singleLine = true
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("İstanbul", "Ankara", "İzmir", "Antalya").forEach { city ->
                                FilterChip(
                                    selected = homeCity == city,
                                    onClick = { homeCity = city },
                                    label = { Text(city) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (step > 0) {
                    TextButton(onClick = { step-- }) { Text("Geri") }
                }
                Button(
                    onClick = {
                        if (step < 2) {
                            step++
                        } else {
                            onHomeCityChange(homeCity.trim().ifBlank { "İstanbul" })
                            onComplete()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (step == 2) "Wearly'yi başlat" else "Devam")
                }
            }
        }
    }
}

private fun StylePreference.label(): String = when (this) {
    StylePreference.CASUAL -> "Günlük"
    StylePreference.ELEGANT -> "Zarif"
    StylePreference.SPORTY -> "Sportif"
    StylePreference.MINIMAL -> "Minimal"
    StylePreference.BUSINESS -> "Ofis"
}

private fun TemperatureSensitivity.label(): String = when (this) {
    TemperatureSensitivity.COLD_SENSITIVE -> "Çabuk üşürüm"
    TemperatureSensitivity.BALANCED -> "Dengeli"
    TemperatureSensitivity.HEAT_SENSITIVE -> "Çabuk ısınırım"
}
