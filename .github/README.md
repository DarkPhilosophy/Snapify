# Screenshot Manager

<!-- LATEST-BUILD-STATUS-START -->
(https://github.com/DarkPhilosophy/Ko/actions)
[![Build Status](https://github.com/DarkPhilosophy/Ko/actions/workflows/ci.yaml/badge.svg)]
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Version 1.0.0](https://img.shields.io/badge/Version-1.0.0-blue.svg)](https://github.com/DarkPhilosophy/android-Snapify)
<!-- LATEST-BUILD-STATUS-END -->

A modern, intelligent Android application to automatically manage and organize your screenshots. Keep what matters, delete the rest automatically.

<!-- LATEST-VERSION-START -->
<!-- LATEST-VERSION-END -->

## Validation Status
<!-- LINT-RESULT-START -->
### Linting Status
> **Status**: ✅ **Passing**  
> **Last Updated**: 2026-01-13 05:40:01 UTC  
> **Summary**: 0 errors, 0 warnings

<details>
<summary>Click to view full lint output</summary>

```
Starting a Gradle Daemon (subsequent builds will be faster)
Calculating task graph as no cached configuration is available for tasks: spotlessCheck detekt test

...

BUILD SUCCESSFUL in 2m 7s
115 actionable tasks: 115 executed
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