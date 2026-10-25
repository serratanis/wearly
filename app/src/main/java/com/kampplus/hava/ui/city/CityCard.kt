package com.kampplus.hava.ui.city

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kampplus.hava.model.City
import com.kampplus.hava.ui.theme.HavaTheme

/**
 * Listedeki tek bir şehir satırı.
 *
 * Yapısı: `Card` → `Row` → rozet + `Spacer` + `Column(weight(1f))` + sağ ok.
 *
 * `Modifier.weight(1f)`: ortadaki sütun kalan boşluğun tamamını kaplar, böylece
 * sağ oku sağ kenara iter. Uzun şehir adları (Afyonkarahisar) rozeti ezmez.
 *
 * @param onClick Karta dokunulduğunda çağrılır. Bileşen ne olacağını bilmez,
 *   yalnızca haber verir — olay yukarı, veri aşağı (state hoisting).
 */
@Composable
fun CityCard(
    city: City,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onFavoriteClick: (() -> Unit)? = null
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CountryBadge(code = city.countryCode)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = city.name.replaceFirstChar(Char::titlecase),
                    style = MaterialTheme.typography.titleMedium,
                    // Tek satıra sığmayan ad "..." ile kısaltılır, satır kaymaz.
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = city.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (city.countryCode == "TR" &&
                        !city.isDistrict
                    ) {
                        "İlçeleri veya il merkezini seç"
                    } else {
                        "Güncel tahmini görüntüle"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (onFavoriteClick != null) {
                val favoriteDescription = if (isFavorite) "Favorilerden çıkar" else "Favorilere ekle"
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.semantics { contentDescription = favoriteDescription }
                ) {
                    Text(
                        text = if (isFavorite) "★" else "☆",
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Ülke kodu rozeti ("TR").
 *
 * Veri API'den geldiği için elimizde plaka numarası yok; rozete ülke kodunu
 * koyuyoruz. Kod gelmezse "?" gösterilir.
 */
@Composable
private fun CountryBadge(code: String?, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.size(44.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = code?.countryFlag() ?: "⌖",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

private fun String.countryFlag(): String {
    if (length != 2) return uppercase()
    return uppercase().map { letter ->
        String(Character.toChars(0x1F1E6 + (letter.code - 'A'.code)))
    }.joinToString("")
}

@Preview(showBackground = true)
@Composable
private fun CityCardPreview() {
    HavaTheme {
        CityCard(
            city = City(
                id = 323786,
                name = "Ankara",
                latitude = 39.91987,
                longitude = 32.85427,
                countryCode = "TR",
                country = "Türkiye",
                admin1 = "Ankara"
            ),
            onClick = {}
        )
    }
}
