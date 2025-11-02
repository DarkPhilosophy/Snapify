plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") version "2.1.0-1.0.29"
    id("com.google.dagger.hilt.android") version "2.57.2"
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = Coordinates.APP_ID
    compileSdk = Coordinates.COMPILE_SDK

    defaultConfig {
        applicationId = Coordinates.APP_ID
        minSdk = Coordinates.MIN_SDK
        targetSdk = Coordinates.TARGET_SDK
        versionCode = Coordinates.APP_VERSION_CODE
        versionName = Coordinates.APP_VERSION_NAME

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug") // For testing; use proper signing for production
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

detekt {
    config.setFrom(files("../detekt.yml"))
}

dependencies {
    // Module dependencies
    implementation(project(":core"))

    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    // Material Design
    implementation(libs.material)

    // ConstraintLayout
    implementation(libs.androidx.constraint.layout)

    // Room Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Lifecycle & ViewModel
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.service)

    // WorkManager
    implementation(libs.work.runtime.ktx)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Gson for JSON persistence
    implementation(libs.gson)

    // DataStore
    implementation(libs.datastore.preferences)

    // Activity & Fragment KTX
    implementation(libs.activity.ktx)
    implementation(libs.fragment.ktx)

    // RecyclerView
    implementation(libs.recyclerview)

    // Glide for image loading
    implementation(libs.glide)
    ksp(libs.glide.compiler)

    // Hilt
    implementation("com.google.dagger:hilt-android:2.57.2")
    add("kapt", "com.google.dagger:hilt-compiler:2.57.2")
    // Hilt Work (for injecting into WorkManager workers)
    implementation("androidx.hilt:hilt-work:1.0.0")
    add("kapt", "androidx.hilt:hilt-compiler:1.0.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Static Analysis
    detektPlugins(libs.detekt.formatting)
}
