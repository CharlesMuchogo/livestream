package com.charlesmuchogo.livestream.data.local.repository

import com.charlesmuchogo.livestream.data.local.dao.EventsDao
import com.charlesmuchogo.livestream.data.local.databaseclasses.Events
import kotlinx.coroutines.flow.Flow

class EventRepository(private val eventDao: EventsDao) {
     suspend fun getAllEvents(): Flow<List<Events>> {
        return eventDao.getAll()
    }
    suspend fun getFavourites(): List<Events> {
        return eventDao.getFavourites()
    }

    suspend fun insertEvent(events: List<Events>) {
        eventDao.upsertAll(events)
    }

    suspend fun favouriteEvent(id: Int, favourite: Boolean) {
        eventDao.favouriteEvent(id = id, favourite = favourite)
    }
}