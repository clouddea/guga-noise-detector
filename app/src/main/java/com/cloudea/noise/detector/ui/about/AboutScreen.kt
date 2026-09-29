package com.cloudea.noise.detector.ui.about

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cloudea.noise.detector.BuildConfig
import com.cloudea.noise.detector.R
import com.cloudea.noise.detector.ui.components.MascotBadge
import com.cloudea.noise.detector.ui.theme.DialHalo
import com.cloudea.noise.detector.ui.theme.GugaAccentInk
import com.cloudea.noise.detector.ui.theme.GugaInk
import com.cloudea.noise.detector.ui.theme.GugaInk2
import com.cloudea.noise.detector.ui.theme.GugaLine
import com.cloudea.noise.detector.ui.theme.GugaPink
import com.cloudea.noise.detector.ui.theme.GugaPinkSoft
import com.cloudea.noise.detector.ui.theme.GugaWhite

private const val GITHUB_URL = "https://github.com/clouddea/guga-noise-detector"
private const val GITHUB_LABEL = "github.com/clouddea/guga-noise-detector"

@Composable
fun AboutScreen(modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 6.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(12.dp))
        MascotBadge(sticker = R.drawable.gg_wave, bg = DialHalo, size = 150.dp)

        Spacer(Modifier.height(14.dp))
        Text(
            text = "咕嗄声音检测",
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 24.sp, letterSpacing = 1.sp),
            color = GugaInk,
        )
        Text(
            text = "GugaNoiseDetector",
            style = MaterialTheme.typography.labelLarge,
            color = GugaInk2,
            modifier = Modifier.padding(top = 3.dp),
        )

        Text(
            text = "v${BuildConfig.VERSION_NAME} · build ${BuildConfig.VERSION_CODE}",
            style = MaterialTheme.typography.labelLarge,
            color = GugaInk,
            modifier = Modifier
                .padding(top = 10.dp)
                .background(GugaPink, RoundedCornerShape(12.dp))
                .padding(horizontal = 13.dp, vertical = 4.dp),
        )

        Row(
            modifier = Modifier.padding(top = 13.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FeatureChip("本机存储")
            FeatureChip("无需登录")
            FeatureChip("实时波形")
        }

        Spacer(Modifier.height(14.dp))
        GugaCard {
            Text(
                text = "咕嗄是一个穿着企鹅连体衣的小女孩。她替你聆听世界的音量——家里的安静、办公室的嗡鸣、马路上呼啸而过的车流。声音太大时，她的表情会替你着急，提醒你戴上耳机，或者走远一点。所有数据只保存在本机，不上传、不联网。",
                style = MaterialTheme.typography.bodyLarge,
                color = GugaInk,
            )
        }

        Spacer(Modifier.height(12.dp))
        GugaCard {
            Text(
                text = "⚠️ 读数由手机麦克风估算，未做声学校准，误差可达 ±10 dB。仅供日常参考，不能用于法定噪声测量或职业健康评估。",
                style = MaterialTheme.typography.bodyMedium,
                color = GugaInk2,
            )
        }

        Spacer(Modifier.height(12.dp))
        GugaCard {
            InfoRow("开发者", "Cloudea Studio")
            InfoRowDivider()
            InfoRow("包名", "com.cloudea.noise.detector", mono = true)
            InfoRowDivider()
            InfoRow(
                key = "GitHub",
                value = GITHUB_LABEL,
                mono = true,
                modifier = Modifier.clickable { uriHandler.openUri(GITHUB_URL) },
            )
            InfoRowDivider()
            InfoRow("开源许可", "MIT License")
            InfoRowDivider()
            InfoRow("字体", "Nunito · OFL 1.1")
        }
    }
}

@Composable
private fun FeatureChip(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = GugaAccentInk,
        modifier = Modifier
            .background(GugaPinkSoft, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 5.dp),
    )
}

@Composable
private fun GugaCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(GugaWhite, RoundedCornerShape(22.dp))
            .border(1.5.dp, GugaLine, RoundedCornerShape(22.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        content()
    }
}

@Composable
private fun InfoRow(
    key: String,
    value: String,
    mono: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = key,
            style = MaterialTheme.typography.bodyMedium,
            color = GugaInk2,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = if (mono) 12.sp else 13.sp,
            ),
            color = GugaInk,
        )
    }
}

@Composable
private fun InfoRowDivider() {
    Spacer(
        Modifier
            .fillMaxWidth()
            .height(1.5.dp)
            .background(GugaLine),
    )
}
