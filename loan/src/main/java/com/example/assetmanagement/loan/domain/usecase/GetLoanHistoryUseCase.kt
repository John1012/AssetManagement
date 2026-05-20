package com.example.assetmanagement.loan.domain.usecase

import com.example.assetmanagement.loan.domain.model.LoanHistoryItem
import com.example.assetmanagement.loan.domain.repository.LoanRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLoanHistoryUseCase @Inject constructor(
    private val repository: LoanRepository
) {
    operator fun invoke(): Flow<List<LoanHistoryItem>> = repository.getAll()
}
