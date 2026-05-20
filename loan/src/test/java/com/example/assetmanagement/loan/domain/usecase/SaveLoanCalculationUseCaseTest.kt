package com.example.assetmanagement.loan.domain.usecase

import com.example.assetmanagement.loan.domain.model.LoanHistoryItem
import com.example.assetmanagement.loan.domain.model.LoanInput
import com.example.assetmanagement.loan.domain.model.LoanResult
import com.example.assetmanagement.loan.domain.repository.LoanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SaveLoanCalculationUseCaseTest {

    @Test
    fun `delegates save to repository`() = runTest {
        var savedInput: LoanInput? = null
        var savedResult: LoanResult? = null
        val fakeRepo = object : LoanRepository {
            override suspend fun save(input: LoanInput, result: LoanResult) {
                savedInput = input; savedResult = result
            }
            override fun getAll(): Flow<List<LoanHistoryItem>> = flowOf(emptyList())
            override suspend fun deleteById(id: Long) {}
        }
        val input = LoanInput(100_000.0, 5.0, 24)
        val result = LoanResult(4_386.52, 105_276.48, 5_276.48, emptyList())
        SaveLoanCalculationUseCase(fakeRepo)(input, result)
        assertEquals(input, savedInput)
        assertEquals(result, savedResult)
    }
}
