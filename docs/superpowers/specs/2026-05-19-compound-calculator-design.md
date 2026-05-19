# Compound Rate Calculator — Design Spec

**Date:** 2026-05-19  
**Feature:** Compound interest calculator with year-by-year growth chart and history  
**Approach:** Approach B — two-screen app (Calculator + History) within a single `calculator` module following Clean Architecture (data / domain / ui packages)

---

## 1. Package Structure

Under `com.example.assetmanagement`, a new `calculator` package:

```
calculator/
├── data/
│   ├── local/
│   │   ├── CalculationDao.kt
│   │   ├── CalculationEntity.kt
│   │   └── CalculationDatabase.kt
│   └── CalculationRepositoryImpl.kt
│
├── domain/
│   ├── model/
│   │   ├── CalculationInput.kt
│   │   └── CalculationResult.kt
│   ├── repository/
│   │   └── CalculationRepository.kt
│   └── usecase/
│       ├── ComputeCompoundGrowthUseCase.kt
│       ├── SaveCalculationUseCase.kt
│       └── GetCalculationHistoryUseCase.kt
│
└── ui/
    ├── calculator/
    │   ├── CalculatorScreen.kt
    │   └── CalculatorViewModel.kt
    └── history/
        ├── HistoryScreen.kt
        └── HistoryViewModel.kt
```

The existing `data/DataRepository.kt` scaffold is untouched. The calculator feature is fully self-contained.

---

## 2. Domain Models & Calculation Logic

### CalculationInput
| Field | Type | Constraints |
|---|---|---|
| `initialFund` | `Double` | > 0, NT$ |
| `annualROI` | `Double` | 0.0–100.0 (%) |
| `durationYears` | `Int` | 1–100 |
| `annualContribution` | `Double` | ≥ 0, NT$ (defaults to 0.0) |

### YearlySnapshot
| Field | Type | Description |
|---|---|---|
| `year` | `Int` | 1…N |
| `totalValue` | `Double` | Cumulative fund value at end of year |
| `totalInterestEarned` | `Double` | Total interest accumulated so far |

### CalculationResult
| Field | Type | Description |
|---|---|---|
| `finalValue` | `Double` | Fund value at end of duration |
| `totalInterestEarned` | `Double` | `finalValue - totalContributed` |
| `totalContributed` | `Double` | `initialFund + (annualContribution * durationYears)` |
| `yearlySnapshots` | `List<YearlySnapshot>` | Drives the line chart |

### Compound Growth Formula (`ComputeCompoundGrowthUseCase`)

Annual compounding with optional annual contributions. The annual contribution is added at the **start** of each year before interest compounds:

```
value(0) = initialFund
value(n) = (value(n-1) + annualContribution) × (1 + ROI / 100)
```

This use case is a pure function — no coroutines, no side effects, fully unit-testable.

---

## 3. UI & Navigation

### Navigation
Bottom navigation bar with two tabs using Navigation 3:

```
MainActivity
└── NavHost
    ├── CalculatorScreen  (tab: "Calculator")
    └── HistoryScreen     (tab: "History")
```

### Calculator Screen (top → bottom)

1. **Input form** — four fields: NT$ initial fund, ROI %, duration (years, 1–100), NT$ annual contribution (optional). A "Calculate" button triggers computation.
2. **Line chart** — X axis: year, Y axis: NT$ value. Two lines: *Total Value* and *Total Contributed* (shows the interest gap visually). Built with **Vico** (Compose-native).
3. **Result summary card** — Final Value / Total Contributed / Total Interest Earned, formatted as NT$.

### History Screen

- `LazyColumn` of saved calculation cards, newest first.
- Each card shows: date saved, initial fund, ROI, duration, final value.
- Tap a card → navigates to Calculator screen with inputs pre-filled and chart rendered.
- Swipe-to-delete to remove a history entry.

### ViewModel States

```kotlin
// CalculatorUiState
sealed interface CalculatorUiState {
    object Idle : CalculatorUiState
    data class ShowingResult(val input: CalculationInput, val result: CalculationResult) : CalculatorUiState
    data class Error(val message: String) : CalculatorUiState
}

// HistoryUiState
sealed interface HistoryUiState {
    object Loading : HistoryUiState
    data class Success(val items: List<CalculationEntity>) : HistoryUiState
    object Empty : HistoryUiState
}
```

Both exposed as `StateFlow` from their respective ViewModels, collected with `collectAsStateWithLifecycle`.

---

## 4. Data Layer & Dependencies

### Room Schema
Single table `calculations`:

| Column | Type | Notes |
|---|---|---|
| `id` | `Long` (PK, autoincrement) | |
| `initialFund` | `Double` | |
| `annualROI` | `Double` | |
| `durationYears` | `Int` | |
| `annualContribution` | `Double` | |
| `finalValue` | `Double` | Stored for display in history |
| `savedAt` | `Long` | Unix timestamp ms, ordered DESC |

Year-by-year snapshots are **not persisted** — recomputed from stored inputs on demand.

### New Dependencies (`libs.versions.toml`)

| Library | Purpose |
|---|---|
| `androidx.room` (runtime, ktx, compiler) | Local persistence |
| `vico` compose chart | Year-by-year line chart |
| `hilt-android` + `hilt-compiler` | Dependency injection |
| `androidx.hilt:hilt-navigation-compose` | ViewModel injection in Compose nav |

### Dependency Injection Flow (Hilt)

```
CalculationDatabase → CalculationDao
CalculationDao → CalculationRepositoryImpl → CalculationRepository (interface)
CalculationRepository → SaveCalculationUseCase
                      → GetCalculationHistoryUseCase
ComputeCompoundGrowthUseCase  ← pure, no injection needed
UseCases → CalculatorViewModel / HistoryViewModel
```

---

## 5. Out of Scope

- Multiple currencies (NT$ only)
- Compounding frequencies other than annual
- Cloud sync or user accounts
- Export / share functionality
