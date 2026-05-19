package com.example.assetmanagement.calculator.domain.model

data class YearlySnapshot(
    val year: Int,
    val totalValue: Double,
    val totalContributed: Double,
    val totalInterestEarned: Double
)
