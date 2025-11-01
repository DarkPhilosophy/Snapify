# Ko - Android Screenshot Manager

## Build Commands
- `./gradlew assembleDebug` - Build debug APK
- `./gradlew assembleRelease` - Build release APK
- `./gradlew build` - Full build with tests

## Test Commands
- `./gradlew test` - Run unit tests
- `./gradlew connectedAndroidTest` - Run instrumented tests on device
- `./gradlew test --tests "com.ko.app.*Test"` - Run single test class

## Lint Commands
- `./gradlew detekt` - Run static analysis
- `./gradlew detekt --auto-correct` - Auto-fix formatting issues

## Architecture
- **Platform**: Android (minSdk 24, targetSdk 35)
- **Language**: Kotlin with Android Gradle Plugin
- **Architecture**: MVVM with Repository pattern
- **Database**: Room with SQLite
- **Async**: Coroutines + WorkManager
- **Storage**: DataStore preferences
- **Key modules**: app/ (single module), buildSrc/ (build logic)

## Code Style
- Kotlin conventions (120 char lines, 4-space indent)
- KDoc for public APIs, trailing commas
- Detekt rules: no magic numbers, explicit types, naming conventions
- Conventional commits: feat/fix/docs/style/refactor/test/chore
- Use val over var, avoid nullable types when possible

## Dependencies
- AndroidX (Core, Lifecycle, WorkManager, Room)
- Material Design 3, Glide for images, Gson for JSON
- JUnit + Espresso for testing
