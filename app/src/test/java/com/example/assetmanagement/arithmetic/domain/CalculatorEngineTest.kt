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
