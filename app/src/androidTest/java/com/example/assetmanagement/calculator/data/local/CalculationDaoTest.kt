package com.example.assetmanagement.calculator.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class CalculationDaoTest {

    private lateinit var db: CalculationDatabase
    private lateinit var dao: CalculationDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            CalculationDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.calculationDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun getAll_onEmptyTable_returnsEmptyList() = runTest {
        val result = dao.getAll().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun insert_thenGetAll_returnsEntityWithAllFields() = runTest {
        val entity = CalculationEntity(
            initialFund = 100_000.0,
            annualROI = 5.0,
            durationYears = 10,
            annualContribution = 500.0,
            finalValue = 162_889.0,
            savedAt = 1_000_000L
        )
        dao.insert(entity)

        val result = dao.getAll().first()

        assertEquals(1, result.size)
        val saved = result[0]
        assertTrue(saved.id > 0)
        assertEquals(100_000.0, saved.initialFund, 0.0)
        assertEquals(5.0, saved.annualROI, 0.0)
        assertEquals(10, saved.durationYears)
        assertEquals(500.0, saved.annualContribution, 0.0)
        assertEquals(162_889.0, saved.finalValue, 0.0)
        assertEquals(1_000_000L, saved.savedAt)
    }

    @Test
    fun getAll_returnsEntitiesOrderedBySavedAtDescending() = runTest {
        dao.insert(CalculationEntity(initialFund = 1.0, annualROI = 1.0, durationYears = 1, annualContribution = 0.0, finalValue = 1.0, savedAt = 1_000L))
        dao.insert(CalculationEntity(initialFund = 2.0, annualROI = 2.0, durationYears = 2, annualContribution = 0.0, finalValue = 2.0, savedAt = 3_000L))
        dao.insert(CalculationEntity(initialFund = 3.0, annualROI = 3.0, durationYears = 3, annualContribution = 0.0, finalValue = 3.0, savedAt = 2_000L))

        val result = dao.getAll().first()

        assertEquals(3_000L, result[0].savedAt)
        assertEquals(2_000L, result[1].savedAt)
        assertEquals(1_000L, result[2].savedAt)
    }

    @Test
    fun deleteById_removesOnlyTargetEntity() = runTest {
        dao.insert(CalculationEntity(initialFund = 1.0, annualROI = 1.0, durationYears = 1, annualContribution = 0.0, finalValue = 1.0, savedAt = 1_000L))
        dao.insert(CalculationEntity(initialFund = 2.0, annualROI = 2.0, durationYears = 2, annualContribution = 0.0, finalValue = 2.0, savedAt = 2_000L))

        val all = dao.getAll().first()
        val targetId = all.first { it.savedAt == 1_000L }.id
        dao.deleteById(targetId)

        val afterDelete = dao.getAll().first()
        assertEquals(1, afterDelete.size)
        assertEquals(2_000L, afterDelete[0].savedAt)
    }
}
