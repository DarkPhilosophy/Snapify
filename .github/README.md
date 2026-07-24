# Screenshot Manager

<!-- LATEST-BUILD-STATUS-START -->
[![PreBuild](https://img.shields.io/badge/PreBuild-Failing-red)](https://github.com/DarkPhilosophy/Snapify/actions)
[![Build Status](https://github.com/DarkPhilosophy/Snapify/actions/workflows/ci.yaml/badge.svg)](https://github.com/DarkPhilosophy/Snapify/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Version 1.0.0](https://img.shields.io/badge/Version-1.0.0-blue.svg)](https://github.com/DarkPhilosophy/Snapify)
<!-- LATEST-BUILD-STATUS-END -->

A modern, intelligent Android application to automatically manage and organize your screenshots. Keep what matters, delete the rest automatically.

<!-- LATEST-VERSION-START -->
### Latest Update (v1.0.0)
- **Edge-to-Edge UI**: Migrated legacy UI flags to `enableEdgeToEdge()` for a modern, immersive experience.
- **Project Structure**: Added `version.properties` for automated version management.
- **CI/CD**: Added robust GitHub Actions workflow with asset conflict resolution.

### Fixed
- **Share and Delete**: Removed arbitrary timers and implemented aggressive cache cleanup in `OverlayService` (on create & pre-share) to fix sharing failures and storage leaks.
<!-- LATEST-VERSION-END -->

## Validation Status
<!-- LINT-RESULT-START -->
### Linting Status
> **Status**: ❌ **Failing**  
> **Last Updated**: 2026-07-24 00:01:09 UTC  
> **Summary**: Check output for details

<details>
<summary>Click to view full lint output</summary>

```Downloading https://services.gradle.org/distributions/gradle-9.2.0-bin.zip
............10%.............20%.............30%.............40%.............50%.............60%.............70%.............80%.............90%.............100%

Welcome to Gradle 9.2.0!

Here are the highlights of this release:
 - Windows ARM support
 - Improved publishing APIs
 - Better guidance for dependency verification failures

For more details see https://docs.gradle.org/9.2.0/release-notes.html

Starting a Gradle Daemon (subsequent builds will be faster)
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
> Task :app:spotlessKotlinGradle
> Task :app:spotlessKotlinGradleCheck
> Task :app:spotlessCheck
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

> Task :core:detekt
> Task :app:packageDebugResources
> Task :core:processDebugNavigationResources
> Task :app:processDebugNavigationResources
> Task :app:dataBindingMergeDependencyArtifactsDebug
> Task :app:generateDebugBuildConfig
> Task :core:writeDebugAarMetadata
> Task :app:checkDebugAarMetadata
> Task :core:parseDebugLocalResources
> Task :app:parseDebugLocalResources
> Task :app:compileDebugNavigationResources
> Task :app:mapDebugSourceSetPaths
> Task :app:createDebugCompatibleScreenManifests
> Task :core:generateDebugRFile
> Task :app:extractDeepLinksDebug
> Task :core:extractDeepLinksDebug
> Task :core:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :core:compileDebugLibraryResources
> Task :core:generateDebugBuildConfig
> Task :core:javaPreCompileDebug
> Task :core:processDebugManifest
> Task :app:javaPreCompileDebug
> Task :app:preDebugUnitTestBuild UP-TO-DATE
> Task :app:javaPreCompileDebugUnitTest
> Task :app:preReleaseBuild UP-TO-DATE
> Task :app:buildKotlinToolingMetadata
> Task :app:dataBindingMergeDependencyArtifactsRelease
> Task :app:generateReleaseResValues
> Task :app:generateReleaseResources
> Task :app:injectCrashlyticsMappingFileIdRelease
> Task :app:extractReleaseVersionControlInfo
> Task :app:injectCrashlyticsVersionControlInfoRelease
> Task :app:processReleaseGoogleServices
> Task :core:preReleaseBuild UP-TO-DATE
> Task :core:generateReleaseResValues
> Task :core:generateReleaseResources
> Task :core:packageReleaseResources
> Task :app:processDebugMainManifest
> Task :app:processDebugManifest
> Task :app:processDebugManifestForPackage
> Task :app:mergeDebugResources
> Task :app:packageReleaseResources
> Task :app:dataBindingGenBaseClassesDebug
> Task :core:processReleaseNavigationResources
> Task :app:processReleaseNavigationResources
> Task :app:mergeReleaseResources
> Task :app:parseReleaseLocalResources
> Task :app:generateReleaseBuildConfig
> Task :core:writeReleaseAarMetadata
> Task :app:checkReleaseAarMetadata
> Task :app:compileReleaseNavigationResources
> Task :core:parseReleaseLocalResources
> Task :app:mapReleaseSourceSetPaths
> Task :app:createReleaseCompatibleScreenManifests
> Task :core:generateReleaseRFile
> Task :app:extractDeepLinksRelease
> Task :core:extractDeepLinksRelease
> Task :core:processReleaseManifest
> Task :app:dataBindingGenBaseClassesRelease
> Task :core:compileReleaseLibraryResources
> Task :core:generateReleaseBuildConfig
> Task :app:processReleaseMainManifest
> Task :app:processReleaseManifest
> Task :app:processReleaseManifestForPackage
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
> Task :app:processReleaseResources
> Task :core:compileReleaseKotlin
> Task :core:compileDebugKotlin
> Task :core:processDebugJavaRes
> Task :core:processReleaseJavaRes
> Task :core:compileDebugJavaWithJavac
> Task :core:compileReleaseJavaWithJavac
> Task :core:bundleLibCompileToJarDebug
> Task :core:bundleLibRuntimeToJarDebug
> Task :core:bundleLibRuntimeToJarRelease
> Task :core:bundleLibCompileToJarRelease
> Task :core:createFullJarDebug
> Task :core:createFullJarRelease
> Task :core:compileDebugUnitTestKotlin NO-SOURCE
> Task :core:compileDebugUnitTestJavaWithJavac NO-SOURCE
> Task :core:processDebugUnitTestJavaRes NO-SOURCE
> Task :core:testDebugUnitTest NO-SOURCE
> Task :core:compileReleaseUnitTestKotlin NO-SOURCE
> Task :core:compileReleaseUnitTestJavaWithJavac NO-SOURCE
> Task :core:processReleaseUnitTestJavaRes NO-SOURCE
> Task :core:testReleaseUnitTest NO-SOURCE
> Task :core:test UP-TO-DATE
> Task :app:kspReleaseKotlin
> Task :app:kspDebugKotlin
> Task :app:compileReleaseKotlin
> Task :app:compileDebugKotlin
w: file:///home/runner/work/Snapify/Snapify/app/src/main/kotlin/ro/snapify/service/ScreenshotMonitorService.kt:112:29 'field FOREGROUND_SERVICE_TYPE_NONE: Int' is deprecated. Deprecated in Java.
w: file:///home/runner/work/Snapify/Snapify/app/src/main/kotlin/ro/snapify/ui/MainActivity.kt:1:13 Annotation 'androidx.media3.common.util.UnstableApi' is not annotated with '@RequiresOptIn'. '@OptIn' has no effect.
w: file:///home/runner/work/Snapify/Snapify/app/src/main/kotlin/ro/snapify/ui/MainScreen.kt:1:13 Annotation 'androidx.media3.common.util.UnstableApi' is not annotated with '@RequiresOptIn'. '@OptIn' has no effect.
w: file:///home/runner/work/Snapify/Snapify/app/src/main/kotlin/ro/snapify/ui/components/ScreenshotComponents.kt:264:36 This is a delicate API and its use requires care. Make sure you fully read and understand documentation of the declaration that is marked as a delicate API.
w: file:///home/runner/work/Snapify/Snapify/app/src/main/kotlin/ro/snapify/ui/components/ScreenshotComponents.kt:300:24 This is a delicate API and its use requires care. Make sure you fully read and understand documentation of the declaration that is marked as a delicate API.
w: file:///home/runner/work/Snapify/Snapify/app/src/main/kotlin/ro/snapify/ui/components/ScreenshotComponents.kt:314:32 This is a delicate API and its use requires care. Make sure you fully read and understand documentation of the declaration that is marked as a delicate API.
w: file:///home/runner/work/Snapify/Snapify/app/src/main/kotlin/ro/snapify/ui/components/VideoPlayer.kt:40:26 'val LocalLifecycleOwner: ProvidableCompositionLocal<LifecycleOwner>' is deprecated. Moved to lifecycle-runtime-compose library in androidx.lifecycle.compose package.
w: file:///home/runner/work/Snapify/Snapify/app/src/main/kotlin/ro/snapify/ui/components/VideoPreviewDialog.kt:120:26 'val LocalLifecycleOwner: ProvidableCompositionLocal<LifecycleOwner>' is deprecated. Moved to lifecycle-runtime-compose library in androidx.lifecycle.compose package.

> Task :app:compileReleaseKotlin
w: file:///home/runner/work/Snapify/Snapify/app/src/main/kotlin/ro/snapify/service/ScreenshotMonitorService.kt:112:29 'static static field FOREGROUND_SERVICE_TYPE_NONE: Int' is deprecated. Deprecated in Java.
w: file:///home/runner/work/Snapify/Snapify/app/src/main/kotlin/ro/snapify/ui/MainActivity.kt:1:13 Annotation 'androidx.media3.common.util.UnstableApi' is not annotated with '@RequiresOptIn'. '@OptIn' has no effect.
w: file:///home/runner/work/Snapify/Snapify/app/src/main/kotlin/ro/snapify/ui/MainScreen.kt:1:13 Annotation 'androidx.media3.common.util.UnstableApi' is not annotated with '@RequiresOptIn'. '@OptIn' has no effect.
w: file:///home/runner/work/Snapify/Snapify/app/src/main/kotlin/ro/snapify/ui/components/ScreenshotComponents.kt:264:36 This is a delicate API and its use requires care. Make sure you fully read and understand documentation of the declaration that is marked as a delicate API.
w: file:///home/runner/work/Snapify/Snapify/app/src/main/kotlin/ro/snapify/ui/components/ScreenshotComponents.kt:300:24 This is a delicate API and its use requires care. Make sure you fully read and understand documentation of the declaration that is marked as a delicate API.
w: file:///home/runner/work/Snapify/Snapify/app/src/main/kotlin/ro/snapify/ui/components/ScreenshotComponents.kt:314:32 This is a delicate API and its use requires care. Make sure you fully read and understand documentation of the declaration that is marked as a delicate API.
w: file:///home/runner/work/Snapify/Snapify/app/src/main/kotlin/ro/snapify/ui/components/VideoPlayer.kt:40:26 'val LocalLifecycleOwner: ProvidableCompositionLocal<LifecycleOwner>' is deprecated. Moved to lifecycle-runtime-compose library in androidx.lifecycle.compose package.
w: file:///home/runner/work/Snapify/Snapify/app/src/main/kotlin/ro/snapify/ui/components/VideoPreviewDialog.kt:120:26 'val LocalLifecycleOwner: ProvidableCompositionLocal<LifecycleOwner>' is deprecated. Moved to lifecycle-runtime-compose library in androidx.lifecycle.compose package.

> Task :app:compileReleaseJavaWithJavac
> Task :app:compileDebugJavaWithJavac
> Task :app:hiltAggregateDepsRelease
> Task :app:hiltAggregateDepsDebug
> Task :app:hiltJavaCompileDebug
> Task :app:hiltJavaCompileRelease
> Task :app:processDebugJavaRes
> Task :app:processReleaseJavaRes
> Task :app:bundleReleaseClassesToCompileJar
> Task :app:bundleDebugClassesToCompileJar
> Task :app:kspDebugUnitTestKotlin
> Task :app:kspReleaseUnitTestKotlin

> Task :app:compileReleaseUnitTestKotlin FAILED
e: file:///home/runner/work/Snapify/Snapify/app/src/test/kotlin/ro/snapify/ui/NotificationPermissionTest.kt:10:13 Unresolved reference 'NotificationPermissionState'.
e: file:///home/runner/work/Snapify/Snapify/app/src/test/kotlin/ro/snapify/ui/NotificationPermissionTest.kt:11:13 Unresolved reference 'classifyNotificationPermission'.
e: file:///home/runner/work/Snapify/Snapify/app/src/test/kotlin/ro/snapify/ui/NotificationPermissionTest.kt:23:13 Unresolved reference 'NotificationPermissionState'.
e: file:///home/runner/work/Snapify/Snapify/app/src/test/kotlin/ro/snapify/ui/NotificationPermissionTest.kt:24:13 Unresolved reference 'classifyNotificationPermission'.
e: file:///home/runner/work/Snapify/Snapify/app/src/test/kotlin/ro/snapify/ui/NotificationPermissionTest.kt:31:13 Unresolved reference 'NotificationPermissionState'.
e: file:///home/runner/work/Snapify/Snapify/app/src/test/kotlin/ro/snapify/ui/NotificationPermissionTest.kt:32:13 Unresolved reference 'classifyNotificationPermission'.
e: file:///home/runner/work/Snapify/Snapify/app/src/test/kotlin/ro/snapify/ui/NotificationPermissionTest.kt:39:13 Unresolved reference 'NotificationPermissionState'.
e: file:///home/runner/work/Snapify/Snapify/app/src/test/kotlin/ro/snapify/ui/NotificationPermissionTest.kt:40:13 Unresolved reference 'classifyNotificationPermission'.
e: file:///home/runner/work/Snapify/Snapify/app/src/test/kotlin/ro/snapify/ui/NotificationPermissionTest.kt:47:13 Unresolved reference 'NotificationPermissionState'.
e: file:///home/runner/work/Snapify/Snapify/app/src/test/kotlin/ro/snapify/ui/NotificationPermissionTest.kt:48:13 Unresolved reference 'classifyNotificationPermission'.

> Task :app:compileDebugUnitTestKotlin FAILED
e: file:///home/runner/work/Snapify/Snapify/app/src/test/kotlin/ro/snapify/ui/NotificationPermissionTest.kt:10:13 Unresolved reference 'NotificationPermissionState'.
e: file:///home/runner/work/Snapify/Snapify/app/src/test/kotlin/ro/snapify/ui/NotificationPermissionTest.kt:11:13 Unresolved reference 'classifyNotificationPermission'.
e: file:///home/runner/work/Snapify/Snapify/app/src/test/kotlin/ro/snapify/ui/NotificationPermissionTest.kt:23:13 Unresolved reference 'NotificationPermissionState'.
e: file:///home/runner/work/Snapify/Snapify/app/src/test/kotlin/ro/snapify/ui/NotificationPermissionTest.kt:24:13 Unresolved reference 'classifyNotificationPermission'.
e: file:///home/runner/work/Snapify/Snapify/app/src/test/kotlin/ro/snapify/ui/NotificationPermissionTest.kt:31:13 Unresolved reference 'NotificationPermissionState'.
e: file:///home/runner/work/Snapify/Snapify/app/src/test/kotlin/ro/snapify/ui/NotificationPermissionTest.kt:32:13 Unresolved reference 'classifyNotificationPermission'.
e: file:///home/runner/work/Snapify/Snapify/app/src/test/kotlin/ro/snapify/ui/NotificationPermissionTest.kt:39:13 Unresolved reference 'NotificationPermissionState'.
e: file:///home/runner/work/Snapify/Snapify/app/src/test/kotlin/ro/snapify/ui/NotificationPermissionTest.kt:40:13 Unresolved reference 'classifyNotificationPermission'.
e: file:///home/runner/work/Snapify/Snapify/app/src/test/kotlin/ro/snapify/ui/NotificationPermissionTest.kt:47:13 Unresolved reference 'NotificationPermissionState'.
e: file:///home/runner/work/Snapify/Snapify/app/src/test/kotlin/ro/snapify/ui/NotificationPermissionTest.kt:48:13 Unresolved reference 'classifyNotificationPermission'.

> Task :app:transformDebugClassesWithAsm
> Task :app:transformReleaseClassesWithAsm
> Task :app:bundleDebugClassesToRuntimeJar
> Task :app:bundleReleaseClassesToRuntimeJar
gradle/actions: Writing build results to /home/runner/work/_temp/.gradle-actions/build-results/lint_step-1784851005254.json

[Incubating] Problems report is available at: file:///home/runner/work/Snapify/Snapify/build/reports/problems/problems-report.html

FAILURE: Build completed with 2 failures.

1: Task failed with an exception.
-----------
* What went wrong:
Execution failed for task ':app:compileReleaseUnitTestKotlin'.
> A failure occurred while executing org.jetbrains.kotlin.compilerRunner.GradleCompilerRunnerWithWorkers$GradleKotlinCompilerWorkAction
   > Compilation error. See log for more details

* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to generate a Build Scan (powered by Develocity).
> Get more help at https://help.gradle.org.
==============================================================================

2: Task failed with an exception.
-----------
* What went wrong:
Execution failed for task ':app:compileDebugUnitTestKotlin'.
> A failure occurred while executing org.jetbrains.kotlin.compilerRunner.GradleCompilerRunnerWithWorkers$GradleKotlinCompilerWorkAction
   > Compilation error. See log for more details

* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to generate a Build Scan (powered by Develocity).
> Get more help at https://help.gradle.org.
==============================================================================

Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

For more on this, please refer to https://docs.gradle.org/9.2.0/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.

BUILD FAILED in 4m 35s
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

1. Go to [Releases](https://github.com/DarkPhilosophy/Snapify/releases)
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