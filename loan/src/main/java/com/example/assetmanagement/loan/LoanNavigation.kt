package com.example.assetmanagement.loan

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.assetmanagement.loan.ui.history.LoanHistoryScreen
import com.example.assetmanagement.loan.ui.loan.LoanScreen

@Composable
fun LoanNavigation() {
    val backStack = rememberNavBackStack(LoanCalculatorKey())
    val current = backStack.lastOrNull()

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = current is LoanCalculatorKey,
                    onClick = { backStack.clear(); backStack.add(LoanCalculatorKey()) },
                    icon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
                    label = { Text("Loan") }
                )
                NavigationBarItem(
                    selected = current == LoanHistoryKey,
                    onClick = { backStack.clear(); backStack.add(LoanHistoryKey) },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    label = { Text("History") }
                )
            }
        }
    ) { paddingValues ->
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryProvider = entryProvider {
                entry<LoanCalculatorKey> { key ->
                    LoanScreen(
                        prefillAmount = key.prefillAmount,
                        prefillRate = key.prefillRate,
                        prefillMonths = key.prefillMonths,
                        hasPrefill = key.hasPrefill
                    )
                }
                entry<LoanHistoryKey> {
                    LoanHistoryScreen(
                        onItemClick = { item ->
                            backStack.clear()
                            backStack.add(
                                LoanCalculatorKey(
                                    prefillAmount = item.loanAmount,
                                    prefillRate = item.annualRate,
                                    prefillMonths = item.termMonths,
                                    hasPrefill = true
                                )
                            )
                        }
                    )
                }
            },
            modifier = Modifier.padding(paddingValues)
        )
    }
}
