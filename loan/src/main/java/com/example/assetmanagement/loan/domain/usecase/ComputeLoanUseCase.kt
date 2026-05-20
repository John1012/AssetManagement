package com.example.assetmanagement.loan.domain.usecase

import com.example.assetmanagement.loan.domain.model.LoanInput
import com.example.assetmanagement.loan.domain.model.LoanResult
import com.example.assetmanagement.loan.domain.model.LoanScheduleEntry
import javax.inject.Inject
import kotlin.math.pow

class ComputeLoanUseCase @Inject constructor() {

    operator fun invoke(input: LoanInput): LoanResult {
        val n = input.termMonths
        val monthlyPayment: Double

        if (input.annualRate == 0.0) {
            monthlyPayment = input.loanAmount / n
        } else {
            val r = input.annualRate / 100.0 / 12.0
            monthlyPayment = input.loanAmount * (r * (1 + r).pow(n)) / ((1 + r).pow(n) - 1)
        }

        var balance = input.loanAmount
        val schedule = mutableListOf<LoanScheduleEntry>()
        val r = input.annualRate / 100.0 / 12.0

        for (month in 1..n) {
            balance = if (month == n) 0.0 else (balance * (1 + r) - monthlyPayment).coerceAtLeast(0.0)
            schedule.add(LoanScheduleEntry(month = month, remainingBalance = balance))
        }

        val totalRepayment = monthlyPayment * n
        return LoanResult(
            monthlyPayment = monthlyPayment,
            totalRepayment = totalRepayment,
            totalInterest = totalRepayment - input.loanAmount,
            schedule = schedule
        )
    }
}
