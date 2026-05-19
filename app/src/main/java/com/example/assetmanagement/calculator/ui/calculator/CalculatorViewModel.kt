package com.example.assetmanagement.calculator.ui.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assetmanagement.calculator.domain.model.CalculationInput
import com.example.assetmanagement.calculator.domain.model.CalculationResult
import com.example.assetmanagement.calculator.domain.usecase.ComputeCompoundGrowthUseCase
import com.example.assetmanagement.calculator.domain.usecase.SaveCalculationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CalculatorUiState {
    object Idle : CalculatorUiState
    data class ShowingResult(val input: CalculationInput, val result: CalculationResult) : CalculatorUiState
    data class Error(val message: String) : CalculatorUiState
}

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val computeCompoundGrowthUseCase: ComputeCompoundGrowthUseCase,
    private val saveCalculationUseCase: SaveCalculationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CalculatorUiState>(CalculatorUiState.Idle)
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    fun calculate(input: CalculationInput) {
        viewModelScope.launch {
            try {
                val result = computeCompoundGrowthUseCase(input)
                saveCalculationUseCase(input, result.finalValue)
                _uiState.value = CalculatorUiState.ShowingResult(input, result)
            } catch (e: Exception) {
                _uiState.value = CalculatorUiState.Error(e.message ?: "Calculation failed")
            }
        }
    }

    fun prefill(initialFund: Double, annualROI: Double, durationYears: Int, annualContribution: Double) {
        val input = CalculationInput(initialFund, annualROI, durationYears, annualContribution)
        _uiState.value = CalculatorUiState.ShowingResult(input, computeCompoundGrowthUseCase(input))
    }
}
