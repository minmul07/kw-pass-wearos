plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.google.devtools.ksp)

    kotlin("plugin.serialization") version "2.0.0"
}

android {
    namespace = "minmul.kwpass.shared"
    compileSdk = 36
    defaultConfig {

        minSdk = 29
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        buildConfig = true
        compose = false
    }
}

dependencies {
    api(libs.kotlinx.coroutines.core)

    api(libs.retrofit)

    implementation(libs.play.services.measurement.api)

    api(libs.kotlinx.serialization.json)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.zxing.core)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.timber)

    implementation(platform(libs.firebase.bom))
    api(libs.firebase.analytics)
}
