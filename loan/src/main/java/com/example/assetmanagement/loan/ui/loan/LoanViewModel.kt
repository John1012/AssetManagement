package com.example.assetmanagement.loan.ui.loan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assetmanagement.loan.domain.model.LoanInput
import com.example.assetmanagement.loan.domain.model.LoanResult
import com.example.assetmanagement.loan.domain.usecase.ComputeLoanUseCase
import com.example.assetmanagement.loan.domain.usecase.SaveLoanCalculationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface LoanUiState {
    object Idle : LoanUiState
    data class ShowingResult(val input: LoanInput, val result: LoanResult) : LoanUiState
    data class Error(val message: String) : LoanUiState
}

@HiltViewModel
class LoanViewModel @Inject constructor(
    private val computeLoanUseCase: ComputeLoanUseCase,
    private val saveLoanCalculationUseCase: SaveLoanCalculationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoanUiState>(LoanUiState.Idle)
    val uiState: StateFlow<LoanUiState> = _uiState.asStateFlow()

    fun calculate(input: LoanInput) {
        viewModelScope.launch {
            try {
                val result = computeLoanUseCase(input)
                saveLoanCalculationUseCase(input, result)
                _uiState.value = LoanUiState.ShowingResult(input, result)
            } catch (e: Exception) {
                _uiState.value = LoanUiState.Error(e.message ?: "Calculation failed")
            }
        }
    }

    fun prefill(loanAmount: Double, annualRate: Double, termMonths: Int) {
        val input = LoanInput(loanAmount, annualRate, termMonths)
        _uiState.value = LoanUiState.ShowingResult(input, computeLoanUseCase(input))
    }
}
