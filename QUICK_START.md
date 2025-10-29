# Ko - Quick Start Guide

This guide will get you up and running with the Ko Android app in the fastest way possible.

## ⚡ Fastest Path to APK (Using Android Studio)

### Step 1: Install Android Studio (15-20 minutes)
1. Download Android Studio from: https://developer.android.com/studio
2. Run the installer
3. Follow the setup wizard (accept all defaults)
4. Wait for initial SDK components to download

### Step 2: Open Project (2-3 minutes)
1. Launch Android Studio
2. Click "Open" on the welcome screen
3. Navigate to and select the `Ko` folder
4. Click "OK"
5. Wait for Gradle sync to complete (first time may take 5-10 minutes)

### Step 3: Build APK (1-2 minutes)
1. Click "Build" menu → "Build Bundle(s) / APK(s)" → "Build APK(s)"
2. Wait for build to complete
3. Click "locate" in the notification popup
4. Your APK is ready! File: `app-debug.apk`

**Total Time: ~20-25 minutes**

---

## 🚀 Alternative: Command Line Build (For Advanced Users)

### Prerequisites
- JDK 11+ installed
- Android SDK installed

### Quick Commands

```bash
# Navigate to Ko folder
cd Ko

# Create local.properties file
# Windows:
echo sdk.dir=C:\\Users\\%USERNAME%\\AppData\\Local\\Android\\Sdk > local.properties

# macOS/Linux:
echo "sdk.dir=$HOME/Library/Android/sdk" > local.properties

# Build APK (Windows)
.\gradlew.bat assembleDebug

# Build APK (macOS/Linux)
./gradlew assembleDebug

# Find your APK at:
# Ko/app/build/outputs/apk/debug/app-debug.apk
```

---

## 📱 Testing the App

### Option 1: Android Emulator (In Android Studio)
1. Click "Device Manager" icon
2. Click "Create Device"
3. Select "Pixel 5" → "Next"
4. Download a system image (API 35 recommended) → "Next" → "Finish"
5. Click the green "Run" button (▶️)
6. Wait for emulator to start and app to launch

### Option 2: Physical Device
1. Enable Developer Options on your phone:
   - Settings → About Phone → Tap "Build Number" 7 times
2. Enable USB Debugging:
   - Settings → System → Developer Options → USB Debugging
3. Connect phone via USB
4. In Android Studio, select your device from dropdown
5. Click the green "Run" button (▶️)

### Option 3: Manual Install
1. Copy `app-debug.apk` to your phone
2. Open the file on your phone
3. Enable "Install from Unknown Sources" if prompted
4. Tap "Install"
5. Tap "Open"

---

## 🎯 What You Should See

When the app launches:
- A white screen (or dark if in dark mode)
- A centered purple button with text "PRESS"
- Text "by Adalbert Alexandru" at bottom right (semi-transparent)
- Button provides haptic feedback when tapped

---

## ❓ Common Issues

### "SDK location not found"
**Solution**: Create `local.properties` file in Ko folder with:
```
sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
```
(Replace with your actual SDK path)

### "Gradle sync failed"
**Solution**: 
1. Check internet connection
2. In Android Studio: File → Invalidate Caches / Restart
3. Try again

### "Build failed"
**Solution**:
1. Ensure JDK 11+ is installed: `java -version`
2. Clean and rebuild: Build → Clean Project, then Build → Rebuild Project

### "App won't install on phone"
**Solution**:
1. Enable "Install from Unknown Sources" in phone settings
2. Ensure phone has Android 7.0 (API 24) or higher
3. Check available storage space

---

## 🔄 Making Changes

### Change Button Text
1. Open `app/src/main/res/values/strings.xml`
2. Change `<string name="button_press">PRESS</string>`
3. Rebuild the app

### Change Button Color
1. Open `app/src/main/res/values/colors.xml`
2. Change `<color name="primary">#6200EE</color>`
3. Rebuild the app

### Add Button Functionality
1. Open `app/src/main/kotlin/com/ko/app/MainActivity.kt`
2. Find the `setOnClickListener` block
3. Add your code after the haptic feedback line
4. Rebuild the app

---

## 📊 Build Variants

### Debug Build (For Testing)
- Larger file size
- Includes debugging information
- Not optimized
- Command: `./gradlew assembleDebug`

### Release Build (For Distribution)
- Smaller file size
- Optimized and minified
- Requires signing for production
- Command: `./gradlew assembleRelease`

---

## 🎓 Next Steps

1. **Read the full README.md** for detailed information
2. **Explore the code** in `MainActivity.kt` and `activity_main.xml`
3. **Customize the app** to your needs
4. **Learn Kotlin** at https://kotlinlang.org/docs/home.html
5. **Learn Android** at https://developer.android.com/courses

---

## 📞 Need Help?

1. Check the main **README.md** for detailed troubleshooting
2. Review **Logcat** in Android Studio for error messages
3. Verify all prerequisites are installed correctly
4. Ensure SDK components are up to date

---

**Happy Coding! 🎉**

