package com.charlesmuchogo.livestream.presentation.saved

import com.charlesmuchogo.livestream.domain.models.League
import com.charlesmuchogo.livestream.domain.models.Match

enum class SavedTab { Matches, Leagues }

data class SavedState(
    val tab: SavedTab = SavedTab.Matches,
    val matches: List<Match> = emptyList(),
    val leagues: List<League> = emptyList(),
    val followedIds: Set<Long> = emptySet(),
    val leagueFilter: String = "",
    val leaguesLoading: Boolean = true,
    val leaguesError: String? = null,
) {
    val visibleLeagues: List<League>
        get() = leagues
            .filter { leagueFilter.isBlank() || it.name.contains(leagueFilter.trim(), ignoreCase = true) || it.sportLabel.contains(leagueFilter.trim(), ignoreCase = true) }
            .sortedByDescending { it.id in followedIds }
}
