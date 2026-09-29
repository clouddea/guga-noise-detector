package com.cloudea.noise.detector.data

import android.content.Context

/** 极简 service locator：仓库全局唯一，避免引入 DI 框架 */
object AppGraph {

    @Volatile
    private var repository: SessionRepository? = null

    fun repository(context: Context): SessionRepository =
        repository ?: synchronized(this) {
            repository ?: FileSessionRepository(context.applicationContext).also { repository = it }
        }
}
