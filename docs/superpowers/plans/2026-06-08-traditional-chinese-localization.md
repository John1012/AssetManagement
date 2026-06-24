# Traditional Chinese Localization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the active calculator UI render in Traditional Chinese when the device system language is Traditional Chinese (any region), and English otherwise, by externalizing all hardcoded UI strings into resources and adding a `values-b+zh+Hant` translation.

**Architecture:** Externalize every user-facing string in the calculator feature into the default `res/values/strings.xml` (English), reference them with `stringResource(...)`, then add a parallel `res/values-b+zh+Hant/strings.xml`. Android's resource resolution picks the Traditional Chinese file automatically from the system locale — no in-app picker, no manifest change.

**Tech Stack:** Android resource qualifiers (BCP-47 script qualifier `b+zh+Hant`), Jetpack Compose `androidx.compose.ui.res.stringResource`, Gradle (`./gradlew`).

---

## Notes for the implementer

- This is a UI string-externalization refactor. There are **no new JUnit tests** — string resolution is Android framework behavior. Each task is verified by **compiling** (`./gradlew :app:compileDebugKotlin`), and the whole feature is verified by a build + lint + manual locale switch at the end (Task 7).
- `:app:compileDebugKotlin` runs `processDebugResources`, which generates the `R` class. So a string typo or missing `R.string.*` reference will fail the compile — that is our per-task safety net.
- Every `stringResource(...)` call must be inside a `@Composable` function (all our call sites already are).
- Two imports are needed in each Kotlin UI file you touch:
  ```kotlin
  import androidx.compose.ui.res.stringResource
  import com.example.assetmanagement.R
  ```
- Keep the literal `NT$` currency mark in both languages (it is the Taiwan-dollar symbol, not prose).
- Run all `git` and `./gradlew` commands from the repo root: `/Users/ddtddt55/AndroidStudioProjects/AssetManagement`.

---

### Task 1: Create the default (English) string resources

**Files:**
- Modify: `app/src/main/res/values/strings.xml`

This file currently contains only `app_name`. Replace its contents with the full English key set. `app_name` keeps the value "Asset Management" (an identical copy will go in the zh file in Task 6, so the displayed app name stays "Asset Management" everywhere and `MissingTranslation` lint stays clean).

- [ ] **Step 1: Replace `app/src/main/res/values/strings.xml` with the full key set**

```xml
<resources>
    <string name="app_name">Asset Management</string>

    <!-- Bottom navigation -->
    <string name="nav_calculator">Calculator</string>
    <string name="nav_history">History</string>

    <!-- Calculator screen -->
    <string name="calculator_title">Compound Calculator</string>
    <string name="field_initial_fund">Initial Fund (NT$)</string>
    <string name="field_annual_roi">Annual ROI (%)</string>
    <string name="field_duration">Duration (years, 1–100)</string>
    <string name="field_monthly_contribution">Monthly Contribution (NT$, optional)</string>
    <string name="action_calculate">Calculate</string>
    <string name="calculator_error">Error: %1$s</string>

    <!-- Result summary -->
    <string name="results_title">Results</string>
    <string name="result_final_value">Final Value</string>
    <string name="result_total_contributed">Total Contributed</string>
    <string name="result_total_interest">Total Interest Earned</string>
    <string name="result_row">%1$s: NT$%2$s</string>

    <!-- Chart legend -->
    <string name="legend_with_dca">With DCA</string>
    <string name="legend_without_dca">Without DCA</string>
    <string name="legend_contributed">Contributed</string>

    <!-- History screen -->
    <string name="history_title">History</string>
    <string name="history_loading">Loading…</string>
    <string name="history_empty">No calculations saved yet.</string>
    <string name="history_delete">Delete</string>
    <string name="history_summary">NT$%1$s  ROI: %2$s%%  %3$dyr</string>
    <string name="history_final">Final: NT$%1$s</string>
</resources>
```

- [ ] **Step 2: Verify resources compile**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL` (the new keys are valid XML and generate `R.string.*` entries; no callers reference them yet, which is fine).

- [ ] **Step 3: Commit**

```bash
git add app/src/main/res/values/strings.xml
git commit -m "feat: externalize calculator UI strings into English string resources"
```

---

### Task 2: Replace hardcoded strings in Navigation.kt

**Files:**
- Modify: `app/src/main/java/com/example/assetmanagement/Navigation.kt`

- [ ] **Step 1: Add the two imports**

After the existing import block (the last import is `import com.example.assetmanagement.calculator.ui.history.HistoryScreen` on line 18), add:

```kotlin
import androidx.compose.ui.res.stringResource
import com.example.assetmanagement.R
```

- [ ] **Step 2: Replace the two nav labels**

Change line 32 from:

```kotlin
                    label = { Text("Calculator") }
```

to:

```kotlin
                    label = { Text(stringResource(R.string.nav_calculator)) }
```

Change line 38 from:

```kotlin
                    label = { Text("History") }
```

to:

```kotlin
                    label = { Text(stringResource(R.string.nav_history)) }
```

- [ ] **Step 3: Verify it compiles**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/assetmanagement/Navigation.kt
git commit -m "feat: use string resources for bottom nav labels"
```

---

### Task 3: Replace hardcoded strings in CalculatorScreen.kt

**Files:**
- Modify: `app/src/main/java/com/example/assetmanagement/calculator/ui/calculator/CalculatorScreen.kt`

- [ ] **Step 1: Add the two imports**

After line 29 (`import com.example.assetmanagement.calculator.domain.model.CalculationResult`), add:

```kotlin
import androidx.compose.ui.res.stringResource
import com.example.assetmanagement.R
```

- [ ] **Step 2: Replace the title and field labels**

Line 66:
```kotlin
        Text("Compound Calculator", style = MaterialTheme.typography.headlineSmall)
```
→
```kotlin
        Text(stringResource(R.string.calculator_title), style = MaterialTheme.typography.headlineSmall)
```

Line 72:
```kotlin
            label = { Text("Initial Fund (NT\$)") },
```
→
```kotlin
            label = { Text(stringResource(R.string.field_initial_fund)) },
```

Line 80:
```kotlin
            label = { Text("Annual ROI (%)") },
```
→
```kotlin
            label = { Text(stringResource(R.string.field_annual_roi)) },
```

Line 88:
```kotlin
            label = { Text("Duration (years, 1–100)") },
```
→
```kotlin
            label = { Text(stringResource(R.string.field_duration)) },
```

Line 96:
```kotlin
            label = { Text("Monthly Contribution (NT\$, optional)") },
```
→
```kotlin
            label = { Text(stringResource(R.string.field_monthly_contribution)) },
```

- [ ] **Step 3: Replace the Calculate button and error text**

Line 114:
```kotlin
        ) { Text("Calculate") }
```
→
```kotlin
        ) { Text(stringResource(R.string.action_calculate)) }
```

Line 127:
```kotlin
            is CalculatorUiState.Error -> Text("Error: ${s.message}", color = MaterialTheme.colorScheme.error)
```
→
```kotlin
            is CalculatorUiState.Error -> Text(stringResource(R.string.calculator_error, s.message), color = MaterialTheme.colorScheme.error)
```

- [ ] **Step 4: Replace the result summary labels**

Lines 137–141:
```kotlin
            Text("Results", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            ResultRow("Final Value", result.finalValue)
            ResultRow("Total Contributed", result.totalContributed)
            ResultRow("Total Interest Earned", result.totalInterestEarned)
```
→
```kotlin
            Text(stringResource(R.string.results_title), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            ResultRow(stringResource(R.string.result_final_value), result.finalValue)
            ResultRow(stringResource(R.string.result_total_contributed), result.totalContributed)
            ResultRow(stringResource(R.string.result_total_interest), result.totalInterestEarned)
```

- [ ] **Step 5: Update the `ResultRow` body to use the format resource**

Lines 146–150:
```kotlin
@Composable
private fun ResultRow(label: String, value: Double) {
    val formatted = NumberFormat.getNumberInstance(Locale.TAIWAN).format(value.toLong())
    Text("$label: NT\$$formatted")
}
```
→
```kotlin
@Composable
private fun ResultRow(label: String, value: Double) {
    val formatted = NumberFormat.getNumberInstance(Locale.TAIWAN).format(value.toLong())
    Text(stringResource(R.string.result_row, label, formatted))
}
```

Note: `label` is already a resolved string (passed in from Step 4), so it goes straight into `%1$s`; `formatted` is the number string for `%2$s`.

- [ ] **Step 6: Verify it compiles**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/example/assetmanagement/calculator/ui/calculator/CalculatorScreen.kt
git commit -m "feat: use string resources across the calculator screen"
```

---

### Task 4: Replace hardcoded strings in HistoryScreen.kt

**Files:**
- Modify: `app/src/main/java/com/example/assetmanagement/calculator/ui/history/HistoryScreen.kt`

- [ ] **Step 1: Add the two imports**

After line 27 (`import com.example.assetmanagement.calculator.domain.model.HistoryItem`), add:

```kotlin
import androidx.compose.ui.res.stringResource
import com.example.assetmanagement.R
```

- [ ] **Step 2: Replace the title and state strings**

Lines 42, 45, 46:
```kotlin
        Text("History", style = MaterialTheme.typography.headlineSmall)
```
→
```kotlin
        Text(stringResource(R.string.history_title), style = MaterialTheme.typography.headlineSmall)
```

```kotlin
            HistoryUiState.Loading -> Text("Loading...")
            HistoryUiState.Empty -> Text("No calculations saved yet.")
```
→
```kotlin
            HistoryUiState.Loading -> Text(stringResource(R.string.history_loading))
            HistoryUiState.Empty -> Text(stringResource(R.string.history_empty))
```

- [ ] **Step 3: Replace the Delete background label**

Line 77:
```kotlin
            ) { Text("Delete", color = MaterialTheme.colorScheme.onError) }
```
→
```kotlin
            ) { Text(stringResource(R.string.history_delete), color = MaterialTheme.colorScheme.onError) }
```

- [ ] **Step 4: Replace the history item rows with format resources**

Lines 91–92:
```kotlin
            Text("NT\$${numFmt.format(item.initialFund.toLong())}  ROI: ${item.annualROI}%  ${item.durationYears}yr")
            Text("Final: NT\$${numFmt.format(item.finalValue.toLong())}", style = MaterialTheme.typography.titleSmall)
```
→
```kotlin
            Text(
                stringResource(
                    R.string.history_summary,
                    numFmt.format(item.initialFund.toLong()),
                    item.annualROI,
                    item.durationYears
                )
            )
            Text(
                stringResource(R.string.history_final, numFmt.format(item.finalValue.toLong())),
                style = MaterialTheme.typography.titleSmall
            )
```

Note on argument types for `history_summary` (`NT$%1$s  ROI: %2$s%%  %3$dyr`): `numFmt.format(...)` is a `String` for `%1$s`; `item.annualROI` is a `Double` formatted by `%2$s` (renders e.g. `5.0`, matching the current output); `item.durationYears` is an `Int` for `%3$d`.

- [ ] **Step 5: Verify it compiles**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/example/assetmanagement/calculator/ui/history/HistoryScreen.kt
git commit -m "feat: use string resources across the history screen"
```

---

### Task 5: Replace hardcoded legend strings in GrowthChartContent.kt

**Files:**
- Modify: `app/src/main/java/com/example/assetmanagement/calculator/ui/calculator/GrowthChartContent.kt`

The three legend labels are passed at the `LegendItem(...)` call sites (currently `LegendItem("With DCA", dcaColor)`, `LegendItem("Without DCA", noDcaColor)`, `LegendItem("Contributed", contributedColor)`).

- [ ] **Step 1: Add the two imports**

Add to the import block (alongside the other `androidx.compose.ui.*` imports):

```kotlin
import androidx.compose.ui.res.stringResource
import com.example.assetmanagement.R
```

- [ ] **Step 2: Replace the three legend labels**

```kotlin
                LegendItem("With DCA", dcaColor)
                LegendItem("Without DCA", noDcaColor)
                LegendItem("Contributed", contributedColor)
```
→
```kotlin
                LegendItem(stringResource(R.string.legend_with_dca), dcaColor)
                LegendItem(stringResource(R.string.legend_without_dca), noDcaColor)
                LegendItem(stringResource(R.string.legend_contributed), contributedColor)
```

(`LegendItem` is a `@Composable` and its `label: String` parameter receives the already-resolved string — no change to `LegendItem` itself.)

- [ ] **Step 3: Verify it compiles**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/assetmanagement/calculator/ui/calculator/GrowthChartContent.kt
git commit -m "feat: use string resources for growth chart legend"
```

---

### Task 6: Add the Traditional Chinese translation

**Files:**
- Create: `app/src/main/res/values-b+zh+Hant/strings.xml`

Create the directory and file. It must contain a translation for **every** key in the default file (Task 1), with identical placeholder names/order so positional format args stay valid. `app_name` is an identical copy ("Asset Management") so the app name is unchanged and `MissingTranslation` lint stays clean. Unit suffixes like "yr"→"年" are translated inside the format strings.

- [ ] **Step 1: Create `app/src/main/res/values-b+zh+Hant/strings.xml`**

```xml
<resources>
    <string name="app_name">Asset Management</string>

    <!-- Bottom navigation -->
    <string name="nav_calculator">計算機</string>
    <string name="nav_history">歷史紀錄</string>

    <!-- Calculator screen -->
    <string name="calculator_title">複利計算機</string>
    <string name="field_initial_fund">初始本金（NT$）</string>
    <string name="field_annual_roi">年報酬率（%）</string>
    <string name="field_duration">投資年期（1–100 年）</string>
    <string name="field_monthly_contribution">每月定期投入（NT$，選填）</string>
    <string name="action_calculate">計算</string>
    <string name="calculator_error">錯誤：%1$s</string>

    <!-- Result summary -->
    <string name="results_title">計算結果</string>
    <string name="result_final_value">最終價值</string>
    <string name="result_total_contributed">總投入金額</string>
    <string name="result_total_interest">總獲利</string>
    <string name="result_row">%1$s：NT$%2$s</string>

    <!-- Chart legend -->
    <string name="legend_with_dca">含定期定額</string>
    <string name="legend_without_dca">未定期定額</string>
    <string name="legend_contributed">累計投入</string>

    <!-- History screen -->
    <string name="history_title">歷史紀錄</string>
    <string name="history_loading">載入中…</string>
    <string name="history_empty">尚未儲存任何計算。</string>
    <string name="history_delete">刪除</string>
    <string name="history_summary">NT$%1$s  報酬率：%2$s%%  %3$d 年</string>
    <string name="history_final">最終：NT$%1$s</string>
</resources>
```

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/res/values-b+zh+Hant/strings.xml
git commit -m "feat: add Traditional Chinese (b+zh+Hant) string translations"
```

---

### Task 7: Full verification

**Files:** none (verification only)

- [ ] **Step 1: Full build**

Run: `./gradlew assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 2: Lint check for hardcoded text and missing translations**

Run: `./gradlew :app:lintDebug`
Expected: `BUILD SUCCESSFUL`. Then open `app/build/reports/lint-results-debug.html` and confirm:
- No new `HardcodedText` issues in `CalculatorScreen.kt`, `HistoryScreen.kt`, `Navigation.kt`, or `GrowthChartContent.kt`.
- No `MissingTranslation` issues (every default key has a `b+zh+Hant` counterpart).

If lint flags a pre-existing `HardcodedText` issue in legacy files (`MainScreen.kt`), leave it — legacy scaffolding is out of scope per the spec.

- [ ] **Step 3: Run existing unit tests (regression guard)**

Run: `./gradlew testDebugUnitTest`
Expected: `BUILD SUCCESSFUL` — string externalization does not touch logic, so all existing tests still pass.

- [ ] **Step 4: Manual locale verification**

On an emulator or device (API 24+):
1. Install: `./gradlew installDebug`.
2. Set system language to **繁體中文（台灣）** in Settings → System → Languages.
3. Open the app. Confirm Traditional Chinese on: bottom nav (計算機 / 歷史紀錄), calculator title and all four field labels, the 計算 button, the Results card labels, the chart legend (含定期定額 / 未定期定額 / 累計投入), and the History screen (title, empty/loading text, item rows, swipe-to-delete 刪除).
4. (Optional) Switch to **繁體中文（香港）** and confirm it still shows Traditional Chinese — proves the `b+zh+Hant` script qualifier matches non-TW regions.
5. Set system language back to **English**. Confirm the entire UI falls back to English.

- [ ] **Step 5: Final confirmation**

No code commit in this task. If any check fails, return to the relevant earlier task, fix, and re-verify.

---

## Self-Review Notes

- **Spec coverage:** §1 externalize active UI strings → Tasks 1–5; §2 composite/format resources → `result_row`, `history_summary`, `history_final`, `calculator_error` (Tasks 1, 3, 4); §3 ViewModel strings (UI wrapper only, raw message untouched) → `calculator_error` in Task 3 Step 3; §4 `values-b+zh+Hant` translation → Task 6; §5 no manifest change → respected (no manifest task); §6 testing (build, lint, manual, existing units) → Task 7. All success criteria map to Task 7 checks.
- **Out-of-scope respected:** legacy `MainScreen.kt`/`DataRepository` strings untouched; no in-app picker; no Simplified Chinese; `NT$` and date formats unchanged.
- **Type consistency:** `ResultRow(label: String, value: Double)` signature unchanged; `stringResource(R.string.result_row, label, formatted)` passes `String, String`. `history_summary` receives `String, Double, Int` matching `%1$s/%2$s/%3$d`. Key names are identical between Task 1 and Task 6.
