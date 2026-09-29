package com.cloudea.noise.detector.ui.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cloudea.noise.detector.audio.DbLevel
import com.cloudea.noise.detector.data.Session
import com.cloudea.noise.detector.ui.components.MascotBadge
import com.cloudea.noise.detector.ui.components.WaveMode
import com.cloudea.noise.detector.ui.components.WaveformChart
import com.cloudea.noise.detector.ui.components.fmt1
import com.cloudea.noise.detector.ui.components.formatDuration
import com.cloudea.noise.detector.ui.components.formatRange
import com.cloudea.noise.detector.ui.theme.GugaAccentInk
import com.cloudea.noise.detector.ui.theme.GugaCream
import com.cloudea.noise.detector.ui.theme.GugaInk
import com.cloudea.noise.detector.ui.theme.GugaInk2
import com.cloudea.noise.detector.ui.theme.GugaLine
import com.cloudea.noise.detector.ui.theme.GugaPink
import com.cloudea.noise.detector.ui.theme.GugaPinkSoft
import com.cloudea.noise.detector.ui.theme.GugaWhite
import com.cloudea.noise.detector.ui.theme.Tabular

private enum class HistFilter(val label: String, val level: DbLevel?) {
    ALL("全部", null),
    QUIET("安静", DbLevel.QUIET),
    NORMAL("正常", DbLevel.NORMAL),
    NOISY("嘈杂", DbLevel.NOISY),
    LOUD("震耳", DbLevel.LOUD),
}

@Composable
fun HistoryScreen(
    sessions: List<Session>,
    onOpen: (Long) -> Unit,
    onGoDetect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var filter by remember { mutableStateOf(HistFilter.ALL) }
    val list = if (filter.level == null) sessions else sessions.filter { it.level == filter.level }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 6.dp, bottom = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = "检测历史",
                style = MaterialTheme.typography.headlineLarge,
                color = GugaInk,
            )
            Text(
                text = "共 ${list.size} 条记录",
                style = MaterialTheme.typography.labelMedium,
                color = GugaInk2,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            HistFilter.entries.forEach { f ->
                FilterChip(
                    label = f.label,
                    selected = filter == f,
                    onClick = { filter = f },
                )
            }
        }

        if (list.isEmpty()) {
            EmptyState(onGoDetect)
        } else {
            list.forEach { session ->
                HistoryCard(session, onClick = { onOpen(session.id) })
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
        color = if (selected) GugaAccentInk else GugaInk2,
        modifier = Modifier
            .background(if (selected) GugaPinkSoft else GugaWhite, RoundedCornerShape(14.dp))
            .border(
                1.5.dp,
                if (selected) androidx.compose.ui.graphics.Color(0xFFF4C4D6) else GugaLine,
                RoundedCornerShape(14.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
    )
}

@Composable
private fun HistoryCard(session: Session, onClick: () -> Unit) {
    val level = session.level
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(GugaWhite, RoundedCornerShape(22.dp))
            .border(1.5.dp, GugaLine, RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(start = 15.dp, end = 15.dp, top = 13.dp, bottom = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MascotBadge(level = level, size = 36.dp)
            Spacer(Modifier.width(9.dp))
            Text(
                text = session.placeLabel,
                style = MaterialTheme.typography.titleMedium,
                color = GugaInk,
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
            Box(
                Modifier
                    .height(24.dp)
                    .background(level.bg, RoundedCornerShape(12.dp))
                    .padding(horizontal = 11.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(7.dp).background(level.fg, RoundedCornerShape(4.dp)))
                    Spacer(Modifier.width(5.dp))
                    Text(level.label, style = MaterialTheme.typography.labelLarge, color = GugaInk)
                }
            }
        }

        Text(
            text = "${formatRange(session.startMillis, session.endMillis)} · ${formatDuration(session.durationSeconds)}",
            style = MaterialTheme.typography.labelMedium,
            color = GugaInk2,
            modifier = Modifier.padding(top = 3.dp),
        )

        WaveformChart(
            data = session.waveform,
            lo = 26f,
            hi = 94f,
            modifier = Modifier.fillMaxWidth().height(34.dp).padding(vertical = 9.dp),
            mode = WaveMode.SPARK,
            accentColor = level.fg,
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            MetricInline("平均", session.avgDb)
            Spacer(Modifier.width(16.dp))
            MetricInline("最大", session.maxDb)
            Spacer(Modifier.weight(1f))
            Chevron()
        }
    }
}

@Composable
private fun MetricInline(label: String, value: Float) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = value.fmt1(),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Black,
                fontSize = 14.5.sp,
            ).merge(Tabular),
            color = GugaInk,
        )
        Text(
            text = " $label dB",
            style = MaterialTheme.typography.labelMedium,
            color = GugaInk2,
        )
    }
}

@Composable
private fun Chevron() {
    Canvas(Modifier.size(18.dp)) {
        val u = size.width / 24f
        drawPath(
            path = androidx.compose.ui.graphics.Path().apply {
                moveTo(9.5f * u, 5.5f * u)
                lineTo(16f * u, 12f * u)
                lineTo(9.5f * u, 18.5f * u)
            },
            color = androidx.compose.ui.graphics.Color(0xFFC9B8BF),
            style = Stroke(width = 2.6f * u, cap = StrokeCap.Round),
        )
    }
}

@Composable
private fun EmptyState(onGoDetect: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MascotBadge(
            sticker = com.cloudea.noise.detector.R.drawable.gg_wave,
            bg = com.cloudea.noise.detector.ui.theme.DialHalo,
            size = 128.dp,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "还没有检测记录",
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.5.sp),
            color = GugaInk,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "去「检测」页开启第一次声音测量，\n咕嗄会陪你一起聆听世界～",
            style = MaterialTheme.typography.labelLarge.copy(lineHeight = 21.sp),
            color = GugaInk2,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Spacer(Modifier.height(18.dp))
        Text(
            text = "开始第一次检测",
            style = MaterialTheme.typography.titleMedium,
            color = GugaInk,
            modifier = Modifier
                .background(GugaPink, RoundedCornerShape(29.dp))
                .clickable(onClick = onGoDetect)
                .padding(horizontal = 34.dp, vertical = 16.dp),
        )
    }
}
