## AI Studio Android App

A native Android experience that recreates the promotional landing page for Google AI Studio with a
traditional view-based UI.

- Kotlin + ViewBinding + Material 3 components
- Remote banner image loading with Coil
- Browser deep link CTA with graceful fallback messaging
- GitHub Actions workflow for unit tests and debug APK assembly

### Prerequisites

- Android Studio Ladybug or newer
- Android SDK Platform 35 with Build Tools 35.0.0
- JDK 17 (bundled with Android Studio)

### Getting Started

1. Clone the repository and open it in Android Studio.
2. Let Gradle sync complete (internet required the first time for dependency downloads).
3. Select a physical device or emulator running API 24+.
4. Click **Run** or execute:

```bash
./gradlew :app:installDebug
```

### Testing

```bash
./gradlew testDebugUnitTest
```

> The `connectedDebugAndroidTest` task requires a locally configured Android SDK / device.

### Continuous Integration

The workflow in `.github/workflows/android-ci.yml` runs on every push and pull request:

- Sets up JDK 17 and caches Gradle artifacts
- Installs the Android SDK
- Executes unit tests (`testDebugUnitTest`)
- Builds the debug APK (`assembleDebug`)

### Project Structure

- `app/src/main/java/com/example/aistudioapp` Activity logic and helpers
- `app/src/main/res/layout` Layout XML for the landing screen
- `app/src/main/res/values` Strings, colors, and themes
- `app/build.gradle.kts` Android application module configuration

### Next Steps

- Localize strings and provide offline image caching
- Add analytics for button interactions
- Expand the layout into additional informational screens as needed
