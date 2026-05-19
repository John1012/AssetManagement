package com.example.assetmanagement.calculator.data

import com.example.assetmanagement.calculator.data.local.CalculationDao
import com.example.assetmanagement.calculator.data.local.CalculationEntity
import com.example.assetmanagement.calculator.domain.model.CalculationInput
import com.example.assetmanagement.calculator.domain.model.HistoryItem
import com.example.assetmanagement.calculator.domain.repository.CalculationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CalculationRepositoryImpl @Inject constructor(
    private val dao: CalculationDao
) : CalculationRepository {

    override suspend fun save(input: CalculationInput, finalValue: Double) {
        dao.insert(
            CalculationEntity(
                initialFund = input.initialFund,
                annualROI = input.annualROI,
                durationYears = input.durationYears,
                annualContribution = input.annualContribution,
                finalValue = finalValue,
                savedAt = System.currentTimeMillis()
            )
        )
    }

    override fun getAll(): Flow<List<HistoryItem>> =
        dao.getAll().map { list -> list.map { it.toHistoryItem() } }

    override suspend fun deleteById(id: Long) = dao.deleteById(id)

    private fun CalculationEntity.toHistoryItem() = HistoryItem(
        id = id,
        initialFund = initialFund,
        annualROI = annualROI,
        durationYears = durationYears,
        annualContribution = annualContribution,
        finalValue = finalValue,
        savedAt = savedAt
    )
}
