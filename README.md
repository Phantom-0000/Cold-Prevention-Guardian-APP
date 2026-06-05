# Cold Prevention Guardian

Cold Prevention Guardian is an undergraduate health-support project for daily cold and flu prevention. It contains both a native Android application and a web version with a similar feature set: body-temperature tracking, trend visualization, rule-based health assessment, medication guidance, AI-assisted advice, user accounts, multilingual UI, and a lightweight community feed.

The project explores how a simple daily measurement can be turned into understandable health feedback through mobile/web interfaces, cloud storage, and contextual AI interaction.

## Project Versions

| Version | Branch | Main files | Description |
| --- | --- | --- | --- |
| Android app | [`Android`](https://github.com/Phantom-0000/Cold-Prevention-Guardian-APP/tree/Android) | `Cold Prevention Guardian APP/` | Native Android implementation built with Kotlin, Jetpack Compose, Firebase, and Retrofit. |
| Web app | [`Web`](https://github.com/Phantom-0000/Cold-Prevention-Guardian-APP/tree/Web) | `Cold Prevention Guardian.html`, `profile.html` | Browser-based implementation built with HTML, CSS, JavaScript, Firebase Web SDK, Chart.js, and DeepSeek API calls. |

The default `main` branch is used as the GitHub landing page for the whole project.

## Shared Features

- Email/password login and registration through Firebase Authentication.
- User profile with age-group and language preferences.
- Daily body-temperature recording with date selection and input validation.
- Recent temperature history with a line chart and fever reference markers.
- Health assessment based on the latest temperature.
- Medication and prevention guidance with warning notes.
- AI health assistant that uses the user's latest temperature as conversation context.
- Community comments with real-time updates and like interactions.
- Multilingual interface support for Chinese, English, German, French, and Spanish.

## Android Implementation

The Android version is a native mobile app built around Jetpack Compose and an MVVM-style structure.

### Android Tech Stack

| Area | Technology |
| --- | --- |
| UI | Kotlin, Jetpack Compose, Material 3 |
| State management | ViewModel, Kotlin StateFlow |
| Navigation | Jetpack Navigation Compose |
| Backend services | Firebase Authentication, Firebase Realtime Database |
| AI integration | DeepSeek Chat API through Retrofit and OkHttp |
| Charts | MPAndroidChart embedded with `AndroidView` |
| Build system | Gradle Kotlin DSL |

### Android Architecture

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

The Android source code is organized into `data`, `ui`, and `viewmodel` packages. Repositories encapsulate Firebase and network operations, while ViewModels expose reactive UI state to Compose screens.

## Web Implementation

The Web version provides the same core workflow in a static browser application.

### Web Tech Stack

| Area | Technology |
| --- | --- |
| UI | HTML, CSS, JavaScript |
| Authentication | Firebase Web SDK v8 |
| Database | Firebase Realtime Database |
| Charts | Chart.js |
| AI integration | DeepSeek Chat API through `fetch` |
| Deployment model | Static pages, no build step required |

### Web Pages

```text
Cold Prevention Guardian.html   Main dashboard, health assessment, medication guidance, comments, AI assistant
profile.html                    Login, registration, profile, language, and age-group settings
```

The web app includes a responsive dark-themed interface, multilingual content blocks, Firebase-backed user data, real-time comments, Chart.js temperature visualization, and AI advice generation based on the latest recorded temperature.

## Health Assessment Logic

Both versions use the latest temperature record to generate immediate guidance:

| Latest temperature | Assessment level | Example response |
| --- | --- | --- |
| `< 37.3 C` | Normal temperature | General prevention, hygiene, and monitoring advice |
| `37.3 C - 37.9 C` | Mildly elevated temperature | Rest, hydration, and closer monitoring |
| `38.0 C - 38.9 C` | High fever / possible flu | Stronger warnings and doctor-consultation guidance |
| `>= 39.0 C` | Emergency high fever | Urgent medical intervention advice |

This project is for educational and preventive guidance only. It is not a medical diagnosis system and should not replace professional medical care.

## Data Model

Firebase Realtime Database is used for user profiles, temperature records, and community comments. The Android and Web versions use the same conceptual structure, with minor implementation differences in how lists are stored.

```text
users/{uid}
  username
  email
  language
  ageGroup
  registeredAt
  temperatureRecords
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

Temperature records are sorted by date or timestamp and limited to the most recent records for compact trend monitoring.

## Running the Android App

```bash
git clone https://github.com/Phantom-0000/Cold-Prevention-Guardian-APP.git
cd Cold-Prevention-Guardian-APP
git checkout Android
cd "Cold Prevention Guardian APP"
```

Open the project in Android Studio, configure Firebase, then build:

Windows:

```powershell
.\gradlew.bat assembleDebug
```

macOS/Linux:

```bash
./gradlew assembleDebug
```

Firebase setup:

- Enable Email/Password authentication.
- Enable Firebase Realtime Database.
- Add an Android app with package name `com.example.coldpreventionguardianapp`.
- Replace `app/google-services.json` with your own Firebase project configuration if needed.

## Running the Web App

```bash
git clone https://github.com/Phantom-0000/Cold-Prevention-Guardian-APP.git
cd Cold-Prevention-Guardian-APP
git checkout Web
```

Then open:

```text
Cold Prevention Guardian.html
```

The page can be opened directly in a browser. For a more realistic local environment, it can also be served with any static file server.

## Security Notes

The coursework version contains API configuration directly in client-side code. Before making the project public or reusing it in production:

- Move DeepSeek API keys out of Android source code and web JavaScript.
- Do not call paid or private LLM APIs directly from client-side JavaScript in production.
- Use a backend proxy or serverless function to protect secrets.
- Rotate any exposed keys before publishing the repository.
- Keep Firebase rules restrictive so users can only access the data they are allowed to read or write.

## What This Project Demonstrates

- End-to-end development of the same product idea across web and native Android platforms.
- Practical use of Firebase Authentication and Realtime Database.
- Reactive Android UI design with Jetpack Compose and StateFlow.
- Static web app development with Firebase, Chart.js, and JavaScript DOM interaction.
- AI-assisted health guidance with live user context.
- Multilingual UI design for an international user base.
- Health-oriented UX thinking around risk thresholds, warnings, prevention, and user-friendly feedback.

## Future Improvements

- Add screenshots or a short demo video for both Android and Web versions.
- Add automated tests for temperature thresholds, repository mapping, and ViewModel logic.
- Move shared assessment rules into a reusable module or documented decision table.
- Improve offline handling and network error states.
- Add optional wearable or Bluetooth thermometer integration.
- Deploy the Web version with protected API access through a backend service.
