package com.example.assetmanagement.calculator.domain.usecase

import com.example.assetmanagement.calculator.domain.model.CalculationInput
import com.example.assetmanagement.calculator.domain.model.CalculationResult
import com.example.assetmanagement.calculator.domain.model.YearlySnapshot
import javax.inject.Inject

class ComputeCompoundGrowthUseCase @Inject constructor() {

    operator fun invoke(input: CalculationInput): CalculationResult {
        val rate = 1.0 + input.annualROI / 100.0
        var value = input.initialFund
        val snapshots = mutableListOf<YearlySnapshot>()

        for (year in 1..input.durationYears) {
            value = (value + input.annualContribution) * rate
            val contributed = input.initialFund + input.annualContribution * year
            snapshots.add(
                YearlySnapshot(
                    year = year,
                    totalValue = value,
                    totalContributed = contributed,
                    totalInterestEarned = value - contributed
                )
            )
        }

        val totalContributed = input.initialFund + input.annualContribution * input.durationYears
        return CalculationResult(
            finalValue = value,
            totalContributed = totalContributed,
            totalInterestEarned = value - totalContributed,
            yearlySnapshots = snapshots
        )
    }
}
