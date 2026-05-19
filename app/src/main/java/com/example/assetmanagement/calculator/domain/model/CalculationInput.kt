package com.example.assetmanagement.calculator.domain.model

data class CalculationInput(
    val initialFund: Double,
    val annualROI: Double,
    val durationYears: Int,
    val annualContribution: Double = 0.0
)
