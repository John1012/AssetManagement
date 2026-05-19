package com.example.assetmanagement

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
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
import com.example.assetmanagement.calculator.ui.calculator.CalculatorScreen
import com.example.assetmanagement.calculator.ui.history.HistoryScreen

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(CalculatorKey())
    val current = backStack.lastOrNull()

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = current is CalculatorKey,
                    onClick = { backStack.clear(); backStack.add(CalculatorKey()) },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Calculator") }
                )
                NavigationBarItem(
                    selected = current == HistoryKey,
                    onClick = { backStack.clear(); backStack.add(HistoryKey) },
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
                entry<CalculatorKey> { key ->
                    CalculatorScreen(
                        prefillFund = key.prefillFund,
                        prefillROI = key.prefillROI,
                        prefillYears = key.prefillYears,
                        prefillContribution = key.prefillContribution,
                        hasPrefill = key.hasPrefill
                    )
                }
                entry<HistoryKey> {
                    HistoryScreen(
                        onItemClick = { item ->
                            backStack.clear()
                            backStack.add(
                                CalculatorKey(
                                    prefillFund = item.initialFund,
                                    prefillROI = item.annualROI,
                                    prefillYears = item.durationYears,
                                    prefillContribution = item.annualContribution,
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
