plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)

    id("com.google.android.gms.oss-licenses-plugin")

    kotlin("plugin.serialization") version "2.0.0"

    kotlin("kapt")
}

android {
    namespace = "minmul.kwpass"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "minmul.kwpass"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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
    kotlinOptions {
        jvmTarget = "17"
    }
    useLibrary("wear-sdk")
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.play.services.wearable)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.wear.tooling.preview)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.play.services.wearable)
    implementation(libs.androidx.compose.navigation)
    implementation(libs.compose.material3)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.compose.ui.tooling)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.androidx.material3)
    implementation(libs.zxing.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.tiles.tooling.preview)
    implementation(libs.watchface.complications.data.source.ktx)
    implementation(libs.watchface.complications.data)
    implementation(libs.play.services.oss.licenses)


    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(project(":shared"))
}