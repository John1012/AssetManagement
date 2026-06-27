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
