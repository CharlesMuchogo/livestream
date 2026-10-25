package com.charlesmuchogo.livestream

import com.charlesmuchogo.livestream.data.repository.LibraryRepository
import com.charlesmuchogo.livestream.data.repository.MatchRepository
import com.charlesmuchogo.livestream.domain.models.HomeFeed
import com.charlesmuchogo.livestream.domain.models.League
import com.charlesmuchogo.livestream.domain.models.Match
import com.charlesmuchogo.livestream.domain.models.MatchDetail
import com.charlesmuchogo.livestream.domain.models.MatchPage
import com.charlesmuchogo.livestream.domain.models.MatchStatus
import com.charlesmuchogo.livestream.domain.models.ScheduleDay
import com.charlesmuchogo.livestream.domain.models.StreamKind
import com.charlesmuchogo.livestream.domain.models.StreamSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

fun match(id: Long, leagueId: Long = 1, live: Boolean = true, streams: Int = 3) = Match(
    id = id,
    sport = "soccer",
    sportLabel = "Soccer",
    leagueId = leagueId,
    league = "League $leagueId",
    homeTeam = "Home $id",
    awayTeam = "Away $id",
    homeLogo = "",
    awayLogo = "",
    startsAt = null,
    status = if (live) MatchStatus.Live else MatchStatus.Scheduled,
    streamCount = streams,
)

fun source(id: Long, verified: Boolean = false, ads: Int = 1, channel: String = "Willow", kind: StreamKind = StreamKind.Page) = StreamSource(
    id = id,
    label = "Source $id",
    streamer = "Source $id",
    channel = channel,
    quality = "HD",
    language = "English",
    ads = ads,
    verified = verified,
    kind = kind,
    url = "https://s$id.test/watch",
)

class FakeMatchRepository(
    var homeResult: Result<HomeFeed> = Result.success(HomeFeed(emptyList(), emptyList(), emptyList(), null)),
    var detailResult: Result<MatchDetail>? = null,
) : MatchRepository {
    var homeCalls = 0
    override suspend fun home(sport: String): Result<HomeFeed> { homeCalls++; return homeResult }
    override suspend fun matchesOn(date: String?, search: String?) = Result.success(MatchPage(emptyList(), false))
    override suspend fun match(id: Long) = detailResult ?: Result.failure(IllegalStateException("no detail"))
    override suspend fun scheduleDays() = Result.success(emptyList<ScheduleDay>())
    override suspend fun leagues() = Result.success(emptyList<League>())
}

class FakeLibraryRepository : LibraryRepository {
    val saved = MutableStateFlow<Map<Long, Match>>(emptyMap())
    val followed = MutableStateFlow<Set<Long>>(emptySet())

    override fun savedMatches(): Flow<List<Match>> = saved.map { it.values.toList() }
    override fun savedIds(): Flow<Set<Long>> = saved.map { it.keys }
    override fun followedLeagueIds(): Flow<Set<Long>> = followed
    override suspend fun save(match: Match) { saved.value = saved.value + (match.id to match) }
    override suspend fun unsave(matchId: Long) { saved.value = saved.value - matchId }
    override suspend fun refreshSaved(match: Match) { if (match.id in saved.value) save(match) }
    override suspend fun follow(league: League) { followed.value = followed.value + league.id }
    override suspend fun unfollow(leagueId: Long) { followed.value = followed.value - leagueId }
    override suspend fun pruneFinished() = Unit
}
