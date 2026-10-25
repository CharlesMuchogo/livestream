package com.charlesmuchogo.livestream.presentation.live

import com.charlesmuchogo.livestream.domain.models.Match

sealed interface LiveAction {
    data class OnSportSelected(val sport: String) : LiveAction
    data class OnMatchClick(val match: Match) : LiveAction
    data class OnToggleSave(val match: Match) : LiveAction
    data object OnRefresh : LiveAction
    data object OnResume : LiveAction
    data object OnSearchClick : LiveAction
}
