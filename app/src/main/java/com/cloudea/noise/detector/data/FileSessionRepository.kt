package com.cloudea.noise.detector.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * 用 org.json + 应用私有目录单文件持久化（零第三方依赖）。
 * 写入走「临时文件 + rename」原子替换；解析失败备份为 .bad 后空启动，绝不崩。
 */
class FileSessionRepository(context: Context) : SessionRepository {

    private val appContext = context.applicationContext
    private val file = File(appContext.filesDir, FILE_NAME)

    private val _sessions = MutableStateFlow<List<Session>>(emptyList())
    override val sessions: StateFlow<List<Session>> = _sessions.asStateFlow()

    @Volatile
    private var loaded = false

    override suspend fun load() {
        if (loaded) return
        val data = withContext(Dispatchers.IO) { readFromDisk() }
        _sessions.value = data
        loaded = true
    }

    override suspend fun add(session: Session) {
        load()
        val next = (listOf(session) + _sessions.value)
            .sortedByDescending { it.startMillis }
            .take(MAX_RECORDS)
        _sessions.value = next
        persist(next)
    }

    override suspend fun delete(id: Long) {
        load()
        val next = _sessions.value.filterNot { it.id == id }
        _sessions.value = next
        persist(next)
    }

    override suspend fun clear() {
        load()
        _sessions.value = emptyList()
        persist(emptyList())
    }

    private suspend fun persist(list: List<Session>) = withContext(Dispatchers.IO) {
        runCatching {
            val tmp = File(appContext.filesDir, "$FILE_NAME.tmp")
            tmp.writeText(toJson(list))
            if (file.exists()) file.delete()
            tmp.renameTo(file)
        }
    }

    private fun readFromDisk(): List<Session> {
        if (!file.exists()) return emptyList()
        return runCatching { fromJson(file.readText()) }.getOrElse {
            runCatching { file.renameTo(File(appContext.filesDir, "$FILE_NAME.bad")) }
            emptyList()
        }
    }

    private fun toJson(list: List<Session>): String {
        val arr = JSONArray()
        for (s in list) {
            val o = JSONObject()
            o.put("id", s.id)
            o.put("start", s.startMillis)
            o.put("end", s.endMillis)
            o.put("dur", s.durationMillis)
            o.put("place", s.placeLabel)
            o.put("lat", s.latitude ?: JSONObject.NULL)
            o.put("lng", s.longitude ?: JSONObject.NULL)
            o.put("min", s.minDb.toDouble())
            o.put("avg", s.avgDb.toDouble())
            o.put("med", s.medianDb.toDouble())
            o.put("max", s.maxDb.toDouble())
            val wave = JSONArray()
            for (v in s.waveform) wave.put(v.toDouble())
            o.put("wave", wave)
            arr.put(o)
        }
        return JSONObject().apply {
            put("version", 1)
            put("sessions", arr)
        }.toString()
    }

    private fun fromJson(text: String): List<Session> {
        val arr = JSONObject(text).optJSONArray("sessions") ?: return emptyList()
        val out = ArrayList<Session>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val waveArr = o.optJSONArray("wave")
            val wave = ArrayList<Float>(waveArr?.length() ?: 0)
            if (waveArr != null) for (j in 0 until waveArr.length()) {
                wave.add(waveArr.getDouble(j).toFloat())
            }
            out.add(
                Session(
                    id = o.optLong("id", i.toLong()),
                    startMillis = o.optLong("start"),
                    endMillis = o.optLong("end"),
                    durationMillis = o.optLong("dur"),
                    placeLabel = o.optString("place", "未知地点"),
                    latitude = if (o.isNull("lat")) null else o.optDouble("lat"),
                    longitude = if (o.isNull("lng")) null else o.optDouble("lng"),
                    minDb = o.optDouble("min", 0.0).toFloat(),
                    avgDb = o.optDouble("avg", 0.0).toFloat(),
                    medianDb = o.optDouble("med", 0.0).toFloat(),
                    maxDb = o.optDouble("max", 0.0).toFloat(),
                    waveform = wave,
                )
            )
        }
        return out
    }

    private companion object {
        const val FILE_NAME = "history.json"
        const val MAX_RECORDS = 500
    }
}
