package com.charlesmuchogo.livestream.data.repository

import com.charlesmuchogo.livestream.data.remote.LivestreamApi
import com.charlesmuchogo.livestream.domain.mappers.toHomeFeed
import com.charlesmuchogo.livestream.domain.mappers.toLeague
import com.charlesmuchogo.livestream.domain.mappers.toMatchDetail
import com.charlesmuchogo.livestream.domain.mappers.toMatchPage
import com.charlesmuchogo.livestream.domain.mappers.toScheduleDay
import com.charlesmuchogo.livestream.domain.models.HomeFeed
import com.charlesmuchogo.livestream.domain.models.League
import com.charlesmuchogo.livestream.domain.models.MatchDetail
import com.charlesmuchogo.livestream.domain.models.MatchPage
import com.charlesmuchogo.livestream.domain.models.ScheduleDay
import kotlinx.coroutines.CancellationException
import kotlinx.datetime.TimeZone

interface MatchRepository {
    suspend fun home(sport: String): Result<HomeFeed>
    suspend fun matchesOn(date: String?, search: String?): Result<MatchPage>
    suspend fun match(id: Long): Result<MatchDetail>
    suspend fun scheduleDays(): Result<List<ScheduleDay>>
    suspend fun leagues(): Result<List<League>>
}

class MatchRepositoryImpl(
    private val api: LivestreamApi,
    private val timeZone: () -> TimeZone = { TimeZone.currentSystemDefault() },
) : MatchRepository {
    private val zone: String get() = timeZone().id

    override suspend fun home(sport: String) = attempt { api.home(sport, zone).toHomeFeed() }

    override suspend fun matchesOn(date: String?, search: String?) =
        attempt { api.matches(date, zone, search).toMatchPage() }

    override suspend fun match(id: Long) = attempt { api.match(id).toMatchDetail() }

    override suspend fun scheduleDays() = attempt { api.scheduleDays(zone).map { it.toScheduleDay() } }

    override suspend fun leagues() = attempt { api.leagues().map { it.toLeague() } }

    private suspend fun <T> attempt(block: suspend () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Result.failure(error)
        }
}
