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
