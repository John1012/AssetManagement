package com.example.assetmanagement.loan.ui.loan

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
import com.example.assetmanagement.loan.domain.model.LoanInput
import com.example.assetmanagement.loan.domain.model.LoanResult
import java.text.NumberFormat
import java.util.Locale

@Composable
fun LoanScreen(
    prefillAmount: Double = 0.0,
    prefillRate: Double = 0.0,
    prefillMonths: Int = 0,
    hasPrefill: Boolean = false,
    modifier: Modifier = Modifier,
    viewModel: LoanViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var amountText by remember { mutableStateOf("") }
    var rateText by remember { mutableStateOf("") }
    var monthsText by remember { mutableStateOf("") }

    LaunchedEffect(hasPrefill) {
        if (hasPrefill) {
            amountText = prefillAmount.toLong().toString()
            rateText = prefillRate.toString()
            monthsText = prefillMonths.toString()
            viewModel.prefill(prefillAmount, prefillRate, prefillMonths)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Loan Calculator", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text("Loan Amount (NT\$)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = rateText,
            onValueChange = { rateText = it },
            label = { Text("Annual Interest Rate (%)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = monthsText,
            onValueChange = { monthsText = it },
            label = { Text("Loan Term (months, 1–360)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.calculate(
                    LoanInput(
                        loanAmount = amountText.toDoubleOrNull() ?: 0.0,
                        annualRate = rateText.toDoubleOrNull() ?: 0.0,
                        termMonths = monthsText.toIntOrNull()?.coerceIn(1, 360) ?: 1
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Calculate") }

        Spacer(Modifier.height(24.dp))

        when (val s = state) {
            is LoanUiState.ShowingResult -> {
                LoanChartContent(schedule = s.result.schedule)
                Spacer(Modifier.height(16.dp))
                LoanResultCard(result = s.result)
            }
            is LoanUiState.Error -> Text("Error: ${s.message}", color = MaterialTheme.colorScheme.error)
            LoanUiState.Idle -> {}
        }
    }
}

@Composable
private fun LoanResultCard(result: LoanResult) {
    val fmt = NumberFormat.getNumberInstance(Locale.TAIWAN)
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Results", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text("Monthly Payment: NT\$${fmt.format(result.monthlyPayment.toLong())}")
            Text("Total Repayment: NT\$${fmt.format(result.totalRepayment.toLong())}")
            Text(
                "Total Interest: NT\$${fmt.format(result.totalInterest.toLong())}",
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
