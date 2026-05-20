package com.example.assetmanagement.loan.domain.model

data class LoanResult(
    val monthlyPayment: Double,
    val totalRepayment: Double,
    val totalInterest: Double,
    val schedule: List<LoanScheduleEntry>
)
