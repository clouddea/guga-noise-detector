package com.cloudea.noise.detector.audio

import org.junit.Assert.assertEquals
import org.junit.Test

class DbMathTest {

    @Test
    fun rmsToDb_silence_returnsFloor() {
        assertEquals(DbMath.FLOOR_DB, DbMath.rmsToDb(0f), 0.001f)
    }

    @Test
    fun rmsToDb_fullScale_isAbout94() {
        // rms = 1.0 → 20*log10(1) + 94 = 94
        assertEquals(94f, DbMath.rmsToDb(1f), 0.01f)
    }

    @Test
    fun rmsToDb_halfAmplitude_isAbout88() {
        // 20*log10(0.5) = -6.02 → 87.98
        assertEquals(87.98f, DbMath.rmsToDb(0.5f), 0.05f)
    }

    @Test
    fun median_handlesOddAndEven() {
        assertEquals(3f, DbMath.median(listOf(1f, 3f, 5f)), 0.001f)
        assertEquals(2.5f, DbMath.median(listOf(1f, 2f, 3f, 4f)), 0.001f)
        assertEquals(0f, DbMath.median(emptyList()), 0.001f)
    }

    @Test
    fun energyMean_matchesLeqDefinition() {
        assertEquals(60f, DbMath.energyMean(listOf(60f, 60f)), 0.01f)
        // 能量平均 > 算术平均：50 和 70 的算术均值是 60，能量均值应更大
        val e = DbMath.energyMean(listOf(50f, 70f))
        assertEquals(true, e > 60f)
    }

    @Test
    fun rmsOf_constantHalfScale() {
        val samples = ShortArray(100) { 16384 } // 16384/32768 = 0.5
        assertEquals(0.5f, DbMath.rmsOf(samples), 0.01f)
    }
}
