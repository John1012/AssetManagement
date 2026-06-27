# Arithmetic Calculator Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a standard four-operation (+ − × ÷) calculator screen as a third bottom-navigation tab in the AssetManagement app, with no history persistence.

**Architecture:** Lightweight layered. A pure-Kotlin `CalculatorEngine` (no Android deps) reduces `(state, action) -> state` for immediate/chained evaluation. A Hilt `ArithmeticViewModel` wraps the engine and exposes a `StateFlow<CalculatorState>`. A Compose `ArithmeticScreen` renders the display + keypad. A third `ArithmeticKey` tab wires it into the existing Navigation3 graph.

**Tech Stack:** Kotlin, Jetpack Compose (Material3), Navigation3, Hilt, StateFlow, JUnit 4.

## Global Constraints

- Package root: `com.example.assetmanagement` — new feature lives under `arithmetic/`.
- Do NOT touch the existing `calculator/` package (compound-growth feature) or its two nav tabs.
- New nav tab label: `"Math"` (NOT `"Calculator"`, to avoid clashing with the existing compound-growth tab).
- Evaluation model: immediate / chained (`2 + 3 × 4 =` → `20`). NO operator precedence.
- No Room, no repository, no usecase layer (history is out of scope).
- Tests: JUnit 4, `org.junit.Assert.assertEquals`, backtick test method names (match existing style).
- Run unit tests with: `./gradlew testDebugUnitTest --tests "<fqcn>"`.

---

### Task 1: Domain types + CalculatorEngine

**Files:**
- Create: `app/src/main/java/com/example/assetmanagement/arithmetic/domain/CalculatorState.kt`
- Create: `app/src/main/java/com/example/assetmanagement/arithmetic/domain/CalculatorAction.kt`
- Create: `app/src/main/java/com/example/assetmanagement/arithmetic/domain/CalculatorEngine.kt`
- Test: `app/src/test/java/com/example/assetmanagement/arithmetic/domain/CalculatorEngineTest.kt`

**Interfaces:**
- Produces:
  - `enum class Operator(val symbol: String) { Plus("+"), Minus("−"), Times("×"), Divide("÷") }`
  - `data class CalculatorState(display: String = "0", accumulator: Double? = null, pendingOp: Operator? = null, overwrite: Boolean = false, isError: Boolean = false)`
  - `sealed interface CalculatorAction` with: `Digit(value: Int)`, `Decimal`, `Op(operator: Operator)`, `Equals`, `Clear`, `Backspace`, `ToggleSign`, `Percent`
  - `class CalculatorEngine @Inject constructor()` with `fun reduce(state: CalculatorState, action: CalculatorAction): CalculatorState`

- [ ] **Step 1: Create the state + operator types**

Create `CalculatorState.kt`:

```kotlin
package com.example.assetmanagement.arithmetic.domain

enum class Operator(val symbol: String) {
    Plus("+"),
    Minus("−"),
    Times("×"),
    Divide("÷");

    fun apply(a: Double, b: Double): Double = when (this) {
        Plus -> a + b
        Minus -> a - b
        Times -> a * b
        Divide -> a / b
    }
}

data class CalculatorState(
    val display: String = "0",
    val accumulator: Double? = null,
    val pendingOp: Operator? = null,
    val overwrite: Boolean = false,
    val isError: Boolean = false
)
```

- [ ] **Step 2: Create the action type**

Create `CalculatorAction.kt`:

```kotlin
package com.example.assetmanagement.arithmetic.domain

sealed interface CalculatorAction {
    data class Digit(val value: Int) : CalculatorAction
    data object Decimal : CalculatorAction
    data class Op(val operator: Operator) : CalculatorAction
    data object Equals : CalculatorAction
    data object Clear : CalculatorAction
    data object Backspace : CalculatorAction
    data object ToggleSign : CalculatorAction
    data object Percent : CalculatorAction
}
```

- [ ] **Step 3: Write the failing engine tests**

Create `CalculatorEngineTest.kt`:

```kotlin
package com.example.assetmanagement.arithmetic.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorEngineTest {

    private val engine = CalculatorEngine()

    private fun run(vararg actions: CalculatorAction): CalculatorState {
        var state = CalculatorState()
        for (action in actions) state = engine.reduce(state, action)
        return state
    }

    @Test
    fun `digits append and replace leading zero`() {
        val state = run(CalculatorAction.Digit(1), CalculatorAction.Digit(2))
        assertEquals("12", state.display)
    }

    @Test
    fun `decimal point cannot repeat`() {
        val state = run(
            CalculatorAction.Digit(1),
            CalculatorAction.Decimal,
            CalculatorAction.Digit(5),
            CalculatorAction.Decimal,
            CalculatorAction.Digit(2)
        )
        assertEquals("1.52", state.display)
    }

    @Test
    fun `chained evaluation is immediate not precedence`() {
        val state = run(
            CalculatorAction.Digit(2),
            CalculatorAction.Op(Operator.Plus),
            CalculatorAction.Digit(3),
            CalculatorAction.Op(Operator.Times),
            CalculatorAction.Digit(4),
            CalculatorAction.Equals
        )
        assertEquals("20", state.display)
    }

    @Test
    fun `consecutive operators keep only the last`() {
        val state = run(
            CalculatorAction.Digit(5),
            CalculatorAction.Op(Operator.Plus),
            CalculatorAction.Op(Operator.Minus),
            CalculatorAction.Digit(3),
            CalculatorAction.Equals
        )
        assertEquals("2", state.display)
    }

    @Test
    fun `simple division`() {
        val state = run(
            CalculatorAction.Digit(8),
            CalculatorAction.Op(Operator.Divide),
            CalculatorAction.Digit(2),
            CalculatorAction.Equals
        )
        assertEquals("4", state.display)
    }

    @Test
    fun `divide by zero enters error state`() {
        val state = run(
            CalculatorAction.Digit(5),
            CalculatorAction.Op(Operator.Divide),
            CalculatorAction.Digit(0),
            CalculatorAction.Equals
        )
        assertEquals("Error", state.display)
        assertTrue(state.isError)
    }

    @Test
    fun `only clear is honored while in error state`() {
        var state = run(
            CalculatorAction.Digit(5),
            CalculatorAction.Op(Operator.Divide),
            CalculatorAction.Digit(0),
            CalculatorAction.Equals
        )
        state = engine.reduce(state, CalculatorAction.Digit(7))
        assertEquals("Error", state.display)
        state = engine.reduce(state, CalculatorAction.Clear)
        assertEquals("0", state.display)
        assertFalse(state.isError)
    }

    @Test
    fun `toggle sign flips current display`() {
        val state = run(CalculatorAction.Digit(9), CalculatorAction.ToggleSign)
        assertEquals("-9", state.display)
    }

    @Test
    fun `percent divides current display by 100`() {
        val state = run(
            CalculatorAction.Digit(5),
            CalculatorAction.Digit(0),
            CalculatorAction.Percent
        )
        assertEquals("0.5", state.display)
    }

    @Test
    fun `backspace removes last char and bottoms out at zero`() {
        var state = run(CalculatorAction.Digit(1), CalculatorAction.Digit(2))
        state = engine.reduce(state, CalculatorAction.Backspace)
        assertEquals("1", state.display)
        state = engine.reduce(state, CalculatorAction.Backspace)
        assertEquals("0", state.display)
    }

    @Test
    fun `digit length is capped at 12`() {
        var state = CalculatorState()
        repeat(15) { state = engine.reduce(state, CalculatorAction.Digit(9)) }
        assertEquals("999999999999", state.display)
        assertEquals(12, state.display.length)
    }

    @Test
    fun `clear resets to initial state`() {
        val state = run(
            CalculatorAction.Digit(7),
            CalculatorAction.Op(Operator.Plus),
            CalculatorAction.Digit(3),
            CalculatorAction.Clear
        )
        assertEquals("0", state.display)
        assertEquals(null, state.pendingOp)
        assertEquals(null, state.accumulator)
    }
}
```

- [ ] **Step 4: Run the tests to verify they fail**

Run: `./gradlew testDebugUnitTest --tests "com.example.assetmanagement.arithmetic.domain.CalculatorEngineTest"`
Expected: FAIL — `CalculatorEngine` unresolved / compilation error.

- [ ] **Step 5: Implement CalculatorEngine**

Create `CalculatorEngine.kt`:

```kotlin
package com.example.assetmanagement.arithmetic.domain

import kotlin.math.abs
import javax.inject.Inject

class CalculatorEngine @Inject constructor() {

    companion object {
        private const val MAX_DIGITS = 12
    }

    fun reduce(state: CalculatorState, action: CalculatorAction): CalculatorState {
        if (state.isError && action !is CalculatorAction.Clear) return state
        return when (action) {
            is CalculatorAction.Digit -> inputDigit(state, action.value)
            CalculatorAction.Decimal -> inputDecimal(state)
            is CalculatorAction.Op -> applyOperator(state, action.operator)
            CalculatorAction.Equals -> applyEquals(state)
            CalculatorAction.Clear -> CalculatorState()
            CalculatorAction.Backspace -> backspace(state)
            CalculatorAction.ToggleSign -> toggleSign(state)
            CalculatorAction.Percent -> percent(state)
        }
    }

    private fun inputDigit(state: CalculatorState, digit: Int): CalculatorState {
        if (!state.overwrite && state.display.count { it.isDigit() } >= MAX_DIGITS) return state
        val newDisplay = when {
            state.overwrite -> digit.toString()
            state.display == "0" -> digit.toString()
            else -> state.display + digit
        }
        return state.copy(display = newDisplay, overwrite = false)
    }

    private fun inputDecimal(state: CalculatorState): CalculatorState {
        val newDisplay = when {
            state.overwrite -> "0."
            state.display.contains(".") -> return state
            else -> state.display + "."
        }
        return state.copy(display = newDisplay, overwrite = false)
    }

    private fun applyOperator(state: CalculatorState, op: Operator): CalculatorState {
        // Pressing operators back to back just swaps the pending operator.
        if (state.overwrite && state.pendingOp != null) {
            return state.copy(pendingOp = op)
        }
        val operand = state.display.toDouble()
        if (state.pendingOp != null && state.accumulator != null) {
            val result = compute(state.accumulator, state.pendingOp, operand)
                ?: return errorState()
            return state.copy(
                display = format(result),
                accumulator = result,
                pendingOp = op,
                overwrite = true
            )
        }
        return state.copy(accumulator = operand, pendingOp = op, overwrite = true)
    }

    private fun applyEquals(state: CalculatorState): CalculatorState {
        val op = state.pendingOp ?: return state
        val acc = state.accumulator ?: return state
        val operand = state.display.toDouble()
        val result = compute(acc, op, operand) ?: return errorState()
        return state.copy(
            display = format(result),
            accumulator = null,
            pendingOp = null,
            overwrite = true
        )
    }

    private fun backspace(state: CalculatorState): CalculatorState {
        var newDisplay = state.display.dropLast(1)
        if (newDisplay.isEmpty() || newDisplay == "-") newDisplay = "0"
        return state.copy(display = newDisplay, overwrite = false)
    }

    private fun toggleSign(state: CalculatorState): CalculatorState {
        if (state.display == "0") return state
        val newDisplay =
            if (state.display.startsWith("-")) state.display.drop(1)
            else "-${state.display}"
        return state.copy(display = newDisplay)
    }

    private fun percent(state: CalculatorState): CalculatorState {
        val value = state.display.toDouble() / 100
        return state.copy(display = format(value), overwrite = true)
    }

    private fun compute(a: Double, op: Operator, b: Double): Double? {
        if (op == Operator.Divide && b == 0.0) return null
        return op.apply(a, b)
    }

    private fun errorState() = CalculatorState(display = "Error", isError = true)

    private fun format(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Error"
        return if (value == value.toLong().toDouble() && abs(value) < 1e15) {
            value.toLong().toString()
        } else {
            value.toString()
        }
    }
}
```

- [ ] **Step 6: Run the tests to verify they pass**

Run: `./gradlew testDebugUnitTest --tests "com.example.assetmanagement.arithmetic.domain.CalculatorEngineTest"`
Expected: PASS — all 12 tests green.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/example/assetmanagement/arithmetic/domain app/src/test/java/com/example/assetmanagement/arithmetic/domain
git commit -m "feat: add arithmetic CalculatorEngine with chained evaluation"
```

---

### Task 2: ArithmeticViewModel

**Files:**
- Create: `app/src/main/java/com/example/assetmanagement/arithmetic/ui/ArithmeticViewModel.kt`
- Test: `app/src/test/java/com/example/assetmanagement/arithmetic/ui/ArithmeticViewModelTest.kt`

**Interfaces:**
- Consumes: `CalculatorEngine`, `CalculatorState`, `CalculatorAction`, `Operator` (Task 1).
- Produces:
  - `class ArithmeticViewModel @Inject constructor(engine: CalculatorEngine) : ViewModel()`
  - `val state: StateFlow<CalculatorState>`
  - `fun onAction(action: CalculatorAction)`

- [ ] **Step 1: Write the failing ViewModel test**

Create `ArithmeticViewModelTest.kt`:

```kotlin
package com.example.assetmanagement.arithmetic.ui

import com.example.assetmanagement.arithmetic.domain.CalculatorAction
import com.example.assetmanagement.arithmetic.domain.CalculatorEngine
import com.example.assetmanagement.arithmetic.domain.Operator
import org.junit.Assert.assertEquals
import org.junit.Test

class ArithmeticViewModelTest {

    private val viewModel = ArithmeticViewModel(CalculatorEngine())

    @Test
    fun `actions flow through the engine into state`() {
        viewModel.onAction(CalculatorAction.Digit(2))
        viewModel.onAction(CalculatorAction.Op(Operator.Plus))
        viewModel.onAction(CalculatorAction.Digit(3))
        viewModel.onAction(CalculatorAction.Op(Operator.Times))
        viewModel.onAction(CalculatorAction.Digit(4))
        viewModel.onAction(CalculatorAction.Equals)
        assertEquals("20", viewModel.state.value.display)
    }

    @Test
    fun `initial state shows zero`() {
        assertEquals("0", viewModel.state.value.display)
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew testDebugUnitTest --tests "com.example.assetmanagement.arithmetic.ui.ArithmeticViewModelTest"`
Expected: FAIL — `ArithmeticViewModel` unresolved.

- [ ] **Step 3: Implement ArithmeticViewModel**

Create `ArithmeticViewModel.kt`:

```kotlin
package com.example.assetmanagement.arithmetic.ui

import androidx.lifecycle.ViewModel
import com.example.assetmanagement.arithmetic.domain.CalculatorAction
import com.example.assetmanagement.arithmetic.domain.CalculatorEngine
import com.example.assetmanagement.arithmetic.domain.CalculatorState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ArithmeticViewModel @Inject constructor(
    private val engine: CalculatorEngine
) : ViewModel() {

    private val _state = MutableStateFlow(CalculatorState())
    val state: StateFlow<CalculatorState> = _state.asStateFlow()

    fun onAction(action: CalculatorAction) {
        _state.value = engine.reduce(_state.value, action)
    }
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew testDebugUnitTest --tests "com.example.assetmanagement.arithmetic.ui.ArithmeticViewModelTest"`
Expected: PASS — both tests green.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/assetmanagement/arithmetic/ui/ArithmeticViewModel.kt app/src/test/java/com/example/assetmanagement/arithmetic/ui/ArithmeticViewModelTest.kt
git commit -m "feat: add ArithmeticViewModel wrapping CalculatorEngine"
```

---

### Task 3: ArithmeticScreen (Compose keypad)

**Files:**
- Create: `app/src/main/java/com/example/assetmanagement/arithmetic/ui/ArithmeticScreen.kt`

**Interfaces:**
- Consumes: `ArithmeticViewModel`, `CalculatorState`, `CalculatorAction`, `Operator`.
- Produces: `@Composable fun ArithmeticScreen(viewModel: ArithmeticViewModel = hiltViewModel())`

No unit test (Compose UI). Verified by compilation + manual run in Task 4.

- [ ] **Step 1: Implement ArithmeticScreen**

Create `ArithmeticScreen.kt`:

```kotlin
package com.example.assetmanagement.arithmetic.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.assetmanagement.arithmetic.domain.CalculatorAction
import com.example.assetmanagement.arithmetic.domain.Operator

@Composable
fun ArithmeticScreen(viewModel: ArithmeticViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onAction = viewModel::onAction

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.BottomEnd
        ) {
            Text(
                text = state.display,
                fontSize = 64.sp,
                maxLines = 1,
                textAlign = TextAlign.End,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        val rows: List<List<CalcButton>> = listOf(
            listOf(CalcButton.Action("C", CalculatorAction.Clear, isFunction = true),
                CalcButton.Action("⌫", CalculatorAction.Backspace, isFunction = true),
                CalcButton.Action("%", CalculatorAction.Percent, isFunction = true),
                CalcButton.Action("÷", CalculatorAction.Op(Operator.Divide), isOperator = true)),
            listOf(CalcButton.Digit(7), CalcButton.Digit(8), CalcButton.Digit(9),
                CalcButton.Action("×", CalculatorAction.Op(Operator.Times), isOperator = true)),
            listOf(CalcButton.Digit(4), CalcButton.Digit(5), CalcButton.Digit(6),
                CalcButton.Action("−", CalculatorAction.Op(Operator.Minus), isOperator = true)),
            listOf(CalcButton.Digit(1), CalcButton.Digit(2), CalcButton.Digit(3),
                CalcButton.Action("+", CalculatorAction.Op(Operator.Plus), isOperator = true)),
            listOf(CalcButton.Action("+/−", CalculatorAction.ToggleSign, isFunction = true),
                CalcButton.Digit(0),
                CalcButton.Action(".", CalculatorAction.Decimal),
                CalcButton.Action("=", CalculatorAction.Equals, isOperator = true)),
        )

        rows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { button ->
                    KeypadButton(
                        button = button,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        onClick = { onAction(button.action) }
                    )
                }
            }
        }
    }
}

private sealed class CalcButton(val label: String, val action: CalculatorAction) {
    class Digit(value: Int) : CalcButton(value.toString(), CalculatorAction.Digit(value))
    class Action(
        label: String,
        action: CalculatorAction,
        val isOperator: Boolean = false,
        val isFunction: Boolean = false
    ) : CalcButton(label, action)
}

@Composable
private fun KeypadButton(
    button: CalcButton,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isOperator = button is CalcButton.Action && button.isOperator
    val isFunction = button is CalcButton.Action && button.isFunction
    val containerColor = when {
        isOperator -> MaterialTheme.colorScheme.primary
        isFunction -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = when {
        isOperator -> MaterialTheme.colorScheme.onPrimary
        isFunction -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Button(
        onClick = onClick,
        modifier = modifier.padding(vertical = 4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Text(text = button.label, fontSize = 24.sp, color = Color.Unspecified)
    }
}
```

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/assetmanagement/arithmetic/ui/ArithmeticScreen.kt
git commit -m "feat: add ArithmeticScreen calculator keypad UI"
```

---

### Task 4: Navigation wiring (third tab)

**Files:**
- Modify: `app/src/main/java/com/example/assetmanagement/NavigationKeys.kt`
- Modify: `app/src/main/java/com/example/assetmanagement/Navigation.kt`

**Interfaces:**
- Consumes: `ArithmeticScreen` (Task 3).
- Produces: `@Serializable data object ArithmeticKey : NavKey` and a third `NavigationBarItem`.

- [ ] **Step 1: Add the nav key**

In `NavigationKeys.kt`, add after the `HistoryKey` line:

```kotlin
@Serializable data object ArithmeticKey : NavKey
```

- [ ] **Step 2: Add the import in Navigation.kt**

In `Navigation.kt`, add to the imports (alongside the existing `calculator`/`history` screen imports):

```kotlin
import androidx.compose.material.icons.filled.Add
import com.example.assetmanagement.arithmetic.ui.ArithmeticScreen
```

- [ ] **Step 3: Add the third NavigationBarItem**

In `Navigation.kt`, inside `NavigationBar { ... }`, add a third item after the `HistoryKey` item:

```kotlin
                NavigationBarItem(
                    selected = current == ArithmeticKey,
                    onClick = { backStack.clear(); backStack.add(ArithmeticKey) },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    label = { Text("Math") }
                )
```

- [ ] **Step 4: Add the entry in the entryProvider**

In `Navigation.kt`, inside `entryProvider { ... }`, add after the `entry<HistoryKey> { ... }` block:

```kotlin
                entry<ArithmeticKey> {
                    ArithmeticScreen()
                }
```

- [ ] **Step 5: Verify it compiles**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Build the debug APK**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 7: Manual smoke test**

Install and launch the app (or run from Android Studio). Tap the new **Math** tab. Verify:
- Display shows `0`.
- `2 + 3 × 4 =` shows `20`.
- `5 ÷ 0 =` shows `Error`; only `C` recovers.
- `+/−`, `%`, `⌫`, `.` behave per spec.

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/example/assetmanagement/NavigationKeys.kt app/src/main/java/com/example/assetmanagement/Navigation.kt
git commit -m "feat: wire arithmetic calculator as third Math nav tab"
```

---

## Self-Review Notes

- **Spec coverage:** buttons (Task 1 actions + Task 3 keypad), chained eval (Task 1), no Room/history (none added), divide-by-zero + length cap (Task 1), third tab labelled "Math" (Task 4), TDD on engine + ViewModel (Tasks 1–2). All spec sections mapped.
- **Type consistency:** `reduce`, `onAction`, `CalculatorAction.Op(Operator)`, `Operator` symbols, and `CalculatorState.display/overwrite/isError` are used identically across Tasks 1–4.
- **No placeholders:** every code step contains complete code.
