package com.kampplus.hava

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kampplus.hava.ui.theme.HavaTheme

/**
 * İlk gün Compose ve Activity yaşam döngüsü maddelerini ana uygulamadan bağımsız
 * denemek için yalnızca debug sürümüne eklenen ekran.
 */
class DayOneActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        enableEdgeToEdge()
        setContent {
            HavaTheme {
                DayOnePracticeScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding()
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }

    private companion object {
        const val TAG = "DayOneLifecycle"
    }
}

/**
 * Column elemanları dikey, Row yatay dizer; Box ise içeriği aynı alan içinde
 * hizalar. Sayaç state'i değiştiğinde Compose bu ekranı yeniden değerlendirir.
 */
@Composable
fun DayOnePracticeScreen(modifier: Modifier = Modifier) {
    var count by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Compose 1. Gün")

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(if (count == 0) "Butona bas ve değişimi gör" else "Ekran yeni değeri gösterdi")

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Sayaç: $count")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { count += 1 },
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Artır")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DayOnePracticeScreenPreview() {
    HavaTheme {
        DayOnePracticeScreen(modifier = Modifier.fillMaxSize())
    }
}
