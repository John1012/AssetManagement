package com.example.assetmanagement.loan.data

import com.example.assetmanagement.loan.data.local.LoanDao
import com.example.assetmanagement.loan.data.local.LoanEntity
import com.example.assetmanagement.loan.domain.model.LoanHistoryItem
import com.example.assetmanagement.loan.domain.model.LoanInput
import com.example.assetmanagement.loan.domain.model.LoanResult
import com.example.assetmanagement.loan.domain.repository.LoanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LoanRepositoryImpl @Inject constructor(
    private val dao: LoanDao
) : LoanRepository {

    override suspend fun save(input: LoanInput, result: LoanResult) {
        dao.insert(
            LoanEntity(
                loanAmount = input.loanAmount,
                annualRate = input.annualRate,
                termMonths = input.termMonths,
                monthlyPayment = result.monthlyPayment,
                totalInterest = result.totalInterest,
                savedAt = System.currentTimeMillis()
            )
        )
    }

    override fun getAll(): Flow<List<LoanHistoryItem>> =
        dao.getAll().map { list -> list.map { it.toHistoryItem() } }

    override suspend fun deleteById(id: Long) = dao.deleteById(id)

    private fun LoanEntity.toHistoryItem() = LoanHistoryItem(
        id = id,
        loanAmount = loanAmount,
        annualRate = annualRate,
        termMonths = termMonths,
        monthlyPayment = monthlyPayment,
        totalInterest = totalInterest,
        savedAt = savedAt
    )
}
