## AI Studio Android App

A native Android experience that recreates the promotional landing page for Google AI Studio.
It mirrors the original web app banner, messaging, and call to action while taking advantage of
Material 3 design components and Jetpack Compose for a responsive layout.

- Kotlin, Jetpack Compose, Material 3, Coil image loading
- Dynamic color support and light/dark themes
- Deep link button that opens the AI Studio website in the user's browser
- Instrumented and unit test stubs ready for expansion

### Prerequisites

- Android Studio Ladybug or newer
- Android SDK Platform 35 with Build Tools 35.0.0
- JDK 17 (bundled with Android Studio)

### Getting Started

1. Clone the repository and open it in Android Studio.
2. Let Gradle sync complete (internet required the first time for dependency downloads).
3. Select a physical device or emulator running API 24+.
4. Click "Run" or execute the command below.

```bash
./gradlew :app:installDebug
```

### Testing

```bash
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest
```

### Project Structure

- `app/src/main/java/com/example/aistudioapp` Main activity and Compose UI
- `app/src/main/java/com/example/aistudioapp/ui/theme` Theming utilities
- `app/src/main/res` Resources such as strings, icons, and theme XML
- `app/build.gradle.kts` Module configuration using the Android Gradle Plugin

### Next Steps

- Localize strings and provide offline image caching
- Add analytics for button interactions
- Expand the layout into additional informational screens as needed
