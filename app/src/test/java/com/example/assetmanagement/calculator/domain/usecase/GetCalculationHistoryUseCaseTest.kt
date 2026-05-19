package com.example.assetmanagement.calculator.domain.usecase

import com.example.assetmanagement.calculator.domain.model.CalculationInput
import com.example.assetmanagement.calculator.domain.model.HistoryItem
import com.example.assetmanagement.calculator.domain.repository.CalculationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetCalculationHistoryUseCaseTest {

    private val items = listOf(HistoryItem(1L, 100_000.0, 5.0, 10, 0.0, 162_889.0, 1_000_000L))

    private val fakeRepo = object : CalculationRepository {
        override suspend fun save(input: CalculationInput, finalValue: Double) {}
        override fun getAll(): Flow<List<HistoryItem>> = flowOf(items)
        override suspend fun deleteById(id: Long) {}
    }

    @Test
    fun `invoke returns items from repository`() = runTest {
        assertEquals(items, GetCalculationHistoryUseCase(fakeRepo)().first())
    }
}
