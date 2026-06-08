package com.example.assetmanagement.calculator.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.assetmanagement.calculator.domain.model.HistoryItem
import androidx.compose.ui.res.stringResource
import com.example.assetmanagement.R
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    onItemClick: (HistoryItem) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(stringResource(R.string.history_title), style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        when (val s = state) {
            HistoryUiState.Loading -> Text(stringResource(R.string.history_loading))
            HistoryUiState.Empty -> Text(stringResource(R.string.history_empty))
            is HistoryUiState.Success -> {
                LazyColumn {
                    items(s.items, key = { it.id }) { item ->
                        SwipeToDismissHistoryCard(
                            item = item,
                            onDelete = { viewModel.delete(item.id) },
                            onClick = { onItemClick(item) }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDismissHistoryCard(item: HistoryItem, onDelete: () -> Unit, onClick: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) { onDelete(); true } else false
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.error).padding(end = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) { Text(stringResource(R.string.history_delete), color = MaterialTheme.colorScheme.onError) }
        }
    ) { HistoryItemCard(item = item, onClick = onClick) }
}

@Composable
private fun HistoryItemCard(item: HistoryItem, onClick: () -> Unit) {
    val numFmt = NumberFormat.getNumberInstance(Locale.TAIWAN)
    val dateFmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(dateFmt.format(Date(item.savedAt)), style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(
                    R.string.history_summary,
                    numFmt.format(item.initialFund.toLong()),
                    item.annualROI,
                    item.durationYears
                )
            )
            Text(
                stringResource(R.string.history_final, numFmt.format(item.finalValue.toLong())),
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}
