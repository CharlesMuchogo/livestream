package com.charlesmuchogo.livestream.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.charlesmuchogo.livestream.data.local.databaseclasses.Channels
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelsDao {
    @Query("SELECT * FROM channels")
    fun getAll(): Flow<List<Channels>>

    @Upsert
    suspend fun upsertAll(products: List<Channels>)

}