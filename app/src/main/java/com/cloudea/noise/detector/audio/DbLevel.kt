package com.cloudea.noise.detector.audio

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.cloudea.noise.detector.R
import com.cloudea.noise.detector.ui.theme.LvLoudBg
import com.cloudea.noise.detector.ui.theme.LvLoudFg
import com.cloudea.noise.detector.ui.theme.LvNoisyBg
import com.cloudea.noise.detector.ui.theme.LvNoisyFg
import com.cloudea.noise.detector.ui.theme.LvNormalBg
import com.cloudea.noise.detector.ui.theme.LvNormalFg
import com.cloudea.noise.detector.ui.theme.LvQuietBg
import com.cloudea.noise.detector.ui.theme.LvQuietFg

/** 分贝等级：阈值 45 / 60 / 80，角色贴纸与台词随等级切换 */
enum class DbLevel(
    val label: String,
    val fg: Color,
    val bg: Color,
    @DrawableRes val sticker: Int,
    val phrase: String,
) {
    QUIET("安静", LvQuietFg, LvQuietBg, R.drawable.gg_sleep, "好安静呀……适合睡个好觉"),
    NORMAL("正常", LvNormalFg, LvNormalBg, R.drawable.gg_happy, "音量刚刚好，好舒服～"),
    NOISY("嘈杂", LvNoisyFg, LvNoisyBg, R.drawable.gg_shock, "有点吵了哦，注意护耳"),
    LOUD("震耳", LvLoudFg, LvLoudBg, R.drawable.gg_angry, "太吵啦！快捂住耳朵！");

    companion object {
        fun of(db: Float): DbLevel = when {
            db < 45f -> QUIET
            db < 60f -> NORMAL
            db < 80f -> NOISY
            else -> LOUD
        }
    }
}
