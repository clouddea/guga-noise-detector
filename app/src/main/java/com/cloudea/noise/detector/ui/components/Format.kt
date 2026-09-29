package com.cloudea.noise.detector.ui.components

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Float.fmt1(): String = String.format(Locale.US, "%.1f", this)

/** "45 秒" / "12 分钟" / "1 小时 20 分" */
fun formatDuration(seconds: Long): String = when {
    seconds < 60 -> "${seconds} 秒"
    seconds < 3600 -> "${seconds / 60} 分钟"
    else -> {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        if (m == 0L) "$h 小时" else "$h 小时 $m 分"
    }
}

private val dateTimeFmt = SimpleDateFormat("M月d日 HH:mm", Locale.CHINA)

fun formatDateTime(millis: Long): String = dateTimeFmt.format(Date(millis))

/** "今天 08:15 – 08:27" 这类时间范围 */
fun formatRange(startMillis: Long, endMillis: Long): String {
    val s = dateTimeFmt.format(Date(startMillis))
    val e = SimpleDateFormat("HH:mm", Locale.CHINA).format(Date(endMillis))
    return "$s – $e"
}

/** 毫秒时间戳 → "00:00" 形式，用于检测计时 */
fun formatClock(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(Locale.US, m, s)
}
