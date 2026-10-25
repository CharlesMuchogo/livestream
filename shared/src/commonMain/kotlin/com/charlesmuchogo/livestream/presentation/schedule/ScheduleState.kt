package com.charlesmuchogo.livestream.presentation.schedule

import com.charlesmuchogo.livestream.domain.models.Match
import com.charlesmuchogo.livestream.domain.models.ScheduleDay

data class LeagueGroup(
    val key: String,
    val league: String,
    val sport: String,
    val sportLabel: String,
    val matches: List<Match>,
) {
    val liveCount: Int get() = matches.count { it.isLive }
}

data class ScheduleState(
    val days: List<ScheduleDay> = emptyList(),
    val selectedDate: String? = null,
    val query: String = "",
    val groups: List<LeagueGroup> = emptyList(),
    val collapsed: Set<String> = emptySet(),
    val savedIds: Set<Long> = emptySet(),
    val isLoading: Boolean = true,
    val error: String? = null,
) {
    val isSearching: Boolean get() = query.isNotBlank()
}
