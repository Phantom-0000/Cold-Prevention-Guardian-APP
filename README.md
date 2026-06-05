# Cold Prevention Guardian

Cold Prevention Guardian is an undergraduate Android project for daily cold and flu prevention support. The app helps users record body temperature, monitor short-term trends, receive rule-based health assessments, read medication guidance, ask an AI assistant for contextual advice, and interact with a lightweight community feed.

The project explores how a mobile health app can turn a simple daily measurement into timely, understandable feedback while combining cloud storage, multilingual UI design, and AI-assisted conversation.

## Source Code

The Android application source is currently kept on the [`Android` branch](https://github.com/Phantom-0000/Cold-Prevention-Guardian-APP/tree/Android/Cold%20Prevention%20Guardian%20APP) under:

```text
Cold Prevention Guardian APP/
```

The default `main` branch is used as the GitHub project landing page. An earlier HTML prototype is available on the `Web` branch.

## Key Features

- User authentication with Firebase Email/Password login and registration.
- Personal temperature tracking with date selection, input validation, and cloud persistence.
- Seven-record temperature history with a line chart and fever reference line.
- Rule-based health assessment for normal temperature, mild elevation, fever, and emergency high-fever scenarios.
- Medication information cards for prevention and treatment guidance, including warnings for selected medicines.
- AI health assistant using DeepSeek chat completions, with the user's latest temperature injected into the conversation context.
- Community comment feed with real-time Firebase updates and like toggling.
- Profile settings for age group and app language.
- Multilingual resources for Chinese, English, German, French, and Spanish.

## Tech Stack

| Area | Technology |
| --- | --- |
| Mobile app | Kotlin, Android, Jetpack Compose, Material 3 |
| Architecture | MVVM-style ViewModels, Kotlin StateFlow, repository layer |
| Navigation | Jetpack Navigation Compose |
| Backend services | Firebase Authentication, Firebase Realtime Database |
| AI integration | DeepSeek Chat API through Retrofit and OkHttp |
| Charts | MPAndroidChart embedded in Compose through AndroidView |
| Build system | Gradle Kotlin DSL, Android Gradle Plugin |

## App Architecture

```text
Jetpack Compose Screens
        |
        v
ViewModels with StateFlow state
        |
        v
Repository Layer
        |
        +-- Firebase Auth
        +-- Firebase Realtime Database
        +-- DeepSeek Chat API
```

The UI is built with Compose screens for authentication, dashboard monitoring, health details, AI chat, and profile settings. ViewModels hold reactive screen state and call repository classes that encapsulate Firebase and network operations.

## Health Assessment Logic

The app uses the latest temperature record to generate an immediate health assessment:

| Latest temperature | Assessment level | Example response |
| --- | --- | --- |
| `< 37.3 C` | Normal temperature | General prevention and hygiene advice |
| `37.3 C - 37.9 C` | Mildly elevated temperature | Rest, hydration, and closer monitoring |
| `38.0 C - 38.9 C` | High fever / suspected flu | Stronger warnings and doctor consultation guidance |
| `>= 39.0 C` | Emergency high fever | Urgent medical intervention advice |

This logic is intended for educational and preventative guidance only. It is not a medical diagnosis system and should not replace professional medical care.

## Data Model

Firebase Realtime Database is used for user profiles, personal temperature records, and community comments.

```text
users/{uid}
  username
  email
  language
  ageGroup
  registeredAt
  temperatureRecords/{index}
    date
    temperature
    timestamp

comments/{commentId}
  author
  content
  date
  timestamp
  likes
  likedBy
```

Temperature records are sorted by timestamp and trimmed to the latest seven entries for compact trend monitoring.

## Running Locally

1. Clone the repository and switch to the Android source branch:

```bash
git clone https://github.com/Phantom-0000/Cold-Prevention-Guardian-APP.git
cd Cold-Prevention-Guardian-APP
git checkout Android
cd "Cold Prevention Guardian APP"
```

2. Open the project in Android Studio.

3. Configure Firebase:

- Create or select a Firebase project.
- Enable Email/Password authentication.
- Enable Firebase Realtime Database.
- Add an Android app with package name `com.example.coldpreventionguardianapp`.
- Replace `app/google-services.json` with your own Firebase configuration if you are using a different Firebase project.

4. Configure the AI API key.

The coursework version calls DeepSeek through `NetworkModule.kt`. For public or production use, do not hardcode API keys in source code. Move the key into a local Gradle property, `BuildConfig` field, or another secret-management mechanism before publishing.

5. Build the debug APK:

Windows:

```powershell
.\gradlew.bat assembleDebug
```

macOS/Linux:

```bash
./gradlew assembleDebug
```

## Project Structure

```text
Cold Prevention Guardian APP/
  app/
    src/main/java/com/example/coldpreventionguardianapp/
      data/
        model/          Domain models
        remote/         DeepSeek Retrofit client
        repository/     Firebase and app data repositories
      ui/
        auth/           Login and registration
        dashboard/      Temperature input, latest status, and chart
        details/        Health assessment, medication cards, community feed
        chat/           AI assistant conversation UI
        profile/        User settings and language selection
      viewmodel/        Dashboard and chat state management
    src/main/res/       Localized strings, themes, launcher assets
  gradle/
  build.gradle.kts
  settings.gradle.kts
```

## What This Project Demonstrates

- End-to-end Android application development with Kotlin and Jetpack Compose.
- Reactive UI state management with StateFlow and ViewModel.
- Cloud-backed authentication and real-time data synchronization.
- Integration of a third-party AI chat API into a mobile workflow.
- Health-oriented UX design, including risk thresholds, feedback cards, and user-friendly warnings.
- Internationalization through Android resource files.

## Future Improvements

- Move all API secrets out of source code and rotate any exposed keys.
- Add automated tests for temperature thresholds, repository mapping, and ViewModel state transitions.
- Improve data validation and error handling for offline or unstable network conditions.
- Add optional wearable or Bluetooth thermometer integration for automatic temperature capture.
- Add screenshots or a demo video to make the repository easier to review.
