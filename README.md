# GeminiChat - AI Studio App

An Android application showcasing Google's AI Studio with a clean, Material Design interface.

## Features

- 🎨 Modern Material Design 3 UI
- 🖼️ Dynamic banner loading with Coil
- 🔗 Direct link to Google AI Studio
- 📱 Responsive layout for all screen sizes
- 🚀 Optimized release builds with ProGuard

## Build & Release

### Prerequisites

- Android Studio Hedgehog or later
- JDK 17
- Android SDK 35
- Gradle 8.7+

### Building Locally

```bash
# Clone the repository
git clone https://github.com/Kaleaon/GeminiChat.git
cd GeminiChat

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run tests
./gradlew test
```

### Automated Builds

This project uses GitHub Actions for automated building and releasing:

#### Continuous Integration (CI)
- Runs on every push and pull request
- Executes lint checks and unit tests
- Builds debug APK
- Uploads artifacts for review

#### Release Workflow
- Triggers on version tags (e.g., `v1.0.0`)
- Builds signed release APK and AAB
- Runs all tests
- Automatically creates GitHub Release
- Uploads APK and AAB to release

### Creating a Release

1. **Tag a version:**
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```

2. **Or use manual workflow dispatch:**
   - Go to Actions → Build and Release
   - Click "Run workflow"
   - Enter version number

### Signing Configuration

To enable APK signing in CI/CD, add these secrets to your repository:

1. Go to Settings → Secrets and variables → Actions
2. Add the following secrets:
   - `SIGNING_KEY`: Base64 encoded keystore file
   - `KEY_ALIAS`: Your key alias
   - `KEY_STORE_PASSWORD`: Keystore password
   - `KEY_PASSWORD`: Key password

**Generate base64 keystore:**
```bash
base64 -i your-keystore.jks | pbcopy  # macOS
base64 -i your-keystore.jks | xclip   # Linux
```

## Project Structure

```
GeminiChat/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/aistudioapp/
│   │   │   │   └── MainActivity.kt
│   │   │   ├── res/
│   │   │   └── AndroidManifest.xml
│   │   └── test/
│   │       └── java/com/example/aistudioapp/
│   │           └── MainActivityTest.kt
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── .github/
│   └── workflows/
│       ├── android-ci.yml
│       └── release.yml
└── build.gradle.kts
```

## Technologies Used

- **Kotlin** - Modern Android development language
- **Material Design 3** - Latest Material Design components
- **ViewBinding** - Type-safe view access
- **Coil** - Image loading library
- **JUnit** - Unit testing framework
- **GitHub Actions** - CI/CD automation

## Development

### Code Style
- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Add KDoc comments for public APIs
- Keep functions small and focused

### Testing
```bash
# Run unit tests
./gradlew test

# Run with coverage
./gradlew testDebugUnitTest

# Run lint checks
./gradlew lintDebug
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Google AI Studio team for the amazing platform
- Material Design team for the beautiful components
- Coil library for efficient image loading

## Support

For issues and questions, please open an issue on GitHub.