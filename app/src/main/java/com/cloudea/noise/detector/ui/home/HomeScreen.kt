package com.cloudea.noise.detector.ui.home

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cloudea.noise.detector.audio.DbLevel
import com.cloudea.noise.detector.audio.DbMath
import com.cloudea.noise.detector.ui.components.DbGauge
import com.cloudea.noise.detector.ui.components.GUGA_GAUGE_HI
import com.cloudea.noise.detector.ui.components.LevelChip
import com.cloudea.noise.detector.ui.components.MascotBadge
import com.cloudea.noise.detector.ui.components.StatGrid
import com.cloudea.noise.detector.ui.components.WaveMode
import com.cloudea.noise.detector.ui.components.WaveformChart
import com.cloudea.noise.detector.ui.components.fmt1
import com.cloudea.noise.detector.ui.components.formatClock
import com.cloudea.noise.detector.ui.theme.BtnShadowPause
import com.cloudea.noise.detector.ui.theme.BtnShadowRun
import com.cloudea.noise.detector.ui.theme.GugaAccentInk
import com.cloudea.noise.detector.ui.theme.GugaCream
import com.cloudea.noise.detector.ui.theme.GugaInk
import com.cloudea.noise.detector.ui.theme.GugaInk2
import com.cloudea.noise.detector.ui.theme.GugaLine
import com.cloudea.noise.detector.ui.theme.GugaPink
import com.cloudea.noise.detector.ui.theme.GugaWhite
import com.cloudea.noise.detector.ui.theme.Tabular

private val ButtonBorder = Color(0xFFF2B9CD)
private val WaveHi = GUGA_GAUGE_HI

@Composable
fun HomeScreen(
    state: DetectorUiState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val level = DbLevel.of(state.currentDb)
    val running = state.state == DetectorState.RUNNING

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 6.dp, bottom = 16.dp),
    ) {
        // ---- 顶部：标题 + 定位 + 角色头像 ----
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = "咕嗄声音检测",
                    style = MaterialTheme.typography.headlineLarge,
                    color = GugaInk,
                )
                Row(
                    modifier = Modifier.padding(top = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LocationPin()
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = state.locationLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = GugaInk2,
                    )
                }
            }
            MascotBadge(level = level, size = 44.dp)
        }

        // ---- 角色 + 台词气泡 ----
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            MascotBadge(level = level, size = 94.dp)
            Spacer(Modifier.width(11.dp))
            SpeechBubble(level.phrase)
        }

        // ---- 仪表 + 实时读数 ----
        Spacer(Modifier.height(8.dp))
        DbGauge(value = state.currentDb.coerceIn(DbMath.FLOOR_DB, GUGA_GAUGE_HI))
        Column(
            modifier = Modifier.fillMaxWidth().offset(y = (-6).dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = state.currentDb.fmt1(),
                    style = MaterialTheme.typography.displayLarge.merge(Tabular),
                    color = GugaInk,
                )
                Text(
                    text = "dB",
                    style = MaterialTheme.typography.headlineMedium,
                    color = GugaInk2,
                    modifier = Modifier.padding(start = 6.dp, bottom = 6.dp),
                )
            }
            Spacer(Modifier.height(9.dp))
            LevelChip(level)
            Spacer(Modifier.height(9.dp))
            Text(
                text = subLine(state),
                style = MaterialTheme.typography.labelMedium,
                color = GugaInk2,
            )
        }

        // ---- 四统计值 ----
        Spacer(Modifier.height(14.dp))
        StatGrid(
            min = state.minDb,
            avg = state.avgDb,
            median = state.medianDb,
            max = state.maxDb,
        )

        // ---- 波形 ----
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
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "近 60 秒波形",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = GugaInk,
                )
                Text(
                    text = "峰值 ${state.maxDb.fmt1()} dB · 抖动 ${DbMath.stdDev(state.recent).fmt1()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = GugaInk2,
                )
            }
            Spacer(Modifier.height(8.dp))
            WaveformChart(
                data = state.recent,
                lo = 28f,
                hi = WaveHi,
                modifier = Modifier.fillMaxWidth().height(88.dp),
                mode = WaveMode.FULL,
            )
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("−60 s", style = MaterialTheme.typography.labelMedium, color = GugaInk2)
                Text("−30 s", style = MaterialTheme.typography.labelMedium, color = GugaInk2)
                Text("现在", style = MaterialTheme.typography.labelMedium, color = GugaInk2)
            }
        }

        // ---- 主按钮 ----
        Spacer(Modifier.height(14.dp))
        GugaMainButton(
            running = running,
            onClick = { if (running) onPause() else onStart() },
        )

        // ---- 结束并保存（有会话时才显示）----
        if (state.state != DetectorState.IDLE) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = "结束并保存记录",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = GugaAccentInk,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onFinish)
                    .padding(vertical = 10.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }

        state.error?.let { err ->
            Spacer(Modifier.height(10.dp))
            Text(
                text = "⚠️ $err",
                style = MaterialTheme.typography.bodyMedium,
                color = GugaAccentInk,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun subLine(state: DetectorUiState): String {
    val clock = formatClock(state.elapsedSeconds)
    return when (state.state) {
        DetectorState.RUNNING -> "检测中 $clock · 采样 48 kHz · 未校准 · 近似值"
        DetectorState.PAUSED -> "已暂停 $clock · 采样 48 kHz · 未校准 · 近似值"
        DetectorState.IDLE -> "点「开始检测」开始测量 · 未校准 · 近似值"
    }
}

@Composable
private fun LocationPin() {
    Canvas(Modifier.size(11.dp, 14.dp)) {
        val u = size.width / 12f
        val path = Path().apply {
            moveTo(6f * u, 14.5f * u)
            cubicTo(6f * u, 14.5f * u, 1.4f * u, 8.4f * u, 1.4f * u, 5.8f * u)
            cubicTo(1.4f * u, 3.2f * u, 3.4f * u, 1.2f * u, 6f * u, 1.2f * u)
            cubicTo(8.6f * u, 1.2f * u, 10.6f * u, 3.2f * u, 10.6f * u, 5.8f * u)
            cubicTo(10.6f * u, 8.4f * u, 6f * u, 14.5f * u, 6f * u, 14.5f * u)
            close()
        }
        drawPath(path, GugaAccentInk, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.4f * u))
        drawCircle(GugaAccentInk, radius = 1.6f * u, center = Offset(6f * u, 5.8f * u))
    }
}

@Composable
private fun SpeechBubble(text: String) {
    Box(contentAlignment = Alignment.CenterStart) {
        Box(
            Modifier
                .size(12.dp)
                .rotate(45f)
                .background(GugaWhite)
                .border(1.5.dp, GugaLine, RoundedCornerShape(2.dp)),
        )
        Box(
            Modifier
                .padding(start = 5.dp)
                .background(GugaWhite, RoundedCornerShape(16.dp))
                .border(1.5.dp, GugaLine, RoundedCornerShape(16.dp))
                .padding(horizontal = 13.dp, vertical = 9.dp),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(lineHeight = 19.sp),
                color = GugaInk,
            )
        }
    }
}

@Composable
private fun GugaMainButton(running: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val shape = RoundedCornerShape(29.dp)
    val bg = if (running) GugaWhite else GugaPink
    val shadow = if (running) BtnShadowPause else BtnShadowRun
    val contentColor = if (running) GugaAccentInk else GugaInk
    val pressOffset by animateDpAsState(if (pressed) 5.dp else 0.dp, label = "press")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(65.dp),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(58.dp)
                .offset(y = 7.dp)
                .background(shadow, shape),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .offset(y = pressOffset)
                .clip(shape)
                .background(bg)
                .then(if (running) Modifier.border(2.5.dp, ButtonBorder, shape) else Modifier)
                .clickable(interactionSource = interaction, indication = null, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(Modifier.size(16.dp)) {
                    val w = size.width
                    val h = size.height
                    if (running) {
                        drawRoundRect(
                            color = contentColor,
                            topLeft = Offset(w * 0.16f, h * 0.10f),
                            size = Size(w * 0.24f, h * 0.80f),
                            cornerRadius = CornerRadius(w * 0.12f),
                        )
                        drawRoundRect(
                            color = contentColor,
                            topLeft = Offset(w * 0.60f, h * 0.10f),
                            size = Size(w * 0.24f, h * 0.80f),
                            cornerRadius = CornerRadius(w * 0.12f),
                        )
                    } else {
                        val path = Path().apply {
                            moveTo(w * 0.24f, h * 0.15f)
                            lineTo(w * 0.84f, h * 0.50f)
                            lineTo(w * 0.24f, h * 0.85f)
                            close()
                        }
                        drawPath(path, contentColor)
                    }
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = if (running) "暂停检测" else "开始检测",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp, letterSpacing = 1.sp),
                    color = contentColor,
                )
            }
        }
    }
}
