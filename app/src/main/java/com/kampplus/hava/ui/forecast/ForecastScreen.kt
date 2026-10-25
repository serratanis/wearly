package com.kampplus.hava.ui.forecast

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kampplus.hava.R
import com.kampplus.hava.model.ActivityType
import com.kampplus.hava.model.CurrentWeather
import com.kampplus.hava.model.DailyForecast
import com.kampplus.hava.model.Forecast
import com.kampplus.hava.model.HourlyForecast
import com.kampplus.hava.model.TemperatureUnit
import com.kampplus.hava.model.UserSettings
import com.kampplus.hava.model.WeatherCondition
import com.kampplus.hava.model.bestTimeFor
import com.kampplus.hava.model.comfortScore
import com.kampplus.hava.model.dailyAdvice
import com.kampplus.hava.model.dayInsight
import com.kampplus.hava.model.sunProtection
import com.kampplus.hava.ui.LoadState
import com.kampplus.hava.ui.component.ErrorView
import com.kampplus.hava.ui.theme.HavaTheme
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.roundToInt

/** Tahmin ekranının bağlayıcı katmanı; ViewModel'i edinir ve state'i aktarır. */
@Composable
fun ForecastRoute(
    onBack: () -> Unit,
    onStyleClick: (String, Double, Double) -> Unit,
    settings: UserSettings,
    modifier: Modifier = Modifier,
    viewModel: ForecastViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ForecastScreen(
        cityName = viewModel.cityName,
        subtitle = viewModel.subtitle,
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::onRetry,
        onStyleClick = { onStyleClick(viewModel.cityName, viewModel.latitude, viewModel.longitude) },
        settings = settings,
        modifier = modifier
    )
}

/** Hava durumuna göre renklenen, saatlik ve yedi günlük tahmini bir arada sunan ekran. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastScreen(
    cityName: String,
    subtitle: String,
    uiState: LoadState<Forecast>,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onStyleClick: () -> Unit,
    settings: UserSettings,
    modifier: Modifier = Modifier
) {
    val forecast = (uiState as? LoadState.Success)?.data
    val context = LocalContext.current

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = { Text(cityName.ifEmpty { stringResource(R.string.detail_title) }) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        enabled = forecast != null,
                        onClick = {
                            forecast?.let {
                                shareForecast(context, cityName, it.current, settings.temperatureUnit)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = stringResource(R.string.action_share)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                LoadState.Loading -> CircularProgressIndicator()
                LoadState.Empty -> ErrorView(message = stringResource(R.string.forecast_empty))
                is LoadState.Error -> ErrorView(message = uiState.message, onRetry = onRetry)
                is LoadState.Success -> ForecastContent(
                    cityName = cityName,
                    subtitle = subtitle,
                    forecast = uiState.data,
                    settings = settings,
                    onStyleClick = onStyleClick
                )
            }
        }
    }
}

@Composable
private fun ForecastContent(
    cityName: String,
    subtitle: String,
    forecast: Forecast,
    settings: UserSettings,
    onStyleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        WeatherHero(
            cityName = cityName,
            subtitle = subtitle,
            current = forecast.current,
            unit = settings.temperatureUnit,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        DayInsightCard(
            forecast = forecast,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        ComfortScoreCard(
            forecast = forecast,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        ActivityPlannerCard(
            forecast = forecast,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (settings.dailyAdviceEnabled) {
            DailyAssistantCard(
                forecast = forecast,
                onStyleClick = onStyleClick
            )
        }

        SunlightCard(
            forecast = forecast,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        SectionTitle(
            title = stringResource(R.string.detail_hourly),
            subtitle = stringResource(R.string.hourly_subtitle)
        )
        HourlyRow(hours = forecast.hourly, unit = settings.temperatureUnit)

        SectionTitle(
            title = stringResource(R.string.detail_daily),
            subtitle = stringResource(R.string.daily_subtitle)
        )
        DailyForecastCard(
            days = forecast.daily,
            unit = settings.temperatureUnit,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        DataSourceNote()
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun ActivityPlannerCard(forecast: Forecast, modifier: Modifier = Modifier) {
    var selected by rememberSaveable { mutableStateOf(ActivityType.WALK) }
    val window = forecast.bestTimeFor(selected)
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Planın için en iyi zaman", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "Önümüzdeki 24 saatin sıcaklık ve yağış verileri birlikte değerlendirilir.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ActivityType.entries) { activity ->
                    AssistChip(
                        onClick = { selected = activity },
                        label = { Text(if (selected == activity) "✓ ${activity.label}" else activity.label) }
                    )
                }
            }
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                    Text(window?.timeRange ?: "Uygun aralık bulunamadı", style = MaterialTheme.typography.titleMedium)
                    window?.let { Text(it.detail, style = MaterialTheme.typography.bodySmall) }
                }
            }
        }
    }
}

private val ActivityType.label: String
    get() = when (this) {
        ActivityType.WALK -> "🚶 Yürüyüş"
        ActivityType.SPORT -> "🏃 Spor"
        ActivityType.COFFEE -> "☕ Kahve"
        ActivityType.COMMUTE -> "🚌 Yolculuk"
    }

@Composable
private fun ComfortScoreCard(forecast: Forecast, modifier: Modifier = Modifier) {
    val score = forecast.comfortScore()
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(score.value.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("/ 100", style = MaterialTheme.typography.labelSmall)
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Wearly Comfort Score", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text(score.label, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(score.explanation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** Hava koduna göre renk değiştiren ana bilgi kartı. */
@Composable
private fun WeatherHero(cityName: String, subtitle: String, current: CurrentWeather, unit: TemperatureUnit, modifier: Modifier = Modifier) {
    val condition = WeatherCondition.fromWeatherCode(current.weatherCode)
    val shape = RoundedCornerShape(32.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.linearGradient(weatherGradient(condition)))
            .padding(22.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cityName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                Text(
                    text = condition.emoji,
                    style = MaterialTheme.typography.displayMedium
                )
            }

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = temperature(current.temperatureC, unit),
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Light
                )
                Column(modifier = Modifier.padding(start = 12.dp, bottom = 10.dp)) {
                    Text(
                        text = condition.label,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(
                            R.string.updated_at,
                            current.observedAt.format(OBSERVED_FORMAT)
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.76f)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HeroMetric(
                    label = stringResource(R.string.detail_feels_like),
                    value = current.apparentTemperatureC?.let { temperature(it, unit) } ?: "-",
                    modifier = Modifier.weight(1f)
                )
                HeroMetric(
                    label = stringResource(R.string.detail_humidity),
                    value = current.humidityPercent?.let { "%$it" } ?: "-",
                    modifier = Modifier.weight(1f)
                )
                HeroMetric(
                    label = stringResource(R.string.detail_wind),
                    value = current.windSpeedKmh?.let { "${it.roundToInt()} km/sa" } ?: "-",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun HeroMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(alpha = 0.16f),
        contentColor = Color.White,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(text = value, style = MaterialTheme.typography.labelLarge, maxLines = 1)
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.74f),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun DayInsightCard(forecast: Forecast, modifier: Modifier = Modifier) {
    val insight = forecast.dayInsight()
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Bugünün kısa özeti", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text(insight.headline, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(insight.summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            insight.bestTime?.let {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        "✨ $it",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyAssistantCard(forecast: Forecast, onStyleClick: () -> Unit, modifier: Modifier = Modifier) {
    val advice = forecast.dailyAdvice()
    if (advice.isEmpty()) return

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle(
            title = stringResource(R.string.daily_assistant_title),
            subtitle = "Sana özel hazırlık"
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(advice) { item ->
                ElevatedCard(
                    modifier = Modifier.width(280.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                item.icon,
                                modifier = Modifier.padding(10.dp),
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                        Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(item.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        Button(
            onClick = onStyleClick,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text("✨ Stil Asistanına sor")
        }
    }
}

@Composable
private fun SunlightCard(forecast: Forecast, modifier: Modifier = Modifier) {
    val day = forecast.daily.firstOrNull()
    if (day?.sunrise == null && day?.sunset == null && day?.uvIndexMax == null) return
    val protection = forecast.sunProtection()

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.sunlight_title),
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                SmallMetric("🌅", stringResource(R.string.sunrise), day.sunrise?.format(SUN_FORMAT) ?: "-")
                SmallMetric("🌇", stringResource(R.string.sunset), day.sunset?.format(SUN_FORMAT) ?: "-")
                SmallMetric("☀️", stringResource(R.string.uv_index), day.uvIndexMax?.let(::uvLabel) ?: "-")
            }
            protection?.let {
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(it.action, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(it.level, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    Text("🧴", style = MaterialTheme.typography.headlineMedium)
                }
                LinearProgressIndicator(
                    progress = { it.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Text(it.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "Genel korunma rehberidir; kişisel sağlık önerisi değildir.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SmallMetric(icon: String, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, style = MaterialTheme.typography.titleLarge)
        Text(text = value, style = MaterialTheme.typography.titleMedium)
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HourlyRow(hours: List<HourlyForecast>, unit: TemperatureUnit, modifier: Modifier = Modifier) {
    val now = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS)
    val upcoming = hours.filter { !it.time.isBefore(now) }.take(HOURLY_COUNT).ifEmpty { hours.take(HOURLY_COUNT) }

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items = upcoming, key = { it.time.toString() }) { hour ->
            val isCurrent = hour == upcoming.firstOrNull()
            ElevatedCard(
                modifier = Modifier.width(92.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = if (isCurrent) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (isCurrent) stringResource(R.string.now) else hour.time.format(HOUR_FORMAT),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = WeatherCondition.fromWeatherCode(hour.weatherCode).emoji,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = temperature(hour.temperatureC, unit),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = percent(hour.precipitationProbability) ?: " ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyForecastCard(days: List<DailyForecast>, unit: TemperatureUnit, modifier: Modifier = Modifier) {
    val rangeMin = days.minOfOrNull { it.minTemperatureC } ?: 0.0
    val rangeMax = days.maxOfOrNull { it.maxTemperatureC } ?: 1.0

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        days.forEachIndexed { index, day ->
            if (index > 0) HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            DailyRow(
                day = day,
                isToday = index == 0,
                rangeMin = rangeMin,
                rangeMax = rangeMax,
                unit = unit
            )
        }
    }
}

@Composable
private fun DailyRow(
    day: DailyForecast,
    isToday: Boolean,
    rangeMin: Double,
    rangeMax: Double,
    unit: TemperatureUnit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isToday) {
                stringResource(R.string.today)
            } else {
                day.date.format(DAY_FORMAT).replaceFirstChar(Char::titlecase)
            },
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = percent(day.precipitationProbability).orEmpty(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(38.dp),
            textAlign = TextAlign.End
        )
        Text(
            text = WeatherCondition.fromWeatherCode(day.weatherCode).emoji,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Text(
            text = temperature(day.minTemperatureC, unit),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(48.dp),
            textAlign = TextAlign.End
        )
        TemperatureRange(
            min = day.minTemperatureC,
            max = day.maxTemperatureC,
            rangeMin = rangeMin,
            rangeMax = rangeMax,
            modifier = Modifier
                .width(64.dp)
                .padding(horizontal = 8.dp)
        )
        Text(
            text = temperature(day.maxTemperatureC, unit),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(48.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun TemperatureRange(min: Double, max: Double, rangeMin: Double, rangeMax: Double, modifier: Modifier = Modifier) {
    val span = (rangeMax - rangeMin).takeIf { it > 0 } ?: 1.0
    val start = ((min - rangeMin) / span).toFloat().coerceIn(0f, 1f)
    val end = ((max - rangeMin) / span).toFloat().coerceIn(0f, 1f)
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val rangeColor = MaterialTheme.colorScheme.primary
    val description = "$min ile $max derece arası"

    Canvas(
        modifier = modifier
            .height(8.dp)
            .semantics { contentDescription = description }
    ) {
        val centerY = size.height / 2
        drawLine(
            color = trackColor,
            start = Offset(0f, centerY),
            end = Offset(size.width, centerY),
            strokeWidth = size.height,
            cap = StrokeCap.Round
        )
        drawLine(
            color = rangeColor,
            start = Offset(size.width * start, centerY),
            end = Offset(size.width * end, centerY),
            strokeWidth = size.height,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DataSourceNote(modifier: Modifier = Modifier) {
    Text(
        text = "Open-Meteo gerçek zamanlı verileri · Yerel saat dilimi",
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

private fun weatherGradient(condition: WeatherCondition): List<Color> = when (condition) {
    WeatherCondition.CLEAR,
    WeatherCondition.MAINLY_CLEAR -> listOf(Color(0xFF075985), Color(0xFF0284C7))
    WeatherCondition.PARTLY_CLOUDY -> listOf(Color(0xFF334155), Color(0xFF0369A1))
    WeatherCondition.OVERCAST,
    WeatherCondition.FOG -> listOf(Color(0xFF334155), Color(0xFF64748B))
    WeatherCondition.DRIZZLE,
    WeatherCondition.RAIN,
    WeatherCondition.RAIN_SHOWERS -> listOf(Color(0xFF0F172A), Color(0xFF2563EB))
    WeatherCondition.SNOW,
    WeatherCondition.SNOW_SHOWERS -> listOf(Color(0xFF475569), Color(0xFF94A3B8), Color(0xFF0EA5E9))
    WeatherCondition.THUNDERSTORM -> listOf(Color(0xFF111827), Color(0xFF4338CA))
    WeatherCondition.UNKNOWN -> listOf(Color(0xFF0F766E), Color(0xFF0891B2))
}

private fun temperature(celsius: Double, unit: TemperatureUnit): String = when (unit) {
    TemperatureUnit.CELSIUS -> "${celsius.roundToInt()}°C"
    TemperatureUnit.FAHRENHEIT -> "${(celsius * 9 / 5 + 32).roundToInt()}°F"
}

private fun percent(value: Int?) = value?.takeIf { it > 0 }?.let { "%$it" }

private fun uvLabel(value: Double): String = when {
    value < 3 -> "${value.roundToInt()} · Düşük"
    value < 6 -> "${value.roundToInt()} · Orta"
    value < 8 -> "${value.roundToInt()} · Yüksek"
    value < 11 -> "${value.roundToInt()} · Çok yüksek"
    else -> "${value.roundToInt()} · Aşırı"
}

private fun shareForecast(context: Context, cityName: String, current: CurrentWeather, unit: TemperatureUnit) {
    val condition = WeatherCondition.fromWeatherCode(current.weatherCode)
    val text = "$cityName: ${temperature(current.temperatureC, unit)}, ${condition.label}. wearly. ile paylaşıldı."
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.action_share)))
}

private const val HOURLY_COUNT = 24
private val HOUR_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val OBSERVED_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val SUN_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val DAY_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE", Locale.forLanguageTag("tr"))

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun ForecastScreenPreview() {
    HavaTheme {
        ForecastScreen(
            cityName = "Ankara",
            subtitle = "Ankara, Türkiye",
            uiState = LoadState.Success(
                Forecast(
                    current = CurrentWeather(
                        temperatureC = 21.0,
                        weatherCode = 0,
                        observedAt = LocalDateTime.of(2026, 10, 25, 14, 0),
                        apparentTemperatureC = 20.0,
                        humidityPercent = 45,
                        windSpeedKmh = 12.0
                    ),
                    hourly = List(24) {
                        HourlyForecast(
                            time = LocalDateTime.now().plusHours(it.toLong()),
                            temperatureC = 18.0 + it / 3.0,
                            weatherCode = if (it < 8) 1 else 2,
                            precipitationProbability = it * 2
                        )
                    },
                    daily = List(7) {
                        DailyForecast(
                            date = LocalDate.now().plusDays(it.toLong()),
                            minTemperatureC = 9.0 + it,
                            maxTemperatureC = 19.0 + it,
                            weatherCode = if (it < 3) 1 else 61,
                            precipitationProbability = it * 8,
                            sunrise = LocalDate.now().plusDays(it.toLong()).atTime(7, 12),
                            sunset = LocalDate.now().plusDays(it.toLong()).atTime(18, 4),
                            uvIndexMax = 4.8
                        )
                    }
                )
            ),
            onBack = {},
            onRetry = {},
            onStyleClick = {},
            settings = UserSettings()
        )
    }
}
