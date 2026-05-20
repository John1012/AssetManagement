package com.example.assetmanagement.loan.domain.model

data class LoanHistoryItem(
    val id: Long,
    val loanAmount: Double,
    val annualRate: Double,
    val termMonths: Int,
    val monthlyPayment: Double,
    val totalInterest: Double,
    val savedAt: Long
)
