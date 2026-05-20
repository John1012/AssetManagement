package com.example.assetmanagement.loan.domain.usecase

import com.example.assetmanagement.loan.domain.model.LoanInput
import com.example.assetmanagement.loan.domain.model.LoanResult
import com.example.assetmanagement.loan.domain.repository.LoanRepository
import javax.inject.Inject

class SaveLoanCalculationUseCase @Inject constructor(
    private val repository: LoanRepository
) {
    suspend operator fun invoke(input: LoanInput, result: LoanResult) =
        repository.save(input, result)
}
