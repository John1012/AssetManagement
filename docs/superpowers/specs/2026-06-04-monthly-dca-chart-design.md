# Monthly DCA Comparison Chart — Design Spec

**Date:** 2026-06-04
**Status:** Approved

## Summary

Replace the existing `annualContribution` field with a `monthlyContribution` field (定期定額), and update the growth chart to display a three-line comparison when a monthly contribution is entered: growth with DCA, growth without DCA (initial fund only), and total amount contributed.

---

## Domain Layer

### `CalculationInput`
- Remove `annualContribution: Double`
- Add `monthlyContribution: Double = 0.0`

### `CalculationResult`
- Keep `yearlySnapshots: List<YearlySnapshot>` — represents growth **with** DCA
- Add `baselineSnapshots: List<YearlySnapshot>?` — represents growth **without** DCA (initial fund only, same `annualROI`); `null` when `monthlyContribution == 0`

### `ComputeCompoundGrowthUseCase`
- Compounding remains **annual** (once per year)
- Annual contribution = `monthlyContribution × 12`, added at the start of each year before compounding
- Formula per year: `value = (value + monthlyContribution * 12) * (1 + annualROI / 100)`
- `totalContributed` = `initialFund + monthlyContribution × 12 × durationYears`
- Baseline is computed with the same logic but `monthlyContribution = 0`
- Both `yearlySnapshots` and `baselineSnapshots` are returned in a single `CalculationResult`

### `YearlySnapshot`
- No changes required

---

## UI Layer

### `CalculatorScreen`
- Replace the "Annual Contribution (NT\$, optional)" field with **"Monthly Contribution (NT\$, optional)"**
- Pass `monthlyContribution` to `CalculationInput`
- Update `prefillContribution` parameter to represent monthly value

### `GrowthChartContent`
- Add parameter `baselineSnapshots: List<YearlySnapshot>?`
- When `baselineSnapshots != null` (monthly contribution > 0): display **three lines**
  1. With DCA — `yearlySnapshots.totalValue`
  2. Without DCA — `baselineSnapshots.totalValue`
  3. Total Contributed — `yearlySnapshots.totalContributed`
- When `baselineSnapshots == null` (monthly contribution = 0): display original **two lines**
  1. Total Value — `yearlySnapshots.totalValue`
  2. Total Contributed — `yearlySnapshots.totalContributed`
- Add a legend below the chart labelling each line's colour

### `CalculatorViewModel`
- No structural changes; `CalculatorUiState.ShowingResult` already carries `CalculationResult` which now includes `baselineSnapshots`
- The UI reads `baselineSnapshots` from the result to decide chart mode

---

## Data Layer

### `CalculationEntity`
- Rename column `annualContribution` → `monthlyContribution`
- Bump Room database version: **1 → 2**

### Migration Strategy
- Use `fallbackToDestructiveMigration()` — old records store annual values; reinterpreting them as monthly would produce incorrect prefill data. The app is in early development (`exportSchema = false`), so clearing history is acceptable.

### `CalculationRepositoryImpl`
- Map `input.monthlyContribution` when saving the entity

### `HistoryItem`
- Rename `annualContribution` → `monthlyContribution`

### `Navigation.kt`
- Update `prefillContribution = item.annualContribution` → `item.monthlyContribution`

---

## Affected Files

| File | Change |
|------|--------|
| `domain/model/CalculationInput.kt` | Replace field |
| `domain/model/CalculationResult.kt` | Add `baselineSnapshots` |
| `domain/model/HistoryItem.kt` | Rename field |
| `domain/usecase/ComputeCompoundGrowthUseCase.kt` | Monthly → annual conversion, compute baseline |
| `data/local/CalculationEntity.kt` | Rename column, bump version |
| `data/local/CalculationDatabase.kt` | Version 2, add destructive migration |
| `data/CalculationRepositoryImpl.kt` | Use `monthlyContribution` |
| `ui/calculator/CalculatorScreen.kt` | Update field label and input mapping |
| `ui/calculator/GrowthChartContent.kt` | Three-line comparison chart + legend |
| `Navigation.kt` | Update prefill mapping |

---

## Out of Scope

- Changing the compounding frequency to monthly
- Adding a legend for the existing two-line mode
- Modifying the history card display format
