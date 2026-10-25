# Wearly mimarisi

Wearly tek `:app` modülünde, projenin eğitim amacıyla okunabilir kalmasını sağlayan üç ana katman kullanır.

```text
UI (Jetpack Compose)
  ├─ Route: ViewModel/state bağlama
  └─ Screen: stateless görünüm
             ↓ events / ↑ state
ViewModel (StateFlow + viewModelScope)
             ↓ suspend
Repository
             ↓
Retrofit + Gson ──→ Open-Meteo / ilçe servisi

Yerel Store sınıfları ──→ SharedPreferences + Gson
```

## Katman sınırları

- `ui/` yalnızca state çizer ve kullanıcı olaylarını yukarı iletir; doğrudan ağ çağrısı yapmaz.
- ViewModel'ler ağ isteklerini yaşam döngüsüne bağlı coroutine'lerde çalıştırır ve `LoadState` üretir.
- Repository arayüzleri veri kaynağını UI'dan ayırır; testlerde sahte implementasyon verilebilir.
- `model/` API'den bağımsız iş kararlarını barındırır: Comfort Score, etkinlik zamanı, güneş koruması ve Wearly Look burada hesaplanır.
- `data/` ağ istemcileri ile cihaz içi kalıcı store sınıflarını içerir.

## Paketler

```text
com.kampplus.hava
├── MainActivity.kt
├── model/
│   ├── City.kt, District.kt, Forecast.kt
│   ├── WeatherCondition.kt, WeatherAdvice.kt
│   ├── StyleAssistant.kt
│   ├── DayPlan.kt, WardrobeItem.kt
│   └── UserSettings.kt
├── data/
│   ├── ApiClient.kt, *Api.kt, *Repository.kt
│   ├── UserPreferences.kt
│   ├── FavoriteCityStore.kt
│   ├── DayPlanStore.kt
│   └── WardrobeStore.kt
└── ui/
    ├── HavaNavHost.kt, LoadState.kt
    ├── city/, district/, forecast/
    ├── style/, day/, wardrobe/, settings/
    ├── component/
    └── theme/
```

## Ağ veri akışı

1. Kullanıcı bir şehir arar veya kayıtlı konumu açar.
2. Route ekranı olayı ViewModel'e iletir.
3. ViewModel `viewModelScope` içinde repository çağrısını başlatır.
4. Repository Retrofit ile isteği gerçekleştirir ve DTO'yu uygulama modeline dönüştürür.
5. ViewModel `Loading`, `Success`, `Empty` veya `Error` state'i yayınlar.
6. Compose yalnızca yeni state'i çizer.

Hatalar kullanıcıya sade mesajlarla gösterilir; teknik ayrıntılar Logcat'e yazılır. Ağ katmanı `IOException`, HTTP, bozuk JSON ve eksik veri durumlarını ayrı ayrı ele alır.

## Yerel veri ve gizlilik

Hesap veya sunucu tabanlı profil yoktur. Aşağıdaki bilgiler cihazdaki ayrı SharedPreferences dosyalarında Gson JSON olarak tutulur:

- sıcaklık, tema ve stil kişiselleştirme ayarları;
- favori şehirler;
- My Day planları;
- My Wardrobe parçaları.

Store sınıfları veriyi `StateFlow` olarak yayınlar ve ekleme/düzenleme/silme işlemlerinden sonra tek noktadan kalıcılaştırır. Gardırop öneri motoruna yalnızca kullanıcının açıkça kaydettiği parçalar verilir.

## Wearly öneri motoru

Öneri motoru deterministiktir ve ağdaki bir LLM'e bağlı değildir:

```text
gerçek tahmin
+ plan cümlesindeki amaç
+ stil tercihi
+ sıcaklık hassasiyeti
+ konfor/şıklık önceliği
+ isteğe bağlı gerçek gardırop
→ yapılandırılmış Wearly Look
```

Bu karar fonksiyonlarının Android framework bağımlılığı yoktur; doğrudan JVM birim testleriyle doğrulanır. Kullanıcı “dolabımdan” dediğinde gardırop boşsa açıkça bilgi verilir, parça uydurulmaz.

## Navigasyon

Navigation Compose rotaları:

- `city`: ana arama/favori ekranı
- `district`: Türkiye ilinden il merkezi veya ilçe seçimi
- `forecast`: gerçek tahmin ve günlük karar kartları
- `style`: Wearly Look asistanı
- `my-day`: kalıcı plan CRUD
- `wardrobe`: kalıcı gardırop CRUD
- `settings`: birim, tema ve kişiselleştirme

Şehir, koordinat ve alt başlık rota parametreleri `Uri.encode` ile taşınır. ViewModel'ler parametreleri `SavedStateHandle` üzerinden okur.

## Kalite yaklaşımı

- İş mantığı için birim testleri
- Repository ve JSON eşlemeleri için MockWebServer testleri
- ViewModel state testleri
- Spotless/ktlint
- Android lint
- Manuel emülatör akışı ve Logcat çökme kontrolü

Yeni bir özellik eklenirken önce model/karar fonksiyonu test edilebilir tutulur; UI yalnızca sonucu sunar.
