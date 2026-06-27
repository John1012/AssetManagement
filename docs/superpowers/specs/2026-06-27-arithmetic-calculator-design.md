# Arithmetic Calculator — Design

**Date:** 2026-06-27
**Status:** Approved, pending implementation
**Branch:** `feat/arithmetic-calculator`

## Goal

Add a standard four-operation (+ − × ÷) calculator screen to the existing
AssetManagement Android app, as a third bottom-navigation tab. No history
persistence. Light Clean Architecture matching the project's existing style
(Navigation3 + Hilt + StateFlow + Compose).

## Decisions (locked)

- **Placement:** feature inside the existing app (not a separate app).
- **Scope:** a full calculator screen, not an inline utility.
- **History:** none. No Room, no repository, no usecase layer.
- **Buttons:** standard phone-calculator set — digits `0–9`, `.`,
  `+ − × ÷`, `=`, `C` (clear), `⌫` (backspace), `+/−` (toggle sign),
  `%` (percent).
- **Calculation behavior:** immediate / chained evaluation (phone-calculator
  style). `2 + 3 × 4 =` yields **20** (each operator first evaluates the
  pending operation). NOT mathematical precedence.
- **Architecture:** Method 1 — lightweight layered:
  pure-Kotlin `domain` engine + `ui` (ViewModel + Screen). No Android
  dependency in domain so it is unit-testable.

## Naming note

The existing `calculator` package is a **compound-growth investment
calculator** (`ComputeCompoundGrowthUseCase`, `YearlySnapshot`,
`GrowthChartContent`, Room history). To avoid a name clash, the new feature
lives in a separate `arithmetic` package. The existing package is left
untouched.

## Package structure

```
arithmetic/
├── domain/
│   ├── CalculatorState.kt      // current calculator state (pure data)
│   ├── CalculatorAction.kt     // user key presses (sealed interface)
│   └── CalculatorEngine.kt     // (state, action) -> state, pure logic
└── ui/
    ├── ArithmeticViewModel.kt  // Hilt + StateFlow, wraps the engine
    └── ArithmeticScreen.kt     // display area + button keypad
```

`CalculatorEngine` has no Android dependency → testable with plain JUnit.
The ViewModel converts key presses into actions, feeds them to the engine,
and pushes the returned state to a `StateFlow`.

## State & actions

**CalculatorState** — minimal state needed for display and computation:
- `display: String` — number currently shown on screen (default `"0"`).
- `accumulator: Double?` — the previously accumulated operand.
- `pendingOp: Operator?` — operator waiting to be applied (`+ − × ÷`).
- `isError: Boolean` — whether the calculator is in an error state.

**Operator** — enum/sealed: `Plus`, `Minus`, `Times`, `Divide`.

**CalculatorAction** — sealed interface of key presses:
- `Digit(value: Int)` (0–9)
- `Decimal`
- `Op(operator: Operator)`
- `Equals`
- `Clear`
- `Backspace`
- `ToggleSign`
- `Percent`

## Calculation behavior (immediate / chained)

- **Digit:** append to `display`. Handle leading zero (replace `"0"`), and
  allow only one decimal point.
- **Operator:** if a `pendingOp` already exists, first compute
  `accumulator (pendingOp) display`, put the result back into `display` and
  `accumulator`, then record the new operator. Otherwise move `display` into
  `accumulator`. This produces the `2 + 3 × 4 =` → `20` behavior.
  - Pressing operators consecutively (e.g. `5 + + 3`) keeps only the last
    operator; no spurious computation.
- **Equals:** compute `accumulator (pendingOp) display`, show the result,
  clear `pendingOp`. Equals with no pending op is a no-op.
- **Backspace (`⌫`):** remove the last character of `display`; if it becomes
  empty, reset to `"0"`.
- **ToggleSign (`+/−`):** flip the sign of the current `display`.
- **Percent (`%`):** divide the current `display` by 100.
- **Clear (`C`):** reset everything to the initial state (`display = "0"`).

## Error handling

- **Divide by zero:** `display = "Error"`, `isError = true`. While in error
  state, all keys except `C` are ignored; `C` returns to `"0"`.
- **Length cap:** `display` is capped (12 digits). Once at the cap, further
  digit presses are ignored.

## Navigation integration

- Add `ArithmeticKey : NavKey` in `NavigationKeys.kt`.
- In `Navigation.kt`, add a **third** bottom-navigation tab. Label `"Math"`
  (to avoid confusion with the existing `"Calculator"` compound-growth tab),
  icon e.g. `Icons.Default.Add`.
- The existing two tabs (Calculator, History) are not modified.

## Testing (TDD)

Pure-logic unit tests on `CalculatorEngine` (plain JUnit). Key boundaries:
- `2 + 3 × 4 =` → `20` (chained evaluation)
- Consecutive operators (`5 + + 3`) keep only the last operator
- Divide by zero → `Error`; afterward only `C` is effective
- Decimal point cannot repeat; leading-zero handling
- `+/−` (toggle sign) and `%` (percent) behavior
- Backspace down to empty resets to `"0"`
- Length cap stops accepting digits

## Out of scope (YAGNI)

- History / persistence (no Room, repository, or usecase).
- Operator precedence / full expression parsing.
- Scientific functions, memory keys (M+, M−, MR).
- Landscape-specific layout.
