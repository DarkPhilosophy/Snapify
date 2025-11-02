plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.ko.app.core"
    compileSdk = Coordinates.COMPILE_SDK

    defaultConfig {
        minSdk = Coordinates.MIN_SDK
        targetSdk = Coordinates.TARGET_SDK
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)

    // Room runtime annotations (no kapt/ksp in core) - provides @Entity etc.
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // DataStore
    implementation(libs.datastore.preferences)

    // Gson
    implementation(libs.gson)

    // WorkManager (used by util scheduler if needed)
    implementation(libs.work.runtime.ktx)
}
