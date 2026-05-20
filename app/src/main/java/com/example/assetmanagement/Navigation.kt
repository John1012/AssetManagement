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
import com.example.assetmanagement.loan.LoanNavigation
import com.example.assetmanagement.ui.home.HomeScreen

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(HomeKey)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<HomeKey> {
                HomeScreen(
                    onCompoundClick = { backStack.add(CalculatorKey()) },
                    onLoanClick = { backStack.add(LoanKey) }
                )
            }
            entry<CalculatorKey> { key ->
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = true,
                                onClick = {},
                                icon = { Icon(Icons.Default.Home, contentDescription = null) },
                                label = { Text("Calculator") }
                            )
                            NavigationBarItem(
                                selected = false,
                                onClick = { backStack.clear(); backStack.add(HomeKey); backStack.add(HistoryKey) },
                                icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                                label = { Text("History") }
                            )
                        }
                    }
                ) { paddingValues ->
                    CalculatorScreen(
                        prefillFund = key.prefillFund,
                        prefillROI = key.prefillROI,
                        prefillYears = key.prefillYears,
                        prefillContribution = key.prefillContribution,
                        hasPrefill = key.hasPrefill,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
            entry<HistoryKey> {
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = false,
                                onClick = { backStack.clear(); backStack.add(HomeKey); backStack.add(CalculatorKey()) },
                                icon = { Icon(Icons.Default.Home, contentDescription = null) },
                                label = { Text("Calculator") }
                            )
                            NavigationBarItem(
                                selected = true,
                                onClick = {},
                                icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                                label = { Text("History") }
                            )
                        }
                    }
                ) { paddingValues ->
                    HistoryScreen(
                        onItemClick = { item ->
                            backStack.clear()
                            backStack.add(HomeKey)
                            backStack.add(
                                CalculatorKey(
                                    prefillFund = item.initialFund,
                                    prefillROI = item.annualROI,
                                    prefillYears = item.durationYears,
                                    prefillContribution = item.annualContribution,
                                    hasPrefill = true
                                )
                            )
                        },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
            entry<LoanKey> {
                LoanNavigation()
            }
        }
    )
}
