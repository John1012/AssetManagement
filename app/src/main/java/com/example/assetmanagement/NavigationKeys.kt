package com.example.assetmanagement

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Main : NavKey

@Serializable data object HomeKey : NavKey

@Serializable
data class CalculatorKey(
    val prefillFund: Double = 0.0,
    val prefillROI: Double = 0.0,
    val prefillYears: Int = 0,
    val prefillContribution: Double = 0.0,
    val hasPrefill: Boolean = false
) : NavKey

@Serializable data object HistoryKey : NavKey

@Serializable data object LoanKey : NavKey
