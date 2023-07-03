package com.charlesmuchogo.livestream.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.charlesmuchogo.livestream.data.local.databaseclasses.Events

@Dao
interface EventsDao {
    @Query("SELECT * FROM events")
    suspend fun getAll(): List<Events>

    @Upsert
    suspend fun upsertAll(products: List<Events>)
}