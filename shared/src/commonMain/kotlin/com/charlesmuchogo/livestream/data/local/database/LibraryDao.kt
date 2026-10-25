package com.charlesmuchogo.livestream.data.local.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedMatchesDao {
    @Query("SELECT * FROM saved_matches ORDER BY isLive DESC, startsAtEpochMillis ASC")
    fun observeAll(): Flow<List<SavedMatchEntity>>

    @Query("SELECT id FROM saved_matches")
    fun observeIds(): Flow<List<Long>>

    @Query("SELECT * FROM saved_matches WHERE id = :id")
    suspend fun find(id: Long): SavedMatchEntity?

    @Upsert
    suspend fun upsert(match: SavedMatchEntity)

    @Query("DELETE FROM saved_matches WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM saved_matches WHERE isLive = 0 AND startsAtEpochMillis IS NOT NULL AND startsAtEpochMillis < :beforeEpochMillis")
    suspend fun deleteStartedBefore(beforeEpochMillis: Long)
}

@Dao
interface FollowedLeaguesDao {
    @Query("SELECT * FROM followed_leagues ORDER BY name")
    fun observeAll(): Flow<List<FollowedLeagueEntity>>

    @Upsert
    suspend fun upsert(league: FollowedLeagueEntity)

    @Query("DELETE FROM followed_leagues WHERE id = :id")
    suspend fun delete(id: Long)
}
