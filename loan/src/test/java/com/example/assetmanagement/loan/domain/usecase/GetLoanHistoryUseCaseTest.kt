package com.example.assetmanagement.loan.domain.usecase

import com.example.assetmanagement.loan.domain.model.LoanHistoryItem
import com.example.assetmanagement.loan.domain.model.LoanInput
import com.example.assetmanagement.loan.domain.model.LoanResult
import com.example.assetmanagement.loan.domain.repository.LoanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetLoanHistoryUseCaseTest {

    @Test
    fun `returns flow from repository`() = runTest {
        val items = listOf(LoanHistoryItem(1L, 100_000.0, 5.0, 24, 4_386.52, 5_276.48, 0L))
        val fakeRepo = object : LoanRepository {
            override suspend fun save(input: LoanInput, result: LoanResult) {}
            override fun getAll(): Flow<List<LoanHistoryItem>> = flowOf(items)
            override suspend fun deleteById(id: Long) {}
        }
        val result = GetLoanHistoryUseCase(fakeRepo)().first()
        assertEquals(items, result)
    }
}
