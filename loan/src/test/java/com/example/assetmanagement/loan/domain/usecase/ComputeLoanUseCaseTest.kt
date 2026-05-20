package com.example.assetmanagement.loan.domain.usecase

import com.example.assetmanagement.loan.domain.model.LoanInput
import org.junit.Assert.assertEquals
import org.junit.Test

class ComputeLoanUseCaseTest {

    private val useCase = ComputeLoanUseCase()

    @Test
    fun `standard 12-month loan produces correct monthly payment`() {
        // P=120000, rate=6%, n=12 → r=0.005
        // M = 120000 * (0.005 * 1.005^12) / (1.005^12 - 1) ≈ 10327.93
        val result = useCase(LoanInput(120_000.0, 6.0, 12))
        assertEquals(10_327.93, result.monthlyPayment, 1.0)
    }

    @Test
    fun `total repayment equals monthly payment times term`() {
        val result = useCase(LoanInput(120_000.0, 6.0, 12))
        assertEquals(result.monthlyPayment * 12, result.totalRepayment, 0.01)
    }

    @Test
    fun `total interest equals total repayment minus principal`() {
        val result = useCase(LoanInput(120_000.0, 6.0, 12))
        assertEquals(result.totalRepayment - 120_000.0, result.totalInterest, 0.01)
    }

    @Test
    fun `zero interest rate monthly payment equals principal divided by term`() {
        // rate=0 → M = P / n
        val result = useCase(LoanInput(120_000.0, 0.0, 12))
        assertEquals(10_000.0, result.monthlyPayment, 0.01)
        assertEquals(0.0, result.totalInterest, 0.01)
    }

    @Test
    fun `one-month term monthly payment equals principal plus one month interest`() {
        // P=100000, rate=12%, n=1 → r=0.01, M = 100000 * 1.01 = 101000
        val result = useCase(LoanInput(100_000.0, 12.0, 1))
        assertEquals(101_000.0, result.monthlyPayment, 0.01)
        assertEquals(1_000.0, result.totalInterest, 0.01)
    }

    @Test
    fun `schedule has correct number of entries and final balance near zero`() {
        val result = useCase(LoanInput(120_000.0, 6.0, 12))
        assertEquals(12, result.schedule.size)
        assertEquals(0.0, result.schedule.last().remainingBalance, 1.0)
    }
}
