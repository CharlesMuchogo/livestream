package com.charlesmuchogo.livestream.data.repository

import com.charlesmuchogo.livestream.data.local.database.FollowedLeagueEntity
import com.charlesmuchogo.livestream.data.local.database.FollowedLeaguesDao
import com.charlesmuchogo.livestream.data.local.database.SavedMatchesDao
import com.charlesmuchogo.livestream.data.local.database.toMatch
import com.charlesmuchogo.livestream.data.local.database.toSavedEntity
import com.charlesmuchogo.livestream.domain.models.League
import com.charlesmuchogo.livestream.domain.models.Match
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

interface LibraryRepository {
    fun savedMatches(): Flow<List<Match>>
    fun savedIds(): Flow<Set<Long>>
    fun followedLeagueIds(): Flow<Set<Long>>
    suspend fun save(match: Match)
    suspend fun unsave(matchId: Long)
    suspend fun refreshSaved(match: Match)
    suspend fun follow(league: League)
    suspend fun unfollow(leagueId: Long)
    suspend fun pruneFinished()
}

class LibraryRepositoryImpl(
    private val savedMatchesDao: SavedMatchesDao,
    private val followedLeaguesDao: FollowedLeaguesDao,
    private val clock: Clock = Clock.System,
) : LibraryRepository {
    override fun savedMatches(): Flow<List<Match>> = savedMatchesDao.observeAll().map { rows -> rows.map { it.toMatch() } }

    override fun savedIds(): Flow<Set<Long>> = savedMatchesDao.observeIds().map { it.toSet() }

    override fun followedLeagueIds(): Flow<Set<Long>> = followedLeaguesDao.observeAll().map { rows -> rows.mapTo(mutableSetOf()) { it.id } }

    override suspend fun save(match: Match) {
        savedMatchesDao.upsert(match.toSavedEntity(clock.now().toEpochMilliseconds()))
    }

    override suspend fun unsave(matchId: Long) = savedMatchesDao.delete(matchId)

    override suspend fun refreshSaved(match: Match) {
        val existing = savedMatchesDao.find(match.id) ?: return
        savedMatchesDao.upsert(match.toSavedEntity(existing.savedAtEpochMillis))
    }

    override suspend fun follow(league: League) {
        followedLeaguesDao.upsert(FollowedLeagueEntity(id = league.id, name = league.name, sportLabel = league.sportLabel))
    }

    override suspend fun unfollow(leagueId: Long) = followedLeaguesDao.delete(leagueId)

    override suspend fun pruneFinished() {
        savedMatchesDao.deleteStartedBefore((clock.now() - 12.hours).toEpochMilliseconds())
    }
}
