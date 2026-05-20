package com.example.assetmanagement.loan.domain.repository

import com.example.assetmanagement.loan.domain.model.LoanHistoryItem
import com.example.assetmanagement.loan.domain.model.LoanInput
import com.example.assetmanagement.loan.domain.model.LoanResult
import kotlinx.coroutines.flow.Flow

interface LoanRepository {
    suspend fun save(input: LoanInput, result: LoanResult)
    fun getAll(): Flow<List<LoanHistoryItem>>
    suspend fun deleteById(id: Long)
}
