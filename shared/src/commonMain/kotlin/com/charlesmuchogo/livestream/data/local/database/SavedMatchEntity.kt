package com.charlesmuchogo.livestream.data.local.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.charlesmuchogo.livestream.domain.models.Match
import com.charlesmuchogo.livestream.domain.models.MatchStatus
import kotlin.time.Instant

@Entity(tableName = "saved_matches")
data class SavedMatchEntity(
    @PrimaryKey val id: Long,
    val sport: String,
    val sportLabel: String,
    val leagueId: Long,
    val league: String,
    val homeTeam: String,
    val awayTeam: String,
    val homeLogo: String,
    val awayLogo: String,
    val startsAtEpochMillis: Long?,
    val isLive: Boolean,
    val streamCount: Int,
    val savedAtEpochMillis: Long,
)

@Entity(tableName = "followed_leagues")
data class FollowedLeagueEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val sportLabel: String,
)

fun SavedMatchEntity.toMatch() = Match(
    id = id,
    sport = sport,
    sportLabel = sportLabel,
    leagueId = leagueId,
    league = league,
    homeTeam = homeTeam,
    awayTeam = awayTeam,
    homeLogo = homeLogo,
    awayLogo = awayLogo,
    startsAt = startsAtEpochMillis?.let { Instant.fromEpochMilliseconds(it) },
    status = if (isLive) MatchStatus.Live else MatchStatus.Scheduled,
    streamCount = streamCount,
)

fun Match.toSavedEntity(savedAtEpochMillis: Long) = SavedMatchEntity(
    id = id,
    sport = sport,
    sportLabel = sportLabel,
    leagueId = leagueId,
    league = league,
    homeTeam = homeTeam,
    awayTeam = awayTeam,
    homeLogo = homeLogo,
    awayLogo = awayLogo,
    startsAtEpochMillis = startsAt?.toEpochMilliseconds(),
    isLive = isLive,
    streamCount = streamCount,
    savedAtEpochMillis = savedAtEpochMillis,
)
