package com.kampplus.hava.ui.district

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kampplus.hava.model.City
import com.kampplus.hava.ui.LoadState
import com.kampplus.hava.ui.component.EmptyView
import com.kampplus.hava.ui.component.ErrorView

@Composable
fun DistrictRoute(
    onBack: () -> Unit,
    onLocationClick: (City) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DistrictViewModel = viewModel()
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DistrictScreen(
        provinceName = viewModel.provinceName,
        provinceLatitude = viewModel.provinceLatitude,
        provinceLongitude = viewModel.provinceLongitude,
        query = query,
        uiState = uiState,
        onQueryChange = viewModel::onQueryChange,
        onBack = onBack,
        onRetry = viewModel::load,
        onLocationClick = onLocationClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DistrictScreen(
    provinceName: String,
    provinceLatitude: Double,
    provinceLongitude: Double,
    query: String,
    uiState: LoadState<List<City>>,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onLocationClick: (City) -> Unit,
    modifier: Modifier = Modifier
) {
    val province = City(
        id = provinceName.hashCode().toLong(),
        name = provinceName,
        latitude = provinceLatitude,
        longitude = provinceLongitude,
        countryCode = "TR",
        country = "Türkiye",
        admin1 = provinceName
    )
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(provinceName) },
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
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("İlçe seç", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(
                    "İl merkezini veya sana en yakın ilçeyi seç; tahmin o noktanın koordinatından alınır.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ProvinceCard(province = province, onClick = { onLocationClick(province) })
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("$provinceName ilçelerinde ara") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Temizle")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp)
                )
            }
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                when (uiState) {
                    LoadState.Loading -> CircularProgressIndicator()
                    LoadState.Empty -> EmptyView(title = "İlçe bulunamadı", message = "Arama metnini kontrol et.")
                    is LoadState.Error -> ErrorView(message = uiState.message, onRetry = onRetry)
                    is LoadState.Success -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.data, key = City::id) { district ->
                            DistrictCard(district, onClick = { onLocationClick(district) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProvinceCard(province: City, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("🏙️", style = MaterialTheme.typography.headlineMedium)
            Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
                Text("${province.name} il merkezi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Genel şehir tahminini görüntüle", style = MaterialTheme.typography.bodySmall)
            }
            Text("→", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun DistrictCard(district: City, onClick: () -> Unit) {
    ElevatedCard(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⌖", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
                Text(district.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "${district.admin1} · İlçe",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text("→", style = MaterialTheme.typography.titleLarge)
        }
    }
}
