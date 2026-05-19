package com.example.assetmanagement.calculator.ui.calculator

import com.example.assetmanagement.calculator.domain.model.CalculationInput
import com.example.assetmanagement.calculator.domain.model.HistoryItem
import com.example.assetmanagement.calculator.domain.repository.CalculationRepository
import com.example.assetmanagement.calculator.domain.usecase.ComputeCompoundGrowthUseCase
import com.example.assetmanagement.calculator.domain.usecase.SaveCalculationUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CalculatorViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeRepo = object : CalculationRepository {
        override suspend fun save(input: CalculationInput, finalValue: Double) {}
        override fun getAll(): Flow<List<HistoryItem>> = flowOf(emptyList())
        override suspend fun deleteById(id: Long) {}
    }

    private lateinit var viewModel: CalculatorViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = CalculatorViewModel(ComputeCompoundGrowthUseCase(), SaveCalculationUseCase(fakeRepo))
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `initial state is Idle`() {
        assertTrue(viewModel.uiState.value is CalculatorUiState.Idle)
    }

    @Test
    fun `calculate transitions to ShowingResult with correct snapshot count`() = runTest {
        viewModel.calculate(CalculationInput(100_000.0, 10.0, 5, 0.0))
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as CalculatorUiState.ShowingResult
        assertTrue(state.result.yearlySnapshots.size == 5)
    }

    @Test
    fun `prefill transitions to ShowingResult with provided values`() = runTest {
        viewModel.prefill(200_000.0, 8.0, 10, 5_000.0)
        val state = viewModel.uiState.value as CalculatorUiState.ShowingResult
        assertTrue(state.input.initialFund == 200_000.0)
        assertTrue(state.input.annualROI == 8.0)
    }
}
