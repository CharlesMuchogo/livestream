package com.charlesmuchogo.livestream.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MatchDto(
    val id: Long,
    val slug: String = "",
    val sport: String = "",
    @SerialName("sport_label") val sportLabel: String = "",
    @SerialName("league_id") val leagueId: Long = 0,
    val league: String = "",
    @SerialName("home_team") val homeTeam: String = "",
    @SerialName("away_team") val awayTeam: String = "",
    @SerialName("home_logo") val homeLogo: String = "",
    @SerialName("away_logo") val awayLogo: String = "",
    @SerialName("starts_at") val startsAt: String? = null,
    val status: String = "scheduled",
    @SerialName("stream_count") val streamCount: Int = 0,
)

@Serializable
data class StreamDto(
    val id: Long,
    val label: String = "",
    val streamer: String = "",
    val channel: String = "",
    val quality: String = "",
    val language: String = "",
    val ads: Int = 0,
    val verified: Boolean = false,
    val kind: String = "page",
    val url: String,
)

@Serializable
data class SportDto(
    val id: String,
    val label: String,
    val live: Int = 0,
    val upcoming: Int = 0,
)

@Serializable
data class HomeDto(
    val sports: List<SportDto> = emptyList(),
    val live: List<MatchDto> = emptyList(),
    @SerialName("up_next") val upNext: List<MatchDto> = emptyList(),
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class MatchListDto(
    val matches: List<MatchDto> = emptyList(),
    @SerialName("has_more") val hasMore: Boolean = false,
)

@Serializable
data class MatchDetailDto(
    val match: MatchDto,
    val streams: List<StreamDto> = emptyList(),
)

@Serializable
data class LeagueDto(
    val id: Long,
    val name: String,
    val sport: String = "",
    @SerialName("sport_label") val sportLabel: String = "",
    val live: Int = 0,
    val matches: Int = 0,
)

@Serializable
data class ScheduleDayDto(
    val date: String,
    val count: Int = 0,
    val live: Int = 0,
)
