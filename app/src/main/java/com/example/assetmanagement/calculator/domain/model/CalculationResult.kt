package com.example.assetmanagement.calculator.domain.model

data class CalculationResult(
    val finalValue: Double,
    val totalContributed: Double,
    val totalInterestEarned: Double,
    val yearlySnapshots: List<YearlySnapshot>
)
