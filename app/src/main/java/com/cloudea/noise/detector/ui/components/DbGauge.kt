package com.cloudea.noise.detector.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.cloudea.noise.detector.ui.theme.BandLoud
import com.cloudea.noise.detector.ui.theme.BandNoisy
import com.cloudea.noise.detector.ui.theme.BandNormal
import com.cloudea.noise.detector.ui.theme.BandQuiet
import com.cloudea.noise.detector.ui.theme.DialHalo
import com.cloudea.noise.detector.ui.theme.GugaAccent
import com.cloudea.noise.detector.ui.theme.GugaCream
import com.cloudea.noise.detector.ui.theme.GugaInk
import com.cloudea.noise.detector.ui.theme.GugaInk2
import com.cloudea.noise.detector.ui.theme.TickMajor
import com.cloudea.noise.detector.ui.theme.TickMinor
import kotlin.math.cos
import kotlin.math.sin

// 表盘几何照抄设计稿 dialSVG()：viewBox 0 0 300 200
private const val CX = 150f
private const val CY = 140f
private const val R = 96f
private const val A_START = 205f
private const val A_SWEEP = 230f
const val DB_MIN = 30f
const val DB_MAX = 90f

/** 仪表量程上限，供调用方做数值钳制与波形归一 */
const val GUGA_GAUGE_HI = DB_MAX

private const val VIEW_W = 300f

private fun angleFor(v: Float): Float {
    val f = ((v - DB_MIN) / (DB_MAX - DB_MIN)).coerceIn(0f, 1f)
    return A_START - A_SWEEP * f
}

/** 极坐标 → viewBox 坐标（y 轴向上，与 SVG 一致） */
private fun polar(aDeg: Float, r: Float): Offset {
    val rad = Math.toRadians(aDeg.toDouble())
    return Offset(CX + (r * cos(rad)).toFloat(), CY - (r * sin(rad)).toFloat())
}

@Composable
fun DbGauge(value: Float, modifier: Modifier = Modifier) {
    val measurer = rememberTextMeasurer()
    val labelStyle = TextStyle(
        color = GugaInk2,
        fontSize = 12.sp,
        fontWeight = FontWeight.ExtraBold,
        fontFeatureSettings = "tnum",
    )
    Canvas(modifier.fillMaxWidth().aspectRatio(VIEW_W / 200f)) {
        val s = size.width / VIEW_W
        scale(s, s, pivot = Offset.Zero) {
            drawGaugeShapes(value)
        }
        // 量程数字用真实像素绘制（不再叠加缩放），否则会被放大数倍
        for (t in intArrayOf(30, 60, 90)) {
            val p = polar(angleFor(t.toFloat()), 114f)
            val layout = measurer.measure(AnnotatedString(t.toString()), style = labelStyle)
            drawText(
                textLayoutResult = layout,
                topLeft = Offset(
                    p.x * s - layout.size.width / 2f,
                    p.y * s - layout.size.height / 2f,
                ),
            )
        }
    }
}

private fun DrawScope.drawGaugeShapes(value: Float) {
    // 柔和粉色光晕，位于表盘之后（允许溢出画布，形成设计稿里那圈粉色底盘）
    drawCircle(color = DialHalo, radius = 116f, center = Offset(CX, CY))

    // 四段彩带
    val gap = 2.6f
    val bands = listOf(
        Triple(30f, 45f, BandQuiet),
        Triple(45f, 60f, BandNormal),
        Triple(60f, 80f, BandNoisy),
        Triple(80f, 90f, BandLoud),
    )
    for ((lo, hi, color) in bands) {
        val a0 = angleFor(lo) - gap / 2f
        val a1 = angleFor(hi) + gap / 2f
        drawArc(
            color = color,
            startAngle = -a0,
            sweepAngle = a0 - a1,
            useCenter = false,
            topLeft = Offset(CX - R, CY - R),
            size = Size(R * 2, R * 2),
            style = Stroke(width = 16f, cap = StrokeCap.Round),
        )
    }

    // 刻度线
    var d = 30
    while (d <= 90) {
        val major = d % 10 == 0
        val a = angleFor(d.toFloat())
        drawLine(
            color = if (major) TickMajor else TickMinor,
            start = polar(a, if (major) 71f else 77f),
            end = polar(a, 86f),
            strokeWidth = if (major) 1.8f else 1.2f,
            cap = StrokeCap.Round,
        )
        d += 5
    }

    // 指针（Compose 的 rotate 与 SVG 一样是顺时针为正，公式原样可移植）
    rotate(degrees = 90f - angleFor(value), pivot = Offset(CX, CY)) {
        drawLine(
            color = GugaInk,
            start = Offset(CX, CY - 14f),
            end = Offset(CX, CY - 68f),
            strokeWidth = 5f,
            cap = StrokeCap.Round,
        )
        drawCircle(color = GugaAccent, radius = 5.2f, center = Offset(CX, CY - 68f))
    }

    // 中心轴
    drawCircle(color = GugaCream, radius = 9f, center = Offset(CX, CY))
    drawCircle(color = GugaInk, radius = 9f, center = Offset(CX, CY), style = Stroke(width = 3f))
    drawCircle(color = GugaAccent, radius = 3f, center = Offset(CX, CY))
}
