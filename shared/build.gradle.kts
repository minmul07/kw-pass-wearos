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
        minSdk = 26

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
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    api(libs.kotlinx.coroutines.core)

    api(libs.retrofit)
    api(libs.retrofit.converter)

    implementation(libs.core)
    api(libs.annotation)
    kapt(libs.processor)

    api(libs.kotlinx.serialization.json)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.zxing.core)

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
}