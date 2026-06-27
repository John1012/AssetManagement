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
