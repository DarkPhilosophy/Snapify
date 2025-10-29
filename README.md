# Ko - Android Application 🤖

[![Build APK](https://github.com/DarkPhilosophy/Ko/actions/workflows/build-apk.yaml/badge.svg)](https://github.com/DarkPhilosophy/Ko/actions/workflows/build-apk.yaml)
[![Pre Merge Checks](https://github.com/DarkPhilosophy/Ko/actions/workflows/pre-merge.yaml/badge.svg)](https://github.com/DarkPhilosophy/Ko/actions/workflows/pre-merge.yaml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

A modern Android application with a centered button and haptic feedback, built with Kotlin and following industry best practices.

## 📱 Application Features

- **Single Button Interface**: A centered "PRESS" button with Material Design styling
- **Haptic Feedback**: Provides tactile feedback when the button is pressed
- **Portrait Orientation**: Locked to portrait mode for consistent user experience
- **Attribution**: Displays "by Adalbert Alexandru" at the bottom right
- **Dark Mode Support**: Automatically adapts to system dark mode settings
- **Material Design 3**: Modern UI following Google's latest design guidelines

## 🎨 Project Features

- **100% Kotlin**: Modern, type-safe Android development
- **Gradle Kotlin DSL**: Type-safe build configuration
- **Gradle Version Catalog**: Centralized dependency management via `libs.versions.toml`
- **buildSrc Module**: Shared build logic and coordinates
- **GitHub Actions CI/CD**: Automated builds, tests, and releases
- **Static Analysis**: Code quality checks with Detekt and ktlint
- **View Binding**: Type-safe view access
- **ProGuard**: Code optimization and obfuscation for release builds
- **Issue & PR Templates**: Structured contribution workflow

## 🛠️ Technology Stack

- **Language**: Kotlin 2.1.0
- **Minimum SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 15 (API 35)
- **Build System**: Gradle 8.7.3 with Kotlin DSL
- **UI Framework**: Material Design 3 Components
- **Architecture**: Single Activity with View Binding
- **Static Analysis**: Detekt 1.23.8 with ktlint formatting

## 📋 Prerequisites

Before you begin, ensure you have the following installed:

### Required Software

1. **Java Development Kit (JDK) 11 or higher**
   - Download from: https://adoptium.net/
   - Verify installation: `java -version`

2. **Android Studio** (Recommended - Latest Stable Version)
   - Download from: https://developer.android.com/studio
   - Includes Android SDK, emulator, and all necessary tools
   - **OR** Manual SDK installation (see below)

3. **Android SDK** (if not using Android Studio)
   - Download Android Command Line Tools from: https://developer.android.com/studio#command-tools
   - Required SDK components:
     - Android SDK Platform 35 (API 35)
     - Android SDK Build-Tools 35.0.0 or higher
     - Android SDK Platform-Tools
     - Android SDK Tools

### Optional but Recommended

- **Git**: For version control
- **Physical Android Device**: For testing on real hardware (requires USB debugging enabled)

## 🚀 Setup Instructions

### Option 1: Using Android Studio (Recommended for Beginners)

1. **Install Android Studio**
   - Download and install from https://developer.android.com/studio
   - During installation, ensure "Android SDK" and "Android Virtual Device" are selected

2. **Configure Android Studio**
   - Launch Android Studio
   - Complete the setup wizard
   - Install any additional SDK components if prompted

3. **Open the Project**
   - Click "Open" on the welcome screen
   - Navigate to the `Ko` folder and select it
   - Wait for Gradle sync to complete (this may take several minutes on first run)

4. **Configure local.properties**
   - Android Studio should automatically create this file
   - If not, copy `local.properties.example` to `local.properties`
   - Update the SDK path if necessary

5. **Build the Project**
   - Click "Build" → "Make Project" (or press Ctrl+F9 / Cmd+F9)
   - Wait for the build to complete

### Option 2: Using Command Line

1. **Install JDK 11+**
   ```bash
   # Verify Java installation
   java -version
   ```

2. **Install Android SDK**
   - Download Android Command Line Tools
   - Extract to a directory (e.g., `C:\Android\Sdk` on Windows)
   - Add to PATH environment variable

3. **Install Required SDK Components**
   ```bash
   # Windows (PowerShell)
   cd C:\Android\Sdk\cmdline-tools\latest\bin
   .\sdkmanager.bat "platform-tools" "platforms;android-35" "build-tools;35.0.0"
   
   # macOS/Linux
   cd ~/Android/Sdk/cmdline-tools/latest/bin
   ./sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0"
   ```

4. **Configure local.properties**
   ```bash
   # Navigate to Ko folder
   cd Ko
   
   # Copy example file
   cp local.properties.example local.properties
   
   # Edit local.properties and set your SDK path
   # Windows: sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
   # macOS: sdk.dir=/Users/YourUsername/Library/Android/sdk
   # Linux: sdk.dir=/home/YourUsername/Android/Sdk
   ```

5. **Build the Project**
   ```bash
   # Windows
   .\gradlew.bat build
   
   # macOS/Linux
   ./gradlew build
   ```

## 📦 Building the APK

### Using Android Studio

1. **Build Debug APK** (for testing)
   - Click "Build" → "Build Bundle(s) / APK(s)" → "Build APK(s)"
   - Wait for build to complete
   - Click "locate" in the notification to find the APK
   - Location: `Ko/app/build/outputs/apk/debug/app-debug.apk`

2. **Build Release APK** (for distribution)
   - Click "Build" → "Generate Signed Bundle / APK"
   - Select "APK" and click "Next"
   - Create a new keystore or use existing one
   - Fill in keystore details and click "Next"
   - Select "release" build variant
   - Click "Finish"
   - Location: `Ko/app/build/outputs/apk/release/app-release.apk`

### Using Command Line

1. **Build Debug APK**
   ```bash
   # Windows
   .\gradlew.bat assembleDebug
   
   # macOS/Linux
   ./gradlew assembleDebug
   ```
   - Output: `Ko/app/build/outputs/apk/debug/app-debug.apk`

2. **Build Release APK** (unsigned - for testing only)
   ```bash
   # Windows
   .\gradlew.bat assembleRelease
   
   # macOS/Linux
   ./gradlew assembleRelease
   ```
   - Output: `Ko/app/build/outputs/apk/release/app-release-unsigned.apk`

3. **Build Signed Release APK**
   ```bash
   # First, create a keystore (one-time setup)
   keytool -genkey -v -keystore ko-release-key.keystore -alias ko-key -keyalg RSA -keysize 2048 -validity 10000
   
   # Then build with signing
   # Windows
   .\gradlew.bat assembleRelease -Pandroid.injected.signing.store.file=ko-release-key.keystore -Pandroid.injected.signing.store.password=YOUR_PASSWORD -Pandroid.injected.signing.key.alias=ko-key -Pandroid.injected.signing.key.password=YOUR_PASSWORD
   
   # macOS/Linux
   ./gradlew assembleRelease -Pandroid.injected.signing.store.file=ko-release-key.keystore -Pandroid.injected.signing.store.password=YOUR_PASSWORD -Pandroid.injected.signing.key.alias=ko-key -Pandroid.injected.signing.key.password=YOUR_PASSWORD
   ```

## 📱 Running the Application

### On Android Emulator (Android Studio)

1. **Create an Emulator**
   - Click "Device Manager" in Android Studio
   - Click "Create Device"
   - Select a device (e.g., Pixel 5)
   - Select a system image (API 35 recommended)
   - Click "Finish"

2. **Run the App**
   - Select the emulator from the device dropdown
   - Click the "Run" button (green play icon) or press Shift+F10
   - Wait for the emulator to start and the app to install

### On Physical Device

1. **Enable Developer Options**
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times
   - Go back to Settings → System → Developer Options

2. **Enable USB Debugging**
   - In Developer Options, enable "USB Debugging"

3. **Connect Device**
   - Connect your Android device via USB
   - Accept the USB debugging prompt on your device

4. **Install and Run**
   
   **Using Android Studio:**
   - Select your device from the device dropdown
   - Click "Run" button
   
   **Using Command Line:**
   ```bash
   # Install the APK
   adb install app/build/outputs/apk/debug/app-debug.apk
   
   # Launch the app
   adb shell am start -n com.ko.app/.MainActivity
   ```

### Manual Installation

1. **Transfer APK to Device**
   - Copy the APK file to your device (via USB, email, cloud storage, etc.)

2. **Install APK**
   - On your device, navigate to the APK file using a file manager
   - Tap the APK file
   - If prompted, enable "Install from Unknown Sources"
   - Tap "Install"
   - Tap "Open" to launch the app

## 🔍 Static Analysis

This project uses **Detekt** with **ktlint** formatting rules to maintain code quality.

### Running Static Analysis

```bash
# Run Detekt analysis
./gradlew detekt

# View the report
open app/build/reports/detekt/detekt.html
```

### Configuration

- **Detekt Config**: `config/detekt/detekt.yml`
- **Auto-formatting**: Enabled via ktlint-formatting plugin
- **CI Integration**: Runs automatically on every push/PR

## ⚙️ CI/CD with GitHub Actions

The project includes three automated workflows:

### 1. Gradle Wrapper Validation
- **Trigger**: On every push and PR
- **Purpose**: Validates the Gradle wrapper checksum for security
- **File**: `.github/workflows/gradle-wrapper-validation.yml`

### 2. Pre Merge Checks
- **Trigger**: On every push to main and all PRs
- **Actions**:
  - Builds the project
  - Runs Detekt static analysis
  - Executes all tests
  - Uploads build reports as artifacts
- **File**: `.github/workflows/pre-merge.yaml`

### 3. Build APK
- **Trigger**: On push to main, tags, PRs, or manual dispatch
- **Actions**:
  - Builds debug and release APKs
  - Uploads APKs as artifacts
  - Creates GitHub releases for version tags
- **File**: `.github/workflows/build-apk.yaml`

### Setting Up CI

1. Push your code to GitHub
2. Go to repository Settings → Actions → General
3. Enable "Read and write permissions" for workflows
4. Workflows will run automatically on push/PR

## 📁 Project Structure

```
Ko/
├── .github/                               # GitHub configuration
│   ├── workflows/                         # CI/CD workflows
│   │   ├── gradle-wrapper-validation.yml
│   │   ├── pre-merge.yaml
│   │   └── build-apk.yaml
│   ├── ISSUE_TEMPLATE/                    # Issue templates
│   │   ├── bug_report.md
│   │   └── feature_request.md
│   └── PULL_REQUEST_TEMPLATE.md           # PR template
├── app/                                   # Main application module
│   ├── src/
│   │   └── main/
│   │       ├── kotlin/com/ko/app/         # Kotlin source files
│   │       │   └── MainActivity.kt        # Main activity
│   │       ├── res/                       # Resources
│   │       │   ├── drawable/              # Drawable resources
│   │       │   ├── layout/                # Layout XML files
│   │       │   │   └── activity_main.xml  # Main layout
│   │       │   ├── mipmap-anydpi-v26/     # Adaptive icons
│   │       │   ├── values/                # Values (strings, colors, themes)
│   │       │   └── values-night/          # Dark theme values
│   │       └── AndroidManifest.xml        # App manifest
│   ├── build.gradle.kts                   # App-level build configuration
│   └── proguard-rules.pro                 # ProGuard rules
├── buildSrc/                              # Build logic and coordinates
│   ├── src/main/kotlin/
│   │   └── Coordinates.kt                 # App coordinates (ID, versions)
│   └── build.gradle.kts
├── config/
│   └── detekt/
│       └── detekt.yml                     # Detekt configuration
├── gradle/
│   ├── libs.versions.toml                 # Version catalog
│   └── wrapper/
│       └── gradle-wrapper.properties      # Gradle wrapper config
├── build.gradle.kts                       # Root build configuration
├── settings.gradle.kts                    # Project settings
├── gradle.properties                      # Gradle properties
├── .gitignore                             # Git ignore rules
└── README.md                              # This file
```

## 🔧 Key Files Explained

### MainActivity.kt
The main entry point of the application. Handles:
- View binding initialization
- Button click listeners
- Haptic feedback implementation

### activity_main.xml
Defines the UI layout:
- ConstraintLayout for flexible positioning
- MaterialButton for the main "PRESS" button
- TextView for attribution text

### build.gradle.kts (app level)
Configures:
- Application ID: `com.ko.app`
- Minimum SDK: 24 (Android 7.0)
- Target SDK: 35 (Android 15)
- Dependencies (Material Design, AndroidX)
- Build types (debug/release)

### AndroidManifest.xml
Declares:
- Application metadata
- MainActivity as launcher activity
- Portrait orientation lock
- Permissions (none required currently)

## 🎨 Customization

### Changing Button Text
Edit `app/src/main/res/values/strings.xml`:
```xml
<string name="button_press">YOUR TEXT HERE</string>
```

### Changing Colors
Edit `app/src/main/res/values/colors.xml`:
```xml
<color name="primary">#YOUR_COLOR_HERE</color>
```

### Changing Attribution
Edit `app/src/main/res/values/strings.xml`:
```xml
<string name="attribution">your text here</string>
```

### Adding Button Functionality
Edit `MainActivity.kt` in the button click listener:
```kotlin
binding.pressButton.setOnClickListener { view ->
    view.performHapticFeedback(...)
    // Add your functionality here
}
```

## 🐛 Troubleshooting

### Gradle Sync Failed
- Ensure you have a stable internet connection
- Check that `local.properties` has the correct SDK path
- Try "File" → "Invalidate Caches / Restart" in Android Studio

### Build Failed
- Check that you have the required SDK components installed
- Verify JDK version is 11 or higher
- Clean and rebuild: `./gradlew clean build`

### App Crashes on Launch
- Check Logcat in Android Studio for error messages
- Ensure minimum SDK version is met (API 24+)
- Verify all dependencies are properly installed

### APK Won't Install
- Enable "Install from Unknown Sources" in device settings
- Ensure the APK is not corrupted
- Check device has sufficient storage space

## 📄 License

This project is created by Adalbert Alexandru.

## 🤝 Contributing

This is a personal project. For questions or suggestions, please contact the author.

## 📞 Support

For issues or questions:
1. Check the Troubleshooting section above
2. Review Android Studio's Logcat for error messages
3. Ensure all prerequisites are properly installed

---

**Version**: 1.0.0  
**Last Updated**: 2025-10-29

