# 1. Gün değerlendirme kontrolü

## Android temelleri

- **Proje ve emülatör:** `main` branch'i debug APK olarak derlenip emülatörde çalıştırılabilir.
- **Activity ve Fragment:** Activity uygulamanın pencere ve yaşam döngüsü sahibidir. Fragment, bir Activity içinde tekrar kullanılabilen klasik Android ekran parçasıdır. Bu proje yeni ekranlar için Fragment yerine Navigation Compose ve composable fonksiyonlar kullanır.
- **Yaşam döngüsü:** `MainActivity` ve debug `DayOneActivity`, `onCreate`, `onStart`, `onResume`, `onPause` ve `onStop` çağrılarını Logcat'e yazar.
- **Text değişikliği:** Alıştırma ekranında butona basınca sayaç metni değişir.
- **Araçlar:**
  - **Run:** Uygulamayı seçili cihazda derler, kurar ve açar.
  - **Build:** Kaynak kodu APK gibi çalıştırılabilir çıktıya dönüştürür; uygulamayı açmak zorunda değildir.
  - **Logcat:** Çalışan uygulamanın günlüklerini ve hatalarını gösterir.

## Compose ile UI

Debug sürümündeki `DayOnePracticeScreen` şu maddeleri birlikte gösterir:

- `Text` ve `Button` ile basit ekran.
- `Column` ile dikey, `Row` ile yatay dizilim, `Box` ile aynı alan içinde hizalama.
- `Modifier.padding`, `fillMaxWidth` ve `height` ile boşluk ve boyut ayarı.
- `@Preview` ile Android Studio önizlemesi.
- `remember { mutableIntStateOf(0) }` ile sayaç state'i.
- Buton state'i değiştirir; Compose yeni değerle yeniden değerlendirme (recomposition) yapar.

## Emülatörde açma

Önce normal debug uygulamasını kurun. Ardından Android Studio Terminal'de:

```bash
adb shell am start -n com.kampplus.hava/.DayOneActivity
```

Butona bastıkça `Sayaç: 0`, `Sayaç: 1`, `Sayaç: 2` olarak ilerler.

## Yaşam döngüsünü gözlemleme

Logcat aramasına `DayOneLifecycle` yazın:

1. Ekran açılırken `onCreate → onStart → onResume` görünür.
2. Ana ekrana dönünce `onPause → onStop` görünür.
3. Uygulamaya geri gelince `onStart → onResume` görünür.

## Kaynaklar

- Ana uygulama girişi: `app/src/main/java/com/kampplus/hava/MainActivity.kt`
- Compose alıştırması: `app/src/debug/java/com/kampplus/hava/DayOneActivity.kt`
- Debug manifesti: `app/src/debug/AndroidManifest.xml`
