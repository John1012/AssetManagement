package com.example.assetmanagement.calculator.ui.calculator

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.assetmanagement.calculator.domain.model.CalculationInput
import com.example.assetmanagement.calculator.domain.model.CalculationResult
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CalculatorScreen(
    prefillFund: Double = 0.0,
    prefillROI: Double = 0.0,
    prefillYears: Int = 0,
    prefillContribution: Double = 0.0,
    hasPrefill: Boolean = false,
    modifier: Modifier = Modifier,
    viewModel: CalculatorViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var fundText by remember { mutableStateOf("") }
    var roiText by remember { mutableStateOf("") }
    var yearsText by remember { mutableStateOf("") }
    var contributionText by remember { mutableStateOf("") }

    LaunchedEffect(hasPrefill) {
        if (hasPrefill) {
            fundText = prefillFund.toLong().toString()
            roiText = prefillROI.toString()
            yearsText = prefillYears.toString()
            contributionText = prefillContribution.toLong().toString()
            viewModel.prefill(prefillFund, prefillROI, prefillYears, prefillContribution)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Compound Calculator", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = fundText,
            onValueChange = { fundText = it },
            label = { Text("Initial Fund (NT\$)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = roiText,
            onValueChange = { roiText = it },
            label = { Text("Annual ROI (%)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = yearsText,
            onValueChange = { yearsText = it },
            label = { Text("Duration (years, 1–100)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = contributionText,
            onValueChange = { contributionText = it },
            label = { Text("Annual Contribution (NT\$, optional)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.calculate(
                    CalculationInput(
                        initialFund = fundText.toDoubleOrNull() ?: 0.0,
                        annualROI = roiText.toDoubleOrNull() ?: 0.0,
                        durationYears = yearsText.toIntOrNull()?.coerceIn(1, 100) ?: 1,
                        annualContribution = contributionText.toDoubleOrNull() ?: 0.0
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Calculate") }

        Spacer(Modifier.height(24.dp))

        when (val s = state) {
            is CalculatorUiState.ShowingResult -> {
                GrowthChartContent(snapshots = s.result.yearlySnapshots)
                Spacer(Modifier.height(16.dp))
                ResultSummaryCard(result = s.result)
            }
            is CalculatorUiState.Error -> Text("Error: ${s.message}", color = MaterialTheme.colorScheme.error)
            CalculatorUiState.Idle -> {}
        }
    }
}

@Composable
private fun ResultSummaryCard(result: CalculationResult) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Results", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            ResultRow("Final Value", result.finalValue)
            ResultRow("Total Contributed", result.totalContributed)
            ResultRow("Total Interest Earned", result.totalInterestEarned)
        }
    }
}

@Composable
private fun ResultRow(label: String, value: Double) {
    val formatted = NumberFormat.getNumberInstance(Locale.TAIWAN).format(value.toLong())
    Text("$label: NT\$$formatted")
}
