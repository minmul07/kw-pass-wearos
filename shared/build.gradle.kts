plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)

    kotlin("plugin.serialization") version "2.0.0"

    kotlin("kapt")
}

android {
    namespace = "minmul.kwpass.shared"
    compileSdk {
        version = release(36)
    }
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
                debugSymbolLevel = "SYMBOL_TABLE" // "FULL"?
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        buildConfig = true
        compose = false
    }
}

dependencies {
    api(libs.kotlinx.coroutines.core)

    api(libs.retrofit)
    api(libs.retrofit.converter)

    implementation(libs.core)
    api(libs.annotation)
    implementation(libs.play.services.measurement.api)
    kapt(libs.processor)

    api(libs.kotlinx.serialization.json)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.zxing.core)

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    implementation(libs.timber)

    implementation(platform(libs.firebase.bom))
    api(libs.firebase.analytics)
}