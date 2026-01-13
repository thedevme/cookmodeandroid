# CookMode

A hands-free recipe execution app for Android. Add your own recipes, then tap "Cook" to enter a distraction-free, step-by-step mode designed for messy hands and quick glances.

## Features

- Add and save recipes (title, image, prep time, servings, ingredients, steps)
- **Cooking Mode**: One step at a time, large readable text, embedded timers, back/next navigation
- Recipe list with search
- Edit/delete recipes
- Timer notifications (even when backgrounded)

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Database**: Room
- **DI**: Hilt
- **Architecture**: MVVM
- **Min SDK**: 24 (Android 7.0)

## Building

```bash
./gradlew assembleDebug
```

## Testing

```bash
# Unit tests
./gradlew test

# UI tests
./gradlew connectedAndroidTest
```

## Package Structure

```
io.designtoswiftui.cookmode/
├── data/           # Database, DAOs
│   └── repository/ # Repository pattern
├── models/         # Data models (Room entities)
├── timer/          # Timer service
├── ui/
│   ├── components/ # Reusable UI components
│   ├── cooking/    # Cooking mode screen
│   ├── home/       # Home screen
│   ├── paywall/    # Paywall screen
│   ├── recipe/     # Recipe add/edit screen
│   └── theme/      # App theme
├── util/           # Utility classes
└── viewmodels/     # ViewModels
```

## License

Proprietary - Cocoa Academy
