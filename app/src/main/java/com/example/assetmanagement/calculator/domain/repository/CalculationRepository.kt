package com.example.assetmanagement.calculator.domain.repository

import com.example.assetmanagement.calculator.domain.model.CalculationInput
import com.example.assetmanagement.calculator.domain.model.HistoryItem
import kotlinx.coroutines.flow.Flow

interface CalculationRepository {
    suspend fun save(input: CalculationInput, finalValue: Double)
    fun getAll(): Flow<List<HistoryItem>>
    suspend fun deleteById(id: Long)
}
