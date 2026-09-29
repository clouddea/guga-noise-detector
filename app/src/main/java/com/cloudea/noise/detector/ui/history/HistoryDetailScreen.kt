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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cloudea.noise.detector.data.LocationProvider
import com.cloudea.noise.detector.data.Session
import com.cloudea.noise.detector.ui.components.LevelChip
import com.cloudea.noise.detector.ui.components.MascotBadge
import com.cloudea.noise.detector.ui.components.StatGrid
import com.cloudea.noise.detector.ui.components.WaveMode
import com.cloudea.noise.detector.ui.components.WaveformChart
import com.cloudea.noise.detector.ui.components.fmt1
import com.cloudea.noise.detector.ui.components.formatDuration
import com.cloudea.noise.detector.ui.components.formatRange
import com.cloudea.noise.detector.ui.theme.DialHalo
import com.cloudea.noise.detector.ui.theme.GugaAccentInk
import com.cloudea.noise.detector.ui.theme.GugaInk
import com.cloudea.noise.detector.ui.theme.GugaInk2
import com.cloudea.noise.detector.ui.theme.GugaLine
import com.cloudea.noise.detector.ui.theme.GugaWhite
import com.cloudea.noise.detector.ui.theme.Tabular

@Composable
fun HistoryDetailScreen(
    session: Session,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val level = session.level
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 6.dp, bottom = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(GugaWhite, CircleShape)
                    .border(1.5.dp, GugaLine, CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                BackArrow()
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = "检测详情",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                color = GugaInk,
            )
        }

        // ---- Hero ----
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DialHalo, RoundedCornerShape(24.dp))
                .padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 15.dp),
        ) {
            MascotBadge(
                level = level,
                size = 56.dp,
                modifier = Modifier.align(Alignment.TopEnd),
            )
            Column {
                Text(
                    text = session.placeLabel,
                    style = MaterialTheme.typography.headlineMedium,
                    color = GugaInk,
                    modifier = Modifier.padding(end = 60.dp),
                )
                Text(
                    text = "${formatRange(session.startMillis, session.endMillis)} · ${formatDuration(session.durationSeconds)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = GugaInk2,
                    modifier = Modifier.padding(top = 4.dp, end = 60.dp),
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = session.avgDb.fmt1(),
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 44.sp).merge(Tabular),
                            color = GugaInk,
                        )
                        Text(
                            text = " 平均 dB",
                            style = MaterialTheme.typography.labelLarge,
                            color = GugaInk2,
                            modifier = Modifier.padding(bottom = 5.dp),
                        )
                    }
                    LevelChip(level)
                }
            }
        }

        // ---- 四统计值 ----
        Spacer(Modifier.height(14.dp))
        StatGrid(
            min = session.minDb,
            avg = session.avgDb,
            median = session.medianDb,
            max = session.maxDb,
        )

        // ---- 完整波形 ----
        Spacer(Modifier.height(13.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(GugaWhite, RoundedCornerShape(22.dp))
                .border(1.5.dp, GugaLine, RoundedCornerShape(22.dp))
                .padding(start = 14.dp, end = 14.dp, top = 13.dp, bottom = 10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "完整波形",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = GugaInk,
                )
                Text(
                    text = "${formatDuration(session.durationSeconds)} · 采样 48 kHz",
                    style = MaterialTheme.typography.labelMedium,
                    color = GugaInk2,
                )
            }
            Spacer(Modifier.height(8.dp))
            WaveformChart(
                data = session.waveform,
                lo = 26f,
                hi = 96f,
                modifier = Modifier.fillMaxWidth().height(104.dp),
                mode = WaveMode.FULL,
            )
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("开始", style = MaterialTheme.typography.labelMedium, color = GugaInk2)
                Text("中段", style = MaterialTheme.typography.labelMedium, color = GugaInk2)
                Text("结束", style = MaterialTheme.typography.labelMedium, color = GugaInk2)
            }
        }

        // ---- 元信息 ----
        Spacer(Modifier.height(13.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(GugaWhite, RoundedCornerShape(22.dp))
                .border(1.5.dp, GugaLine, RoundedCornerShape(22.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp),
        ) {
            MetaRow("地点", coordText(session))
            MetaDivider()
            MetaRow("时长", "${formatDuration(session.durationSeconds)}（${formatRange(session.startMillis, session.endMillis)}）")
            MetaDivider()
            MetaRow("近似等效声级 Leq", "${session.avgDb.fmt1()} dB（未计权）")
            MetaDivider()
            MetaRow("设备", "手机内置麦克风 · 未校准")
        }
    }
}

private fun coordText(session: Session): String {
    val lat = session.latitude
    val lng = session.longitude
    if (lat == null || lng == null) return session.placeLabel
    // placeLabel 本身可能已经是「城市 · 坐标」形式，避免重复拼接
    return if (session.placeLabel.contains("°")) {
        session.placeLabel
    } else {
        "${session.placeLabel} · ${LocationProvider.formatCoord(lat, lng)}"
    }
}

@Composable
private fun MetaRow(key: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(key, style = MaterialTheme.typography.bodyMedium, color = GugaInk2)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = GugaInk,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
}

@Composable
private fun MetaDivider() {
    Spacer(Modifier.fillMaxWidth().height(1.5.dp).background(GugaLine))
}

@Composable
private fun BackArrow() {
    Canvas(Modifier.size(20.dp)) {
        val u = size.width / 24f
        drawPath(
            path = Path().apply {
                moveTo(14.8f * u, 5f * u)
                lineTo(8f * u, 12f * u)
                lineTo(14.8f * u, 19f * u)
            },
            color = GugaAccentInk,
            style = Stroke(width = 2.8f * u, cap = StrokeCap.Round),
        )
    }
}
