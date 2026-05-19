package com.example.assetmanagement.calculator.domain.usecase

import com.example.assetmanagement.calculator.domain.model.CalculationInput
import com.example.assetmanagement.calculator.domain.model.HistoryItem
import com.example.assetmanagement.calculator.domain.repository.CalculationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SaveCalculationUseCaseTest {

    private var savedInput: CalculationInput? = null
    private var savedFinalValue: Double? = null

    private val fakeRepo = object : CalculationRepository {
        override suspend fun save(input: CalculationInput, finalValue: Double) {
            savedInput = input; savedFinalValue = finalValue
        }
        override fun getAll(): Flow<List<HistoryItem>> = flowOf(emptyList())
        override suspend fun deleteById(id: Long) {}
    }

    @Test
    fun `invoke delegates to repository with correct args`() = runTest {
        val input = CalculationInput(100_000.0, 5.0, 10, 0.0)
        SaveCalculationUseCase(fakeRepo)(input, 162_889.46)
        assertEquals(input, savedInput)
        assertEquals(162_889.46, savedFinalValue!!, 0.01)
    }
}
