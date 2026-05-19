package com.example.assetmanagement.calculator.ui.history

import com.example.assetmanagement.calculator.domain.model.CalculationInput
import com.example.assetmanagement.calculator.domain.model.HistoryItem
import com.example.assetmanagement.calculator.domain.repository.CalculationRepository
import com.example.assetmanagement.calculator.domain.usecase.GetCalculationHistoryUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val itemsFlow = MutableSharedFlow<List<HistoryItem>>(replay = 1)

    private val fakeRepo = object : CalculationRepository {
        override suspend fun save(input: CalculationInput, finalValue: Double) {}
        override fun getAll(): Flow<List<HistoryItem>> = itemsFlow
        override suspend fun deleteById(id: Long) {}
    }

    private lateinit var viewModel: HistoryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = HistoryViewModel(GetCalculationHistoryUseCase(fakeRepo), fakeRepo)
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `initial state is Loading`() {
        assertTrue(viewModel.uiState.value is HistoryUiState.Loading)
    }

    @Test
    fun `empty list emits Empty`() = runTest {
        itemsFlow.emit(emptyList())
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is HistoryUiState.Empty)
    }

    @Test
    fun `non-empty list emits Success with correct items`() = runTest {
        val items = listOf(HistoryItem(1L, 100_000.0, 5.0, 10, 0.0, 162_889.0, 1_000_000L))
        itemsFlow.emit(items)
        advanceUntilIdle()
        val state = viewModel.uiState.value as HistoryUiState.Success
        assertTrue(state.items == items)
    }
}
