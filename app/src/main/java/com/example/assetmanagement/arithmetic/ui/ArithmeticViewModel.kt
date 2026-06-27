package com.example.assetmanagement.arithmetic.ui

import androidx.lifecycle.ViewModel
import com.example.assetmanagement.arithmetic.domain.CalculatorAction
import com.example.assetmanagement.arithmetic.domain.CalculatorEngine
import com.example.assetmanagement.arithmetic.domain.CalculatorState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ArithmeticViewModel @Inject constructor(
    private val engine: CalculatorEngine
) : ViewModel() {

    private val _state = MutableStateFlow(CalculatorState())
    val state: StateFlow<CalculatorState> = _state.asStateFlow()

    fun onAction(action: CalculatorAction) {
        _state.value = engine.reduce(_state.value, action)
    }
}
