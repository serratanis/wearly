<div align="center">

# wearly.

### Your weather. Your style. Your day.

Hava durumunu keşfet, tarzını oluştur ve gününü sana özel önerilerle planla.

**Kotlin · Jetpack Compose · Material 3 · MVVM · Open-Meteo**

</div>

## Nedir?

wearly., hava tahminini yalnızca derece gösteren bir ekrandan çıkarıp günlük kararlara dönüştüren kişisel bir yaşam asistanıdır. Gerçek hava verisini; kullanıcının planı, sıcaklık hassasiyeti, stil tercihi ve kendi gardırobuyla birleştirir.

Uygulama API anahtarı veya hesap istemez. Hava verileri Open-Meteo'dan alınır; planlar, tercihler, favoriler ve gardırop yalnızca cihazda saklanır.

## Ürün deneyimi

<p align="center">
  <img src="docs/screenshots/home.png" width="30%" alt="wearly ana ekran" />
  <img src="docs/screenshots/forecast.png" width="30%" alt="wearly gerçek hava tahmini ve Comfort Score" />
  <img src="docs/screenshots/style-assistant.png" width="30%" alt="Wearly Look kombin önerisi" />
</p>

<p align="center">
  <img src="docs/screenshots/my-day.png" width="30%" alt="My Day plan ekleme" />
  <img src="docs/screenshots/settings.png" width="30%" alt="wearly kişiselleştirme ayarları" />
</p>

## Öne çıkan özellikler

- **Gerçek ve küresel hava:** Dünya genelinde şehir arama, Türkiye'nin 81 ili ve il seçildikten sonra bağlı ilçeler.
- **Kişisel başlangıç:** Atlanabilir onboarding; stil, sıcaklık hassasiyeti ve ana şehir tercihlerini cihazda kalıcı saklar.
- **Şehir karşılaştırma:** İki şehrin gerçek Open-Meteo verisini, hissedilen sıcaklığını, yağışını, rüzgârını ve Comfort Score'unu karşılaştırır.
- **Ayrıntılı tahmin:** Anlık ve hissedilen sıcaklık, nem, rüzgâr, yağış olasılığı, UV, gün doğumu/batımı, 24 saat ve 7 gün.
- **Wearly Comfort Score:** Sıcaklık, yağış, rüzgâr, nem ve UV'yi açıklanabilir bir 0–100 puana dönüştürür.
- **Planın için en iyi zaman:** Yürüyüş, spor, kahve veya yolculuk için gerçek saatlik tahminden en uygun iki saatlik aralığı seçer.
- **Wearly Look:** Planı doğal dille alır; üst, alt, dış katman, ayakkabı, aksesuar, renk paleti ve hava korumasını ayrı ayrı açıklar.
- **Kişisel öneri motoru:** Günlük/zarif/sportif/minimal/ofis stili, üşüme-ısınma hassasiyeti ve konfor-şıklık önceliğini öneriye uygular.
- **My Wardrobe:** Kullanıcı kendi gerçek kıyafetlerini ekler, düzenler ve siler. “Dolabımdan” istendiğinde sistem yalnızca kayıtlı parçaları kullanır; olmayan ürünü uydurmaz.
- **My Day:** Tarih, saat, konum, etkinlik, giyim beklentisi ve not içeren planlar cihazda kalıcı olarak eklenebilir, düzenlenebilir ve silinebilir.
- **Favoriler ve ayarlar:** Şehir favorileri, °C/°F, sistem/açık/koyu tema ve kalıcı kişiselleştirme tercihleri.
- **Günlük koruma:** Şemsiye, katman, rüzgâr ve UV seviyesine göre SPF yenileme önerileri.

> Wearly Look bir bulut LLM'i gibi davranıyormuş izlenimi vermez. Mevcut sürüm, gerçek tahmin + kullanıcı tercihleri + gardırop verisiyle çalışan deterministik ve test edilebilir bir öneri motorudur. Böylece gizli API anahtarı, sahte sunucu yanıtı veya uydurulmuş gardırop verisi yoktur.

## Mimari

```text
Compose UI
   ↓ state / events
ViewModel + StateFlow
   ↓ suspend calls
Repository
   ↓
Retrofit + Gson → Open-Meteo / ilçe servisi

Yerel veriler → SharedPreferences + Gson
  • kullanıcı tercihleri
  • favori şehirler
  • My Day planları
  • My Wardrobe parçaları
```

Tek `:app` modülünde sade bir `model / data / ui` ayrımı kullanılır. UI ağ çağrısı yapmaz; ViewModel yükleniyor, başarılı, boş ve hata durumlarını yönetir. Ayrıntılar için [mimari belgesi](docs/ARCHITECTURE.md).

## Veri kaynakları ve doğruluk

- Şehir arama: [Open-Meteo Geocoding API](https://open-meteo.com/en/docs/geocoding-api)
- Hava tahmini: [Open-Meteo Forecast API](https://open-meteo.com/en/docs)
- Türkiye ilçe listeleri/koordinatları: projenin `DistrictApi` katmanındaki açık coğrafya servisi
- Hava kodları: WMO kodları `WeatherCondition.fromWeatherCode` ile tek noktada eşlenir.
- Tahmin isteğinde `timezone=auto` kullanıldığı için saatler seçilen konumun yerel saatidir.

## Çalıştırma

Gereksinimler: Android Studio, Android SDK 36 ve JDK 17 veya 21.

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew spotlessCheck
```

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`

`local.properties` yalnızca yerel Android SDK yolunu içerir ve sürüm kontrolüne alınmaz:

```properties
sdk.dir=/kullaniciya/ozel/Android/sdk/yolu
```

## Demo akışı

1. Ana ekranda **İstanbul** sonucunu favorilere ekle.
2. İstanbul'a girip **Kartal** veya il merkezini seç.
3. Gerçek tahminde **Wearly Comfort Score** ve **Planın için en iyi zaman** kartlarını göster.
4. **Stil Asistanına sor** → “Şık bir kombin” ile yapılandırılmış Wearly Look üret.
5. Ayarlardan “Çabuk üşürüm” ve “Şıklık” seçeneklerini değiştirip önerinin kişiselleştiğini göster.
6. **My Wardrobe**'a bir üst, alt ve ayakkabı ekleyip “Dolabımdan kombin” iste.
7. **My Day**'e yeni bir plan ekle; düzenle ve sil akışını göster.
8. **Şehirleri karşılaştır** ile İstanbul ve Ankara'nın canlı verilerini yan yana göster.

## Kalite

- 47 birim test: API/JSON eşleme, repository dönüşümleri, ViewModel durumları, hava önerileri, Comfort Score, etkinlik zamanı ve Wearly Look.
- Spotless/ktlint biçimlendirme.
- Android lint.
- Debug ve küçültülmüş release yapılandırması.
- TalkBack açıklamaları, dinamik tipografi, açık/koyu tema ve ayrı loading/empty/error durumları.

## Proje bağlamı

Bu ürün, Turkcell Bilişim Kampı Android başlangıç reposu üzerine geliştirilmiştir. Ders checkpoint'leri ve eğitim notları korunmuştur; Wearly özellikleri `feature/wearly` dalında ürünleştirilmiştir.

Geliştirme checkpoint'leri için [Git geçmişi](docs/GIT-HISTORY.md) ve [gün kontrol listesi](docs/DAY1-CHECKLIST.md) belgelerine bakılabilir.
