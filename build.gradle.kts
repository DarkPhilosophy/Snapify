// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.detekt) apply false
}

buildscript {
    dependencies {
        // Force a compatible JavaPoet on the buildscript classpath to avoid Hilt plugin runtime errors
        classpath("com.squareup:javapoet:1.13.0")
    }
}

allprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    configurations.all {
        resolutionStrategy.force("com.squareup:javapoet:1.13.0")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
