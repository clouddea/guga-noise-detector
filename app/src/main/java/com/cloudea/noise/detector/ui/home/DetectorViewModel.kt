package com.cloudea.noise.detector.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudea.noise.detector.audio.AudioMeter
import com.cloudea.noise.detector.audio.DbMath
import com.cloudea.noise.detector.data.LocationInfo
import com.cloudea.noise.detector.data.LocationProvider
import com.cloudea.noise.detector.data.Session
import com.cloudea.noise.detector.data.SessionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.log10
import kotlin.math.pow

enum class DetectorState { IDLE, RUNNING, PAUSED }

data class DetectorUiState(
    val state: DetectorState = DetectorState.IDLE,
    val currentDb: Float = DbMath.FLOOR_DB,
    val minDb: Float = 0f,
    val avgDb: Float = 0f,
    val medianDb: Float = 0f,
    val maxDb: Float = 0f,
    val elapsedSeconds: Long = 0L,
    val recent: List<Float> = emptyList(),
    val locationLabel: String = "正在定位…",
    val error: String? = null,
    val sampleCount: Int = 0,
)

class DetectorViewModel(
    private val repository: SessionRepository,
    appContext: Context,
) : ViewModel() {

    private val meter = AudioMeter()
    private val locationProvider = LocationProvider(appContext)

    private val _ui = MutableStateFlow(DetectorUiState())
    val ui: StateFlow<DetectorUiState> = _ui.asStateFlow()

    private var collectJob: Job? = null
    private var timerJob: Job? = null

    private var startMillis = 0L
    private var elapsedBase = 0L
    private var runningSince = 0L

    private val samples = ArrayList<Float>(4096)
    private var energySum = 0.0
    private var minDb = Float.MAX_VALUE
    private var maxDb = -Float.MAX_VALUE
    private val recentWindow = ArrayDeque<Float>()
    private var bucketSum = 0.0
    private var bucketCount = 0
    private var bucketStartAt = 0L
    private var lastUiAt = 0L

    private var locationInfo: LocationInfo? = null
    private var saved = false

    init {
        viewModelScope.launch { repository.load() }
        refreshLocation()
    }

    fun start() {
        if (_ui.value.state == DetectorState.RUNNING) return
        if (_ui.value.state == DetectorState.IDLE) {
            resetSession()
            startMillis = System.currentTimeMillis()
            saved = false
        }
        runningSince = System.currentTimeMillis()
        _ui.value = _ui.value.copy(state = DetectorState.RUNNING, error = null)
        refreshLocation()
        launchCollect()
        startTimer()
    }

    fun pause() {
        if (_ui.value.state != DetectorState.RUNNING) return
        elapsedBase += (System.currentTimeMillis() - runningSince) / 1000
        runningSince = 0L
        collectJob?.cancel(); collectJob = null
        timerJob?.cancel(); timerJob = null
        _ui.value = _ui.value.copy(state = DetectorState.PAUSED, elapsedSeconds = elapsedBase)
    }

    /** 结束当前会话并写入历史；无有效采样则只清空现场 */
    fun finishAndSave() {
        val end = System.currentTimeMillis()
        val active = _ui.value.state != DetectorState.IDLE
        collectJob?.cancel(); collectJob = null
        timerJob?.cancel(); timerJob = null

        if (active && !saved && samples.size >= 3) {
            val duration = (end - startMillis).coerceAtLeast(0)
            saved = true
            val session = Session(
                id = end,
                startMillis = if (startMillis > 0) startMillis else end,
                endMillis = end,
                durationMillis = duration,
                placeLabel = LocationProvider.displayLabel(locationInfo),
                latitude = locationInfo?.latitude,
                longitude = locationInfo?.longitude,
                minDb = if (minDb == Float.MAX_VALUE) DbMath.FLOOR_DB else minDb,
                avgDb = energyToDb(energySum, samples.size),
                medianDb = DbMath.median(samples),
                maxDb = if (maxDb == -Float.MAX_VALUE) DbMath.FLOOR_DB else maxDb,
                waveform = downsample(samples, 512),
            )
            viewModelScope.launch { repository.add(session) }
        }
        resetSession()
        _ui.value = _ui.value.copy(
            state = DetectorState.IDLE,
            currentDb = DbMath.FLOOR_DB,
            minDb = 0f, avgDb = 0f, medianDb = 0f, maxDb = 0f,
            elapsedSeconds = 0L,
            recent = emptyList(),
            sampleCount = 0,
        )
    }

    /** App 退到后台时兜底保存，避免进程被杀丢数据 */
    fun onBackgrounded() {
        if (_ui.value.state == DetectorState.IDLE) return
        val elapsed = elapsedBase + (if (runningSince > 0) (System.currentTimeMillis() - runningSince) / 1000 else 0)
        if (elapsed >= 5 && samples.size >= 3) finishAndSave()
    }

    fun refreshLocation() {
        viewModelScope.launch {
            val info = locationProvider.current()
            if (info != null) {
                locationInfo = info
                _ui.value = _ui.value.copy(locationLabel = LocationProvider.displayLabel(info))
            } else if (!locationProvider.hasPermission()) {
                _ui.value = _ui.value.copy(locationLabel = "未授权定位 · 仅本地检测")
            } else {
                _ui.value = _ui.value.copy(locationLabel = "定位失败 · 可继续检测")
            }
        }
    }

    private fun launchCollect() {
        collectJob?.cancel()
        collectJob = viewModelScope.launch {
            meter.dbStream()
                .catch { e ->
                    collectJob = null
                    _ui.value = _ui.value.copy(
                        state = DetectorState.PAUSED,
                        error = e.message ?: "麦克风不可用",
                    )
                }
                .collect { onSample(it) }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                val elapsed = elapsedBase +
                    (if (runningSince > 0) (System.currentTimeMillis() - runningSince) / 1000 else 0)
                _ui.value = _ui.value.copy(
                    elapsedSeconds = elapsed,
                    medianDb = if (samples.isEmpty()) 0f else DbMath.median(samples),
                )
            }
        }
    }

    private fun onSample(value: Float) {
        samples.add(value)
        energySum += 10.0.pow(value / 10.0)
        if (value < minDb) minDb = value
        if (value > maxDb) maxDb = value

        val now = System.currentTimeMillis()
        if (bucketStartAt == 0L) bucketStartAt = now
        bucketSum += value
        bucketCount++
        if (now - bucketStartAt >= 1000L) {
            recentWindow.addLast((bucketSum / bucketCount).toFloat())
            while (recentWindow.size > 60) recentWindow.removeFirst()
            bucketSum = 0.0
            bucketCount = 0
            bucketStartAt = now
        }

        if (now - lastUiAt < 100L) return
        lastUiAt = now
        _ui.value = _ui.value.copy(
            currentDb = value,
            minDb = if (minDb == Float.MAX_VALUE) 0f else minDb,
            maxDb = if (maxDb == -Float.MAX_VALUE) 0f else maxDb,
            avgDb = energyToDb(energySum, samples.size),
            recent = recentWindow.toList(),
            sampleCount = samples.size,
        )
    }

    private fun resetSession() {
        samples.clear()
        energySum = 0.0
        minDb = Float.MAX_VALUE
        maxDb = -Float.MAX_VALUE
        recentWindow.clear()
        bucketSum = 0.0
        bucketCount = 0
        bucketStartAt = 0L
        lastUiAt = 0L
        startMillis = 0L
        elapsedBase = 0L
        runningSince = 0L
    }

    private fun energyToDb(sum: Double, count: Int): Float =
        if (count == 0) 0f else (10.0 * log10(sum / count)).toFloat()

    private fun downsample(src: List<Float>, maxPoints: Int): List<Float> {
        if (src.size <= maxPoints) return src.toList()
        val out = ArrayList<Float>(maxPoints)
        val step = src.size.toFloat() / maxPoints
        for (i in 0 until maxPoints) {
            val from = (i * step).toInt().coerceIn(0, src.size - 1)
            val to = (((i + 1) * step).toInt()).coerceIn(from + 1, src.size)
            var sum = 0f
            for (j in from until to) sum += src[j]
            out.add(sum / (to - from))
        }
        return out
    }
}
