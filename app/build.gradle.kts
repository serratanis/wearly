import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.kampplus.hava"
    // compileSdk: derlemede erisilebilen Android API seviyesi.
    compileSdk = 36

    defaultConfig {
        applicationId = "com.kampplus.hava"
        // minSdk: uygulamanin calisacagi en dusuk Android surumu.
        minSdk = 26
        // targetSdk: davranislarin hangi surume gore ayarlanacagi.
        targetSdk = 36
        // versionCode sayisal surum kodu, versionName kullaniciya gosterilen surum.
        versionCode = 3
        versionName = "1.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Imza bilgileri repoya girmez: keystore.properties (bkz. keystore.properties.example).
    // Dosya yoksa release build debug anahtariyla imzalanir; kampta telefona kurmak icin
    // yeterlidir, magazaya yuklemek icin gercek keystore zorunludur.
    val keystoreFile = rootProject.file("keystore.properties")
    val releaseSigning = if (keystoreFile.exists()) {
        val keystore = Properties().apply { keystoreFile.inputStream().use(::load) }
        signingConfigs.create("release") {
            storeFile = rootProject.file(keystore.getProperty("storeFile"))
            storePassword = keystore.getProperty("storePassword")
            keyAlias = keystore.getProperty("keyAlias")
            keyPassword = keystore.getProperty("keyPassword")
        }
    } else {
        signingConfigs.getByName("debug")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = releaseSigning
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests {
            // JVM testlerinde Android framework sınıfları (ör. android.util.Log) gerçekte
            // yoktur. Varsayılan olarak her çağrı exception fırlatır; bu da ViewModel'in
            // catch bloğundaki Log.e satırının testi düşürmesine yol açar.
            // Bu ayar, framework çağrılarının hata yerine varsayılan değer dönmesini sağlar.
            isReturnDefaultValues = true
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)

    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver)

    // @Preview'lari Android Studio'da gosterebilmek icin yalnizca debug derlemesinde.
    debugImplementation(libs.androidx.compose.ui.tooling)
}
