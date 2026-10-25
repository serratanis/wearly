package com.kampplus.hava.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kampplus.hava.model.City
import com.kampplus.hava.model.TemperatureUnit
import com.kampplus.hava.model.ThemeMode
import com.kampplus.hava.model.UserSettings
import com.kampplus.hava.ui.city.CityRoute
import com.kampplus.hava.ui.district.DistrictRoute
import com.kampplus.hava.ui.district.DistrictViewModel
import com.kampplus.hava.ui.forecast.ForecastRoute
import com.kampplus.hava.ui.forecast.ForecastViewModel
import com.kampplus.hava.ui.settings.SettingsScreen
import com.kampplus.hava.ui.style.StyleAssistantRoute
import com.kampplus.hava.ui.style.StyleAssistantViewModel

/**
 * Uygulamanın iki ekranı ve aralarındaki geçiş.
 *
 * Navigation Compose, her ekranı bir "rota" (metin adresi) ile tanımlar. Bir şehre
 * dokunulduğunda adresi üretip [NavHostController.navigate] ile o adrese gideriz;
 * geri tuşu yığından bir önceki ekrana döner.
 *
 * Parametreleri yol parçası (`forecast/{name}`) yerine sorgu parametresi olarak
 * taşıyoruz (`forecast?name=...`); çünkü değerler boş olabilir ve boş bir yol
 * parçası rotayı eşleşmez hâle getirir.
 */
@Composable
fun HavaNavHost(
    settings: UserSettings,
    onTemperatureUnitChange: (TemperatureUnit) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onDailyAdviceChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = CITY_ROUTE,
        modifier = modifier
    ) {
        composable(CITY_ROUTE) {
            CityRoute(
                onCityClick = { city ->
                    navController.navigate(
                        if (city.countryCode == "TR" && !city.isDistrict) districtRoute(city) else forecastRoute(city)
                    )
                },
                onStyleClick = { city -> navController.navigate(styleRoute(city.name, city.latitude, city.longitude)) },
                onSettingsClick = { navController.navigate(SETTINGS_ROUTE) }
            )
        }
        composable(
            route = DISTRICT_ROUTE,
            arguments = listOf(
                navArgument(DistrictViewModel.ARG_PROVINCE_NAME) { type = NavType.StringType },
                navArgument(DistrictViewModel.ARG_LATITUDE) { type = NavType.StringType },
                navArgument(DistrictViewModel.ARG_LONGITUDE) { type = NavType.StringType }
            )
        ) {
            DistrictRoute(
                onBack = { navController.popBackStack() },
                onLocationClick = { city -> navController.navigate(forecastRoute(city)) }
            )
        }
        composable(
            route = FORECAST_ROUTE,
            arguments = listOf(
                // Hepsi String: koordinatlar ViewModel içinde Double'a çevrilir.
                navArgument(ForecastViewModel.ARG_NAME) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(ForecastViewModel.ARG_SUBTITLE) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(ForecastViewModel.ARG_LATITUDE) {
                    type = NavType.StringType
                    defaultValue = "0"
                },
                navArgument(ForecastViewModel.ARG_LONGITUDE) {
                    type = NavType.StringType
                    defaultValue = "0"
                }
            )
        ) {
            ForecastRoute(
                settings = settings,
                onBack = { navController.popBackStack() },
                onStyleClick = { name, latitude, longitude ->
                    navController.navigate(styleRoute(name, latitude, longitude))
                }
            )
        }
        composable(
            route = STYLE_ROUTE,
            arguments = listOf(
                navArgument(StyleAssistantViewModel.ARG_NAME) { type = NavType.StringType },
                navArgument(StyleAssistantViewModel.ARG_LATITUDE) { type = NavType.StringType },
                navArgument(StyleAssistantViewModel.ARG_LONGITUDE) { type = NavType.StringType }
            )
        ) {
            StyleAssistantRoute(onBack = { navController.popBackStack() })
        }
        composable(SETTINGS_ROUTE) {
            SettingsScreen(
                settings = settings,
                onBack = { navController.popBackStack() },
                onTemperatureUnitChange = onTemperatureUnitChange,
                onThemeModeChange = onThemeModeChange,
                onDailyAdviceChange = onDailyAdviceChange
            )
        }
    }
}

/** Liste ekranının adresi. */
private const val CITY_ROUTE = "city"
private const val SETTINGS_ROUTE = "settings"
private const val DISTRICT_ROUTE =
    "district?provinceName={provinceName}&latitude={latitude}&longitude={longitude}"
private const val STYLE_ROUTE =
    "style?styleName={styleName}&styleLatitude={styleLatitude}&styleLongitude={styleLongitude}"

/** Tahmin ekranının adres şablonu; süslü parantezler çalışma anında doldurulur. */
private const val FORECAST_ROUTE =
    "forecast?name={name}&subtitle={subtitle}&latitude={latitude}&longitude={longitude}"

/**
 * Bir şehir için tahmin ekranının gerçek adresini üretir.
 *
 * `Uri.encode`: şehir adındaki boşluk ve Türkçe karakterler adreste sorun
 * çıkarmasın diye kaçışlanır ("Afyonkarahisar, Türkiye" → "Afyonkarahisar%2C%20T%C3%BCrkiye").
 */
private fun forecastRoute(city: City): String = buildString {
    append("forecast")
    append("?name=").append(Uri.encode(city.name))
    append("&subtitle=").append(Uri.encode(city.subtitle))
    append("&latitude=").append(city.latitude)
    append("&longitude=").append(city.longitude)
}

private fun districtRoute(city: City): String = buildString {
    append("district")
    append("?provinceName=").append(Uri.encode(city.name))
    append("&latitude=").append(city.latitude)
    append("&longitude=").append(city.longitude)
}

private fun styleRoute(name: String, latitude: Double, longitude: Double): String = buildString {
    append("style")
    append("?styleName=").append(Uri.encode(name))
    append("&styleLatitude=").append(latitude)
    append("&styleLongitude=").append(longitude)
}
