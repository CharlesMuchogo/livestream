package com.charlesmuchogo.livestream.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.charlesmuchogo.livestream.data.local.databaseclasses.Events
import kotlinx.coroutines.flow.Flow

@Dao
interface EventsDao {
    @Query("SELECT * FROM events")
    fun getAll(): Flow<List<Events>>
    @Query("SELECT * FROM events WHERE favourite = true")
    fun getFavourites(): Flow<List<Events>>

    @Upsert
    suspend fun insert(event: Events)

    @Query("UPDATE events SET favourite = :favourite WHERE id = :id")
    suspend fun favouriteEvent(id: Int, favourite: Boolean)
}