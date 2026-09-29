@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package com.cloudea.noise.detector.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.cloudea.noise.detector.R

// Nunito 是可变字体（wght 轴 200–1000）。用 FontVariation 驱动字重轴，
// 一个字体文件就能出全部字重；中文没有字形，系统会自动回退到 CJK 字体。
private fun nunito(weight: FontWeight, axis: Int) = Font(
    resId = R.font.nunito,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(axis)),
)

val NunitoFamily = FontFamily(
    nunito(FontWeight.Normal, 400),
    nunito(FontWeight.SemiBold, 600),
    nunito(FontWeight.Bold, 700),
    nunito(FontWeight.ExtraBold, 800),
    nunito(FontWeight.Black, 900),
)

/** 等宽数字（tabular figures）—— 实时跳动的数字不会左右抖动 */
val Tabular = TextStyle(fontFeatureSettings = "tnum")

val GugaTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = NunitoFamily, fontWeight = FontWeight.Black,
        fontSize = 58.sp, lineHeight = 58.sp, letterSpacing = (-2).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = NunitoFamily, fontWeight = FontWeight.Black,
        fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = 0.5.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = NunitoFamily, fontWeight = FontWeight.Black,
        fontSize = 19.sp, lineHeight = 25.sp, letterSpacing = 0.3.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = NunitoFamily, fontWeight = FontWeight.ExtraBold,
        fontSize = 15.5.sp, lineHeight = 21.sp, letterSpacing = 0.2.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = NunitoFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 24.sp, letterSpacing = 0.2.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = NunitoFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp, lineHeight = 20.sp, letterSpacing = 0.2.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = NunitoFamily, fontWeight = FontWeight.ExtraBold,
        fontSize = 12.5.sp, lineHeight = 17.sp, letterSpacing = 0.3.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = NunitoFamily, fontWeight = FontWeight.Bold,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.3.sp,
    ),
)
