package com.cloudea.noise.detector.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cloudea.noise.detector.audio.DbLevel

private val MascotShadow = Color(0x29C47896)

/**
 * 角色贴纸 + 柔和马卡龙圆底衬。尺寸覆盖设计稿的 94 / 128 / 150 / 56 / 44 dp 五处用法。
 */
@Composable
fun MascotBadge(
    @DrawableRes sticker: Int,
    bg: Color,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(elevation = 9.dp, shape = CircleShape, ambientColor = MascotShadow, spotColor = MascotShadow)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(sticker),
            contentDescription = "咕嗄",
            modifier = Modifier.fillMaxSize(0.86f),
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
fun MascotBadge(level: DbLevel, size: Dp, modifier: Modifier = Modifier) =
    MascotBadge(sticker = level.sticker, bg = level.bg, size = size, modifier = modifier)
