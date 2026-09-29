package com.cloudea.noise.detector.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cloudea.noise.detector.ui.theme.GugaAccentInk
import com.cloudea.noise.detector.ui.theme.GugaAccentLine
import com.cloudea.noise.detector.ui.theme.GugaPink

enum class WaveMode { FULL, SPARK }

/**
 * 分贝波形图。FULL 用于主页/详情，SPARK 用于历史卡片缩略图。
 */
@Composable
fun WaveformChart(
    data: List<Float>,
    lo: Float,
    hi: Float,
    modifier: Modifier = Modifier,
    mode: WaveMode = WaveMode.FULL,
    accentColor: Color = GugaAccentLine,
) {
    val fillColor = if (mode == WaveMode.FULL) GugaPink else accentColor
    val fillAlpha = if (mode == WaveMode.FULL) 0.42f else 0.18f
    val lineWidth: Dp = if (mode == WaveMode.FULL) 2.6.dp else 2.2.dp

    Canvas(modifier) {
        if (data.size < 2) return@Canvas
        val pad = 4.dp.toPx()
        val w = size.width
        val h = size.height
        val span = (hi - lo).takeIf { it > 0f } ?: 1f

        fun x(i: Int) = pad + (i.toFloat() / (data.size - 1)) * (w - pad * 2)
        fun y(v: Float) = (h - pad) - (((v - lo) / span).coerceIn(0f, 1f)) * (h - pad * 2)

        val line = Path()
        data.forEachIndexed { i, v ->
            if (i == 0) line.moveTo(x(i), y(v)) else line.lineTo(x(i), y(v))
        }
        val area = Path().apply {
            addPath(line)
            lineTo(x(data.size - 1), h - pad)
            lineTo(x(0), h - pad)
            close()
        }

        drawPath(area, fillColor.copy(alpha = fillAlpha))

        if (mode == WaveMode.FULL) {
            val mean = data.average().toFloat()
            drawLine(
                color = GugaAccentInk.copy(alpha = 0.45f),
                start = Offset(pad, y(mean)),
                end = Offset(w - pad, y(mean)),
                strokeWidth = 1.4.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 5f)),
            )
        }

        drawPath(
            path = line,
            color = accentColor,
            style = Stroke(
                width = lineWidth.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
    }
}
