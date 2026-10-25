package com.charlesmuchogo.livestream.presentation.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charlesmuchogo.livestream.data.repository.LibraryRepository
import com.charlesmuchogo.livestream.data.repository.MatchRepository
import com.charlesmuchogo.livestream.domain.models.Match
import com.charlesmuchogo.livestream.presentation.components.SERVER_UNREACHABLE
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SavedViewModel(
    private val matchRepository: MatchRepository,
    private val libraryRepository: LibraryRepository,
) : ViewModel() {
    private val screen = MutableStateFlow(SavedState())

    val state: StateFlow<SavedState> = combine(
        screen,
        libraryRepository.savedMatches(),
        libraryRepository.followedLeagueIds(),
    ) { current, matches, followed ->
        current.copy(matches = matches, followedIds = followed)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SavedState())

    init {
        viewModelScope.launch {
            libraryRepository.pruneFinished()
            refreshSavedMatches()
        }
        loadLeagues()
    }

    fun onAction(action: SavedAction) {
        when (action) {
            is SavedAction.OnTabSelected -> screen.update { it.copy(tab = action.tab) }
            is SavedAction.OnRemove -> viewModelScope.launch { libraryRepository.unsave(action.match.id) }
            is SavedAction.OnUndoRemove -> viewModelScope.launch { libraryRepository.save(action.match) }
            is SavedAction.OnToggleFollow -> viewModelScope.launch {
                if (action.league.id in state.value.followedIds) libraryRepository.unfollow(action.league.id) else libraryRepository.follow(action.league)
            }
            is SavedAction.OnLeagueFilterChange -> screen.update { it.copy(leagueFilter = action.filter) }
            SavedAction.OnRetryLeagues -> loadLeagues()
            is SavedAction.OnMatchClick, SavedAction.OnBrowseLive -> Unit
        }
    }

    private suspend fun refreshSavedMatches() {
        val saved: List<Match> = libraryRepository.savedMatches().first()
        saved.map { match ->
            viewModelScope.async { matchRepository.match(match.id).getOrNull()?.match }
        }.awaitAll().filterNotNull().forEach { libraryRepository.refreshSaved(it) }
    }

    private fun loadLeagues() {
        screen.update { it.copy(leaguesLoading = true, leaguesError = null) }
        viewModelScope.launch {
            matchRepository.leagues().fold(
                onSuccess = { leagues -> screen.update { it.copy(leagues = leagues, leaguesLoading = false) } },
                onFailure = { screen.update { it.copy(leaguesLoading = false, leaguesError = SERVER_UNREACHABLE) } },
            )
        }
    }
}
