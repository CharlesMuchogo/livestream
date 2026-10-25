package com.charlesmuchogo.livestream.presentation.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charlesmuchogo.livestream.data.repository.LibraryRepository
import com.charlesmuchogo.livestream.data.repository.MatchRepository
import com.charlesmuchogo.livestream.domain.models.Match
import com.charlesmuchogo.livestream.presentation.components.SERVER_UNREACHABLE
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ScheduleViewModel(
    private val matchRepository: MatchRepository,
    private val libraryRepository: LibraryRepository,
) : ViewModel() {
    private val screen = MutableStateFlow(ScheduleState())
    private var loadJob: Job? = null

    val state: StateFlow<ScheduleState> = combine(screen, libraryRepository.savedIds()) { current, saved ->
        current.copy(savedIds = saved)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScheduleState())

    init {
        loadDaysThenMatches()
    }

    fun onAction(action: ScheduleAction) {
        when (action) {
            is ScheduleAction.OnDaySelected -> {
                screen.update { it.copy(selectedDate = action.date, query = "") }
                loadMatches(debounce = false)
            }
            is ScheduleAction.OnQueryChange -> {
                screen.update { it.copy(query = action.query) }
                loadMatches(debounce = true)
            }
            is ScheduleAction.OnToggleGroup -> screen.update {
                it.copy(collapsed = if (action.key in it.collapsed) it.collapsed - action.key else it.collapsed + action.key)
            }
            is ScheduleAction.OnToggleSave -> toggleSave(action.match)
            ScheduleAction.OnRetry -> if (screen.value.days.isEmpty()) loadDaysThenMatches() else loadMatches(debounce = false)
            is ScheduleAction.OnMatchClick -> Unit
        }
    }

    private fun loadDaysThenMatches() {
        screen.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            matchRepository.scheduleDays().fold(
                onSuccess = { days ->
                    screen.update { it.copy(days = days, selectedDate = it.selectedDate ?: days.firstOrNull()?.date) }
                    loadMatches(debounce = false)
                },
                onFailure = { screen.update { it.copy(isLoading = false, error = SERVER_UNREACHABLE) } },
            )
        }
    }

    private fun loadMatches(debounce: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            if (debounce) delay(350)
            val current = screen.value
            screen.update { it.copy(isLoading = true, error = null) }
            val date = if (current.isSearching) null else current.selectedDate
            matchRepository.matchesOn(date, current.query.trim()).fold(
                onSuccess = { page -> screen.update { it.copy(groups = groupByLeague(page.matches), isLoading = false) } },
                onFailure = { screen.update { it.copy(isLoading = false, error = SERVER_UNREACHABLE) } },
            )
        }
    }

    private fun toggleSave(match: Match) {
        viewModelScope.launch {
            if (match.id in state.value.savedIds) libraryRepository.unsave(match.id) else libraryRepository.save(match)
        }
    }

    companion object {
        fun groupByLeague(matches: List<Match>): List<LeagueGroup> =
            matches
                .groupBy { it.leagueId to it.league }
                .map { (key, items) ->
                    LeagueGroup(
                        key = "${key.first}:${key.second}",
                        league = key.second.ifBlank { "Other" },
                        sport = items.first().sport,
                        sportLabel = items.first().sportLabel,
                        matches = items,
                    )
                }
                .sortedWith(compareByDescending<LeagueGroup> { it.liveCount }.thenBy { group -> group.matches.minOf { it.startsAt?.toEpochMilliseconds() ?: Long.MAX_VALUE } })
    }
}
