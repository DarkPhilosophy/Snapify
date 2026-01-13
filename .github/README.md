# Screenshot Manager

<!-- LATEST-BUILD-STATUS-START -->
[![PreBuild](https://img.shields.io/badge/PreBuild-Passing-brightgreen)](https://github.com/DarkPhilosophy/Snapify/actions)
[![Build Status](https://github.com/DarkPhilosophy/Snapify/actions/workflows/ci.yaml/badge.svg)](https://github.com/DarkPhilosophy/Snapify/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Version 1.0.0](https://img.shields.io/badge/Version-1.0.0-blue.svg)](https://github.com/DarkPhilosophy/Snapify)
<!-- LATEST-BUILD-STATUS-END -->

A modern, intelligent Android application to automatically manage and organize your screenshots. Keep what matters, delete the rest automatically.

<!-- LATEST-VERSION-START -->
<!-- LATEST-VERSION-END -->

## Validation Status
<!-- LINT-RESULT-START -->
### Linting Status
> **Status**: ✅ **Passing**  
> **Last Updated**: 2026-01-13 21:12:09 UTC  
> **Summary**: 0 errors, 0 warnings

<details>
<summary>Click to view full lint output</summary>

```Starting a Gradle Daemon (subsequent builds will be faster)
Calculating task graph as no cached configuration is available for tasks: spotlessCheck detekt test
> Task :buildSrc:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :buildSrc:pluginDescriptors
> Task :buildSrc:processResources NO-SOURCE
> Task :buildSrc:compileKotlin
> Task :buildSrc:compileJava NO-SOURCE
> Task :buildSrc:compileGroovy NO-SOURCE
> Task :buildSrc:classes UP-TO-DATE

> Task :buildSrc:jar
:jar: No valid plugin descriptors were found in META-INF/gradle-plugins

> Task :detekt NO-SOURCE
> Task :app:preBuild UP-TO-DATE
> Task :app:preDebugBuild UP-TO-DATE
> Task :app:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :spotlessInternalRegisterDependencies
> Task :app:spotlessKotlin
> Task :app:spotlessKotlinCheck
> Task :app:dataBindingMergeDependencyArtifactsDebug
> Task :app:generateDebugResValues
> Task :app:generateDebugResources
> Task :app:injectCrashlyticsMappingFileIdDebug
> Task :app:injectCrashlyticsVersionControlInfoDebug
> Task :app:processDebugGoogleServices
> Task :core:preBuild UP-TO-DATE
> Task :core:preDebugBuild UP-TO-DATE
> Task :core:generateDebugResValues
> Task :core:generateDebugResources
> Task :core:packageDebugResources

> Task :app:detekt
Property 'style>ForbiddenComment>values' is deprecated. Use \`comments\` instead, make sure you escape your text for Regular Expressions..
Property 'naming>FunctionNaming>ignoreOverridden' is deprecated. This configuration is ignored and will be removed in the future.

> Task :app:spotlessKotlinGradle
> Task :app:spotlessKotlinGradleCheck
> Task :app:spotlessCheck
> Task :app:packageDebugResources
> Task :core:processDebugNavigationResources
> Task :app:processDebugNavigationResources
> Task :core:detekt
> Task :core:parseDebugLocalResources
> Task :app:parseDebugLocalResources
> Task :app:generateDebugBuildConfig
> Task :core:generateDebugRFile
> Task :app:compileDebugNavigationResources
> Task :app:mapDebugSourceSetPaths
> Task :app:createDebugCompatibleScreenManifests
> Task :app:extractDeepLinksDebug
> Task :core:extractDeepLinksDebug
> Task :core:writeDebugAarMetadata
> Task :core:compileDebugLibraryResources
> Task :core:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :core:processDebugManifest
> Task :core:generateDebugBuildConfig
> Task :app:checkDebugAarMetadata
> Task :core:javaPreCompileDebug
> Task :app:javaPreCompileDebug
> Task :app:preDebugUnitTestBuild UP-TO-DATE
> Task :app:javaPreCompileDebugUnitTest
> Task :app:preReleaseBuild UP-TO-DATE
> Task :app:buildKotlinToolingMetadata
> Task :app:dataBindingMergeDependencyArtifactsRelease
> Task :app:generateReleaseResValues
> Task :app:generateReleaseResources
> Task :app:injectCrashlyticsMappingFileIdRelease
> Task :app:processDebugMainManifest
> Task :app:extractReleaseVersionControlInfo
> Task :app:injectCrashlyticsVersionControlInfoRelease
> Task :app:processReleaseGoogleServices
> Task :core:preReleaseBuild UP-TO-DATE
> Task :core:generateReleaseResValues
> Task :app:mergeDebugResources
> Task :core:generateReleaseResources
> Task :core:packageReleaseResources
> Task :app:processDebugManifest
> Task :app:processDebugManifestForPackage
> Task :app:dataBindingGenBaseClassesDebug
> Task :app:packageReleaseResources
> Task :core:processReleaseNavigationResources
> Task :app:processReleaseNavigationResources
> Task :app:parseReleaseLocalResources
> Task :core:parseReleaseLocalResources
> Task :core:generateReleaseRFile
> Task :app:generateReleaseBuildConfig
> Task :core:writeReleaseAarMetadata
> Task :app:checkReleaseAarMetadata
> Task :app:compileReleaseNavigationResources
> Task :app:mapReleaseSourceSetPaths
> Task :app:createReleaseCompatibleScreenManifests
> Task :app:extractDeepLinksRelease
> Task :core:extractDeepLinksRelease
> Task :core:processReleaseManifest
> Task :app:processReleaseMainManifest
> Task :app:processReleaseManifest
> Task :app:processReleaseManifestForPackage
> Task :core:compileReleaseLibraryResources
> Task :core:generateReleaseBuildConfig
> Task :app:processDebugResources
> Task :core:javaPreCompileRelease
> Task :app:javaPreCompileRelease
> Task :app:preReleaseUnitTestBuild UP-TO-DATE
> Task :app:javaPreCompileReleaseUnitTest
> Task :core:preDebugUnitTestBuild UP-TO-DATE
> Task :core:generateDebugUnitTestStubRFile
> Task :core:javaPreCompileDebugUnitTest
> Task :core:preReleaseUnitTestBuild UP-TO-DATE
> Task :core:generateReleaseUnitTestStubRFile
> Task :core:javaPreCompileReleaseUnitTest
> Task :app:mergeReleaseResources
> Task :app:dataBindingGenBaseClassesRelease
> Task :app:processReleaseResources
> Task :core:compileReleaseKotlin
> Task :core:compileDebugKotlin
> Task :core:processDebugJavaRes
> Task :core:processReleaseJavaRes
> Task :core:compileDebugJavaWithJavac
> Task :core:compileReleaseJavaWithJavac
> Task :core:bundleLibRuntimeToJarDebug
> Task :core:bundleLibRuntimeToJarRelease
> Task :core:bundleLibCompileToJarRelease
> Task :core:createFullJarDebug
> Task :core:bundleLibCompileToJarDebug
> Task :core:createFullJarRelease
> Task :core:compileDebugUnitTestKotlin NO-SOURCE
> Task :core:compileReleaseUnitTestKotlin NO-SOURCE
> Task :core:processDebugUnitTestJavaRes NO-SOURCE
> Task :core:compileDebugUnitTestJavaWithJavac NO-SOURCE
> Task :core:compileReleaseUnitTestJavaWithJavac NO-SOURCE
> Task :core:processReleaseUnitTestJavaRes NO-SOURCE
> Task :core:testDebugUnitTest NO-SOURCE
> Task :core:testReleaseUnitTest NO-SOURCE
> Task :core:test UP-TO-DATE
> Task :app:kspReleaseKotlin
> Task :app:kspDebugKotlin
> Task :app:compileReleaseKotlin
> Task :app:compileDebugKotlin
w: file:///home/runner/work/Snapify/Snapify/app/src/main/kotlin/ro/snapify/service/OverlayService.kt:394:36 This is a delicate API and its use requires care. Make sure you fully read and understand documentation of the declaration that is marked as a delicate API.
w: file:///home/runner/work/Snapify/Snapify/app/src/main/kotlin/ro/snapify/service/ScreenshotMonitorService.kt:112:29 'field FOREGROUND_SERVICE_TYPE_NONE: Int' is deprecated. Deprecated in Java.
w: file:///home/runner/work/Snapify/Snapify/app/src/main/kotlin/ro/snapify/ui/MainActivity.kt:1:13 Annotation 'androidx.media3.common.util.UnstableApi' is not annotated with '@RequiresOptIn'. '@OptIn' has no effect.
w: file:///home/runner/work/Snapify/Snapify/app/src/main/kotlin/ro/snapify/ui/MainScreen.kt:1:13 Annotation 'androidx.media3.common.util.UnstableApi' is not annotated with '@RequiresOptIn'. '@OptIn' has no effect.
w: file:///home/runner/work/Snapify/Snapify/app/src/main/kotlin/ro/snapify/ui/components/ScreenshotComponents.kt:267:36 This is a delicate API and its use requires care. Make sure you fully read and understand documentation of the declaration that is marked as a delicate API.
w: file:///home/runner/work/Snapify/Snapify/app/src/main/kotlin/ro/snapify/ui/components/ScreenshotComponents.kt:303:24 This is a delicate API and its use requires care. Make sure you fully read and understand documentation of the declaration that is marked as a delicate API.
w: file:///home/runner/work/Snapify/Snapify/app/src/main/kotlin/ro/snapify/ui/components/ScreenshotComponents.kt:317:32 This is a delicate API and its use requires care. Make sure you fully read and understand documentation of the declaration that is marked as a delicate API.
w: file:///home/runner/work/Snapify/Snapify/app/src/main/kotlin/ro/snapify/ui/components/VideoPlayer.kt:40:26 'val LocalLifecycleOwner: ProvidableCompositionLocal<LifecycleOwner>' is deprecated. Moved to lifecycle-runtime-compose library in androidx.lifecycle.compose package.
w: file:///home/runner/work/Snapify/Snapify/app/src/main/kotlin/ro/snapify/ui/components/VideoPreviewDialog.kt:121:26 'val LocalLifecycleOwner: ProvidableCompositionLocal<LifecycleOwner>' is deprecated. Moved to lifecycle-runtime-compose library in androidx.lifecycle.compose package.

> Task :app:compileReleaseKotlin
w: file:///home/runner/work/Snapify/Snapify/app/src/main/kotlin/ro/snapify/service/OverlayService.kt:394:36 This is a delicate API and its use requires care. Make sure you fully read and understand documentation of the declaration that is marked as a delicate API.
w: file:///home/runner/work/Snapify/Snapify/app/src/main/kotlin/ro/snapify/service/ScreenshotMonitorService.kt:112:29 'static static field FOREGROUND_SERVICE_TYPE_NONE: Int' is deprecated. Deprecated in Java.
w: file:///home/runner/work/Snapify/Snapify/app/src/main/kotlin/ro/snapify/ui/MainActivity.kt:1:13 Annotation 'androidx.media3.common.util.UnstableApi' is not annotated with '@RequiresOptIn'. '@OptIn' has no effect.
w: file:///home/runner/work/Snapify/Snapify/app/src/main/kotlin/ro/snapify/ui/MainScreen.kt:1:13 Annotation 'androidx.media3.common.util.UnstableApi' is not annotated with '@RequiresOptIn'. '@OptIn' has no effect.
w: file:///home/runner/work/Snapify/Snapify/app/src/main/kotlin/ro/snapify/ui/components/ScreenshotComponents.kt:267:36 This is a delicate API and its use requires care. Make sure you fully read and understand documentation of the declaration that is marked as a delicate API.
w: file:///home/runner/work/Snapify/Snapify/app/src/main/kotlin/ro/snapify/ui/components/ScreenshotComponents.kt:303:24 This is a delicate API and its use requires care. Make sure you fully read and understand documentation of the declaration that is marked as a delicate API.
w: file:///home/runner/work/Snapify/Snapify/app/src/main/kotlin/ro/snapify/ui/components/ScreenshotComponents.kt:317:32 This is a delicate API and its use requires care. Make sure you fully read and understand documentation of the declaration that is marked as a delicate API.
w: file:///home/runner/work/Snapify/Snapify/app/src/main/kotlin/ro/snapify/ui/components/VideoPlayer.kt:40:26 'val LocalLifecycleOwner: ProvidableCompositionLocal<LifecycleOwner>' is deprecated. Moved to lifecycle-runtime-compose library in androidx.lifecycle.compose package.
w: file:///home/runner/work/Snapify/Snapify/app/src/main/kotlin/ro/snapify/ui/components/VideoPreviewDialog.kt:121:26 'val LocalLifecycleOwner: ProvidableCompositionLocal<LifecycleOwner>' is deprecated. Moved to lifecycle-runtime-compose library in androidx.lifecycle.compose package.

> Task :app:compileReleaseJavaWithJavac
> Task :app:compileDebugJavaWithJavac
> Task :app:hiltAggregateDepsRelease
> Task :app:hiltAggregateDepsDebug
> Task :app:hiltJavaCompileRelease
> Task :app:hiltJavaCompileDebug
> Task :app:processDebugJavaRes
> Task :app:processReleaseJavaRes
> Task :app:bundleDebugClassesToCompileJar
> Task :app:bundleReleaseClassesToCompileJar
> Task :app:kspDebugUnitTestKotlin NO-SOURCE
> Task :app:kspReleaseUnitTestKotlin NO-SOURCE
> Task :app:compileDebugUnitTestKotlin NO-SOURCE
> Task :app:compileDebugUnitTestJavaWithJavac NO-SOURCE
> Task :app:compileReleaseUnitTestKotlin NO-SOURCE
> Task :app:compileReleaseUnitTestJavaWithJavac NO-SOURCE
> Task :app:hiltAggregateDepsDebugUnitTest
> Task :app:hiltAggregateDepsReleaseUnitTest
> Task :app:hiltJavaCompileDebugUnitTest NO-SOURCE
> Task :app:hiltJavaCompileReleaseUnitTest NO-SOURCE
> Task :app:processDebugUnitTestJavaRes NO-SOURCE
> Task :app:processReleaseUnitTestJavaRes NO-SOURCE
> Task :app:transformDebugClassesWithAsm
> Task :app:transformReleaseClassesWithAsm
> Task :app:bundleDebugClassesToRuntimeJar
> Task :app:bundleReleaseClassesToRuntimeJar
> Task :app:transformReleaseUnitTestClassesWithAsm
> Task :app:transformDebugUnitTestClassesWithAsm
> Task :app:testReleaseUnitTest NO-SOURCE
> Task :app:testDebugUnitTest NO-SOURCE
> Task :app:test UP-TO-DATE
gradle/actions: Writing build results to /home/runner/work/_temp/.gradle-actions/build-results/lint_step-1768338630448.json

[Incubating] Problems report is available at: file:///home/runner/work/Snapify/Snapify/build/reports/problems/problems-report.html

Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

For more on this, please refer to https://docs.gradle.org/9.2.0/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.

BUILD SUCCESSFUL in 1m 45s
120 actionable tasks: 120 executed
Configuration cache entry stored.
```
</details>
<!-- LINT-RESULT-END -->

<!-- PERSONAL-README-START -->
### Latest Update (v1.0.0)
- **Edge-to-Edge UI**: Migrated legacy UI flags to `enableEdgeToEdge()` for a modern, immersive experience.
- **Project Structure**: Added `version.properties` for automated version management.

### Fixed
- **Share and Delete**: Removed arbitrary timers and implemented aggressive cache cleanup in `OverlayService` (on create & pre-share) to fix sharing failures and storage leaks.

## ✨ Features

### 📸 Smart Screenshot Detection

- **Automatic Monitoring**: Continuously monitors your device for new screenshots
- **Multi-Folder Support**: Detects screenshots in default and custom folders
- **Real-time Processing**: Instant detection and processing of captured screenshots

### 🎯 Intelligent Management

- **Two Modes**: Choose between Manual (decide for each screenshot) or Automatic (set deletion timers)
- **Organized Views**: Filter screenshots by status - Marked, Kept, or All
- **Manual Mode Overlay**: Quick-action overlay appears immediately after screenshot for instant decisions

### 🔔 Smart Notifications

- **Countdown Timers**: Live notifications showing time remaining until deletion
- **Keep Actions**: One-tap keep from notification without opening app
- **Customizable Alerts**: Configure notification preferences

### ⚙️ Advanced Settings

- **Flexible Timers**: Set custom deletion times from 5 minutes to 1 week
- **Custom Folders**: Specify custom screenshot directories
- **Manual/Automatic Mode**: Switch between decision modes anytime
- **Debug Console**: Built-in logging and troubleshooting tools

### 🔧 Technical Excellence

- **Background Service**: Efficient foreground service for 24/7 monitoring
- **WorkManager Integration**: Reliable scheduled deletion tasks
- **Room Database**: Local storage for screenshot metadata
- **Modern Android**: Built with Kotlin, Material Design 3, and latest APIs

## 📱 Screenshots

*Coming soon - add screenshots of the app in action*

## 🚀 Installation

### From GitHub Releases

1. Go to [Releases](https://github.com/DarkPhilosophy/Ko/releases)
2. Download the latest `screenshot-manager-debug.apk` or `screenshot-manager-release.apk`
3. Install on your Android device (enable "Install unknown apps" if needed)

### Build from Source

See [QUICK_START.md](QUICK_START.md) for detailed build instructions.

## 📋 Requirements

- **Android**: API 24+ (Android 7.0 or higher)
- **Permissions**:
  - Storage access (for reading screenshots)
  - Notifications (for deletion timers)
  - Display over other apps (for manual mode overlay)

## 🛠️ Usage

1. **Grant Permissions**: Allow all required permissions when prompted
2. **Choose Mode**:
   - **Manual**: Decide for each screenshot via overlay
   - **Automatic**: Set timer for all screenshots
3. **Monitor**: App runs in background, detecting new screenshots
4. **Manage**: View and organize screenshots in the main app

### Manual Mode

- Take a screenshot
- Overlay appears with options: Keep, or set deletion timer
- Choose your preference

### Automatic Mode

- Set deletion time in settings
- All screenshots automatically marked for deletion
- Receive notifications with countdown
- Tap "Keep" in notification to save important ones

## 🔧 Configuration

Access settings via the floating action button:

- **Deletion Time**: Set automatic deletion timer
- **Operation Mode**: Toggle between Manual/Automatic
- **Custom Folder**: Specify alternative screenshot directory
- **Notifications**: Enable/disable alerts
- **Debug Console**: View logs and troubleshoot

## 🤝 Contributing

We welcome contributions! See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Built with ❤️ using Kotlin and Android Jetpack
- Material Design 3 for beautiful UI
- WorkManager for reliable background tasks

---

**Made with ❤️ by [Adalbert Alexandru Ungureanu](https://github.com/DarkPhilosophy)**
<!-- PERSONAL-README-END -->