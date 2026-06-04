package com.example.assetmanagement.calculator.domain.model

data class CalculationInput(
    val initialFund: Double,
    val annualROI: Double,
    val durationYears: Int,
    val monthlyContribution: Double = 0.0
)
