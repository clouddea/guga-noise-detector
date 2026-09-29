package com.cloudea.noise.detector.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cloudea.noise.detector.audio.DbLevel
import com.cloudea.noise.detector.ui.theme.GugaInk

/** 等级胶囊标签：安静 / 正常 / 嘈杂 / 震耳 */
@Composable
fun LevelChip(level: DbLevel, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(24.dp)
            .background(level.bg, RoundedCornerShape(12.dp))
            .padding(horizontal = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(7.dp).background(level.fg, CircleShape))
        Spacer(Modifier.width(5.dp))
        Text(
            text = level.label,
            style = MaterialTheme.typography.labelLarge,
            color = GugaInk,
        )
    }
}
