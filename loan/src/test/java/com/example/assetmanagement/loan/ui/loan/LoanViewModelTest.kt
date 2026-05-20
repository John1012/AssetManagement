package com.example.assetmanagement.loan.ui.loan

import com.example.assetmanagement.loan.domain.model.LoanHistoryItem
import com.example.assetmanagement.loan.domain.model.LoanInput
import com.example.assetmanagement.loan.domain.model.LoanResult
import com.example.assetmanagement.loan.domain.repository.LoanRepository
import com.example.assetmanagement.loan.domain.usecase.ComputeLoanUseCase
import com.example.assetmanagement.loan.domain.usecase.SaveLoanCalculationUseCase
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
class LoanViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeRepo = object : LoanRepository {
        override suspend fun save(input: LoanInput, result: LoanResult) {}
        override fun getAll(): Flow<List<LoanHistoryItem>> = flowOf(emptyList())
        override suspend fun deleteById(id: Long) {}
    }

    private lateinit var viewModel: LoanViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoanViewModel(ComputeLoanUseCase(), SaveLoanCalculationUseCase(fakeRepo))
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `initial state is Idle`() {
        assertTrue(viewModel.uiState.value is LoanUiState.Idle)
    }

    @Test
    fun `calculate transitions to ShowingResult with correct schedule size`() = runTest {
        viewModel.calculate(LoanInput(120_000.0, 6.0, 12))
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as LoanUiState.ShowingResult
        assertTrue(state.result.schedule.size == 12)
    }

    @Test
    fun `prefill transitions to ShowingResult with provided input values`() = runTest {
        viewModel.prefill(200_000.0, 5.0, 24)
        val state = viewModel.uiState.value as LoanUiState.ShowingResult
        assertTrue(state.input.loanAmount == 200_000.0)
        assertTrue(state.input.annualRate == 5.0)
        assertTrue(state.input.termMonths == 24)
    }
}
