package com.example.assetmanagement.calculator.domain.usecase

import com.example.assetmanagement.calculator.domain.model.CalculationInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ComputeCompoundGrowthUseCaseTest {

    private val useCase = ComputeCompoundGrowthUseCase()

    @Test
    fun `single year no monthly contribution`() {
        val result = useCase(CalculationInput(100_000.0, 10.0, 1, 0.0))
        assertEquals(110_000.0, result.finalValue, 0.01)
        assertEquals(100_000.0, result.totalContributed, 0.01)
        assertEquals(10_000.0, result.totalInterestEarned, 0.01)
        assertEquals(1, result.yearlySnapshots.size)
        assertNull(result.baselineSnapshots)
    }

    @Test
    fun `three years no monthly contribution compounds correctly`() {
        val result = useCase(CalculationInput(100_000.0, 10.0, 3, 0.0))
        assertEquals(133_100.0, result.finalValue, 0.01)
        assertEquals(110_000.0, result.yearlySnapshots[0].totalValue, 0.01)
        assertEquals(121_000.0, result.yearlySnapshots[1].totalValue, 0.01)
        assertEquals(133_100.0, result.yearlySnapshots[2].totalValue, 0.01)
        assertNull(result.baselineSnapshots)
    }

    @Test
    fun `with monthly contribution of 1000 per month`() {
        // monthlyContribution = 1000, annualEquivalent = 12000
        // Year 1: (100000 + 12000) * 1.1 = 123200
        // Year 2: (123200 + 12000) * 1.1 = 148720
        val result = useCase(CalculationInput(100_000.0, 10.0, 2, 1_000.0))
        assertEquals(148_720.0, result.finalValue, 0.01)
        assertEquals(124_000.0, result.totalContributed, 0.01)
        assertEquals(24_720.0, result.totalInterestEarned, 0.01)
        assertNotNull(result.baselineSnapshots)
    }

    @Test
    fun `baseline snapshots reflect growth without monthly contributions`() {
        val result = useCase(CalculationInput(100_000.0, 10.0, 2, 1_000.0))
        val baseline = result.baselineSnapshots!!
        // Baseline Year 1: 100000 * 1.1 = 110000
        // Baseline Year 2: 110000 * 1.1 = 121000
        assertEquals(2, baseline.size)
        assertEquals(110_000.0, baseline[0].totalValue, 0.01)
        assertEquals(121_000.0, baseline[1].totalValue, 0.01)
        assertEquals(100_000.0, baseline[0].totalContributed, 0.01)
        assertEquals(100_000.0, baseline[1].totalContributed, 0.01)
    }

    @Test
    fun `zero ROI with monthly contribution returns sum of contributions`() {
        val result = useCase(CalculationInput(100_000.0, 0.0, 3, 500.0))
        // annual = 6000/year; Y1=106000, Y2=112000, Y3=118000
        assertEquals(118_000.0, result.finalValue, 0.01)
        assertEquals(0.0, result.totalInterestEarned, 0.01)
    }

    @Test
    fun `snapshot totalContributed tracks principal plus monthly contributions per year`() {
        val result = useCase(CalculationInput(50_000.0, 5.0, 2, 1_000.0))
        // annual = 12000/year
        // Y1 contributed: 50000 + 12000 = 62000
        // Y2 contributed: 50000 + 24000 = 74000
        assertEquals(62_000.0, result.yearlySnapshots[0].totalContributed, 0.01)
        assertEquals(74_000.0, result.yearlySnapshots[1].totalContributed, 0.01)
    }
}
