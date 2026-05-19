package com.example.assetmanagement.calculator.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assetmanagement.calculator.domain.model.HistoryItem
import com.example.assetmanagement.calculator.domain.repository.CalculationRepository
import com.example.assetmanagement.calculator.domain.usecase.GetCalculationHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HistoryUiState {
    object Loading : HistoryUiState
    data class Success(val items: List<HistoryItem>) : HistoryUiState
    object Empty : HistoryUiState
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    getCalculationHistoryUseCase: GetCalculationHistoryUseCase,
    private val repository: CalculationRepository
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = getCalculationHistoryUseCase()
        .map { items -> if (items.isEmpty()) HistoryUiState.Empty else HistoryUiState.Success(items) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, HistoryUiState.Loading)

    fun delete(id: Long) {
        viewModelScope.launch { repository.deleteById(id) }
    }
}
