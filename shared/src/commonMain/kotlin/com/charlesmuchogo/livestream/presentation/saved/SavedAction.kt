package com.charlesmuchogo.livestream.presentation.saved

import com.charlesmuchogo.livestream.domain.models.League
import com.charlesmuchogo.livestream.domain.models.Match

sealed interface SavedAction {
    data class OnTabSelected(val tab: SavedTab) : SavedAction
    data class OnMatchClick(val match: Match) : SavedAction
    data class OnRemove(val match: Match) : SavedAction
    data class OnUndoRemove(val match: Match) : SavedAction
    data class OnToggleFollow(val league: League) : SavedAction
    data class OnLeagueFilterChange(val filter: String) : SavedAction
    data object OnBrowseLive : SavedAction
    data object OnRetryLeagues : SavedAction
}
