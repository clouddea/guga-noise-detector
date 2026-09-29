package com.cloudea.noise.detector.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudea.noise.detector.data.Session
import com.cloudea.noise.detector.data.SessionRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(private val repository: SessionRepository) : ViewModel() {

    val sessions: StateFlow<List<Session>> = repository.sessions

    init {
        viewModelScope.launch { repository.load() }
    }

    fun delete(id: Long) {
        viewModelScope.launch { repository.delete(id) }
    }

    fun clearAll() {
        viewModelScope.launch { repository.clear() }
    }
}
