package com.example.assetmanagement.loan.domain.model

data class LoanInput(
    val loanAmount: Double,
    val annualRate: Double,
    val termMonths: Int
)
