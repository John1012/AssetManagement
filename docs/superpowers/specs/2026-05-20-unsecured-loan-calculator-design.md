# Unsecured Loan Calculator — Design Spec

**Date:** 2026-05-20
**Project:** Asset Management (Android)

---

## Overview

Add an Unsecured Loan Calculator as a new `:loan` Gradle module. The app gains a Home screen where users choose which calculator to open. Each calculator maintains its own independent history.

---

## Module Structure

```
:app    — existing module; gains HomeScreen and top-level navigation
:loan   — new Android Library module with Clean Architecture
```

`:app/build.gradle.kts` adds:
```kotlin
implementation(project(":loan"))
```

### `:loan` package layout

```
loan/src/main/java/com/example/assetmanagement/loan/
├── data/
│   ├── local/
│   │   ├── LoanEntity.kt
│   │   ├── LoanDao.kt
│   │   └── LoanDatabase.kt
│   ├── LoanRepositoryImpl.kt
│   └── LoanModule.kt           (Hilt bindings)
├── domain/
│   ├── model/
│   │   ├── LoanInput.kt
│   │   ├── LoanResult.kt
│   │   ├── LoanScheduleEntry.kt
│   │   └── LoanHistoryItem.kt
│   ├── repository/
│   │   └── LoanRepository.kt
│   └── usecase/
│       ├── ComputeLoanUseCase.kt
│       ├── SaveLoanCalculationUseCase.kt
│       └── GetLoanHistoryUseCase.kt
└── ui/
    ├── loan/
    │   ├── LoanViewModel.kt
    │   ├── LoanScreen.kt
    │   └── LoanChartContent.kt
    └── history/
        ├── LoanHistoryViewModel.kt
        └── LoanHistoryScreen.kt
```

---

## Navigation

### Top-level keys (`:app` — `NavigationKeys.kt`)

```kotlin
@Serializable data object HomeKey : NavKey          // new
@Serializable data class CalculatorKey(...) : NavKey // existing (unchanged)
@Serializable data object HistoryKey : NavKey        // existing (unchanged)
@Serializable data object LoanKey : NavKey           // new — entry point to LoanNavigation
```

`LoanNavigation` (in `:loan`) owns its own internal back stack with:
```kotlin
@Serializable data class LoanCalculatorKey(
    val prefillAmount: Double = 0.0,
    val prefillRate: Double = 0.0,
    val prefillMonths: Int = 0,
    val hasPrefill: Boolean = false
) : NavKey
@Serializable data object LoanHistoryKey : NavKey
```

### Flow

```
App start → HomeKey
  ├── Tap "Compound Interest" → CalculatorKey  (existing bottom nav: Calculator | History)
  └── Tap "Unsecured Loan"   → LoanKey        (new bottom nav: Loan | Loan History)
                                              ← back arrow returns to HomeKey
```

---

## Home Screen (`:app`)

**File:** `app/src/main/java/com/example/assetmanagement/ui/home/HomeScreen.kt`

Two tappable cards, full-width, vertically stacked:

| Card | Colour | Tap action |
|------|--------|-----------|
| 📊 Compound Interest — "Grow your investment over time" | Blue tint | `backStack.add(CalculatorKey())` |
| 💳 Unsecured Loan — "Calculate monthly repayments" | Pink tint | `backStack.add(LoanKey)` |

No ViewModel needed — purely navigational.

---

## Loan Calculator Feature (`:loan`)

### Inputs

| Field | Type | Validation |
|-------|------|-----------|
| Loan Amount | `Double` (NT$) | > 0 |
| Annual Interest Rate | `Double` (%) | > 0 |
| Loan Term | `Int` (months) | 1–360 |

### Formula — Standard Amortization (Reducing Balance)

```
r = annualRate / 100 / 12
M = P × [r(1+r)^n] / [(1+r)^n − 1]
```

- `M` = monthly payment
- `P` = loan amount
- `r` = monthly interest rate
- `n` = term in months

### Results

| Output | Calculation |
|--------|------------|
| Monthly Payment | `M` (rounded to 2 dp) |
| Total Repayment | `M × n` |
| Total Interest | `Total Repayment − P` |

### Balance Reduction Chart

Vico line chart (same library as compound calculator). X-axis = month number (0–n), Y-axis = remaining balance. Remaining balance per month:

```
balance(0) = P
balance(m) = balance(m-1) × (1 + r) − M
```

### Domain Models

```kotlin
data class LoanInput(
    val loanAmount: Double,
    val annualRate: Double,
    val termMonths: Int
)

data class LoanResult(
    val monthlyPayment: Double,
    val totalRepayment: Double,
    val totalInterest: Double,
    val schedule: List<LoanScheduleEntry>
)

data class LoanScheduleEntry(val month: Int, val remainingBalance: Double)

data class LoanHistoryItem(
    val id: Long,
    val loanAmount: Double,
    val annualRate: Double,
    val termMonths: Int,
    val monthlyPayment: Double,
    val totalInterest: Double,
    val savedAt: Long
)
```

### LoanScreen UI

- 3 input fields (Loan Amount, Annual Rate, Term)
- Calculate button
- Results card (Monthly Payment, Total Repayment, Total Interest) — shown after calculation
- `LoanChartContent` (balance reduction chart) — shown after calculation
- Calculation auto-saved to Room on each successful compute
- Bottom nav: Loan Calculator | Loan History

### LoanHistoryScreen UI

- LazyColumn of `LoanHistoryItem` cards showing: amount, rate, term, monthly payment
- Tap to prefill inputs back in `LoanScreen` — prefill is handled internally: `LoanHistoryScreen` receives an `onItemClick: (LoanHistoryItem) -> Unit` callback; `LoanNavigation` switches the back stack to a new `LoanCalculatorKey` carrying the prefill fields (same pattern as compound history → `CalculatorKey`)
- Swipe-to-delete (matches compound history pattern)

---

## Data Layer (`:loan`)

- **`LoanEntity`** — Room entity mirroring `LoanHistoryItem` fields + `savedAt` timestamp
- **`LoanDao`** — `insertLoan()`, `getAllLoans(): Flow<List<LoanEntity>>`, `deleteLoan(id)`
- **`LoanDatabase`** — `@Database(entities = [LoanEntity::class], version = 1)`
- **`LoanRepositoryImpl`** — implements `LoanRepository`, injected via Hilt
- **`LoanModule`** — `@Module @InstallIn(SingletonComponent)` providing `LoanDatabase` and `LoanRepository`

---

## Testing

Unit tests in `:loan` module mirroring existing calculator test patterns:

| Test class | Coverage |
|------------|---------|
| `ComputeLoanUseCaseTest` | Formula correctness, zero interest edge case, 1-month term |
| `LoanViewModelTest` | Initial state, calculate action, prefill from history |
| `LoanHistoryViewModelTest` | Loading, empty, success states |
| `SaveLoanCalculationUseCaseTest` | Delegates to repository |
| `GetLoanHistoryUseCaseTest` | Returns Flow from repository |

---

## Changes to `:app`

1. **`NavigationKeys.kt`** — add `HomeKey`, `LoanKey`
2. **`Navigation.kt`** — start at `HomeKey`; add `entry<HomeKey>` and `entry<LoanKey>` (renders `LoanNavigation` composable from `:loan`)
3. **`HomeScreen.kt`** (new) — two calculator cards
4. **`settings.gradle.kts`** — include `":loan"`
5. **`app/build.gradle.kts`** — add `implementation(project(":loan"))`
6. **`.gitignore`** — add `.superpowers/`
