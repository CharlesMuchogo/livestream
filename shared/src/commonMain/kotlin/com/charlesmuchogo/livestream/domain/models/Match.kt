package com.charlesmuchogo.livestream.domain.models

import kotlin.time.Instant

enum class MatchStatus { Live, Scheduled }

data class Match(
    val id: Long,
    val sport: String,
    val sportLabel: String,
    val leagueId: Long,
    val league: String,
    val homeTeam: String,
    val awayTeam: String,
    val homeLogo: String,
    val awayLogo: String,
    val startsAt: Instant?,
    val status: MatchStatus,
    val streamCount: Int,
) {
    val isLive: Boolean get() = status == MatchStatus.Live
    val isSingleEvent: Boolean get() = awayTeam.isBlank() || awayTeam in genericOpponents
    val title: String get() = if (isSingleEvent) homeTeam else "$homeTeam v $awayTeam"

    private companion object {
        val genericOpponents = setOf("Live", "Tournament", "Tennis", "Golf", "NXT")
    }
}
