package com.cloudea.noise.detector.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive

/**
 * 麦克风采集 → 近似分贝流。
 *
 * 采样率优先 48 kHz（设计稿标注 48 kHz），不可用则回退 44.1 kHz。
 * 调用方必须已获得 RECORD_AUDIO 权限，否则会抛 SecurityException。
 */
class AudioMeter {

    @SuppressLint("MissingPermission")
    fun dbStream(): Flow<Float> = flow {
        val (record, rate) = openRecord()
        val chunkSize = 2048
        val chunk = ShortArray(chunkSize)
        var ema = 0f
        try {
            record.startRecording()
            while (currentCoroutineContext().isActive) {
                val n = record.read(chunk, 0, chunkSize)
                if (n > 0) {
                    val raw = DbMath.rmsToDb(DbMath.rmsOf(chunk, n))
                    ema = DbMath.ema(ema, raw)
                    emit(ema)
                }
            }
        } finally {
            runCatching { record.stop() }
            runCatching { record.release() }
        }
    }.flowOn(Dispatchers.IO)

    @SuppressLint("MissingPermission")
    private fun openRecord(): Pair<AudioRecord, Int> {
        for (rate in intArrayOf(48000, 44100)) {
            val minBuf = AudioRecord.getMinBufferSize(
                rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
            )
            if (minBuf <= 0) continue
            val record = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                rate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBuf * 2,
            )
            if (record.state == AudioRecord.STATE_INITIALIZED) return record to rate
            runCatching { record.release() }
        }
        throw IllegalStateException("无法初始化麦克风（可能被占用或缺少权限）")
    }
}
