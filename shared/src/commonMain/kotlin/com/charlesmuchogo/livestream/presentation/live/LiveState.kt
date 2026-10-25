package com.charlesmuchogo.livestream.presentation.live

import com.charlesmuchogo.livestream.domain.models.Match
import com.charlesmuchogo.livestream.domain.models.SportFilter
import kotlin.time.Instant

data class LiveState(
    val sports: List<SportFilter> = emptyList(),
    val selectedSport: String = SportFilter.ALL,
    val live: List<Match> = emptyList(),
    val upNext: List<Match> = emptyList(),
    val savedIds: Set<Long> = emptySet(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val updatedAt: Instant? = null,
) {
    val selectedSportLabel: String
        get() = sports.firstOrNull { it.id == selectedSport }?.label ?: "this sport"
    val liveCount: Int
        get() = sports.firstOrNull { it.id == selectedSport }?.live ?: live.size
}
