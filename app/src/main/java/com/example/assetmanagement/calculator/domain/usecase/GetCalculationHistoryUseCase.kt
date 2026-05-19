package com.example.assetmanagement.calculator.domain.usecase

import com.example.assetmanagement.calculator.domain.model.HistoryItem
import com.example.assetmanagement.calculator.domain.repository.CalculationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCalculationHistoryUseCase @Inject constructor(
    private val repository: CalculationRepository
) {
    operator fun invoke(): Flow<List<HistoryItem>> = repository.getAll()
}
