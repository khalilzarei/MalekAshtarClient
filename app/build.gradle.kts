plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.khz.malekashtarclient"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.khz.malekashtarclient"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    // =========================
    // Compose
    // =========================

    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)

    // =========================
    // Android
    // =========================

    implementation(libs.androidx.core.ktx)

    // =========================
    // Lifecycle / ViewModel
    // =========================

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.compose)

    // =========================
    // Navigation
    // =========================

    implementation(libs.androidx.navigation.compose)


    implementation(libs.androidx.hilt.navigation.compose)

    // =========================
    // Retrofit
    // =========================

    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    // =========================
    // DataStore
    // =========================

    implementation(libs.androidx.datastore.preferences)

    // =========================
    // Coroutines
    // =========================

    implementation(libs.kotlinx.coroutines.android)

    // =========================
    // Tests
    // =========================

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.coil.compose)


    implementation(libs.androidx.compose.foundation)

    // Video player - Media3 ExoPlayer
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.common)

}