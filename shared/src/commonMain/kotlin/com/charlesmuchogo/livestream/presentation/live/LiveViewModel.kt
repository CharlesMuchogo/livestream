package com.charlesmuchogo.livestream.presentation.live

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charlesmuchogo.livestream.data.repository.LibraryRepository
import com.charlesmuchogo.livestream.data.repository.MatchRepository
import com.charlesmuchogo.livestream.domain.models.Match
import com.charlesmuchogo.livestream.domain.models.SportFilter
import com.charlesmuchogo.livestream.presentation.components.SERVER_UNREACHABLE
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class LiveViewModel(
    private val matchRepository: MatchRepository,
    private val libraryRepository: LibraryRepository,
    private val refreshInterval: Duration = 60.seconds,
) : ViewModel() {
    private val feed = MutableStateFlow(LiveState())
    private val messages = Channel<String>(Channel.BUFFERED)
    private var loadJob: Job? = null

    val events: Flow<String> = messages.receiveAsFlow()

    val state: StateFlow<LiveState> = combine(
        feed,
        libraryRepository.savedIds(),
        libraryRepository.followedLeagueIds(),
    ) { current, savedIds, followed ->
        current.copy(
            savedIds = savedIds,
            live = current.live.sortedByDescending { it.leagueId in followed },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LiveState())

    init {
        load(showSpinner = true)
        viewModelScope.launch {
            while (true) {
                delay(refreshInterval)
                load(showSpinner = false)
            }
        }
    }

    fun onAction(action: LiveAction) {
        when (action) {
            is LiveAction.OnSportSelected -> if (action.sport != feed.value.selectedSport) {
                feed.update { it.copy(selectedSport = action.sport, live = emptyList(), upNext = emptyList(), isLoading = true) }
                load(showSpinner = true)
            }
            is LiveAction.OnToggleSave -> toggleSave(action.match)
            LiveAction.OnRefresh -> {
                feed.update { it.copy(isRefreshing = true) }
                load(showSpinner = false)
            }
            LiveAction.OnResume -> if (!feed.value.isLoading) load(showSpinner = false)
            is LiveAction.OnMatchClick, LiveAction.OnSearchClick -> Unit
        }
    }

    private fun load(showSpinner: Boolean) {
        loadJob?.cancel()
        val sport = feed.value.selectedSport
        if (showSpinner) feed.update { it.copy(isLoading = true, error = null) }
        loadJob = viewModelScope.launch {
            matchRepository.home(sport).fold(
                onSuccess = { home ->
                    feed.update {
                        it.copy(
                            sports = home.sports.ifEmpty { listOf(SportFilter(SportFilter.ALL, "All", 0, 0)) },
                            live = home.live,
                            upNext = home.upNext,
                            updatedAt = home.updatedAt,
                            isLoading = false,
                            isRefreshing = false,
                            error = null,
                        )
                    }
                },
                onFailure = {
                    feed.update { current ->
                        val hasContent = current.live.isNotEmpty() || current.upNext.isNotEmpty()
                        current.copy(isLoading = false, isRefreshing = false, error = if (hasContent) current.error else SERVER_UNREACHABLE)
                    }
                    if (!showSpinner) messages.trySend("Couldn't refresh. Showing the last update.")
                },
            )
        }
    }

    private fun toggleSave(match: Match) {
        viewModelScope.launch {
            if (match.id in state.value.savedIds) {
                libraryRepository.unsave(match.id)
                messages.send("Removed from Saved")
            } else {
                libraryRepository.save(match)
                messages.send("Saved ${match.title}")
            }
        }
    }
}
