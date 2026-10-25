package com.charlesmuchogo.livestream.domain.models

import kotlin.time.Instant

data class SportFilter(
    val id: String,
    val label: String,
    val live: Int,
    val upcoming: Int,
) {
    companion object {
        const val ALL = "all"
    }
}

data class HomeFeed(
    val sports: List<SportFilter>,
    val live: List<Match>,
    val upNext: List<Match>,
    val updatedAt: Instant?,
)

data class League(
    val id: Long,
    val name: String,
    val sport: String,
    val sportLabel: String,
    val live: Int,
    val matches: Int,
)

data class ScheduleDay(
    val date: String,
    val count: Int,
    val live: Int,
)

data class MatchPage(
    val matches: List<Match>,
    val hasMore: Boolean,
)
