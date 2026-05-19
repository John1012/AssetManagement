# Asset Management

An Android app featuring a **compound interest calculator** with year-by-year growth visualization and persistent calculation history.

## Screenshots

| Calculator | History |
|---|---|
| Input fields, growth chart, and result summary | Saved calculations with swipe-to-delete |

## Features

- **Compound interest calculator** — enter initial fund, annual ROI (%), duration (1–100 years), and optional annual contribution
- **Year-by-year growth chart** — Vico line chart showing total value vs. total contributed over time
- **Result summary** — final value, total contributed, and total interest earned, all formatted in NT$
- **Calculation history** — every calculation is saved automatically to a local Room database
- **Prefill from history** — tap any history item to reload its inputs into the calculator
- **Swipe to delete** — remove individual history entries with a swipe gesture
- **Bottom navigation** — switch between Calculator and History screens

## Compound Growth Formula

Contribution is added at the **start** of each year before interest compounds:

```
value(year) = (value(year - 1) + annualContribution) × (1 + annualROI / 100)
```

## Architecture

Single-module Clean Architecture with three sub-packages under `calculator/`:

```
calculator/
├── data/
│   ├── local/              # Room Entity, DAO, Database
│   ├── CalculationRepositoryImpl.kt
│   └── CalculatorModule.kt # Hilt DI bindings
├── domain/
│   ├── model/              # CalculationInput, CalculationResult, YearlySnapshot, HistoryItem
│   ├── repository/         # CalculationRepository interface
│   └── usecase/            # ComputeCompoundGrowthUseCase, SaveCalculationUseCase, GetCalculationHistoryUseCase
└── ui/
    ├── calculator/         # CalculatorViewModel, CalculatorScreen, GrowthChartContent
    └── history/            # HistoryViewModel, HistoryScreen
```

## Tech Stack

| Layer | Library | Version |
|-------|---------|---------|
| Language | Kotlin | 2.3.20 |
| UI | Jetpack Compose + Material 3 | BOM 2026.03.01 |
| Navigation | Navigation 3 (`androidx.navigation3`) | 1.0.1 |
| DI | Hilt | 2.59.2 |
| Database | Room | 2.7.1 |
| Chart | Vico (`compose-m3`) | 2.0.0 |
| Annotation processing | KSP | 2.3.0 |
| Build | Android Gradle Plugin | 9.0.1 |
| Min SDK | API 24 (Android 7.0) | |
| Target SDK | API 36 | |

## Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- JDK 17

### Build & Run

```bash
# Clone the repository
git clone <repo-url>
cd AssetManagement

# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew testDebugUnitTest
```

Open the project in Android Studio and run on an emulator or physical device (API 24+).

## Testing

15 unit tests covering the domain and ViewModel layers:

| Test class | Tests |
|---|---|
| `ComputeCompoundGrowthUseCaseTest` | 5 — formula correctness, zero ROI, contributions |
| `CalculatorViewModelTest` | 3 — initial state, calculate, prefill |
| `HistoryViewModelTest` | 3 — loading, empty, success states |
| `SaveCalculationUseCaseTest` | 1 — delegates to repository |
| `GetCalculationHistoryUseCaseTest` | 1 — returns flow from repository |

```bash
./gradlew testDebugUnitTest
```

## Project Structure

```
app/src/main/java/com/example/assetmanagement/
├── AssetManagementApplication.kt   # @HiltAndroidApp
├── MainActivity.kt                 # @AndroidEntryPoint
├── Navigation.kt                   # Bottom nav Scaffold + NavDisplay
├── NavigationKeys.kt               # CalculatorKey, HistoryKey (NavKey)
├── calculator/                     # Compound calculator feature
│   ├── data/
│   ├── domain/
│   └── ui/
└── (legacy scaffolding)
```
