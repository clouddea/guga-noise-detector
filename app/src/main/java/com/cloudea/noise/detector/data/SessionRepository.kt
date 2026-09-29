package com.cloudea.noise.detector.data

import kotlinx.coroutines.flow.StateFlow

/** 历史记录仓库：唯一真相源，UI 靠观察 [sessions] 自动刷新 */
interface SessionRepository {
    val sessions: StateFlow<List<Session>>

    /** 首次读取磁盘；可重复调用，幂等 */
    suspend fun load()

    suspend fun add(session: Session)

    suspend fun delete(id: Long)

    suspend fun clear()
}
