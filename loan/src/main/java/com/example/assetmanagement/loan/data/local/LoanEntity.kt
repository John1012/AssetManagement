package com.example.assetmanagement.loan.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "loans")
data class LoanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val loanAmount: Double,
    val annualRate: Double,
    val termMonths: Int,
    val monthlyPayment: Double,
    val totalInterest: Double,
    val savedAt: Long
)
