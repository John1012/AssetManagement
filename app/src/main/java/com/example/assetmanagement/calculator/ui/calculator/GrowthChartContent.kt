package com.example.assetmanagement.calculator.ui.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.example.assetmanagement.calculator.domain.model.YearlySnapshot
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Fill

private val dcaColor = Color(0xFF1976D2)
private val noDcaColor = Color(0xFFE64A19)
private val contributedColor = Color(0xFF388E3C)

@Composable
fun GrowthChartContent(
    snapshots: List<YearlySnapshot>,
    baselineSnapshots: List<YearlySnapshot>?,
    modifier: Modifier = Modifier
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    val line1 = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(dcaColor.toArgb()))
    )
    val line2 = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(noDcaColor.toArgb()))
    )
    val line3 = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(contributedColor.toArgb()))
    )
    val layer = rememberLineCartesianLayer(
        LineCartesianLayer.LineProvider.series(line1, line2, line3)
    )

    LaunchedEffect(snapshots, baselineSnapshots) {
        modelProducer.runTransaction {
            lineSeries {
                if (baselineSnapshots != null) {
                    series(snapshots.map { it.totalValue.toFloat() })
                    series(baselineSnapshots.map { it.totalValue.toFloat() })
                    series(snapshots.map { it.totalContributed.toFloat() })
                } else {
                    series(snapshots.map { it.totalValue.toFloat() })
                    series(snapshots.map { it.totalContributed.toFloat() })
                }
            }
        }
    }

    Column(modifier = modifier) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                layer,
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom(),
            ),
            modelProducer = modelProducer,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        )
        if (baselineSnapshots != null) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LegendItem("With DCA", dcaColor)
                LegendItem("Without DCA", noDcaColor)
                LegendItem("Contributed", contributedColor)
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
