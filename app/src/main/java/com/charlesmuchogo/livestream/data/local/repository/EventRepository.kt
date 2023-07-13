package com.charlesmuchogo.livestream.data.local.repository

import android.util.Log
import com.charlesmuchogo.livestream.data.local.dao.EventsDao
import com.charlesmuchogo.livestream.data.local.databaseclasses.Events
import kotlinx.coroutines.flow.Flow
import kotlin.math.log

class EventRepository(private val eventDao: EventsDao) {
     suspend fun getAllEvents(): Flow<List<Events>> {
        return eventDao.getAll()
    }
    suspend fun getFavourites(): Flow<List<Events>> {
        return eventDao.getFavourites()
    }

    suspend fun insertEvent(events: Events) {
        eventDao.insert(events)
    }

    suspend fun favouriteEvent(id: Int, favourite: Boolean) {
        eventDao.favouriteEvent(id = id, favourite = favourite)
        Log.e("FavouritesViewModel", "Failed to get events: $id $favourite")

    }
}