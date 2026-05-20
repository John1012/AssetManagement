package com.example.assetmanagement.loan

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class LoanCalculatorKey(
    val prefillAmount: Double = 0.0,
    val prefillRate: Double = 0.0,
    val prefillMonths: Int = 0,
    val hasPrefill: Boolean = false
) : NavKey

@Serializable data object LoanHistoryKey : NavKey
