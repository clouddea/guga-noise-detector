package com.cloudea.noise.detector.data

import com.cloudea.noise.detector.audio.DbLevel

/** 一次检测会话（一条历史记录） */
data class Session(
    val id: Long,
    val startMillis: Long,
    val endMillis: Long,
    val durationMillis: Long,
    val placeLabel: String,
    val latitude: Double?,
    val longitude: Double?,
    val minDb: Float,
    val avgDb: Float,
    val medianDb: Float,
    val maxDb: Float,
    val waveform: List<Float>,
) {
    val level: DbLevel get() = DbLevel.of(avgDb)

    val durationSeconds: Long get() = durationMillis / 1000
}
