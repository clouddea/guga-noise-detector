package com.cloudea.noise.detector.audio

import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 分贝相关的纯函数，方便单测。
 *
 * ⚠️ 重要：手机麦克风没有做过声学校准。这里的 dB 是「用 94 dB SPL ≈ 满量程」
 * 这一通用约定做的近似换算，真实误差可达 ±10 dB 量级，**只能看趋势和相对大小，
 * 不能用于法定噪声测量或职业健康评估**。
 */
object DbMath {

    /** 仪表量程下限，与 DbGauge 的 DB_MIN 对齐 */
    const val FLOOR_DB = 30f

    /** 上限 */
    const val CEIL_DB = 100f

    /** 手机麦克风满量程 ≈ 94 dB SPL（1 Pa 参考灵敏度）的通用偏移量 */
    const val CALIBRATION_OFFSET_DB = 94f

    /** 指针平滑系数 */
    const val EMA_ALPHA = 0.30f

    /** 归一化样本 RMS（[-1,1)）→ 近似 dB */
    fun rmsToDb(rms: Float): Float {
        if (rms < 1e-7f) return FLOOR_DB
        return (20f * log10(rms) + CALIBRATION_OFFSET_DB).coerceIn(FLOOR_DB, CEIL_DB)
    }

    /** PCM 16-bit 样本 → 归一化 RMS */
    fun rmsOf(samples: ShortArray, len: Int = samples.size): Float {
        if (len <= 0) return 0f
        var sum = 0.0
        for (i in 0 until len) {
            val v = samples[i] / 32768.0
            sum += v * v
        }
        return sqrt(sum / len).toFloat()
    }

    /** 能量平均（等效声级 Leq 的定义），不是算术平均 */
    fun energyMean(db: List<Float>): Float {
        if (db.isEmpty()) return 0f
        var sum = 0.0
        for (v in db) sum += 10.0.pow(v / 10.0)
        return (10.0 * log10(sum / db.size)).toFloat()
    }

    fun median(values: List<Float>): Float {
        if (values.isEmpty()) return 0f
        val s = values.sorted()
        val n = s.size
        return if (n % 2 == 1) s[n / 2] else (s[n / 2 - 1] + s[n / 2]) / 2f
    }

    fun stdDev(values: List<Float>): Float {
        if (values.size < 2) return 0f
        val m = values.average()
        var acc = 0.0
        for (v in values) acc += (v - m) * (v - m)
        return sqrt(acc / values.size).toFloat()
    }

    /** 指数滑动平均：只用于指针和实时数字，统计值一律用原始采样 */
    fun ema(prev: Float, next: Float, alpha: Float = EMA_ALPHA): Float =
        if (prev <= 0f) next else alpha * next + (1 - alpha) * prev
}
