# Traditional Chinese Localization (System-Driven) — Design Spec

**Date:** 2026-06-08
**Status:** Approved, pending implementation plan

## Goal

The app displays **Traditional Chinese** when the device's system language resolves to
Traditional Chinese script (any region — Taiwan, Hong Kong, Macau), and **English** otherwise.

Selection is **purely system-driven** — there is no in-app language picker. This relies on
Android's default resource-qualifier resolution: the correct `strings.xml` is chosen
automatically from the device locale.

## Scope

**In scope:** all user-facing strings in the active calculator feature.

**Out of scope:**
- Legacy/demo scaffolding strings (`MainScreen.kt` `"Hello $name!"`, `DataRepository` `"Android"`).
- In-app language switcher / per-app language override.
- Simplified Chinese.
- Currency or number reformatting (the literal `NT$` mark is kept; dates already use `Locale.getDefault()`).

## Approach

### 1. Externalize active UI strings (default `res/values/strings.xml`, English)

English is the default/fallback locale. Every user-facing string in the calculator feature
is moved from hardcoded Compose literals into `res/values/strings.xml` and referenced via
`stringResource(...)`.

Strings to externalize, by file:

| File | Strings |
|------|---------|
| `Navigation.kt` | "Calculator", "History" |
| `CalculatorScreen.kt` | "Compound Calculator", "Initial Fund (NT$)", "Annual ROI (%)", "Duration (years, 1–100)", "Monthly Contribution (NT$, optional)", "Calculate", "Results", "Final Value", "Total Contributed", "Total Interest Earned", error wrapper |
| `HistoryScreen.kt` | "History", "Loading...", "No calculations saved yet.", "Delete", item summary row, "Final:" row |
| `GrowthChartContent.kt` | "With DCA", "Without DCA", "Contributed" |

### 2. Composite / formatted strings → parameterized resources

Strings built via Kotlin interpolation become format resources with positional placeholders.
Examples (final names decided during implementation):

```xml
<string name="history_summary">NT$%1$s  ROI: %2$s%%  %3$dyr</string>
<string name="history_final">Final: NT$%1$s</string>
<string name="result_row">%1$s: NT$%2$s</string>
<string name="calculator_error">Error: %1$s</string>
```

Accessed via `stringResource(R.string.history_summary, fund, roi, years)`.

The `NT$` symbol stays literal in both English and Traditional Chinese (it is the
Taiwan-dollar currency mark, not translatable prose). `%%` escapes a literal percent sign.

### 3. ViewModel-layer strings

`CalculatorViewModel` produces a fallback error message (`e.message ?: "Calculation failed"`)
in a non-Composable context that has no `Context`.

**Decision:** keep the raw exception/fallback message as-is in the ViewModel; localize only
the **UI-layer wrapper** (`calculator_error` = "Error: %1$s") via `stringResource`. The raw
message is a developer-facing exception string; wrapping it in a localized frame is sufficient.

**Rejected alternative:** inject `@ApplicationContext Context` (or a string-resource provider)
into the ViewModel to localize the fallback. This adds DI plumbing and a Context dependency to
the ViewModel for a single rarely-seen fallback string — not worth it (YAGNI).

### 4. Traditional Chinese translation (`res/values-b+zh+Hant/strings.xml`)

Create `res/values-b+zh+Hant/strings.xml` containing a Traditional Chinese translation for
**every** key defined in the default `strings.xml`. Using the BCP-47 script qualifier
`b+zh+Hant` matches all Traditional Chinese system locales (TW/HK/Macau) with a single file,
and is future-proof against new regions.

Format-string resources keep identical placeholder order and count so positional arguments
remain valid across locales.

### 5. Manifest / build config

No manifest change required. `android:localeConfig` is only needed for the per-app language
picker, which is out of scope. Pure system-following works through default resource resolution.

`app_name` remains as-is unless a translated app name is desired (default: keep "Asset Management").

### 6. Testing & verification

- **Build:** `./gradlew assembleDebug` succeeds.
- **Lint:** no new `HardcodedText` warnings in calculator files; `MissingTranslation` clean
  (every default key has a `b+zh+Hant` counterpart).
- **Manual:** on an emulator/device, set system language to 繁體中文（台灣）→ confirm all
  calculator/History/chart-legend/nav strings render in Traditional Chinese. Set back to
  English → confirm fallback. Optionally test 繁體中文（香港）to confirm the script qualifier
  also matches.
- **Unit tests:** existing tests unaffected (string externalization does not change logic).
  No new unit tests required — string resolution is framework behavior, validated manually.

## Success Criteria

1. Every active calculator-feature string is sourced from `strings.xml` (no hardcoded UI text in those files).
2. `res/values-b+zh+Hant/strings.xml` exists with a translation for every default key.
3. With system language set to Traditional Chinese, the entire active UI displays in Traditional Chinese.
4. With any other system language, the UI displays in English.
5. Build and existing unit tests pass.
