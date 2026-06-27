package com.example.assetmanagement.arithmetic.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.assetmanagement.arithmetic.domain.CalculatorAction
import com.example.assetmanagement.arithmetic.domain.Operator

@Composable
fun ArithmeticScreen(viewModel: ArithmeticViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onAction = viewModel::onAction

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.BottomEnd
        ) {
            Text(
                text = state.display,
                fontSize = 64.sp,
                maxLines = 1,
                textAlign = TextAlign.End,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        val rows: List<List<CalcButton>> = listOf(
            listOf(
                CalcButton.Action("C", CalculatorAction.Clear, isFunction = true),
                CalcButton.Action("⌫", CalculatorAction.Backspace, isFunction = true),
                CalcButton.Action("%", CalculatorAction.Percent, isFunction = true),
                CalcButton.Action("÷", CalculatorAction.Op(Operator.Divide), isOperator = true)
            ),
            listOf(
                CalcButton.Digit(7), CalcButton.Digit(8), CalcButton.Digit(9),
                CalcButton.Action("×", CalculatorAction.Op(Operator.Times), isOperator = true)
            ),
            listOf(
                CalcButton.Digit(4), CalcButton.Digit(5), CalcButton.Digit(6),
                CalcButton.Action("−", CalculatorAction.Op(Operator.Minus), isOperator = true)
            ),
            listOf(
                CalcButton.Digit(1), CalcButton.Digit(2), CalcButton.Digit(3),
                CalcButton.Action("+", CalculatorAction.Op(Operator.Plus), isOperator = true)
            ),
            listOf(
                CalcButton.Action("+/−", CalculatorAction.ToggleSign, isFunction = true),
                CalcButton.Digit(0),
                CalcButton.Action(".", CalculatorAction.Decimal),
                CalcButton.Action("=", CalculatorAction.Equals, isOperator = true)
            ),
        )

        rows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { button ->
                    KeypadButton(
                        button = button,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        onClick = { onAction(button.action) }
                    )
                }
            }
        }
    }
}

private sealed class CalcButton(val label: String, val action: CalculatorAction) {
    class Digit(value: Int) : CalcButton(value.toString(), CalculatorAction.Digit(value))
    class Action(
        label: String,
        action: CalculatorAction,
        val isOperator: Boolean = false,
        val isFunction: Boolean = false
    ) : CalcButton(label, action)
}

@Composable
private fun KeypadButton(
    button: CalcButton,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isOperator = button is CalcButton.Action && button.isOperator
    val isFunction = button is CalcButton.Action && button.isFunction
    val containerColor = when {
        isOperator -> MaterialTheme.colorScheme.primary
        isFunction -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = when {
        isOperator -> MaterialTheme.colorScheme.onPrimary
        isFunction -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Button(
        onClick = onClick,
        modifier = modifier.padding(vertical = 4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Text(text = button.label, fontSize = 24.sp, color = Color.Unspecified)
    }
}
