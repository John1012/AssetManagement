package com.example.assetmanagement.calculator.domain.usecase

import com.example.assetmanagement.calculator.domain.model.CalculationInput
import com.example.assetmanagement.calculator.domain.repository.CalculationRepository
import javax.inject.Inject

class SaveCalculationUseCase @Inject constructor(
    private val repository: CalculationRepository
) {
    suspend operator fun invoke(input: CalculationInput, finalValue: Double) =
        repository.save(input, finalValue)
}
