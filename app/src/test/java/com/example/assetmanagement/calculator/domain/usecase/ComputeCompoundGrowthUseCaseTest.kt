package com.example.assetmanagement.calculator.domain.usecase

import com.example.assetmanagement.calculator.domain.model.CalculationInput
import org.junit.Assert.assertEquals
import org.junit.Test

class ComputeCompoundGrowthUseCaseTest {

    private val useCase = ComputeCompoundGrowthUseCase()

    @Test
    fun `single year no contribution`() {
        val result = useCase(CalculationInput(100_000.0, 10.0, 1, 0.0))
        assertEquals(110_000.0, result.finalValue, 0.01)
        assertEquals(100_000.0, result.totalContributed, 0.01)
        assertEquals(10_000.0, result.totalInterestEarned, 0.01)
        assertEquals(1, result.yearlySnapshots.size)
    }

    @Test
    fun `three years no contribution compounds correctly`() {
        val result = useCase(CalculationInput(100_000.0, 10.0, 3, 0.0))
        // 100000 * 1.1^3 = 133100
        assertEquals(133_100.0, result.finalValue, 0.01)
        assertEquals(110_000.0, result.yearlySnapshots[0].totalValue, 0.01)
        assertEquals(121_000.0, result.yearlySnapshots[1].totalValue, 0.01)
        assertEquals(133_100.0, result.yearlySnapshots[2].totalValue, 0.01)
    }

    @Test
    fun `with annual contribution added at start of year`() {
        val result = useCase(CalculationInput(100_000.0, 10.0, 2, 10_000.0))
        // Year 1: (100000 + 10000) * 1.1 = 121000
        // Year 2: (121000 + 10000) * 1.1 = 144100
        assertEquals(144_100.0, result.finalValue, 0.01)
        assertEquals(120_000.0, result.totalContributed, 0.01)
        assertEquals(24_100.0, result.totalInterestEarned, 0.01)
    }

    @Test
    fun `zero ROI returns sum of contributions`() {
        val result = useCase(CalculationInput(100_000.0, 0.0, 3, 5_000.0))
        assertEquals(115_000.0, result.finalValue, 0.01)
        assertEquals(0.0, result.totalInterestEarned, 0.01)
    }

    @Test
    fun `snapshot totalContributed tracks principal plus contributions per year`() {
        val result = useCase(CalculationInput(50_000.0, 5.0, 2, 10_000.0))
        assertEquals(60_000.0, result.yearlySnapshots[0].totalContributed, 0.01)
        assertEquals(70_000.0, result.yearlySnapshots[1].totalContributed, 0.01)
    }
}
