package com.example.assetmanagement.calculator.data

import com.example.assetmanagement.calculator.data.local.CalculationDao
import com.example.assetmanagement.calculator.data.local.CalculationEntity
import com.example.assetmanagement.calculator.domain.model.CalculationInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculationRepositoryImplTest {

    private val stored = mutableListOf<CalculationEntity>()

    private val fakeDao = object : CalculationDao {
        override suspend fun insert(entity: CalculationEntity) {
            stored += entity.copy(id = stored.size.toLong() + 1)
        }
        override fun getAll(): Flow<List<CalculationEntity>> = flowOf(stored.toList())
        override suspend fun deleteById(id: Long) {
            stored.removeIf { it.id == id }
        }
    }

    private val repo = CalculationRepositoryImpl(fakeDao)

    @Test
    fun `save maps all CalculationInput fields onto the entity`() = runTest {
        val input = CalculationInput(
            initialFund = 100_000.0,
            annualROI = 7.5,
            durationYears = 20,
            annualContribution = 1_200.0
        )

        repo.save(input, finalValue = 450_000.0)

        val saved = stored.single()
        assertEquals(100_000.0, saved.initialFund, 0.0)
        assertEquals(7.5, saved.annualROI, 0.0)
        assertEquals(20, saved.durationYears)
        assertEquals(1_200.0, saved.annualContribution, 0.0)
    }

    @Test
    fun `save records finalValue and a current timestamp on the entity`() = runTest {
        val input = CalculationInput(initialFund = 50_000.0, annualROI = 5.0, durationYears = 10)
        val timeBefore = System.currentTimeMillis()

        repo.save(input, finalValue = 81_444.73)

        val timAfter = System.currentTimeMillis()
        val saved = stored.single()
        assertEquals(81_444.73, saved.finalValue, 0.01)
        assertTrue(saved.savedAt in timeBefore..timAfter)
    }

    @Test
    fun `getAll maps every CalculationEntity field to HistoryItem`() = runTest {
        stored += CalculationEntity(
            id = 1L,
            initialFund = 100_000.0,
            annualROI = 5.0,
            durationYears = 10,
            annualContribution = 500.0,
            finalValue = 162_889.0,
            savedAt = 99_999L
        )

        val result = repo.getAll().first()

        assertEquals(1, result.size)
        val item = result[0]
        assertEquals(1L, item.id)
        assertEquals(100_000.0, item.initialFund, 0.0)
        assertEquals(5.0, item.annualROI, 0.0)
        assertEquals(10, item.durationYears)
        assertEquals(500.0, item.annualContribution, 0.0)
        assertEquals(162_889.0, item.finalValue, 0.0)
        assertEquals(99_999L, item.savedAt)
    }

    @Test
    fun `getAll on empty dao returns empty list`() = runTest {
        val result = repo.getAll().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `deleteById delegates to dao`() = runTest {
        stored += CalculationEntity(id = 1L, initialFund = 1.0, annualROI = 1.0, durationYears = 1, annualContribution = 0.0, finalValue = 1.0, savedAt = 1_000L)

        repo.deleteById(1L)

        assertTrue(stored.isEmpty())
    }
}
