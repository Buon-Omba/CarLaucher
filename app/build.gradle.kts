plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    // Screenshot delle schermate Compose sulla JVM (verifica visiva senza emulatore).
    id("app.cash.paparazzi")
}

android {
    namespace = "com.carlauncherthatsucks"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.carlauncherthatsucks"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            // Nessuna firma qui: su F-Droid l'APK di release lo firma F-Droid.
            // Niente metadati di controllo versione nell'APK (build riproducibili).
            vcsInfo.include = false
        }
    }

    // Richiesto da F-Droid: niente blocco "dependency info" cifrato nell'APK/AAB.
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    testImplementation("junit:junit:4.13.2")
    // Implementazione reale di org.json per i test JVM (quella di android.jar è solo uno stub).
    testImplementation("org.json:json:20240303")
}
