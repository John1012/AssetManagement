package com.example.assetmanagement.calculator.domain.model

data class HistoryItem(
    val id: Long,
    val initialFund: Double,
    val annualROI: Double,
    val durationYears: Int,
    val monthlyContribution: Double,
    val finalValue: Double,
    val savedAt: Long
)
