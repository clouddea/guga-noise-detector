package com.cloudea.noise.detector.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cloudea.noise.detector.ui.theme.GugaInk
import com.cloudea.noise.detector.ui.theme.StatAvgBg
import com.cloudea.noise.detector.ui.theme.StatMaxBg
import com.cloudea.noise.detector.ui.theme.StatMedBg
import com.cloudea.noise.detector.ui.theme.StatMinBg
import com.cloudea.noise.detector.ui.theme.Tabular

/** 四统计值：最小 / 平均 / 中位 / 最大 */
@Composable
fun StatGrid(
    min: Float,
    avg: Float,
    median: Float,
    max: Float,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        StatBlock("最小", min, StatMinBg, Modifier.weight(1f))
        StatBlock("平均", avg, StatAvgBg, Modifier.weight(1f))
        StatBlock("中位", median, StatMedBg, Modifier.weight(1f))
        StatBlock("最大", max, StatMaxBg, Modifier.weight(1f))
    }
}

@Composable
private fun StatBlock(label: String, value: Float, bg: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(bg, RoundedCornerShape(18.dp))
            .padding(vertical = 11.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = GugaInk.copy(alpha = 0.72f),
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value.fmt1(),
                style = MaterialTheme.typography.headlineMedium.merge(Tabular),
                fontWeight = FontWeight.Black,
                fontSize = 19.sp,
                color = GugaInk,
            )
            Text(
                text = "dB",
                style = MaterialTheme.typography.labelMedium,
                color = GugaInk.copy(alpha = 0.62f),
                modifier = Modifier.padding(start = 2.dp),
            )
        }
    }
}
