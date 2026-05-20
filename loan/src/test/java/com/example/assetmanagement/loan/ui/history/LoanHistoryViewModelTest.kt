package com.example.assetmanagement.loan.ui.history

import com.example.assetmanagement.loan.domain.model.LoanHistoryItem
import com.example.assetmanagement.loan.domain.model.LoanInput
import com.example.assetmanagement.loan.domain.model.LoanResult
import com.example.assetmanagement.loan.domain.repository.LoanRepository
import com.example.assetmanagement.loan.domain.usecase.GetLoanHistoryUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
class LoanHistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val historyFlow = MutableStateFlow<List<LoanHistoryItem>>(emptyList())

    private val fakeRepo = object : LoanRepository {
        override suspend fun save(input: LoanInput, result: LoanResult) {}
        override fun getAll(): Flow<List<LoanHistoryItem>> = historyFlow
        override suspend fun deleteById(id: Long) {}
    }

    private lateinit var viewModel: LoanHistoryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoanHistoryViewModel(GetLoanHistoryUseCase(fakeRepo), fakeRepo)
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `initial state is Loading`() {
        assertTrue(viewModel.uiState.value is LoanHistoryUiState.Loading)
    }

    @Test
    fun `empty list emits Empty state`() = runTest {
        historyFlow.value = emptyList()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value is LoanHistoryUiState.Empty)
    }

    @Test
    fun `non-empty list emits Success state`() = runTest {
        historyFlow.value = listOf(LoanHistoryItem(1L, 100_000.0, 5.0, 24, 4_386.52, 5_276.48, 0L))
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value is LoanHistoryUiState.Success)
    }
}
