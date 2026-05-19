package com.example.assetmanagement.calculator.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculations")
data class CalculationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val initialFund: Double,
    val annualROI: Double,
    val durationYears: Int,
    val annualContribution: Double,
    val finalValue: Double,
    val savedAt: Long
)
