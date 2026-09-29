package com.cloudea.noise.detector

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cloudea.noise.detector.data.AppGraph
import com.cloudea.noise.detector.ui.about.AboutScreen
import com.cloudea.noise.detector.ui.history.HistoryDetailScreen
import com.cloudea.noise.detector.ui.history.HistoryScreen
import com.cloudea.noise.detector.ui.history.HistoryViewModel
import com.cloudea.noise.detector.ui.home.DetectorViewModel
import com.cloudea.noise.detector.ui.home.HomeScreen
import com.cloudea.noise.detector.ui.theme.GugaAccentInk
import com.cloudea.noise.detector.ui.theme.GugaCream
import com.cloudea.noise.detector.ui.theme.GugaInk
import com.cloudea.noise.detector.ui.theme.GugaInk2
import com.cloudea.noise.detector.ui.theme.GugaLine
import com.cloudea.noise.detector.ui.theme.GugaPink
import com.cloudea.noise.detector.ui.theme.GugaPinkSoft
import com.cloudea.noise.detector.ui.theme.GugaWhite

private enum class Tab(val label: String) {
    HOME("检测"),
    HISTORY("历史"),
    ABOUT("关于"),
}

@Composable
fun GugaApp() {
    val context = LocalContext.current

    val detectorVm: DetectorViewModel = viewModel(factory = viewModelFactory {
        initializer {
            val app = this[APPLICATION_KEY] as Application
            DetectorViewModel(AppGraph.repository(app), app)
        }
    })
    val historyVm: HistoryViewModel = viewModel(factory = viewModelFactory {
        initializer {
            HistoryViewModel(AppGraph.repository(this[APPLICATION_KEY] as Application))
        }
    })

    val detector by detectorVm.ui.collectAsStateWithLifecycle()
    val sessions by historyVm.sessions.collectAsStateWithLifecycle()

    var tabIndex by rememberSaveable { mutableIntStateOf(0) }
    var detailId by rememberSaveable { mutableLongStateOf(-1L) }
    var micDenied by remember { mutableStateOf(false) }
    val tab = Tab.entries[tabIndex]

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result[Manifest.permission.RECORD_AUDIO] == true) {
            micDenied = false
            detectorVm.start()
        } else {
            micDenied = true
        }
    }

    val requestStart: () -> Unit = {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            micDenied = false
            detectorVm.start()
        } else {
            launcher.launch(
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                )
            )
        }
    }

    // 退到后台时兜底保存，避免进程被杀丢数据
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) detectorVm.onBackgrounded()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    BackHandler(enabled = detailId >= 0) { detailId = -1L }

    Scaffold(
        containerColor = GugaCream,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            GugaTabBar(tab) {
                tabIndex = it.ordinal
                detailId = -1L
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .windowInsetsPadding(WindowInsets.statusBars),
        ) {
            val openSession = sessions.firstOrNull { it.id == detailId }
            when {
                detailId >= 0 && openSession != null -> HistoryDetailScreen(
                    session = openSession,
                    onBack = { detailId = -1L },
                )

                tab == Tab.HOME -> HomeScreen(
                    state = detector,
                    onStart = requestStart,
                    onPause = detectorVm::pause,
                    onFinish = detectorVm::finishAndSave,
                )

                tab == Tab.HISTORY -> HistoryScreen(
                    sessions = sessions,
                    onOpen = { detailId = it },
                    onGoDetect = { tabIndex = Tab.HOME.ordinal },
                )

                else -> AboutScreen()
            }

            if (micDenied) {
                MicDeniedCard(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(20.dp),
                    onOpenSettings = {
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null),
                        )
                        context.startActivity(intent)
                    },
                )
            }
        }
    }
}

@Composable
private fun GugaTabBar(current: Tab, onSelect: (Tab) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(GugaCream)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(GugaLine))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .height(60.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Tab.entries.forEach { t ->
                val selected = t == current
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (selected) GugaPinkSoft else Color.Transparent)
                        .clickable { onSelect(t) }
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                ) {
                    TabIcon(t, if (selected) GugaAccentInk else GugaInk2)
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = t.label,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = if (selected) GugaAccentInk else GugaInk2,
                    )
                }
            }
        }
    }
}

@Composable
private fun TabIcon(tab: Tab, color: Color) {
    Canvas(Modifier.size(22.dp)) {
        val u = size.width / 24f
        val sw = 2.2f * u
        when (tab) {
            Tab.HOME -> {
                drawLine(color, Offset(5f * u, 10.6f * u), Offset(5f * u, 13.4f * u), sw, StrokeCap.Round)
                drawLine(color, Offset(12f * u, 6.6f * u), Offset(12f * u, 17.4f * u), sw, StrokeCap.Round)
                drawLine(color, Offset(19f * u, 9.2f * u), Offset(19f * u, 14.8f * u), sw, StrokeCap.Round)
            }

            Tab.HISTORY -> {
                drawCircle(color, radius = 8.4f * u, center = Offset(12f * u, 12f * u), style = Stroke(sw))
                drawLine(color, Offset(12f * u, 7.5f * u), Offset(12f * u, 12f * u), sw, StrokeCap.Round)
                drawLine(color, Offset(12f * u, 12f * u), Offset(15.2f * u, 14.1f * u), sw, StrokeCap.Round)
            }

            Tab.ABOUT -> {
                drawCircle(color, radius = 8.4f * u, center = Offset(12f * u, 12f * u), style = Stroke(sw))
                drawLine(color, Offset(12f * u, 11.2f * u), Offset(12f * u, 16.3f * u), sw, StrokeCap.Round)
                drawCircle(color, radius = 1.1f * u, center = Offset(12f * u, 7.9f * u))
            }
        }
    }
}

@Composable
private fun MicDeniedCard(onOpenSettings: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(GugaWhite, RoundedCornerShape(22.dp))
            .border(1.5.dp, GugaLine, RoundedCornerShape(22.dp))
            .padding(16.dp),
    ) {
        Text(
            text = "需要麦克风权限",
            style = MaterialTheme.typography.titleMedium,
            color = GugaInk,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "咕嗄要听到声音才能测量分贝。请到系统设置里允许「麦克风」权限。",
            style = MaterialTheme.typography.bodyMedium,
            color = GugaInk2,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "去系统设置",
            style = MaterialTheme.typography.titleMedium,
            color = GugaInk,
            modifier = Modifier
                .background(GugaPink, RoundedCornerShape(29.dp))
                .clickable(onClick = onOpenSettings)
                .padding(horizontal = 20.dp, vertical = 10.dp),
        )
    }
}
