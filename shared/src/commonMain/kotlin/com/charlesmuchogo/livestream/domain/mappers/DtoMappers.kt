package com.charlesmuchogo.livestream.domain.mappers

import com.charlesmuchogo.livestream.data.remote.dto.HomeDto
import com.charlesmuchogo.livestream.data.remote.dto.LeagueDto
import com.charlesmuchogo.livestream.data.remote.dto.MatchDetailDto
import com.charlesmuchogo.livestream.data.remote.dto.MatchDto
import com.charlesmuchogo.livestream.data.remote.dto.MatchListDto
import com.charlesmuchogo.livestream.data.remote.dto.ScheduleDayDto
import com.charlesmuchogo.livestream.data.remote.dto.SportDto
import com.charlesmuchogo.livestream.data.remote.dto.StreamDto
import com.charlesmuchogo.livestream.domain.models.HomeFeed
import com.charlesmuchogo.livestream.domain.models.League
import com.charlesmuchogo.livestream.domain.models.Match
import com.charlesmuchogo.livestream.domain.models.MatchDetail
import com.charlesmuchogo.livestream.domain.models.MatchPage
import com.charlesmuchogo.livestream.domain.models.MatchStatus
import com.charlesmuchogo.livestream.domain.models.ScheduleDay
import com.charlesmuchogo.livestream.domain.models.SportFilter
import com.charlesmuchogo.livestream.domain.models.StreamKind
import com.charlesmuchogo.livestream.domain.models.StreamSource
import kotlin.time.Instant

fun parseInstant(value: String?): Instant? = value?.let { runCatching { Instant.parse(it) }.getOrNull() }

fun MatchDto.toMatch() = Match(
    id = id,
    sport = sport,
    sportLabel = sportLabel,
    leagueId = leagueId,
    league = league,
    homeTeam = homeTeam,
    awayTeam = awayTeam,
    homeLogo = homeLogo,
    awayLogo = awayLogo,
    startsAt = parseInstant(startsAt),
    status = if (status == "live") MatchStatus.Live else MatchStatus.Scheduled,
    streamCount = streamCount,
)

fun StreamDto.toStreamSource() = StreamSource(
    id = id,
    label = label.ifBlank { streamer },
    streamer = streamer,
    channel = channel,
    quality = quality,
    language = language,
    ads = ads,
    verified = verified,
    kind = if (kind == "direct") StreamKind.Direct else StreamKind.Page,
    url = url,
)

fun SportDto.toSportFilter() = SportFilter(id = id, label = label, live = live, upcoming = upcoming)

fun HomeDto.toHomeFeed() = HomeFeed(
    sports = sports.map { it.toSportFilter() },
    live = live.map { it.toMatch() },
    upNext = upNext.map { it.toMatch() },
    updatedAt = parseInstant(updatedAt),
)

fun MatchListDto.toMatchPage() = MatchPage(matches = matches.map { it.toMatch() }, hasMore = hasMore)

fun MatchDetailDto.toMatchDetail() = MatchDetail(match = match.toMatch(), streams = streams.map { it.toStreamSource() })

fun LeagueDto.toLeague() = League(id = id, name = name, sport = sport, sportLabel = sportLabel, live = live, matches = matches)

fun ScheduleDayDto.toScheduleDay() = ScheduleDay(date = date, count = count, live = live)
