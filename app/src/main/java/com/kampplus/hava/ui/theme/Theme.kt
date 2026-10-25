package com.kampplus.hava.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.kampplus.hava.model.ThemeMode

/**
 * Açık tema paleti. Her Material rolü ([Color.kt]'deki) bir renge bağlanır:
 * `primary` ana vurgu, `surface` kart zemini, `onSurface` o zemindeki yazı rengidir.
 */
private val LightColors = lightColorScheme(
    primary = Teal40,
    onPrimary = Neutral99,
    primaryContainer = Teal90,
    onPrimaryContainer = Teal10,
    secondary = Amber40,
    secondaryContainer = Amber90,
    background = Neutral99,
    onBackground = Neutral10,
    surface = Neutral99,
    onSurface = Neutral10,
    surfaceVariant = NeutralVariant90,
    onSurfaceVariant = NeutralVariant30,
    surfaceContainer = Neutral95,
    error = Red40
)

/** Koyu tema paleti. Aynı roller, koyu zemine uygun tonlarla. */
private val DarkColors = darkColorScheme(
    primary = Teal80,
    onPrimary = Teal20,
    primaryContainer = Teal30,
    onPrimaryContainer = Teal90,
    secondary = Amber80,
    background = Neutral10,
    onBackground = Neutral90,
    surface = Neutral10,
    onSurface = Neutral90,
    onSurfaceVariant = NeutralVariant90,
    error = Red80
)

/**
 * Uygulamanın teması. Bütün ekranlar bunun içine sarmalanır.
 *
 * @param darkTheme Varsayılan olarak telefonun sistem ayarını izler
 *   ([isSystemInDarkTheme]). @Preview'larda elle `true` verip koyu temayı deneyebilirsin.
 */
@Composable
fun HavaTheme(themeMode: ThemeMode = ThemeMode.SYSTEM, content: @Composable () -> Unit) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = HavaTypography,
        content = content
    )
}
