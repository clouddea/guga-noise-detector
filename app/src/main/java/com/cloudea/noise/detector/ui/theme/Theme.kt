package com.cloudea.noise.detector.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// 设计稿是纯亮色马卡龙配色：固定亮色，绝不用 dynamicColor ——
// 否则 Android 12+ 会用壁纸取色覆盖整套马卡龙色，设计直接失效。
private val GugaColorScheme = lightColorScheme(
    primary = GugaAccent,
    onPrimary = GugaInk,
    secondary = GugaPink,
    onSecondary = GugaInk,
    tertiary = GugaBlue,
    onTertiary = GugaInk,
    background = GugaCream,
    onBackground = GugaInk,
    surface = GugaWhite,
    onSurface = GugaInk,
    surfaceVariant = GugaPinkSoft,
    onSurfaceVariant = GugaInk2,
    outline = GugaLine,
    outlineVariant = GugaLine,
)

@Composable
fun GugaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GugaColorScheme,
        typography = GugaTypography,
        shapes = GugaShapes,
        content = content,
    )
}
