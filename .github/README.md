# Screenshot Manager

[![Pre-Build Status](https://github.com/DarkPhilosophy/Ko/actions/workflows/pre-merge.yaml/badge.svg)](https://github.com/DarkPhilosophy/Ko/actions)
[![Build Status](https://github.com/DarkPhilosophy/Ko/actions/workflows/build-apk.yaml/badge.svg)](https://github.com/DarkPhilosophy/Ko/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Version 1.0.0](https://img.shields.io/badge/Version-1.0.0-blue.svg)](https://github.com/DarkPhilosophy/android-Snapify)

A modern, intelligent Android application to automatically manage and organize your screenshots. Keep what matters, delete the rest automatically.

<!-- LATEST-VERSION-START -->
<!-- LATEST-VERSION-END -->

## Validation Status
<!-- LINT-RESULT-START -->
### Linting Status
> **Status**: ❌ **Failing**  
> **Last Updated**: 2026-01-13 05:35:39 UTC  
> **Summary**: Check output for details

<details>
<summary>Click to view full lint output</summary>

```
Downloading https://services.gradle.org/distributions/gradle-9.2.0-bin.zip
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

> Task :app:preBuild UP-TO-DATE
> Task :app:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :detekt NO-SOURCE
> Task :app:preDebugBuild UP-TO-DATE
> Task :spotlessInternalRegisterDependencies

> Task :app:detekt
Property 'style>ForbiddenComment>values' is deprecated. Use `comments` instead, make sure you escape your text for Regular Expressions..
Property 'naming>FunctionNaming>ignoreOverridden' is deprecated. This configuration is ignored and will be removed in the future.

> Task :core:detekt
> Task :app:spotlessKotlinGradle
> Task :app:spotlessKotlinGradleCheck
> Task :app:generateDebugResValues
> Task :app:generateDebugResources
> Task :app:processDebugGoogleServices FAILED
> Task :core:preBuild UP-TO-DATE
> Task :core:preDebugBuild UP-TO-DATE
> Task :core:generateDebugResValues
> Task :core:generateDebugResources
> Task :core:packageDebugResources
> Task :core:processDebugNavigationResources
> Task :app:spotlessKotlin
> Task :app:spotlessKotlinCheck
> Task :app:spotlessCheck
> Task :app:generateDebugBuildConfig
> Task :core:writeDebugAarMetadata
> Task :app:processDebugNavigationResources
> Task :core:parseDebugLocalResources
> Task :app:dataBindingMergeDependencyArtifactsDebug
> Task :app:createDebugCompatibleScreenManifests
> Task :core:extractDeepLinksDebug
> Task :app:checkDebugAarMetadata
> Task :core:generateDebugRFile
> Task :core:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :core:generateDebugBuildConfig
> Task :core:compileDebugLibraryResources
> Task :app:compileDebugNavigationResources
> Task :core:javaPreCompileDebug
> Task :app:preDebugUnitTestBuild UP-TO-DATE
> Task :app:javaPreCompileDebugUnitTest
> Task :app:preReleaseBuild UP-TO-DATE
> Task :app:buildKotlinToolingMetadata
> Task :app:dataBindingMergeDependencyArtifactsRelease
> Task :core:processDebugManifest
> Task :app:generateReleaseResValues
> Task :app:processReleaseGoogleServices FAILED
> Task :core:preReleaseBuild UP-TO-DATE
> Task :app:generateReleaseResources
> Task :core:generateReleaseResValues
> Task :core:processReleaseNavigationResources
> Task :core:generateReleaseResources
> Task :app:processReleaseNavigationResources
> Task :core:packageReleaseResources
> Task :core:parseReleaseLocalResources
> Task :core:generateReleaseRFile
> Task :core:writeReleaseAarMetadata
> Task :app:checkReleaseAarMetadata
> Task :app:compileReleaseNavigationResources
> Task :app:createReleaseCompatibleScreenManifests
> Task :app:generateReleaseBuildConfig
> Task :core:extractDeepLinksRelease
> Task :core:compileReleaseLibraryResources
> Task :core:generateReleaseBuildConfig
> Task :core:processReleaseManifest
> Task :core:javaPreCompileRelease
> Task :app:javaPreCompileDebug
> Task :app:preReleaseUnitTestBuild UP-TO-DATE
> Task :app:javaPreCompileRelease
> Task :core:preDebugUnitTestBuild UP-TO-DATE
> Task :app:javaPreCompileReleaseUnitTest
> Task :core:javaPreCompileDebugUnitTest
> Task :core:preReleaseUnitTestBuild UP-TO-DATE
> Task :core:generateDebugUnitTestStubRFile
> Task :core:generateReleaseUnitTestStubRFile
> Task :core:javaPreCompileReleaseUnitTest
> Task :core:compileDebugKotlin
> Task :core:compileReleaseKotlin
> Task :core:processDebugJavaRes
> Task :core:processReleaseJavaRes
> Task :core:compileDebugJavaWithJavac
> Task :core:compileReleaseJavaWithJavac
> Task :core:bundleLibCompileToJarDebug
> Task :core:bundleLibCompileToJarRelease
> Task :core:bundleLibRuntimeToJarRelease
> Task :core:bundleLibRuntimeToJarDebug
> Task :core:compileDebugUnitTestKotlin NO-SOURCE
> Task :core:createFullJarDebug
> Task :core:compileReleaseUnitTestKotlin NO-SOURCE
> Task :core:compileDebugUnitTestJavaWithJavac NO-SOURCE
> Task :core:compileReleaseUnitTestJavaWithJavac NO-SOURCE
> Task :core:createFullJarRelease
> Task :core:processDebugUnitTestJavaRes NO-SOURCE
> Task :core:processReleaseUnitTestJavaRes NO-SOURCE
> Task :core:testDebugUnitTest NO-SOURCE
> Task :core:testReleaseUnitTest NO-SOURCE
> Task :core:test UP-TO-DATE
gradle/actions: Writing build results to /home/runner/work/_temp/.gradle-actions/build-results/lint_step-1768282393784.json

[Incubating] Problems report is available at: file:///home/runner/work/Snapify/Snapify/build/reports/problems/problems-report.html

FAILURE: Build completed with 2 failures.

1: Task failed with an exception.
-----------
* What went wrong:
Execution failed for task ':app:processDebugGoogleServices'.
> File google-services.json is missing. 
  The Google Services Plugin cannot function without it. 
  Searched locations: /home/runner/work/Snapify/Snapify/app/src/debug/google-services.json, /home/runner/work/Snapify/Snapify/app/src/debug/google-services.json, /home/runner/work/Snapify/Snapify/app/src/google-services.json, /home/runner/work/Snapify/Snapify/app/src/debug/google-services.json, /home/runner/work/Snapify/Snapify/app/src/Debug/google-services.json, /home/runner/work/Snapify/Snapify/app/google-services.json

* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to generate a Build Scan (powered by Develocity).
> Get more help at https://help.gradle.org.
==============================================================================

2: Task failed with an exception.
-----------
* What went wrong:
Execution failed for task ':app:processReleaseGoogleServices'.
> File google-services.json is missing. 
  The Google Services Plugin cannot function without it. 
  Searched locations: /home/runner/work/Snapify/Snapify/app/src/release/google-services.json, /home/runner/work/Snapify/Snapify/app/src/release/google-services.json, /home/runner/work/Snapify/Snapify/app/src/google-services.json, /home/runner/work/Snapify/Snapify/app/src/release/google-services.json, /home/runner/work/Snapify/Snapify/app/src/Release/google-services.json, /home/runner/work/Snapify/Snapify/app/google-services.json

* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to generate a Build Scan (powered by Develocity).
> Get more help at https://help.gradle.org.
==============================================================================

Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

For more on this, please refer to https://docs.gradle.org/9.2.0/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.

BUILD FAILED in 2m 35s
73 actionable tasks: 73 executed
Configuration cache entry stored.
```

</details>
<!-- LINT-RESULT-END -->

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

