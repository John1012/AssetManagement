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
