package com.myowndime.duoscoreboard.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myowndime.duoscoreboard.domain.model.MatchRecord
import com.myowndime.duoscoreboard.domain.repository.MatchRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val matchRepository: MatchRepository,
) : ViewModel() {

    val matches: StateFlow<List<MatchRecord>> = matchRepository.matches.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList(),
    )

    fun delete(id: Long) {
        viewModelScope.launch { matchRepository.delete(id) }
    }

    fun clearAll() {
        viewModelScope.launch { matchRepository.clear() }
    }
}
