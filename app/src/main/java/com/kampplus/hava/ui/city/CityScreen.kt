package com.kampplus.hava.ui.city

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kampplus.hava.R
import com.kampplus.hava.model.City
import com.kampplus.hava.model.SearchScope
import com.kampplus.hava.ui.LoadState
import com.kampplus.hava.ui.component.EmptyView
import com.kampplus.hava.ui.component.ErrorView
import com.kampplus.hava.ui.component.ShimmerList
import com.kampplus.hava.ui.theme.HavaTheme

@Composable
fun CityRoute(
    onCityClick: (City) -> Unit,
    onStyleClick: (City) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CityViewModel = viewModel()
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val scope by viewModel.scope.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CityScreen(
        query = query,
        scope = scope,
        uiState = uiState,
        onQueryChange = viewModel::onQueryChange,
        onScopeChange = viewModel::onScopeChange,
        onSearch = viewModel::search,
        onCityClick = onCityClick,
        onStyleClick = onStyleClick,
        onSettingsClick = onSettingsClick,
        modifier = modifier
    )
}

@Composable
fun CityScreen(
    query: String,
    scope: SearchScope,
    uiState: LoadState<List<City>>,
    onQueryChange: (String) -> Unit,
    onScopeChange: (SearchScope) -> Unit,
    onSearch: () -> Unit,
    onCityClick: (City) -> Unit,
    onStyleClick: (City) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier, containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SearchHeader(
                query = query,
                scope = scope,
                onQueryChange = onQueryChange,
                onScopeChange = onScopeChange,
                onSearch = onSearch,
                onSettingsClick = onSettingsClick
            )
            ResultsHeader(scope = scope, uiState = uiState)
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                when (uiState) {
                    LoadState.Loading -> ShimmerList()
                    is LoadState.Success -> CityList(
                        cities = uiState.data,
                        scope = scope,
                        onCityClick = onCityClick,
                        onStyleClick = onStyleClick,
                        onExplore = {
                            onQueryChange("")
                            onScopeChange(if (scope == SearchScope.TURKEY) SearchScope.WORLD else SearchScope.TURKEY)
                        }
                    )
                    LoadState.Empty -> EmptyView(
                        title = stringResource(R.string.city_not_found),
                        message = stringResource(R.string.city_not_found_message)
                    )
                    is LoadState.Error -> ErrorView(message = uiState.message, onRetry = onSearch)
                }
            }
        }
    }
}

@Composable
private fun SearchHeader(
    query: String,
    scope: SearchScope,
    onQueryChange: (String) -> Unit,
    onScopeChange: (SearchScope) -> Unit,
    onSearch: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Image(
                    painter = painterResource(R.drawable.hava_logo),
                    contentDescription = null,
                    modifier = Modifier.size(34.dp)
                )
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Filled.Settings, contentDescription = "Ayarlar")
            }
        }
        Text(
            text = stringResource(R.string.search_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = stringResource(R.string.search_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        SearchField(query = query, onQueryChange = onQueryChange, onSearch = onSearch)
        SearchScopePicker(selected = scope, onSelected = onScopeChange)
        Text(
            text = "Hızlı keşfet",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        QuickCities(scope = scope, onSelect = { city ->
            onQueryChange(city)
            onSearch()
        })
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit, onSearch: () -> Unit, modifier: Modifier = Modifier) {
    val keyboardController = LocalSoftwareKeyboardController.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(stringResource(R.string.search_hint)) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.action_clear))
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = {
            keyboardController?.hide()
            onSearch()
        })
    )
}

@Composable
private fun SearchScopePicker(selected: SearchScope, onSelected: (SearchScope) -> Unit) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(5.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                modifier = Modifier.weight(1f),
                selected = selected == SearchScope.TURKEY,
                onClick = { onSelected(SearchScope.TURKEY) },
                label = { Text(stringResource(R.string.scope_turkey)) }
            )
            FilterChip(
                modifier = Modifier.weight(1f),
                selected = selected == SearchScope.WORLD,
                onClick = { onSelected(SearchScope.WORLD) },
                label = { Text(stringResource(R.string.scope_world)) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickCities(scope: SearchScope, onSelect: (String) -> Unit) {
    val cities = if (scope == SearchScope.TURKEY) TURKEY_CITIES else WORLD_CITIES
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        cities.forEach { city ->
            SuggestionChip(onClick = { onSelect(city) }, label = { Text(city) })
        }
    }
}

@Composable
private fun ResultsHeader(scope: SearchScope, uiState: LoadState<List<City>>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = if (scope == SearchScope.TURKEY) {
                stringResource(R.string.results_turkey)
            } else {
                stringResource(R.string.results_world)
            },
            style = MaterialTheme.typography.titleMedium
        )
        if (uiState is LoadState.Success) {
            Text(
                text = stringResource(R.string.city_count, uiState.data.size),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CityList(
    cities: List<City>,
    scope: SearchScope,
    onCityClick: (City) -> Unit,
    onStyleClick: (City) -> Unit,
    onExplore: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items = cities, key = { it.id }) { city ->
            CityCard(city = city, onClick = { onCityClick(city) })
        }
        cities.firstOrNull()?.let { city ->
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clickable { onStyleClick(city) },
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                "✨",
                                modifier = Modifier.padding(10.dp),
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Stil Asistanı", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "${city.name} havasına göre kombinini birlikte hazırlayın.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text("→", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clickable(onClick = onExplore),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(if (scope == SearchScope.TURKEY) "🌍" else "🇹🇷", style = MaterialTheme.typography.headlineMedium)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (scope == SearchScope.TURKEY) "Dünyayı keşfet" else "81 ile geri dön",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (scope ==
                                SearchScope.TURKEY
                            ) {
                                "Londra, Tokyo, Paris ve daha fazlasını ara."
                            } else {
                                "Türkiye'nin tüm illerini resmi adlarıyla ara."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text("→", style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}

private val TURKEY_CITIES = listOf("İstanbul", "Ankara", "İzmir", "Kartal", "Beykoz", "Bursa")
private val WORLD_CITIES = listOf("Londra", "Berlin", "Tokyo", "New York", "Paris")

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun CityScreenPreview() {
    HavaTheme {
        CityScreen(
            query = "Ankara",
            scope = SearchScope.TURKEY,
            uiState = LoadState.Success(
                listOf(City(323786, "Ankara", 39.91987, 32.85427, "TR", "Türkiye", "Ankara", "PPLC"))
            ),
            onQueryChange = {},
            onScopeChange = {},
            onSearch = {},
            onCityClick = {},
            onStyleClick = {},
            onSettingsClick = {}
        )
    }
}
