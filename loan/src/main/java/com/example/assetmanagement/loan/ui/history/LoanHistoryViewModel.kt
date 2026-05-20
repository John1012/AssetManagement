package com.example.assetmanagement.loan.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assetmanagement.loan.domain.model.LoanHistoryItem
import com.example.assetmanagement.loan.domain.repository.LoanRepository
import com.example.assetmanagement.loan.domain.usecase.GetLoanHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface LoanHistoryUiState {
    object Loading : LoanHistoryUiState
    data class Success(val items: List<LoanHistoryItem>) : LoanHistoryUiState
    object Empty : LoanHistoryUiState
}

@HiltViewModel
class LoanHistoryViewModel @Inject constructor(
    getLoanHistoryUseCase: GetLoanHistoryUseCase,
    private val repository: LoanRepository
) : ViewModel() {

    val uiState: StateFlow<LoanHistoryUiState> = getLoanHistoryUseCase()
        .map { items -> if (items.isEmpty()) LoanHistoryUiState.Empty else LoanHistoryUiState.Success(items) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, LoanHistoryUiState.Loading)

    fun delete(id: Long) {
        viewModelScope.launch { repository.deleteById(id) }
    }
}
